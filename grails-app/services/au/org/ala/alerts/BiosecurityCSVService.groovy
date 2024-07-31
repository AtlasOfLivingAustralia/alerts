package au.org.ala.alerts

import groovy.json.JsonOutput

import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.text.SimpleDateFormat

class BiosecurityCSVService {
    def diffService
    def grailsApplication

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

    File createTempCSV(QueryResult qs) {
        def records = diffService.getNewRecords(qs)

        String outputFile = sanitizeFileName("${new SimpleDateFormat("yyyy-MM-dd").format(qs.lastChecked)}.csv")

        def tempFilePath = Files.createTempFile(outputFile, ".csv")
        def tempFile = tempFilePath.toFile()
        String rawHeader = "recordID:uuid,scientificName,taxonConceptID,decimalLatitude,decimalLongitude,eventDate,occurrenceStatus,dataResourceName,multimedia,media_id:image," +
                "vernacularName,taxonConceptID_new,kingdom,phylum,class:classs,order,family,genus,species,subspecies," +
                "firstLoadedDate:firstLoaded,basisOfRecord,match," +
                "search_term,correct_name,provided_name,common_name,state:stateProvince,lga,shape,list_id:listId,list_name:listName,cw_state,shape_feature,creator:collector," +
                "license,mimetype,width,height," +
                "image_url:smallImageUrl," + // TBC , multiple image urls
                "date_sent:dateSent," +// TBC
                "fq, kvs"
        if (grailsApplication.config.biosecurity.csv.headers) {
            rawHeader = grailsApplication.config.biosecurity.csv.headers
        }
        // Not available in biocache_ws.ala.org.au/occurrences/search, but in https://biocache-ws.ala.org.au/ws/occurrences/#id
        // occurrenceStatus:  processed -> occurrenceStatus
        // clxxxx:            processed -> cl -> clxxxx
        // lga:               location -> verbatimLocality
        // firstLoadedDate    firstLoaded

        // Not available in biocache_ws.ala.org.au/occurrences/search
        // match and search_term, for example: matched: scientificName,  search_term: Paratrechina longicornis for the record: 160dfd59-29d0-4f0d-b277-23d69e13343a
        // We are not able to identify the record exactly matched with which search term and value.
        // We can only collection info from the query, for example:
        // q=(genus:"Austropuccinia+psidii")+OR+(species:"Austropuccinia+psidii")+OR+(subspecies:"Austropuccinia+psidii")+OR+(scientificName:"Austropuccinia+psidii")+OR+(raw_scientificName:"Austropuccinia+psidii")

        //Need to be clarified:
        // correct_name,provided_name,common_name, state,shape,list_name,cw_state,shape_feature,

        // state ?== stateProvince
        // ? cs_state

        // Need to collect data from query itself ?
        // shape, shape_feature, list_name


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
                    if(record.containsKey(field)) {
                        def value = record[field]
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
                        record."${field}" ?: ""  // Use "" if the property is null
                    }

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
}
