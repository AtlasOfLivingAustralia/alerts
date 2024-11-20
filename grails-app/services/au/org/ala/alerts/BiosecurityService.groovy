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

package au.org.ala.alerts;

import com.jayway.jsonpath.JsonPath
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import org.apache.commons.lang.time.DateUtils
import org.apache.http.entity.ContentType
import java.text.SimpleDateFormat

/**
 * Process Biosecurity alerts
 */
class BiosecurityService {
    def notificationService
    def queryService
    def grailsApplication
    def emailService
    def webService
    def diffService
    def biosecurityCSVService

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
    @Transactional
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
            qr.hasChanged = diffService.hasChanged(qr)
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
                biosecurityCSVService.generateAuditCSV(qr)
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
            result.status = 0

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
                def headers = ["User-Agent": "${grailsApplication.config.getProperty("customUserAgent", "alerts")}"]
                def speciesList = webService.get(url, [:], ContentType.APPLICATION_JSON, true, false, headers)
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
            //Extra infos for CSV
            def finalResults =  occurrences.values().collect { record ->
                record["dateSent"] = new SimpleDateFormat("dd/MM/yyyy").format(to)
                record["listName"] = query.name
                record["listId"] = drId
                record["listLink"] = grailsApplication.config.getProperty('lists.baseURL') + "/speciesListItem/list/" + drId
                return record
            }

            return ([occurrences: finalResults.sort { a, b -> a.eventDate <=> b.eventDate}, totalRecords: finalResults.size()] as JSON).toString()
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
                def get = JSON.parse(new URL(url).openConnection().with { conn ->
                    conn.setRequestProperty("User-Agent", grailsApplication.config.getProperty("customUserAgent", "alerts"))
                    conn.inputStream.text
                })
                get?.occurrences?.each { occurrence ->
                    occurrences[occurrence.uuid] = occurrence
                    //extra info should be added here
                    occurrence['providedName'] = name
                    occurrence['occurrenceLink'] = grailsApplication.config.getProperty('biocache.baseURL') + '/occurrences/' + occurrence.uuid
                    //occurrence['fq']= searchTerm + fq + legacyFq
                    if (listItem.kvpValues?.size()>0) {
                        //Do not join, let CSV generate handle it
                        occurrence['kvs'] = listItem.kvpValues.collect { kv -> "${kv.key}:${kv.value}" }
                        occurrence['fq'] = listItem.kvpValues?.find { it.key == 'fq' }?.value
                    }
                    fetchExtraInfo(occurrence.uuid, occurrence)
                }
            } catch (Exception e) {
                log.error("failed to get occurrences at URL: " + url)
                log.error(e.message)
            }
        }
    }

    def fetchExtraInfo(def uuid, def occurrence) {
        try {
            def url = grailsApplication.config.getProperty('biocacheService.baseURL') + '/occurrences/' + uuid
            def record = JSON.parse(new URL(url).openConnection().with { conn ->
                conn.setRequestProperty("User-Agent", grailsApplication.config.getProperty("customUserAgent", "alerts"))
                conn.inputStream.text
            })
            occurrence['firstLoaded'] = record.raw?.firstLoaded
            //Do not join, let CSV generate handle it
            occurrence['cl'] = record.processed?.cl?.collect { "${it.key}:${it.value}" }

        } catch (Exception e) {
            log.error("failed to get extra info for uuid: " + uuid)
            log.error(e.message)
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

}
