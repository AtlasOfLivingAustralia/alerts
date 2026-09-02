/*
 *   Copyright (c) 2026.  Atlas of Living Australia
 *   All Rights Reserved.
 *   The contents of this file are subject to the Mozilla Public
 *   License Version 1.1 (the "License"); you may not use this file
 *   except in compliance with the License. You may obtain a copy of
 *   the License at http://www.mozilla.org/MPL/
 *   Software distributed under the License is distributed on an "AS
 *   IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 *   implied. See the License for the specific language governing
 *   rights and limitations under the License.
 *
 */
package au.org.ala.alerts

import grails.core.GrailsApplication
import grails.web.mapping.LinkGenerator
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.core.sync.ResponseTransformer
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3ClientBuilder
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeParseException


/**
 * Generate CSV reports for Biosecurity alerts.
 * This implementation generates CSV files from query results and stores them in AWS S3 only.
 * Local filesystem storage is handled by {@link BiosecurityLocalCSVService}.
 *
 * @author qifeng-bai
 * @author dos009 nickdos
 */
class BiosecurityS3CSVService extends BiosecurityCSVService{
    GrailsApplication grailsApplication
    LinkGenerator grailsLinkGenerator
    static final long  EXPIRED_TIME_MILLIS = 24 * 60 * 60 * 1000*7 // 7 days

    @Override
    def list() throws Exception {
        def sortedFiles = collectFilesInS3('/')
        Map folderMap = [:]
        sortedFiles.each { it ->
            def key = it.key
            def parts = key.split('/')
            if (parts.size() > 2) {
                def folder = parts[1]
                def file = parts[2]
                if (file.endsWith('.csv')) { // Only include CSV files
                    long lastUpdated = it.lastModified() ? it.lastModified().toEpochMilli() : 0L
                    folderMap.computeIfAbsent(folder) { [] }.add([
                            name         : file,
                            size         : it.size(),
                            formattedSize: formatSize(it.size()),
                            lastUpdated  : lastUpdated
                    ])
                }
            }
        }

        def foldersAndFiles = folderMap.collect { k, v ->
            [
                    name     : k,
                    files    : v,
                    fileCount: v.size(),
                    totalSize: v.sum { it.size ?: 0L }   // bytes
            ]
        }.sort { it.name }.reverse()

        long totalFiles = (foldersAndFiles.sum { it.fileCount } ?: 0L) as long
        long totalSize  = (foldersAndFiles.sum { it.totalSize } ?: 0L) as long

        return [status:0, foldersAndFiles: foldersAndFiles, totalFiles: totalFiles, totalSize: formatSize(totalSize)]
    }

    /**
     *
     * @param folderName Assure folder exists
     * @return stream reader
     */
    @Override
    void aggregateCSVFiles(String folderName, OutputStream out) {
        // Folders in S3 are not real folders, but prefixes in the key - doesn't need to be recursive
        String bucketName = grailsApplication.config.getProperty("grails.plugin.awssdk.s3.bucket")
        def s3Files = collectFilesInS3(folderName)
        boolean firstFile = true
        s3Files.each { s3File ->
            if (!s3File.key().endsWith('.csv')) return

            def getReq = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3File.key())
                    .build()

            s3Client.getObject(getReq).withStream { inputStream ->
                inputStream.withReader('UTF-8') { reader ->
                    reader.eachLine { line, lineNumber ->
                        if (firstFile || lineNumber > 1) {
                            out.write((line + System.lineSeparator()).getBytes('UTF-8'))
                        }
                    }
                }
            }

            firstFile = false
        }

        out.flush()
    }



    @Override
    Map asyncAggregateCSVFiles(String folderName) {
        User user = userService.getUser()
        if (user?.email) {
            new Thread({
                try {
                    Collection<File> csvFiles = []
                    // Folders in S3 are not real folders, but prefixes in the key - doesn't need to be recursive
                    def s3Files = collectFilesInS3(folderName)
                    s3Files.each { objectSummary ->
                        def key = objectSummary.key
                        if (key.endsWith('.csv')) { // Filter out deleted files (end with '_deleted')
                            def tempPath = Files.createTempFile("biosecurity_", ".csv")
                            s3GetFile(key, tempPath.toAbsolutePath().toString())
                            csvFiles.add(tempPath.toFile())
                        }
                    }


                    def tempMergedFilePath = Files.createTempFile("biosecurity_merged_", ".csv")
                    def tempMergedFile = tempMergedFilePath.toFile()
                    tempMergedFile.withWriter('UTF-8') { writer ->
                        csvFiles.eachWithIndex { csvFile, index ->
                            csvFile.withReader('UTF-8') { reader ->
                                reader.eachLine { line, lineNumber ->
                                    if (index == 0 || lineNumber > 1) {
                                        // Write header from the first file and skip headers from the rest
                                        writer.writeLine(line)
                                    }
                                }
                            }
                        }
                    }

                    String token = UUID.randomUUID().toString()
                    DownloadToken.withTransaction {
                        new DownloadToken(
                                token: token,
                                fileKey: tempMergedFile.absolutePath,
                                expiresAt: new Date(System.currentTimeMillis() + EXPIRED_TIME_MILLIS)
                        ).save(flush: true,failOnError: true)
                    }

                    def downloadUrl = grailsLinkGenerator.link(
                            namespace: 'biosecurity',
                            controller: 'csv',
                            action: 'downloadWithToken',
                            params: [token: token],
                            absolute: true
                    )

                    sendMail {
                        from grailsApplication.config.mail.details.alertAddressTitle + "<" + grailsApplication.config.mail.details.sender + ">"
                        subject "Your requested Biosecurity CSV is ready for download"
                        to user.email
                        html """
                                <p>Hello Biosecurity User,</p>
                        
                                <p>Your requested CSV file is ready. You can download it using the link below. 
                                Please note that the download link will expire in 7 days.</p>
                        
                                <p><a href="${downloadUrl}">Download the CSV File (zipped)</a></p>
                        
                                <p>If you did not request this download, please ignore this email.</p>
                        
                                <p>Best regards,<br/>
                                Biosecurity Alerts Team</p>
                            """
                    }

                    //Clean up
                    csvFiles.each{
                        it.delete()
                    }
                    log.info("User ${user.id} download processed; email sent with link: ${downloadUrl}")
                } catch (Exception e) {
                    log.error("Error processing download for user ${user.id}: ${e.message}", e)
                }
            }).start()

            return [
                    status: 0,
                    message: "Your download request is being processed. You will receive an email once it is ready."
            ]
        } else {
            return [
                    status: 1,
                    message: "An email address is required to process this download request."
            ]
        }
    }


    @Override
    void generateAuditCSV(QueryResult qs) {
        def task = {
            File outputFile = createTempCSVFromQueryResult(qs)
            String folderName = new SimpleDateFormat("yyyy-MM-dd").format(new Date())
            String fileName = sanitizeFileName("${qs.query.name}")+ "_${qs.query.id}.csv"

            def s3Directory = grailsApplication.config.getProperty('biosecurity.csv.s3.directory', 'biosecurity')
            String s3Key = "${s3Directory}/${folderName}/${fileName}"
            log.debug("Uploading file to S3: " + s3Key)
            s3StoreFile(s3Key, outputFile)
        }

        Thread.start(task)
    }

    /**
     * Get file content
     *
     * @param filename
     * @return file content as String
     */
    @Override
    InputStream getFile(String filename) {
        if (filename) {
            def config = grailsApplication.config
            def folderPrefix = config.getProperty('biosecurity.csv.s3.directory', 'biosecurity')
            if (s3Exists("${folderPrefix}/${filename}")) {
                def tempPath = Files.createTempFile("biosecurity_", ".csv")
                File file = s3GetFile("${folderPrefix}/${filename}", tempPath.toAbsolutePath().toString())
                return new FileInputStream(file) {
                    @Override
                    void close() throws IOException {
                        super.close()
                        Files.deleteIfExists(file.toPath()) // delete temp file after stream is closed
                    }
                }

            }
        }
        return null
    }

    S3Client getS3Client() {
        // no longer using the grails plugin - build S3 client directly
        S3ClientBuilder builder = S3Client.builder()

        String region = grailsApplication.config.getProperty("grails.plugin.awssdk.region", "")
        if (region) {
            builder = builder.region(Region.of(region))
        }

        String profile = grailsApplication.config.getProperty("grails.plugin.awssdk.profile", "")
        if (profile) {
            builder = builder.credentialsProvider(ProfileCredentialsProvider.create(profile))
        }

        String accessKey = grailsApplication.config.getProperty("grails.plugin.awssdk.s3.accessKey", "")
        String secretKey = grailsApplication.config.getProperty("grails.plugin.awssdk.s3.secretKey", "")
        if (accessKey && secretKey) {
            builder = builder.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
        }

        S3Client s3Client = builder.build();

        return s3Client;
    }

    /**
     * todo: limits the maximum number of files returned - to avoid overwhelming the browser
     *
     * Find all files in the specified folder in S3
     *
     * @param folderName - "/" for root folder, or "2024-10-01" for specific date folder
     * @return list of S3ObjectSummary
     */
    List<S3Object> collectFilesInS3(String folderName) {
        if (!folderName) {
            folderName="/"
        }

        S3Client s3Client = getS3Client()

        String s3Directory = grailsApplication.config.getProperty('biosecurity.csv.s3.directory', 'biosecurity')
        String bucketName = grailsApplication.config.getProperty("grails.plugin.awssdk.s3.bucket")
        String prefix = "${s3Directory}/${folderName == '/' ? '' : folderName}"

        List<S3Object> allObjects = new ArrayList<>()
        String continuationToken = null;

        do {
            ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder().bucket(bucketName).prefix(prefix)
            if (continuationToken != null) {
                requestBuilder.continuationToken(continuationToken);
            }
            ListObjectsV2Response response = s3Client.listObjectsV2(requestBuilder.build())
            allObjects.addAll(response.contents().findAll { it.key().endsWith('.csv') })
            continuationToken = response.nextContinuationToken()
        } while (continuationToken != null)

        allObjects.sort((a, b) -> {
            long aTime = a.lastModified() != null ? a.lastModified().toEpochMilli() : 0L
            long bTime = b.lastModified() != null ? b.lastModified().toEpochMilli() : 0L
            return Long.compare(bTime, aTime)
        })
        log.info("Found ${allObjects.size()} files in S3 bucket: ${bucketName}, directory: ${prefix}")
        return allObjects;
    }


    @Override
    Map deleteFile(String filename) {
        if (!filename) return [status:1, message: "File not found"]

        def config = grailsApplication.config
        // Folders in S3 are not real folders, but prefixes in the key
        String folderPrefix = config.getProperty('biosecurity.csv.s3.directory', 'biosecurity')
        String bucket = config.getProperty('grails.plugin.awssdk.s3.bucket', String,null)
        Map message

        if (s3Exists("${folderPrefix}/${filename}")) {
            Boolean success = s3MoveObject(bucket, "${folderPrefix}/${filename}", bucket, "${folderPrefix}/${filename}_deleted")
            message = success ? [status: 0, message: "File deleted"] : [status: 1, message: "File deletion failed"]
        } else {
            message = [status: 1, message: "File not found"]
        }

        return message
    }

    @Override
    boolean folderExists(String folderName) {
        if (!folderName) return false

        def folderPrefix = grailsApplication.config.getProperty('biosecurity.csv.s3.directory', 'biosecurity')

        // Determine the S3 prefix
        String s3Prefix
        if (folderName == '/') {
            s3Prefix = "${folderPrefix}/" // root folder
        } else {
            s3Prefix = "${folderPrefix}/${folderName}/" // exact folder
        }

        ListObjectsV2Response s3Files = listObjects(s3Prefix)
        log.debug("Listing S3: $s3Prefix -> ${s3Files.contents().size()} files")

        // Return true if at least one object exists under this exact folder
        return s3Files.contents().any { it.key().startsWith(s3Prefix) }
    }

    void s3StoreFile(String s3Key, File outputFile) {
        S3Client s3Client = getS3Client()

        String bucketName = grailsApplication.config.getProperty("grails.plugin.awssdk.s3.bucket")
        try (def inputStream = new FileInputStream(outputFile)) {
            s3Client.putObject(builder -> builder.bucket(bucketName).key(s3Key).build(), RequestBody.fromInputStream(inputStream, outputFile.length()))
            log.info("Uploaded file to S3: " + s3Key)
        } catch (Exception e) {
            log.error("Error uploading file to S3: " + e.getMessage(), e)
        }
    }


    // returns a File at the localPath, or null
    File s3GetFile(String s3Key, String localPath) {
        S3Client s3Client = getS3Client()

        String bucketName = grailsApplication.config.getProperty("grails.plugin.awssdk.s3.bucket")
        File localFile = new File(localPath)

        try (def outputStream = new FileOutputStream(localFile)) {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucketName).key(s3Key).build()
            s3Client.getObject(getObjectRequest, ResponseTransformer.toOutputStream(outputStream))
        } catch (Exception e) {
            log.error("Error downloading file from S3: " + e.getMessage(), e)
        }

        return localFile
    }

    Boolean s3Exists(String s3Key) {
        S3Client s3Client = getS3Client()
        String bucketName = grailsApplication.config.getProperty("grails.plugin.awssdk.s3.bucket")
        try {
            s3Client.headObject(builder -> builder.bucket(bucketName).key(s3Key).build())
            return true
        } catch (NoSuchKeyException e) {
            log.error("S3 object does not exist: " + s3Key)
        }
        return false
    }


    ListObjectsV2Response listObjects(String s3Prefix) {
        S3Client s3Client = getS3Client()
        String bucketName = grailsApplication.config.getProperty("grails.plugin.awssdk.s3.bucket")

        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(s3Prefix)
                .build()

        ListObjectsV2Response response = s3Client.listObjectsV2(request)
        return response
    }


    Boolean s3MoveObject(String sourceBucket, String sourceKey, String destinationBucket, String destinationKey) {
        S3Client s3Client = getS3Client()
        try {
            // Copy the object to the new location
            s3Client.copyObject(builder -> builder
                    .copySource("${sourceBucket}/${sourceKey}")
                    .bucket(destinationBucket)
                    .key(destinationKey)
                    .build())

            // Delete the original object
            s3Client.deleteObject(builder -> builder
                    .bucket(sourceBucket)
                    .key(sourceKey)
                    .build())

            log.info("Moved S3 object from ${sourceKey} to ${destinationKey}")
            return true
        } catch (Exception e) {
            log.error("Error moving S3 object: " + e.getMessage(), e)
            return false
        }
    }


    /**
     * Mirror local directory to S3 - should only need to be run once to upload existing files
     * Will not replace existing files in S3, so can be re-run if needed.
     * Default dryRun is true, so no files are uploaded by default - user needs to add '?dryRun=false' to URL to copy files over.
     *
     * @param directory
     * @param s3BasePath
     * @return
     */
    def moveLocalFilesToS3(Boolean dryRun = false) {
        File directory = new File(grailsApplication.config.getProperty('biosecurity.csv.local.directory', '/tmp'))
        String s3BasePath = grailsApplication.config.getProperty('biosecurity.csv.s3.directory', 'biosecurity')
        String bucketName = grailsApplication.config.getProperty('grails.plugin.awssdk.s3.bucket', 'ala-alerts')
        Map msg = [:]

        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("The configured path is not a directory - ${directory.toString()}")
        }

        directory.eachFileRecurse { file ->
            if (file.isFile()) {
                String relativePath = file.absolutePath - directory.absolutePath
                String s3Key = s3BasePath + relativePath.replace(File.separator, '/')

                // Check if the file already exists in S3
                if (s3Exists(s3Key) || !s3Key.endsWith('.csv')) {
                    log.info "File ${file.name} already exists in S3 bucket ${bucketName} with key ${s3Key}. Skipping."
                    msg.filesSkipped = (msg.filesSkipped ?: []) + [file.name]
                    return  // Skip this file and continue with the next
                }

                def inputStream = new FileInputStream(file)
                try {
                    Boolean success = !dryRun && s3StoreFile(s3Key, file)
                    if (success) {
                        msg.filesUploaded = (msg.filesUploaded ?: []) + [s3Key]
                        log.info "Uploaded ${file.name} to S3 bucket ${bucketName} with key ${s3Key} (dryrun: ${dryRun})"
                    } else if (dryRun) {
                        msg.filesUploadedDryRun = (msg.filesUploadedDryRun ?: []) + [s3Key]
                        log.info "Dry run: Would have uploaded ${file.name} to S3 bucket ${bucketName} with key ${s3Key}"
                    } else {
                        msg.filesFailed = (msg.filesFailed ?: []) + [s3Key]
                        log.error "Failed to upload ${file.name} to S3 bucket ${bucketName} with key ${s3Key} (dryrun: ${dryRun})"
                    }
                } finally {
                    inputStream.close()
                }
            }
        }

        msg.message = dryRun ? "Dry run: No files were uploaded to S3, add '?dryRun=false' to URL to copy files over." : "Finished upload to S3"

        msg
    }

    /**
     * Archive all CSV files in S3 for a given year by renaming them with .archived suffix.
     * File pattern: biosecurity/yyyy-MM-dd/*.csv -> biosecurity/yyyy-MM-dd/*.csv.archived
     *
     * @param year 4-digit year to archive files for
     * @return number of files archived
     */
    int archiveCSVFilesByYear(int year) {
        log.info("Starting archive of CSV files for year ${year}...")

        String bucketName = grailsApplication.config.getProperty("grails.plugin.awssdk.s3.bucket")
        
        // List all objects under biosecurity/ prefix
        List<S3Object> allObjects = collectFilesInS3('/')
        
        int archivedCount = 0
        
        allObjects.each { s3Object ->
            String key = s3Object.key()
            
            // Skip if already archived
            if (key.endsWith('.csv.archived')) {
                return
            }
            
            // Only process .csv files
            if (!key.endsWith('.csv')) {
                return
            }
            
            // Extract date folder from key: biosecurity/2025-01-15/file.csv -> 2025-01-15
            String[] parts = key.split('/')
            if (parts.length < 3) {
                return  // Skip malformed keys
            }
            
            String dateFolder = parts[1]
            
            // Parse date and check year
            try {
                LocalDate fileDate = LocalDate.parse(dateFolder)
                if (fileDate.year != year) {
                    return  // Skip files not in target year
                }
            } catch (DateTimeParseException e) {
                log.warn("Skipping S3 object with invalid date folder: ${key}")
                return
            }
            
            // Archive: rename .csv to .csv.archived
            String archivedKey = key + '.archived'
            
            log.debug("Archiving ${key} -> ${archivedKey}")
            
            boolean success = s3MoveObject(bucketName, key, bucketName, archivedKey)
            if (success) {
                archivedCount++
                log.info("Archived: ${key}")
            } else {
                log.error("Failed to archive: ${key}")
            }
        }
        
        log.info("Archived ${archivedCount} CSV file(s) for year ${year}")
        return archivedCount
    }

    /**
     * Download all CSV files for a given year from S3, merge them in date-ascending order
     * (header written once from the first file), then upload the merged result back to S3.
     *
     * S3 target key convention (mirrors mergeBiosecurityCsvByDate.groovy naming):
     *   biosecurity/<year>/annual-records.csv
     *
     * @param year 4-digit year to aggregate
     * @return S3 key of the uploaded merged file, or null on failure
     */
    @Override
    String aggregateAndUploadByYear(int year) {
        log.info("Starting aggregation of CSV files for year ${year}...")

        String s3Directory = grailsApplication.config.getProperty('biosecurity.csv.s3.directory', 'biosecurity')
        String targetKey   = "${s3Directory}/${year}/annual-records.csv"

        // Collect all .csv objects for the given year, sorted by date folder then filename
        List<S3Object> allObjects = collectFilesInS3('/')

        // Build TreeMap: LocalDate -> sorted list of S3 keys (date-ascending)
        TreeMap<LocalDate, List<String>> dateToKeys = new TreeMap<>()

        allObjects.each { s3Object ->
            String key = s3Object.key()

            // Only process plain .csv files (not .csv.archived)
            if (!key.endsWith('.csv') || key.endsWith('.csv.archived')) return

            String[] parts = key.split('/')
            if (parts.length < 3) return

            String dateFolder = parts[1]
            try {
                LocalDate fileDate = LocalDate.parse(dateFolder)
                if (fileDate.year != year) return
                dateToKeys.computeIfAbsent(fileDate) { [] } << key
            } catch (DateTimeParseException e) {
                log.warn("Skipping S3 object with invalid date folder during aggregation: ${key}")
            }
        }

        if (dateToKeys.isEmpty()) {
            log.warn("No CSV files found for year ${year} — skipping aggregation.")
            return null
        }

        // Download each file to a temp file, merge in date-ascending order
        File tempMerged = Files.createTempFile("biosecurity_merged_${year}_", ".csv").toFile()
        int totalRows = 0
        boolean headerWritten = false

        try {
            tempMerged.withWriter('UTF-8') { writer ->
                dateToKeys.each { LocalDate date, List<String> keys ->
                    keys.sort()  // deterministic within same date
                    keys.each { String s3Key ->
                        File tempFile = Files.createTempFile("biosecurity_dl_", ".csv").toFile()
                        try {
                            s3GetFile(s3Key, tempFile.absolutePath)
                            List<String> lines = tempFile.readLines('UTF-8')
                            if (lines.isEmpty()) return

                            // Write header only once from the very first file
                            if (!headerWritten) {
                                writer.writeLine(lines[0])
                                headerWritten = true
                            }

                            // Write data rows, skipping header
                            lines.drop(1).each { line ->
                                if (line?.trim()) {
                                    writer.writeLine(line)
                                    totalRows++
                                }
                            }
                            log.debug("Merged ${lines.size() - 1} row(s) from ${s3Key}")
                        } finally {
                            tempFile.delete()
                        }
                    }
                }
            }

            // Upload merged file to S3: biosecurity/<year>/annual-records.csv
            log.info("Uploading merged file (${totalRows} rows) to S3: ${targetKey}")
            s3StoreFile(targetKey, tempMerged)
            log.info("Aggregation complete for year ${year}: ${targetKey}")
            return targetKey

        } catch (Exception e) {
            log.error("Error during aggregation for year ${year}: ${e.message}", e)
            return null
        } finally {
            tempMerged.delete()
        }
    }

    /**
     * Unarchive all CSV files in S3 for a given year by removing the .archived suffix.
     * File pattern: biosecurity/yyyy-MM-dd/*.csv.archived to biosecurity/yyyy-MM-dd/*.csv
     *
     * @param year 4-digit year to unarchive files for
     * @return number of files unarchived
     */
    int unarchiveCSVFilesByYear(int year) {
        log.info("Starting unarchive of CSV files for year ${year}...")

        String bucketName = grailsApplication.config.getProperty("grails.plugin.awssdk.s3.bucket")

        // List all objects under biosecurity/ prefix
        List<S3Object> allObjects = collectFilesInS3('/')

        int unarchivedCount = 0

        allObjects.each { s3Object ->
            String key = s3Object.key()

            // Only process .csv.archived files
            if (!key.endsWith('.csv.archived')) {
                return
            }

            // Extract date folder from key: biosecurity/2025-01-15/file.csv.archived -> 2025-01-15
            String[] parts = key.split('/')
            if (parts.length < 3) {
                return  // Skip malformed keys
            }

            String dateFolder = parts[1]

            // Parse date and check year
            try {
                LocalDate fileDate = LocalDate.parse(dateFolder)
                if (fileDate.year != year) {
                    return  // Skip files not in target year
                }
            } catch (DateTimeParseException e) {
                log.warn("Skipping S3 object with invalid date folder: ${key}")
                return
            }

            // Unarchive: remove .archived suffix to restore original .csv key
            String restoredKey = key.substring(0, key.length() - '.archived'.length())

            log.debug("Unarchiving ${key} -> ${restoredKey}")

            boolean success = s3MoveObject(bucketName, key, bucketName, restoredKey)
            if (success) {
                unarchivedCount++
                log.info("Unarchived: ${key} -> ${restoredKey}")
            } else {
                log.error("Failed to unarchive: ${key}")
            }
        }

        log.info("Unarchived ${unarchivedCount} CSV file(s) for year ${year}")
        return unarchivedCount
    }
}
