package au.org.ala.alerts;

import com.jayway.jsonpath.JsonPath
import grails.converters.JSON;
import org.apache.commons.lang.time.DateUtils
import org.apache.http.entity.ContentType

import java.nio.file.Files;
import java.nio.file.StandardCopyOption
import java.text.SimpleDateFormat;


class BiosecurityService {
    def notificationService
    def queryService
    def grailsApplication
    def emailService
    def webService
    def diffService

    def biosecurityAlerts() {
        def results = []
        queryService.getALLBiosecurityQuery().each { Query query ->
            def result = triggerBiosecuritySubscription(query)
            results.add(result)
        }
        return results
    }

    /**
     *
     * A query contains a number of independent searches depends on the number of species in the list.
     * Each search will return a number of records from the last checked date to the current timestamp,
     * which means the end timestamp of the each search is different, although only have seconds/minutes differences.
     *
     * Trigger a subscription for a query since last the last checked date
     *
     * @param query
     */
    def triggerBiosecuritySubscription(Query query) {
        //If has not been checked before, then set the lastChecked to 7 days before
        Date lastChecked = query.lastChecked ?: DateUtils.addDays(new Date(), -1 * grailsApplication.config.getProperty("biosecurity.legacy.firstLoadedDateAge", Integer, 7))
        triggerBiosecuritySubscription(query, lastChecked)
    }

    /**
     * It can be used to manually give a date to check the subscription since
     *
     * @param query
     * @param since The local date to check the subscription since
     */
    def triggerBiosecuritySubscription(Query query, Date since) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        Date now = new Date()

        def message = "Checking records of ${query?.id}. ${query?.name} from ${sdf.format(since)} to ${sdf.format(now)}."
        log.info(message)
        def result = [status: 1, message: message, logs: [message]]

        def frequency = 'weekly'
        QueryResult qr = notificationService.getQueryResult(query, Frequency.findByName(frequency))

        try {
            def processedJson = processQueryBiosecurity(query, since, now)

            def recordsFound = JsonPath.read(processedJson, '$.totalRecords')
            result.logs << "${recordsFound} record(s) found since ${sdf.format(since)}."

            // set check time
            qr.previousCheck = since

            // store the last result from the webservice call
            qr.previousResult = qr.lastResult
            qr.lastResult = qr.compress(processedJson)
            qr.lastChecked = now
            /**
             * refreshProperties has to be called before hasChanged
             */
            notificationService.refreshProperties(qr, processedJson)
            qr.hasChanged = notificationService.hasChanged(qr)
            qr.newLog("")

            log.debug("[QUERY " + query.id + "] Has changed?: " + qr.hasChanged)
            if (qr.hasChanged) {
                qr.lastChanged = since
            }

            def todayNDaysAgo = DateUtils.addDays(since, -1 * grailsApplication.config.getProperty("biosecurity.legacy.eventDateAge", Integer, 150))
            def firstLoadedDate = sdf.format(since)
            def occurrenceDate = sdf.format(todayNDaysAgo)

            //Demo purpose. Those URLs are not used in Biosecurity queries
            String queryPath = query.queryPathForUI
            String modifiedPath = queryPath.replaceAll('___DATEPARAM___', firstLoadedDate).replaceAll('___LASTYEARPARAM___', occurrenceDate)
            qr.queryUrlUIUsed = query.baseUrlForUI + modifiedPath

            if (qr.hasChanged) {
                generateAuditCSV(qr)
                def users = queryService.getSubscribers(query.id)
                def recipients = users.collect { user ->
                        def notificationUnsubToken = user.notifications.find { it.query.id == query.id }?.unsubscribeToken
                        [email: user.email, userUnsubToken: user.unsubscribeToken, notificationUnsubToken: notificationUnsubToken]
                }

                def emails = recipients.collect { it.email }
                result.logs << "Sending emails to ${emails.size() <= 2 ? emails.join('; ') : emails.take(2).join('; ') + ' and ' + (emails.size() - 2) + ' other users.'}"

                if (!users.isEmpty()) {
                    def emailStatus = emailService.sendGroupNotification(qr, Frequency.findByName('weekly'), recipients)

                    result.status = emailStatus.status
                    result.logs << emailStatus.message
                }
            } else {
                result.logs << "No emails will be sent because no changes were detected."
            }

            result.logs << "Completed!"
            result.message = "Completion of Subscription check: ${query?.id}. ${query?.name}."

        } catch (Exception e) {
            log.error("Failed to trigger subscription [ ${query?.id}  ${query?.name} ]", e)
            result = [status: 1, message: "Failed to trigger subscription [ ${query?.id}  ${query?.name} ]" + e.message]
        } finally {
            log.info("[Status: ${result.status}] - ${result.message}")

            qr.addLog(result.logs.join("\n"))

            if (!qr.save(validate: true, flush: true,failOnError: true)){
                qr.errors.allErrors.each {
                    log.error(it)
                }
            }
            return result
        }
    }


    def processQueryBiosecurity(Query query, Date since, Date to) {
        def drId = query.listId
        if (drId) {
            int offset = 0
            int max = 400
            def repeat = true

            // prevent duplicates
            def occurrences = [:]

            while (repeat) {
                def url = grailsApplication.config.getProperty('lists.baseURL') + "/ws/speciesListItemsInternal/" + drId + "?includeKVP=true" + "&offset=" + offset + "&max=" + max
                def speciesList = webService.get(url, [:], ContentType.APPLICATION_JSON, true, false)
                if (speciesList.statusCode != 200 && speciesList.statusCode != 201) {
                    log.error("Failed to access: " + url)
                    log.error("Error: " + speciesList.error)
                    break;
                }
                speciesList.resp?.each { listItem ->
                    processListItemBiosecurity(occurrences, listItem, since, to)
                }

                repeat = (max == speciesList.resp?.size())
                offset += max

            }

            return ([occurrences: occurrences.values().sort { a, b -> a.eventDate <=> b.eventDate}, totalRecords: occurrences.values().size()] as JSON).toString()
        } else {
            return ([occurrences: [], totalRecords: 0] as JSON).toString()
        }
    }

    /**
     * Date will be converted to UTC
     *
     * @param occurrences
     * @param listItem
     * @param since
     * @return
     */
    def processListItemBiosecurity(def occurrences, def listItem, Date since, Date to) {
        def names = listItem.kvpValues.find { it.key == 'synonyms' }?.value?.split(',') as List ?: []
        names.add(listItem.name)

        //Convert localtime to UTC
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        def utcTimeZone = TimeZone.getTimeZone("UTC")
        sdf.setTimeZone(utcTimeZone)

        String utcFrom = sdf.format(since)
        String utcTo = sdf.format(to)

        def todayNDaysAgo = DateUtils.addDays(since, -1 * grailsApplication.config.getProperty("biosecurity.legacy.eventDateAge", Integer, 150))
        def firstLoadedDate = '&fq=' + URLEncoder.encode('firstLoadedDate:[' + utcFrom + ' TO ' + utcTo + ' ]', 'UTF-8')
        def dateRange = '&fq=' + URLEncoder.encode('eventDate:[' + sdf.format(todayNDaysAgo) + ' TO ' + utcTo + ' ]', 'UTF-8')

        // legacy date range filter
//        SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd")
//        def today = date // 2023-10-14
//        def todayMinus8 = DateUtils.addDays(today, -1 * grailsApplication.config.getProperty("biosecurity.legacy.firstLoadedDateAge", Integer, 8)) // 2023-10-06
//        def todayNDaysAgo = DateUtils.addDays(today, -1 * grailsApplication.config.getProperty("biosecurity.legacy.eventDateAge", Integer, 29)) // 2023-9-15
//        def firstLoadedDate = '&fq=' + URLEncoder.encode('firstLoadedDate:[' + sdf.format(todayMinus8) + 'T00:00:00Z TO ' + sdf.format(today) + 'T00:00:00Z]', 'UTF-8')
//        def dateRange = '&fq=' + URLEncoder.encode('eventDate:[' + sdf.format(todayNDaysAgo) + 'T00:00:00Z TO ' + sdf.format(today) + 'T00:00:00Z]', 'UTF-8')

        //Build fq for anything in fq of KVP
        def fq = buildFq(listItem)
        // temporary code for backward compatibility
        def legacyFq = legacyFq(listItem)

        names.each { name ->
            name = name.trim()

            def searchTerms = []
            searchTerms.add('genus:"' + name + '"')
            searchTerms.add('species:"' + name + '"')
            searchTerms.add('subspecies:"' + name + '"')
            searchTerms.add('scientificName:"' + name + '"')
            searchTerms.add('raw_scientificName:"' + name + '"')

            def searchTerm = 'q=' + URLEncoder.encode("(" + searchTerms.join(") OR (") + ")")

            def url = grailsApplication.config.getProperty('biocacheService.baseURL') + '/occurrences/search?' + searchTerm + fq + legacyFq + dateRange + firstLoadedDate + "&pageSize=10000"
            log.debug("URL: " + url)

            try {
                def get = JSON.parse(new URL(url).text)
                get?.occurrences?.each { occurrence ->
                    occurrences[occurrence.uuid] = occurrence
                }
            } catch (Exception e) {
                log.error("failed to get occurrences at URL: " + url)
                log.error(e.message)
            }
        }
    }

    def buildFq(def it) {
        def fqValue = it.kvpValues?.find { it.key == 'fq' }?.value
        def fq = ''
        if (fqValue) {
            fq = '&fq=' + URLEncoder.encode(fqValue, "UTF-8")
        }
        fq
    }

    def legacyFq(def it) {
        def fq = ''

        def state = it.kvpValues?.find { it.key == 'state' }?.value
        def lga = it.kvpValues?.find { it.key == 'lga' }?.value
        def shapefile = it.kvpValues?.find { it.key == 'shape' }?.value

        if (state) {
            state?.toString()?.toUpperCase()?.split(",").each { st ->
                def s = st.trim()

                if (fq && s) {
                    fq += " OR "
                }
                if (s == 'AUS') {
                    fq += grailsApplication.config.getProperty('biosecurity.legacy.aus')
                } else if (s == 'NSW') {
                    fq += grailsApplication.config.getProperty('biosecurity.legacy.nsw')
                } else if (s == 'ACT') {
                    fq += grailsApplication.config.getProperty('biosecurity.legacy.act')
                } else if (s == 'QLD') {
                    fq += grailsApplication.config.getProperty('biosecurity.legacy.qld')
                } else if (s == 'SA') {
                    fq += grailsApplication.config.getProperty('biosecurity.legacy.sa')
                } else if (s == 'NT') {
                    fq += grailsApplication.config.getProperty('biosecurity.legacy.nt')
                } else if (s == 'TAS') {
                    fq += grailsApplication.config.getProperty('biosecurity.legacy.tas')
                } else if (s == 'VIC') {
                    fq += grailsApplication.config.getProperty('biosecurity.legacy.vic')
                } else if (s == 'WA') {
                    fq += grailsApplication.config.getProperty('biosecurity.legacy.wa')
                }
            }
        }
        if (lga) {
            fq = grailsApplication.config.getProperty('biosecurity.legacy.lgaField') + ":\"" + lga + "\""
        } else if (shapefile) {
            fq = grailsApplication.config.getProperty('biosecurity.legacy.shape')
        }

        if (fq) {
            fq = '&fq=' + URLEncoder.encode(fq, "UTF-8")
        }

        fq
    }

    void generateAuditCSV(QueryResult qs) {
        def task = {
            def records = diffService.getNewRecords(qs)

            String outputFile = sanitizeFileName("${new SimpleDateFormat("yyyy-MM-dd").format(qs.lastChecked)}.csv")

            def tempFilePath = Files.createTempFile(outputFile, ".csv")
            def tempFile = tempFilePath.toFile()
            String rawHeader = "recordID:uuid,scientificName,taxonConceptID,decimalLatitude,decimalLongitude,eventDate,occurrenceStatus,dataResourceName,multimedia,media_id," +
                    "vernacularName,taxonConceptID_new,kingdom,phylum,class:classs,order,family,genus,species,subspecies," +
                    "firstLoadedDate,basisOfRecord,match," +
                    "search_term,correct_name,provided_name,common_name,state"
            if (grailsApplication.config.biosecurity.csv.headers) {
                rawHeader = grailsApplication.config.biosecurity.csv.headers
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
                        record."${field}" ?: ""  // Use "" if the property is null
                    }
                    writer.write(values.join(","))
                    writer.write("\n")
                }
            }

            if (grailsApplication.config.biosecurity.csv.local.enabled) {
                String destinationFolder = new File(grailsApplication.config.biosecurity.csv.local.directory, sanitizeFileName("${qs.query?.id}_${qs.query.name}")).absolutePath
                File destinationFile = new File(destinationFolder, outputFile)
                moveToDestination(tempFile, destinationFile)
            }
        }

        Thread.start(task)
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

    def listAuditCSV() {
        def BASE_DIRECTORY = grailsApplication.config.biosecurity.csv.local.directory
        if (grailsApplication.config.biosecurity.csv.local.enabled) {
            def dir = new File(BASE_DIRECTORY)
            if (!dir.exists() || !dir.isDirectory()) {
                return [status: 1, message: "Directory not found"]
            }

            def fileList = listFilesRecursively(dir)
            return [status:0, files: fileList]
        } else {
            return [status: 1,  message: "We does support download CSV from S3 here"]
        }
    }

    private List<Map> listFilesRecursively(File dir) {
        def BASE_DIRECTORY = grailsApplication.config.biosecurity.csv.local.directory

        def result = []
        dir.eachFileRecurse { file ->
            if (file.isFile() && !file.isHidden())
            def filePath = file.absolutePath.replace(BASE_DIRECTORY, '')
            if(filePath) {
                result << filePath
            }
        }
        return result
    }
}
