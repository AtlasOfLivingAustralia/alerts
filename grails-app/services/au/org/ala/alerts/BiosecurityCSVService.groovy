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

import au.org.ala.ws.service.WebService
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.csv.CSVFormat
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.text.SimpleDateFormat


/**
 * Generate CSV reports for Biosecurity alerts
 * CSV files are generated from the query results and stored on the local file system
 * or on AWS S3
 *
 * @author qifeng-bai
 * @author dos009 nickdos (S3 additions)
 */
class BiosecurityCSVService {
    def diffService
    def grailsApplication
    WebService webService

    def list() throws Exception {
        if (grailsApplication.config.getProperty('biosecurity.csv.s3.enabled', Boolean, false)) {
            def sortedFiles = collectFilesInS3('/')
            Map folderMap = [:]
            sortedFiles.each { it ->
                def key = it.key
                def parts = key.split('/')
                if (parts.size() > 2) {
                    def folder = parts[1]
                    def file = parts[2]
                    if (file.endsWith('.csv')) { // Only include CSV files
                       folderMap.computeIfAbsent(folder) { [] }.add(file)
                    }
                }
            }
            def foldersAndFiles = folderMap.collect { k, v -> [name: k, files: v] }.sort({ it.name }).reverse()

            return [status:0, foldersAndFiles: foldersAndFiles]
        } else if (grailsApplication.config.getProperty('biosecurity.csv.local.enabled', Boolean, true)) {
            def BASE_DIRECTORY = grailsApplication.config.getProperty('biosecurity.csv.local.directory', '/tmp')
            def dir = new File(BASE_DIRECTORY)
            if (!dir.exists() || !dir.isDirectory()) {
                return [status: 1, message: "Directory not found"]
            }

            def foldersAndFiles = listFilesRecursively(dir)
            return [status:0, foldersAndFiles: foldersAndFiles]
        }
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

    //Batch Query Biocache (Using qid) to collect extra info
    //Those extra info are only stored in CSV file, not included in the Emails
    //e.g. first loaded date, lga layerID, lga name etc
    //
    def fetchExtraOccurrenceInfo(def records) {
        String layerId = grailsApplication.config.getProperty('biosecurity.lga', 'cl11170')
        String qidUrl = grailsApplication.config.getProperty('biocacheService.baseURL') + '/qid'

        int limits = 1000
        records.collate(limits).each {batch ->
            def ids = batch.collect {it.uuid}
            def query = ids.collect { "id:${it}" }.join(" OR ")
            def qidResp = webService.post(
                    qidUrl,
                    ["q": query],
                    [:],
                    ContentType.APPLICATION_FORM_URLENCODED
            )

            if (qidResp.statusCode == 200) {
                def qid = qidResp.resp?.keySet()?.iterator()?.next()
                if (qid) {
                    def occurrenceUrl = grailsApplication.config.getProperty('biocacheService.baseURL') + "/occurrences/search?q=qid:${qid}&pageSize=${limits}&fl=id,firstLoadedDate,${layerId}"
                    def occurrencesResp = webService.get(occurrenceUrl)
                    //e.g.
                    //{
                    //    uuid: "d8b1bd1a-98b6-494d-91c0-f0a4aa636d30",
                    //    otherProperties: {
                    //        firstLoadedDate: "2025-11-13T03:29:22.089+00:00",
                    //        cl11170: "Western Downs"
                    //    }
                    //}
                    if (occurrencesResp.statusCode == 200) {
                        def occurrences = occurrencesResp.resp?["occurrences"]
                        def occMap = occurrences.collectEntries { occ ->
                            [(occ.uuid): occ]
                        }

                        //Update each record only if a matching occurrence exists
                        batch.each { record ->
                            def occ = occMap[record.uuid]
                            if (occ) {
                                record['lgaLayer'] = layerId
                                record['lga'] = occ.otherProperties?[layerId] ?: ""
                                record['firstLoaded'] = occ.otherProperties?.firstLoadedDate
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Called by cron job to generate CSV files when Notification service finds  new records
     *
     * @param qs
     */
    void generateAuditCSV(QueryResult qs) {
        def task = {
            File outputFile = createTempCSVFromQueryResult(qs)
            String folderName = new SimpleDateFormat("yyyy-MM-dd").format(new Date())
            String fileName = sanitizeFileName("${qs.query.name}")+ ".csv"
            if (grailsApplication.config.getProperty('biosecurity.csv.s3.enabled', Boolean, false)) {
                def s3Directory = grailsApplication.config.getProperty('biosecurity.csv.s3.directory', 'biosecurity')
                String s3Key = "${s3Directory}/${folderName}/${fileName}"
                log.debug("Uploading file to S3: " + s3Key)
                amazonS3Service.storeFile(s3Key, outputFile, CannedAccessControlList.Private)
            } else if (grailsApplication.config.getProperty('biosecurity.csv.local.enabled', Boolean, true)) {
              String destinationFolder = new File(grailsApplication.config.getProperty('biosecurity.csv.local.directory', '/tmp'), folderName).absolutePath
              File destinationFile = new File(destinationFolder, fileName)
              moveToDestination(outputFile, destinationFile)
            }
        }

        Thread.start(task)
    }

    /**
     * Main logic to create a temp CSV file from query result
     * @param QueryResult
     * @return File object
     */
    File createTempCSVFromQueryResult(QueryResult qs) {
        def records = diffService.getNewRecords(qs)
        log.info("Generating CSV for ${qs.query?.name} : [ ${records.size()}] occurrences")
        //Batch query extra info for each occurrences
        fetchExtraOccurrenceInfo(records)

        String outputFile = sanitizeFileName("${new SimpleDateFormat("yyyy-MM-dd").format(qs.lastChecked)}")

        def tempFilePath = Files.createTempFile(outputFile, ".csv")
        def tempFile = tempFilePath.toFile()
        // example of rawHeader
        // recordID:uuid  recordID is the header name, uuid is the property in the record
        String rawHeader = "recordID:uuid, recordLink:occurrenceLink, scientificName,taxonConceptID,decimalLatitude,decimalLongitude,eventDate,occurrenceStatus,dataResourceName,multimedia,mediaId:image," +
                "vernacularName,taxonConceptID_new,kingdom,phylum,class:classs,order,family,genus,species,subspecies," +
                "firstLoadedDate:firstLoaded,basisOfRecord,match," +
                "searchTerm:search_term,correct name:scientificName,provided name:providedName,common name:vernacularName,state:stateProvince,lga layer:lgaLayer,lga,fq," +
                "list id:listId,list name:listName, listLink:listLink, cw_state,shape feature:shape_feature,creator:collector," +
                "license,mimetype," +
                "image url:smallImageUrl," + // TBC , multiple image urls
                "date sent:dateSent"
                //"fq, kvs"
        if (grailsApplication.config.getProperty('biosecurity.csv.headers')) {
            rawHeader =  grailsApplication.config.getProperty('biosecurity.csv.headers')
        }

        def headers = []
        def fields = []
        def headersAndFields = rawHeader.split(',')
        headersAndFields.each { entry ->
            def parts = entry.trim().split(':', 2)  // Split on ':' with a limit of 2 parts
            headers << parts[0]  // Add the part before ':' to the first array
            if (parts.size() > 1) {
                fields << parts[1]  // Add the part after ':' to the second array if it exists
            } else {
                // If there's no ':' in the entry, add the same value to the second array
                fields << parts[0]
            }
        }

        tempFile.withWriter { writer ->
            writer.write(headers.join(",")+ "\n")
            records.each { record ->
                def values = fields.collect { field ->
                    def value = record[field]

                    switch (field) {
                        case ["eventDate", "firstLoaded"]:
                            if (value) {
                                try {
                                    value = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(value.toLong())
                                } catch(Exception ignored) {
                                    value = ""
                                }
                            } else {
                                value = ""
                            }
                            break
                        default:
                            if (record.containsKey(field)) {
                                if (value instanceof List) {
                                    value = "\"${value.join(";")}\""  // Join the list with ';' and wrap it in double quotes
                                } else {
                                    value = value.toString()
                                }
                            } else {
                                value = ""
                            }
                            break
                    }

                    return value
                }

                // Write the values to the CSV file using Commons-CSV RFC4180 format
                StringWriter stringWriter = new StringWriter()
                CSVPrinter csvPrinter = new CSVPrinter(stringWriter , CSVFormat.RFC4180)
                csvPrinter.printRecord(values)
                writer.write(stringWriter.toString())
            }
        }
        log.info("The CSV for ${qs.query?.name} was generated")
        tempFile
    }

    /**
     *
     * @param folderName Assure folder exists
     * @return absolute path of the aggregated CSV file
     */
    String aggregateCSVFiles(String folderName) {
        def BASE_DIRECTORY = grailsApplication.config.getProperty('biosecurity.csv.local.directory', '/tmp')
        def folder = new File(BASE_DIRECTORY, folderName)
        Collection<File> csvFiles = []

        if (grailsApplication.config.getProperty('biosecurity.csv.s3.enabled', Boolean, false)) {
            // Folders in S3 are not real folders, but prefixes in the key - doesn't need to be recursive
            def s3Files = collectFilesInS3(folderName)
            s3Files.each { objectSummary ->
                def key = objectSummary.key
                if (key.endsWith('.csv')) { // Filter out deleted files (end with '_deleted')
                    def tempFile = amazonS3Service.getFile(key, "/tmp/${UUID.randomUUID()}.csv")
                    csvFiles.add(tempFile)
                }
            }
        } else if (grailsApplication.config.getProperty('biosecurity.csv.local.enabled', Boolean, true)) {
            // recursively collect CSV files from the folder and its subfolders
            collectLocalCsvFiles(folder, csvFiles)
        }

        def tempFilePath = Files.createTempFile("merged_", ".csv")
        def tempFile = tempFilePath.toFile()
        tempFile.withWriter { writer ->
            try {
                csvFiles.eachWithIndex {  csvFile, index ->
                    csvFile.withReader('UTF-8') { reader ->
                        reader.eachLine { line, lineNumber ->
                            if (index == 0 || lineNumber > 1) { // Write header from the first file and skip headers from the rest
                                writer.writeLine(line)
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error in generating CSV file: ${e.message}")
            }
        }

        return tempFile.absolutePath
    }

    /**
     * Collect CSV files from the folder and its subfolders
     * @param folder
     * @param collectedFiles
     */
    private void collectLocalCsvFiles(File folder, Collection<File> collectedFiles) {
        folder.listFiles().each { file ->
            if (file.isDirectory()) {
                collectLocalCsvFiles(file, collectedFiles) // Recursively collect CSV files from subfolders
            } else if (file.isFile() && file.name.endsWith('.csv')) {
                collectedFiles.add(file)
            }
        }
    }

    /**
     * Check if folder exists
     *
     * @param folderName
     * @return true if folder exists
     */
    Boolean folderExists(String folderName) {
        if (!folderName) return false

        def config = grailsApplication.config
        if (config.getProperty('biosecurity.csv.s3.enabled', Boolean, false)) {
            def folderPrefix = config.getProperty('biosecurity.csv.s3.directory', 'biosecurity')
            def s3Prefix = "${folderPrefix}/${folderName == '/' ? '' : folderName}"
            def s3Files = amazonS3Service.listObjects(s3Prefix)
            log.debug("Listing S3: $s3Prefix -> ${s3Files.objectSummaries.size()} files")
            return !s3Files.objectSummaries.isEmpty()
        } else if (config.getProperty('biosecurity.csv.local.enabled', Boolean, true)) {
            String baseDirectory = config.getProperty('biosecurity.csv.local.directory', String, '/tmp')
            File folder = new File(baseDirectory, folderName)
            return folder.exists() && folder.isDirectory()
        }

        false
    }

    /**
     * Get file content
     *
     * @param filename
     * @return file content as String
     */
    String getFile(String filename) {
        if (!filename) return ''

        def config = grailsApplication.config

        if (config.getProperty('biosecurity.csv.s3.enabled', Boolean, false)) {
            // Folders in S3 are not real folders, but prefixes in the key
            def folderPrefix = config.getProperty('biosecurity.csv.s3.directory', 'biosecurity')
            if (amazonS3Service.exists("${folderPrefix}/${filename}")) {
                def file = amazonS3Service.getFile("${folderPrefix}/${filename}", "/tmp/${filename.tokenize(File.separator).last()}")
                return file.text
            }
        } else if (config.getProperty('biosecurity.csv.local.enabled', Boolean, true)) {
            String BASE_DIRECTORY = config.getProperty('biosecurity.csv.local.directory', String, '/tmp')
            def file = new File(BASE_DIRECTORY, filename)
            if (file.exists()) {
                return file.text
            }
        }

        ''
    }

    Map deleteFile(String filename) {
        if (!filename) return [:]

        def config = grailsApplication.config

        if (config.getProperty('biosecurity.csv.s3.enabled', Boolean, false)) {
            // Folders in S3 are not real folders, but prefixes in the key
            String folderPrefix = config.getProperty('biosecurity.csv.s3.directory', 'biosecurity')
            String bucket = config.getProperty('grails.plugin.awssdk.s3.bucket', null)
            Map message

            if (amazonS3Service.exists("${folderPrefix}/${filename}")) {
                Boolean success = amazonS3Service.moveObject(bucket, "${folderPrefix}/${filename}", bucket, "${folderPrefix}/${filename}_deleted")
                message = success ? [status: 0, message: "File deleted"] : [status: 1, message: "File deletion failed"]
            } else {
                message = [status: 1, message: "File not found"]
            }

            return message
        } else if (config.getProperty('biosecurity.csv.local.enabled', Boolean, true)) {
            def BASE_DIRECTORY = grailsApplication.config.biosecurity.csv.local.directory
            def file = new File(BASE_DIRECTORY, filename)
            def message = [:]

            if (!file.exists() || file.isDirectory()) {
                message['status'] = 1
                message['message'] = "File not found"
            } else {
                if (file.renameTo(new File(BASE_DIRECTORY,  filename +'_deleted'))){
                    message['status'] = 0
                    message['message'] = "The file has been temporarily deleted. You can contact the system administrator to recover it."
                } else {
                    message['status'] = 1
                    message['message'] = "File deletion failed"
                }
            }

            return message
        }

        return [:]
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
                if (amazonS3Service.exists(s3Key) || !s3Key.endsWith('.csv')) {
                    log.info "File ${file.name} already exists in S3 bucket ${bucketName} with key ${s3Key}. Skipping."
                    msg.filesSkipped = (msg.filesSkipped ?: []) + [file.name]
                    return  // Skip this file and continue with the next
                }

                def inputStream = new FileInputStream(file)
                try {
                    Boolean success = !dryRun && amazonS3Service.storeFile(s3Key, file, CannedAccessControlList.Private)
                    if (success) {
                        msg.filesUploaded = (msg.filesUploaded ?: []) + [s3Key]
                        log.info "Uploaded ${file.name} to S3 bucket ${bucketName} with key ${s3Key} (dryrun: ${dryRun})"
                    } else if (dryRun) {
                        msg.filesUploadedDryRun = (msg.filesUploadedDryRun ?: []) + [s3Key]
                        log.info "Dry run: Would have uploaded ${file.name} to S3 bucket ${bucketName} with key ${s3Key}"
                    } else {
                        msg.filesFailed = (msg.filesFailed ?: []) + [s3Key]
                        log.error "Filed to uploaded ${file.name} to S3 bucket ${bucketName} with key ${s3Key} (dryrun: ${dryRun})"
                    }
                } finally {
                    inputStream.close()
                }
            }
        }

        msg.message = dryRun ? "Dry run: No files were uploaded to S3, add '?dryRun=false'`' to URL to copy files over." : "Finished upload to S3"

        msg
    }

    /**
     * Move file from source to destination
     * @param source
     * @param destination
     */
    private void moveToDestination(File source, File destination) {
        File destDir = new File(destination.parent)
        if (!destDir.exists()) {
            destDir.mkdirs()
        }
        //copy source file to destination
        Files.move(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING)
    }

    /**
     * Sanitize file name
     * @param fileName
     * @return sanitized file name
     */
    static String sanitizeFileName(String fileName) {
        // Define a pattern for illegal characters
        def pattern = /[^a-zA-Z0-9\.\-\_]/
        return fileName.replaceAll(pattern, '_')
    }

    /**
     * List all files in the directory recursively, ignoring deleted files
     *
     * @param dir
     * @return Key value pair of folder and files
     * */
    private List<Map> listFilesRecursively(File dir) {
        def BASE_DIRECTORY =  grailsApplication.config.getProperty('biosecurity.csv.local.directory', '/tmp')
        def rootDir = new File(BASE_DIRECTORY)
        def foldersAndFiles = rootDir.listFiles().findAll { it.isDirectory() }.collect { folder ->
            [
                name: folder.name,
                files: folder.listFiles().findAll { File file ->
                    file.isFile() && file.name.endsWith('.csv')  // Filter out deleted files (ends with '_deleted')
                }.collect { file -> file.name }
            ]
        }
        return foldersAndFiles.sort({ it.name }).reverse()
    }
}
