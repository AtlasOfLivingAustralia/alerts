/**
 *   Copyright (c) 2024.  Atlas of Living Australia
 *   All Rights Reserved.
 *   @author Qifeng Bai
 *
 */

package au.org.ala.alerts

import au.org.ala.ws.service.WebService;
import grails.converters.JSON
import grails.util.Holders
import grails.web.mapping.LinkGenerator
import org.apache.commons.lang3.time.DateUtils
import org.apache.http.entity.ContentType

import java.text.SimpleDateFormat

/**
 * Process Biosecurity alerts
 */
class BiosecurityService {
    String EMAIL_TEMPLATE = '/email/biosecurity'
    LinkGenerator grailsLinkGenerator
    def notificationService
    def queryService
    def grailsApplication, messageSource
    def emailService
    WebService webService
    BiosecurityLocalCSVService biosecurityLocalCSVService
    BiosecurityS3CSVService biosecurityS3CSVService
    def diffService
    def siteLocale = new Locale.Builder().setLanguageTag(Holders.config.siteDefaultLanguage as String).build()


    def run() {
        def results = []
        def queries = []
        Query.withTransaction {
            queries = Query.findAllByEmailTemplate(EMAIL_TEMPLATE)
        }

        queries.each { Query query ->
            def result = triggerBiosecuritySubscription(query)
            results.add(result)
        }
        def errorLogs = results.findAll { it.status != 0 }
        if (errorLogs.size() > 0) {
            def message = [:]
            // A yellow warning sign emoji
            message["subject"] = "\u26A0\uFE0F Biosecurity alerts completed with ${errorLogs.size()} error(s) at ${new Date()}"
            message["logs"] = errorLogs
            emailService.notifyMonitoringTeam("BIOSECURITY", message)
        } else {
            def message = [:]
            // A green check mark emoji
            message["subject"] = "\u2705 ${results.size()} Biosecurity alert(s) completed successfully at ${new Date()}"
            emailService.notifyMonitoringTeam("BIOSECURITY", message)
        }

        return results
    }

    // get biosecurity queries with offset and limit
    def list(int offset,int limit) {
        def criteria = Query.createCriteria()
        List<Query> queries = criteria.list(max: limit, offset: offset) {
            eq('emailTemplate', EMAIL_TEMPLATE)
            order('id', 'desc')
        }

        def results = queries.collect{ query ->
            // Biosecurity queries are weekly ONLY, so filter out the other frequencies
            def filteredQueryResults = query.queryResults.findAll { it.frequency?.name == 'weekly' }
            // Get the last QueryResult from the filtered list, if it exists
            QueryResult qr = !filteredQueryResults.isEmpty() ? filteredQueryResults.first() : null
            query
        }

        return results.toList()
    }

    // return the number of biosecurity queries
    def count() {
        int count = 0
        Query.withTransaction {
            count = Query.countByEmailTemplate(EMAIL_TEMPLATE)
        }
        return count
    }


    /**
     * NOTE: Biosecurity query code does not use the queryPath stored in the database
     * @param listid
     * @return
     */

    Query buildQuery(String listid) {
        def sList = queryService.getSpeciesListName(listid)
        String speciesListName = sList.name
        //differentiate non-authoritative / authoritative list
        //demo purpose only, the queryPath is not used in Biosecurity query process
        String queryPathForUITemplate = grailsApplication.config.getProperty("biosecurity.query.template.nonAuthoritativeList", String, "/occurrences/search?q=species_list:___LISTIDPARAM___&fq=decade:2020&fq=country:Australia&fq=first_loaded_date:"+"[___DATEPARAM___ TO *]".encodeAsURL()+"&fq=occurrence_date:"+"[___LASTYEARPARAM___ TO *]".encodeAsURL() +"&sort=first_loaded_date&dir=desc&disableAllQualityFilters=true")
        if (sList.isAuthoritative) {
            queryPathForUITemplate = grailsApplication.config.getProperty("biosecurity.query.template.authoritativeList", String, "/occurrences/search?q=species_list_uid:___LISTIDPARAM___&fq=decade:2020&fq=country:Australia&fq=first_loaded_date:"+"[___DATEPARAM___ TO *]".encodeAsURL()+"&fq=occurrence_date:"+"[___LASTYEARPARAM___ TO *]".encodeAsURL()+"&sort=first_loaded_date&dir=desc&disableAllQualityFilters=true")
        }

        String queryPathForUI = queryPathForUITemplate.replaceAll("___LISTIDPARAM___", listid)

        new Query([
                //Not used
                baseUrl       : grailsApplication.config.biocacheService.baseURL,
                baseUrlForUI  : grailsApplication.config.biocache.baseURL,
                name          : messageSource.getMessage("query.biosecurity.title", null, siteLocale) + ' ' + speciesListName,
                resourceName  : grailsApplication.config.mail.details.defaultResourceName,
                updateMessage : 'more.biosecurity.update.message',
                description   : messageSource.getMessage("query.biosecurity.descr", null, siteLocale) + ' ' + speciesListName,
                //Not used
                queryPath     : queryPathForUI + '&pageSize=20&facets=basis_of_record',
                //Not used
                queryPathForUI: queryPathForUI,
                dateFormat    : """yyyy-MM-dd'T'HH:mm:ss'Z'""",
                emailTemplate : '/email/biosecurity',
                recordJsonPath: '\$.occurrences[*]',
                idJsonPath    : 'uuid',
                custom        : true
        ])
    }

    /**
     * Subscribe a user to a species list ID.
     * If the query for this species list does not exist, it will be created.
     * @param user
     * @param listid
     * @return
     */
    def subscribeToSpeciesList(User user, String listid) {
        Query query = buildQuery(listid)
        query = queryService.addUserToQuery(query, user, true, true)
        return query
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
        Date lastChecked = queryService.getLastCheckedDate(query) ?: DateUtils.addDays(new Date(), -1 * grailsApplication.config.getProperty("biosecurity.legacy.firstLoadedDateAge", Integer, 7))
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
        def result = [status: 1, message: message, logs: [ "Processing at ${sdf.format(now)} ", message]]

        def frequency = queryService.getFrequency("weekly")
        QueryResult qr = notificationService.getQueryResult(query, frequency)
        def recipients = queryService.getRecipients(query.id, "weekly")
        try {
            def processedJson = processQueryBiosecurity(query, since, now)
            // set check time
            qr.previousCheck = since
            // store the last result from the webservice call
            qr.previousResult = qr.lastResult
            qr.lastResult = qr.compress(processedJson)
            qr.lastChecked = now

            def newRecords = diffService.getNewRecords(qr)
            fetchExtraOccurrenceInfo(newRecords)
            def countsByDataProvider = countByDataProvider(newRecords)
            qr.newRecords = newRecords
            qr.totalRecords = qr.newRecords?.size()

            if ( qr.totalRecords > 0) {
                qr.hasChanged = true
                qr.lastChanged = since
            } else {
                qr.hasChanged = false
            }
            qr.succeeded = true
            log.debug("[QUERY " + query.id + "] Has changed?: " + qr.hasChanged)
            result.logs << "${qr.totalRecords} record(s) found since ${sdf.format(since)}."

            def todayNDaysAgo = DateUtils.addDays(since, -1 * grailsApplication.config.getProperty("biosecurity.legacy.eventDateAge", Integer, 150))
            def firstLoadedDate = sdf.format(since)
            def occurrenceDate = sdf.format(todayNDaysAgo)

            //Demo purpose. Those URLs are not used in Biosecurity queries
            String queryPath = query.queryPathForUI
            String modifiedPath = queryPath.replaceAll('___DATEPARAM___', firstLoadedDate).replaceAll('___LASTYEARPARAM___', occurrenceDate)
            qr.queryUrlUIUsed = query.baseUrlForUI + modifiedPath

            if (qr.hasChanged) {
                def csvService =  getCsvService()
                csvService.generateAuditCSV(qr)
                if (recipients) {
                    def emails = recipients.collect { it.email }
                    result.logs << "Sending emails to ${emails.size() <= 2 ? emails.join('; ') : emails.take(2).join('; ') + ' and ' + (emails.size() - 2) + ' other users.'}"

                    def emailStatus = emailService.sendGroupNotification(qr, frequency, recipients,[countByDataProvider: countsByDataProvider])
                    result.status = emailStatus.status
                    result.logs << emailStatus.message
                }
            } else {
                result.logs << "No emails will be sent because no changes were detected."
            }

            result.logs << "Completed!"
            result.message = "Completion of Subscription: [${query?.id}]. ${query?.name}."
            result.status = 0
        } catch (Exception e) {
            qr.succeeded = false
            String error = "Error: Failed to trigger subscription [ ${query?.id}  ${query?.name} ]"
            log.error(error + " - " +e.message)
            result.status = 1
            result.message = "${query?.name}"
            result.url = grailsLinkGenerator.link( controller: 'admin', action: 'index', namespace: 'biosecurity',params: [id: query?.id], absolute: true)
            result.logs << "Failed: ${e.message}"
        } finally {
            log.info(result.message)
            qr.newLogs(result.logs)
            QueryResult.withTransaction {
                qr.save(flush: true, failOnError: true)
            }
        }
        return result
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
                def headers = ["User-Agent": "${grailsApplication.config.getProperty("customUserAgent", "alerts")}"]
                def speciesList = webService.get(url, [:], ContentType.APPLICATION_JSON, true, false, headers)
                if (speciesList.statusCode != 200 && speciesList.statusCode != 201) {
                    log.error("Error: " + speciesList.error)
                    throw new RuntimeException("Failed to process the Species List: ${speciesList.statusCode} " + url)
                }
                speciesList.resp?.each { listItem ->
                    processListItemBiosecurity(occurrences, query, listItem, since, to)
                }

                repeat = (max == speciesList.resp?.size())
                offset += max

            }
            //Extra infos for CSV
            def finalResults = occurrences.values().collect { record ->
                record["dateSent"] = new SimpleDateFormat("dd/MM/yyyy").format(to)
                record["listName"] = query.name
                record["listId"] = drId
                record["listLink"] = grailsApplication.config.getProperty('lists.baseURL') + "/speciesListItem/list/" + drId
                return record
            }

            return ([occurrences: finalResults.sort { a, b -> a.eventDate <=> b.eventDate }, totalRecords: finalResults.size()] as JSON).toString()
        } else {
            return ([status: false, error: 'No species list Id provided', occurrences: [], totalRecords: 0] as JSON).toString()
        }
    }

    /**
     * Date will be converted to UTC
     *
     * @param occurrences a reference to the map of occurrences
     * @param listItem
     * @param since
     * @return
     */
    def processListItemBiosecurity(def occurrences, def query, def listItem, Date since, Date to) {
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

            int pageSize = grailsApplication.config.biocacheService.pageSize as int
            String baseUrl = "${query.baseUrl}/occurrences/search?${searchTerm + fq + legacyFq + dateRange + firstLoadedDate}&pageSize=${pageSize}"
            String userAgent = grailsApplication.config.getProperty("customUserAgent", "alerts")

            try {
                int pageOffset = 0
                int totalRecords = -1   // unknown until first response

                while (totalRecords == -1 || pageOffset < totalRecords) {
                    def url = baseUrl + "&start=${pageOffset}"
                    log.debug("URL (offset=${pageOffset}): " + url)

                    def get = JSON.parse(new URL(url).openConnection().with { conn ->
                        conn.setRequestProperty("User-Agent", userAgent)
                        conn.inputStream.text
                    })

                    // Capture total on first page
                    if (totalRecords == -1) {
                        totalRecords = (get?.totalRecords as Integer) ?: 0
                        log.debug("Biosecurity pagination: totalRecords=${totalRecords}, pageSize=${pageSize} for name='${name}'")
                        if (totalRecords == 0) break
                    }

                    def page = get?.occurrences ?: []
                    if (!page) break  // guard against empty page

                    page.each { occurrence ->
                        occurrences[occurrence.uuid] = occurrence
                        occurrence['providedName'] = name
                        occurrence['occurrenceLink'] = "${grailsApplication.config.getProperty('biocache.baseURL')}/occurrences/${occurrence.uuid}"
                        if (listItem.kvpValues?.size() > 0) {
                            // Do not join, let CSV generate handle it
                            occurrence['kvs'] = listItem.kvpValues.collect { kv -> "${kv.key}:${kv.value}" }
                            occurrence['fq'] = listItem.kvpValues?.find { it.key == 'fq' }?.value
                        }
                    }

                    pageOffset += pageSize
                }
            } catch (Exception e) {
                log.error("Biosecurity: ${e.message}")
                throw new Exception("Biosecurity: failed to process occurrences: ${baseUrl}")
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

    //Batch Query Biocache (Using qid) to collect extra info
    //Those extra info are now stored in CSV file and the Emails
    //e.g. first loaded date, lga layerID, lga name etc
    //
    def fetchExtraOccurrenceInfo(def records) {
        String layerId = grailsApplication.config.getProperty('biosecurity.lga', 'cl11170')
        String qidUrl = grailsApplication.config.getProperty('biocacheService.baseURL') + '/qid'

        int limits = grailsApplication.config.biocacheService.pageSize
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
                                record['firstLoadedDate'] = occ.otherProperties?.firstLoadedDate
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Group the occurrence records by their data provider and count them.
     *
     * @param records the occurrence records
     * @return a map of [dataProvider : count], sorted by dataProvider name in descending order
     */
    def countByDataProvider(def records) {
        if (!records) {
            return [:]
        }
        //sort the records by dataProviderName, dataProvider, or dataResourceName (in that order), and group them by the same criteria
        records.sort { rec ->
            (rec?.dataProviderName ?: rec?.dataProvider ?: rec?.dataResourceName ?: 'Unknown').toString()
        }
        records.groupBy { rec ->
            (rec?.dataProviderName ?: rec?.dataProvider ?: rec?.dataResourceName ?: 'Unknown').toString()
        }.collectEntries { provider, group ->
            [(provider): group.size()]
        }.sort { a, b -> b.key <=> a.key }
    }

    private def getCsvService() {
        return  grailsApplication.config.getProperty('biosecurity.csv.s3.enabled', Boolean, false) ? biosecurityS3CSVService : biosecurityLocalCSVService
    }

}
