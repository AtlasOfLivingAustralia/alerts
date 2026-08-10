package au.org.ala.alerts

import au.org.ala.ws.service.WebService
import grails.core.GrailsApplication
import org.apache.http.entity.ContentType
import org.springframework.beans.factory.annotation.Autowired

import java.nio.file.Files
import java.text.SimpleDateFormat
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.csv.CSVFormat

/**
 *  Abstract service for generating and managing CSV files for Biosecurity alerts.
 *  Concrete implementations include {@code BiosecurityLocalCSVService} for local file storage and {@code BiosecurityS3CSVService} for S3 storage.
 */
abstract class BiosecurityCSVService {
    @Autowired
    protected DiffService diffService
    @Autowired
    protected GrailsApplication grailsApplication
    @Autowired
    protected WebService webService
    @Autowired
    protected UserService userService
    @Autowired
    protected EmailService emailService

    /**
     * List all files, including the total number of files, and the total size of files
     *
     * @return [status:0, foldersAndFiles: [folder:filesInside], totalFiles: int, totalSize: String (GB,MB..)]
     */
    abstract def list()
    abstract void aggregateCSVFiles(String folder, OutputStream out)
    abstract Map asyncAggregateCSVFiles(String folder)
    abstract InputStream getFile(String filename)
    /**
     *  message['status'] = 0 : deletion completed
     * @param filename
     * @return ['status', ',message']
     */
    abstract Map deleteFile(String filename)
    /**
     * Called by cron job to generate CSV files when Notification service finds  new records
     * Should be an Async call
     * @param qs
     */
    abstract void generateAuditCSV(QueryResult qs)
    abstract boolean folderExists(String folderName)

    /**
     * Archive all CSV files for a given year by renaming them with .archived suffix.
     * @param year 4-digit year to archive files for
     * @return number of files archived
     */
    abstract int archiveCSVFilesByYear(int year)

    abstract int unarchiveCSVFilesByYear(int year)

    /**
     * Aggregate all CSV files for a given year into a single merged file and upload to S3.
     * @param year 4-digit year to aggregate
     * @return S3 key of the uploaded merged file, or null on failure
     */
    abstract String aggregateAndUploadByYear(int year)


    static String formatSize(long size) {
        String totalSizeFormatted = ""
        if (size >= 1024 * 1024 * 1024) {
            totalSizeFormatted = "${(size / (1024 * 1024 * 1024)).round()} GB"
        } else if (size >= 1024 * 1024) {
            totalSizeFormatted = "${(size / (1024 * 1024)).round()} MB"
        } else if (size >= 1024) {
            totalSizeFormatted = "${(size / 1024).round()} KB"
        } else {
            totalSizeFormatted = "${size} B"
        }
        return totalSizeFormatted
    }

    /**
     * Sanitize file name
     * @param fileName
     * @return sanitized file name
     */
    static String sanitizeFileName(String fileName) {
        // Define a pattern for illegal characters
        def pattern = /[^a-zA-Z0-9\.\-\_]/
        def sanitized = fileName.replaceAll(pattern, '_')
        // Truncate to 150 characters preserving extension if possible
        if (sanitized.length() > 200) {
            int dotIndex = sanitized.lastIndexOf('.')
            if (dotIndex > 0 && dotIndex > sanitized.length() - 20) {
                // keep extension
                String ext = sanitized.substring(dotIndex)
                sanitized = sanitized.substring(0, 150 - ext.length()) + ext
            } else {
                sanitized = sanitized.substring(0, 150)
            }
        }
        return sanitized
    }


     /**
     * Main logic to create a temp CSV file from query result
     * @param QueryResult
     * @return File object
     */
    File createTempCSVFromQueryResult(QueryResult qs) {
        def records = qs.newRecords
        log.info("Generating CSV for ${qs.query?.name} : [ ${records.size()}] occurrences")

        String outputFile = sanitizeFileName("${new SimpleDateFormat("yyyy-MM-dd").format(qs.lastChecked)}")

        def tempFilePath = Files.createTempFile(outputFile, ".csv")
        def tempFile = tempFilePath.toFile()
        // example of rawHeader
        // recordID:uuid  recordID is the header name, uuid is the property in the record
        String rawHeader = "recordID:uuid, recordLink:occurrenceLink, scientificName,taxonConceptID,decimalLatitude,decimalLongitude,eventDate,occurrenceStatus,dataResourceName,multimedia,mediaId:image," +
                "vernacularName,taxonConceptID_new,kingdom,phylum,class:classs,order,family,genus,species,subspecies," +
                "firstLoadedDate,basisOfRecord,match," +
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
                        case ["eventDate"]:
                            if (value) {
                                try {
                                    value = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(value.toLong())
                                } catch(Exception ignored) {
                                    //keep the original value if it cannot be parsed
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
}
