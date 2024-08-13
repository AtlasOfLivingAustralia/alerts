package au.org.ala.alerts

import groovy.json.JsonOutput

import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.text.SimpleDateFormat

class BiosecurityCSVService {
    def diffService
    def grailsApplication

    def list() {
        def BASE_DIRECTORY = grailsApplication.config.biosecurity.csv.local.directory
        if (grailsApplication.config.biosecurity.csv.local.enabled) {
            def dir = new File(BASE_DIRECTORY)
            if (!dir.exists() || !dir.isDirectory()) {
                return [status: 1, message: "Directory not found"]
            }

            def foldersAndFiles = listFilesRecursively(dir)
            return [status:0, foldersAndFiles: foldersAndFiles]
        } else {
            return [status: 1,  message: "We does support download CSV from S3 here"]
        }
    }

    /**
     * Called by cron job to generate CSV files when Notification service finds  new records
     *
     * @param qs
     */
    void generateAuditCSV(QueryResult qs) {
        def task = {
            def records = diffService.getNewRecords(qs)

            File outputFile = createTempCSV(qs)

            if (grailsApplication.config.biosecurity.csv.local.enabled) {
                String fileName = sanitizeFileName("${qs.query.name}")+ ".csv"
                String folderName = new SimpleDateFormat("yyyy-MM-dd").format(new Date())
                String destinationFolder = new File(grailsApplication.config.biosecurity.csv.local.directory, folderName).absolutePath
                File destinationFile = new File(destinationFolder, fileName)
                moveToDestination(outputFile, destinationFile)
            }
        }

        Thread.start(task)
    }

    /**
     * Main logic to create a temp CSV file from query result
     * @param qs
     * @return
     */
    File createTempCSV(QueryResult qs) {
        def records = diffService.getNewRecords(qs)

        String outputFile = sanitizeFileName("${new SimpleDateFormat("yyyy-MM-dd").format(qs.lastChecked)}.csv")

        def tempFilePath = Files.createTempFile(outputFile, ".csv")
        def tempFile = tempFilePath.toFile()
        String rawHeader = "recordID:uuid, recordLink:occurrenceLink, scientificName,taxonConceptID,decimalLatitude,decimalLongitude,eventDate,occurrenceStatus,dataResourceName,multimedia,media_id:image," +
                "vernacularName,taxonConceptID_new,kingdom,phylum,class:classs,order,family,genus,species,subspecies," +
                "firstLoadedDate:firstLoaded,basisOfRecord,match," +
                "search_term,correct_name:scientificName,provided_name:providedName,common_name:vernacularName,state:stateProvince,lga,shape,list_id:listId,list_name:listName,cw_state,shape_feature,creator:collector," +
                "license,mimetype,width,height," +
                "image_url:smallImageUrl," + // TBC , multiple image urls
                "date_sent:dateSent,"+
                "cl" // TBC
                //"fq, kvs"
        if (grailsApplication.config.biosecurity.csv.headers) {
            rawHeader = grailsApplication.config.biosecurity.csv.headers
        }
        // Not available in biocache_ws.ala.org.au/occurrences/search, but in https://biocache-ws.ala.org.au/ws/occurrences/#id
        // occurrenceStatus:  processed -> occurrenceStatus
        // clxxxx:            processed -> cl -> clxxxx
        // lga:               location -> verbatimLocality
        // firstLoadedDate    firstLoaded


        // state ?== stateProvince
        // ? cs_state

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
                    def value = ""
                    if(record.containsKey(field)) {
                        value = record[field]
                        //if value is a list, convert it to a string. e.g. collectors, images
                        if (value instanceof List) {
                            value = "\"${value.join(";")}\""  // Join the list with ';' and wrap it in double quotes
                        } else {
                            value = value.toString()
                            switch (field) {
                                case "eventDate":
                                    if (value) {
                                        value = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(value.toLong())
                                    }
                                    break
                                default:
                                    if (value instanceof List) {
                                        value = "\"${value.join(";")}\""  // Join the list with ';' and wrap it in double quotes
                                    } else {
                                        value = value.toString()
                                    }
                                    break
                            }
                        }
                    } else {
                        //special cases
                        if(field == "lga") {
                            if (record.containsKey("locality") && record.containsKey("stateProvince")) {
                                value = record["location"]+";"+record["stateProvince"]
                            } else if (record.containsKey("locality")) {
                                value = record["locality"]
                            } else if (record.containsKey("stateProvince")) {
                                value = record["stateProvince"]
                            }
                        }
                    }
                    return value
                }
                writer.write(values.join(","))
                writer.write("\n")
            }
        }

        tempFile
    }

    void moveToDestination(File source, File destination) {
        File destDir = new File(destination.parent)
        if (!destDir.exists()) {
            destDir.mkdirs()
        }
        //copy source file to destination
        Files.move(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING)
    }

    def sanitizeFileName(String fileName) {
        // Define a pattern for illegal characters
        def pattern = /[^a-zA-Z0-9\.\-\_]/
        return fileName.replaceAll(pattern, '_')
    }

    private List<Map> listFilesRecursively(File dir) {
        def BASE_DIRECTORY = grailsApplication.config.biosecurity.csv.local.directory
        def rootDir = new File(BASE_DIRECTORY)
        def foldersAndFiles = rootDir.listFiles().findAll { it.isDirectory() }.collect { folder ->
            [
                    name: folder.name,
                    files: folder.listFiles().findAll { it.isFile() }.collect { file -> file.name }
            ]
        }
        return foldersAndFiles
    }
}
