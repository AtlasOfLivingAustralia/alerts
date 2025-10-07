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

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.amazonaws.services.s3.model.ObjectListing
import com.amazonaws.services.s3.model.S3ObjectSummary
import grails.plugin.awssdk.s3.AmazonS3Service
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

    AmazonS3Service amazonS3Service

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
    List<S3ObjectSummary> collectFilesInS3(String folderName) {
        if (!folderName) {
            folderName="/"
        }

        AmazonS3 s3Client = amazonS3Service.client

        String s3Directory = grailsApplication.config.getProperty('biosecurity.csv.s3.directory', 'biosecurity')
        def allObjects = []
        String bucketName = grailsApplication.config.getProperty("grails.plugin.awssdk.s3.bucket")
        String prefix = "${s3Directory}/${folderName == '/' ? '' : folderName}"
        ObjectListing listing = s3Client.listObjects(bucketName,prefix)

        while (true) {
            allObjects.addAll(listing.getObjectSummaries())
            if (!listing.isTruncated()) break
            listing = s3Client.listNextBatchOfObjects(listing)
        }

        List<S3ObjectSummary> sortedFiles = allObjects.sort { -it.lastModified.time }
        log.info("Found ${sortedFiles.size()} files in S3 bucket: ${bucketName}, directory: ${prefix}")
        sortedFiles
    }

    /**
     * Called by cron job to generate CSV files when Notification service finds  new records
     *
     * @param qs
     */
    void generateAuditCSV(QueryResult qs) {
        def task = {
            File outputFile = createTempCSV(qs)
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
    File createTempCSV(QueryResult qs) {
        def records = diffService.getNewRecords(qs)

        String outputFile = sanitizeFileName("${new SimpleDateFormat("yyyy-MM-dd").format(qs.lastChecked)}")

        def tempFilePath = Files.createTempFile(outputFile, ".csv")
        def tempFile = tempFilePath.toFile()
        // example of rawHeader
        // recordID:uuid  recordID is the header name, uuid is the property in the record
        String rawHeader = "recordID:uuid, recordLink:occurrenceLink, scientificName,taxonConceptID,decimalLatitude,decimalLongitude,eventDate,occurrenceStatus,dataResourceName,multimedia,mediaId:image," +
                "vernacularName,taxonConceptID_new,kingdom,phylum,class:classs,order,family,genus,species,subspecies," +
                "firstLoadedDate:firstLoaded,basisOfRecord,match," +
                "searchTerm:search_term,correct name:scientificName,provided name:providedName,common name:vernacularName,state:stateProvince,lga layer,lga,fq,list id:listId,list name:listName, listLink:listLink, cw_state,shape feature:shape_feature,creator:collector," +
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
                        case "lga":
                            //read from cl (context layer)
                            def cls = record["cl"]
                            //LGA2023 is the default layer id
                            def layerId = grailsApplication.config.getProperty('biosecurity.csv.lga', 'LGA2023')
                            if(cls) {
                                String matched = cls.find {
                                    def (k, v) = it.split(':') // Split the string into key and value
                                    k.toLowerCase() == layerId.toLowerCase()
                                }
                                //assure return "" if matched is null
                                value = matched?.split(':')?.with { it.size() > 1 ? it[1] : "" } ?: ""
                            }
                            break
                        case "lga layer":
                            value = grailsApplication.config.getProperty('biosecurity.csv.lga', 'LGA2023')
                            break
                        case "eventDate":
                            if (value) {
                                value = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(value.toLong())
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
                def parts = key.split('/')
                if (parts.size() > 2) {
                    def file = parts[2]
                    if (file.endsWith('.csv')) { // Filter out deleted files (end with '_deleted')
                        def tempFile = amazonS3Service.getFile(key, "/tmp/${file.tokenize(File.separator).last()}")
                        csvFiles.add(tempFile)
                    }
                }
            }
        } else if (grailsApplication.config.getProperty('biosecurity.csv.local.enabled', Boolean, true)) {
            // recursively collect CSV files from the folder and its subfolders
            collectLocalCsvFiles(folder, csvFiles)
        }

        log.info("Aggregate ${csvFiles.size()} CSV files.....")
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
