/*
 *   Copyright (c) 2024.  Atlas of Living Australia
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
                   folderMap.computeIfAbsent(folder) { [] }.add([
                           name: file,
                           size: it.size()   // bytes
                   ])
                }
            }
        }

        def foldersAndFiles = folderMap.collect { k, v ->
            [
                    name      : k,
                    files     : v.collect { it.name },
                    fileCount: v.size(),
                    totalSize: v.sum { it.size ?: 0L }   // bytes
            ]
        }.sort { it.name }.reverse()

        long totalFiles = foldersAndFiles.sum { it.fileCount }
        long totalSize  = foldersAndFiles.sum { it.totalSize }

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
    void generateAuditCSV(QueryResult qs) {
        def task = {
            File outputFile = createTempCSVFromQueryResult(qs)
            String folderName = new SimpleDateFormat("yyyy-MM-dd").format(new Date())
            String fileName = sanitizeFileName("${qs.query.name}")+ ".csv"

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
    String getFile(String filename) {
        if (filename) {
            def config = grailsApplication.config
            def folderPrefix = config.getProperty('biosecurity.csv.s3.directory', 'biosecurity')
            if (s3Exists("${folderPrefix}/${filename}")) {
                def file = s3GetFile("${folderPrefix}/${filename}", "/tmp/${filename.tokenize(File.separator).last()}")
                return file.text
            }
        }
        ''
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
            allObjects.addAll(response.contents())
            continuationToken = response.nextContinuationToken()
        } while (continuationToken != null)

        allObjects.sort((a, b) -> Long.compare(b.lastModified().toEpochMilli(), a.lastModified().toEpochMilli()));
        log.info("Found ${allObjects.size()} files in S3 bucket: ${bucketName}, directory: ${prefix}")
        return allObjects;
    }


    @Override
    Map deleteFile(String filename) {
        if (!filename) return [status:0, message: "File not found"]

        def config = grailsApplication.config
        // Folders in S3 are not real folders, but prefixes in the key
        String folderPrefix = config.getProperty('biosecurity.csv.s3.directory', 'biosecurity')
        String bucket = config.getProperty('grails.plugin.awssdk.s3.bucket', String,null)
        Map message

        if (s3Exists("${folderPrefix}/${filename}")) {
            Boolean success = s3MoveObject(bucket, "${folderPrefix}/${filename}", bucket, "${folderPrefix}/${filename}_deleted")
            message = success ? [status: 0, message: "File deleted"] : [status: 1, message: "File deletion failed"]
        } else {
            message = [status: 0, message: "File not found"]
        }

        return message
    }

    @Override
    boolean folderExists(String folderName) {
        if (!folderName) return false

        def folderPrefix =  grailsApplication.config.getProperty('biosecurity.csv.s3.directory', 'biosecurity')
        def s3Prefix = "${folderPrefix}/${folderName == '/' ? '' : folderName}"
        ListObjectsV2Response s3Files = listObjects(s3Prefix)
        log.debug("Listing S3: $s3Prefix -> ${s3Files.contents().size()} files")
        return !s3Files.contents().isEmpty()
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
            log.info("Downloaded file from S3: " + s3Key)
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
}
