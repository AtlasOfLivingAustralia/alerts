package au.org.ala.alerts

import au.org.ala.ws.service.WebService
import grails.core.GrailsApplication
import org.apache.http.entity.ContentType
import org.springframework.beans.factory.annotation.Autowired

import java.nio.file.Files
import java.text.SimpleDateFormat
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.csv.CSVFormat


abstract class BiosecurityCSVService {
    @Autowired
    protected DiffService diffService
    @Autowired
    protected GrailsApplication grailsApplication
    @Autowired
    protected WebService webService

    /**
     * List all files, including the total number of files, and the total size of files
     *
     * @return [status:0, foldersAndFiles: [folder:filesInside], totalFiles: int, totalSize: String (GB,MB..)]
     */
    abstract def list()
    abstract void aggregateCSVFiles(String folder, OutputStream out)
    abstract String getFile(String filename)
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


    String formatSize(long size) {
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
        return fileName.replaceAll(pattern, '_')
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
}
