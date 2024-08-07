package au.org.ala.alerts

import com.jayway.jsonpath.JsonPath
import grails.gorm.transactions.NotTransactional
import org.apache.http.entity.ContentType
import grails.converters.JSON
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.time.DateUtils
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONElement
import org.grails.web.json.JSONObject
import grails.gorm.transactions.Transactional
import java.text.SimpleDateFormat
import java.util.zip.GZIPOutputStream

@Transactional
class NotificationService {

    static transactional = true

    int PAGING_MAX = 1000

    def emailService
    def diffService
    def queryService
    def grailsApplication
    def webService

    QueryResult getQueryResult(Query query, Frequency frequency) {
        QueryResult qr = QueryResult.findByQueryAndFrequency(query, frequency)
        if (qr == null) {
            qr = new QueryResult([query: query, frequency: frequency])
        }
        qr
    }

    /**
     * Check the status of queries for this given frequency.
     *
     * @param query
     * @param frequency
     * @return
     */
    boolean checkStatus(Query query, Frequency frequency, boolean flushResult = false) {

        QueryResult qr = getQueryResult(query, frequency)

        //def url = new URL("http://biocache.ala.org.au/ws/occurrences/search?q=*:*&pageSize=1")
        def urls = getQueryUrl(query, frequency)

        def urlString = urls.first()
        def urlStringForUI = urls.last()

        log.debug("[QUERY " + query.id + "] Querying URL: " + urlString)

        try {
            def processedJson

            if (!urlString.contains("___MAX___")) {
                // queries without paging
                if (queryService.isBioSecurityQuery(query)) {
                    // biosecurity query is handled elsewhere
                    Date since = qr.lastChecked ?: DateUtils.addDays(new Date(), -1 * grailsApplication.config.getProperty("biosecurity.legacy.firstLoadedDateAge", Integer, 7))
                    Date to = new Date()
                    processedJson = processQueryBiosecurity(query, since, to)
                } else {
                    // standard query
                    processedJson = processQueryReturnedJson(query, IOUtils.toString(new URL(urlString).newReader()))
                }
            } else {
                // queries with paging
                int max = PAGING_MAX
                int offset = 0
                def result = []
                boolean finished = false
                def allLists = []
                while (!finished && (result = processQueryReturnedJson(query, new URL(urlString.replaceAll('___MAX___', String.valueOf(max)).replaceAll('___OFFSET___', String.valueOf(offset))).text))?.size()) {
                    offset += max

                    try {
                        def latestValue = JsonPath.read(result, query.recordJsonPath)
                        if (latestValue.size() == 0) {
                            finished = true
                        } else {
                            processedJson = result
                            allLists.addAll(latestValue)
                        }
                    } catch (Exception e) {
                        //expected behaviour for missing properties
                        finished = true
                    }
                }
                // only for species lists
                def json = JSON.parse(processedJson) as JSONObject
                if (json.lists) {
                    // set json.lists to allLists such that json.toString() does not convert allLists items to strings
                    json.lists = JSON.parse((allLists as JSON).toString()) as JSONArray
                    processedJson = json.toString()
                }
            }

            //update the stored properties
            refreshProperties(qr, processedJson)

            // When qr is not yet saved, use the unsaved object. Unsure why qr is retrieved here so not removing it.
            QueryResult qCurrent = qr
            qr = QueryResult.findById(qr.id)
            if (qr == null) {
                qr = qCurrent
            }

            // set check time
            qr.previousCheck = qr.lastChecked
            // store the last result from the webservice call
            qr.previousResult = qr.lastResult
            qr.lastResult = gzipResult(processedJson)
            qr.lastChecked = new Date()
            qr.hasChanged = hasChanged(qr)
            qr.queryUrlUsed = urlString
            qr.queryUrlUIUsed = urlStringForUI

            log.debug("[QUERY " + query.id + "] Has changed?: " + qr.hasChanged)
            if (qr.hasChanged) {
                qr.lastChanged = new Date()
            }

            if (!qr.save(validate: true, flush: true)) {
                qr.errors.allErrors.each {
                    log.error(it)
                }
            }
            qr.hasChanged
        } catch (Exception e) {
            log.error("[QUERY " + query.id + "] URL: " + urlString + " " + e.getMessage(), e)
        }
    }

    QueryCheckResult checkStatusDontUpdate(Query query, Frequency frequency) {

        //get the previous result
        long start = System.currentTimeMillis()
        QueryResult lastQueryResult = getQueryResult(query, frequency)
        QueryCheckResult qcr = new QueryCheckResult()

        //get the urls to query
        def urls = getQueryUrl(query, frequency)
        def urlString = urls.first()
        qcr.frequency = frequency
        qcr.urlChecked = urlString
        qcr.query = query
        qcr.queryResult = lastQueryResult

        log.debug("[QUERY " + query.id + "] Querying URL: " + urlString)

        try {
            def processedJson = ''

            if (!urlString.contains("___MAX___")) {
                // queries without paging
                if (!queryService.isBioSecurityQuery(query)) {
                    // standard query
                    processedJson = processQueryReturnedJson(query, IOUtils.toString(new URL(urlString).newReader()))
                } else {
                    // biosecurity query is handled elsewhere
                    Date since = lastQueryResult.lastChecked ?: DateUtils.addDays(new Date(), -1 * grailsApplication.config.getProperty("biosecurity.legacy.firstLoadedDateAge", Integer, 7))
                    Date to = new Date()
                    processedJson = processQueryBiosecurity(query, since, to)
                }
            } else {
                // queries with paging
                int max = PAGING_MAX
                int offset = 0
                def result
                boolean finished = false
                def allLists = []
                while (!finished && (result = processQueryReturnedJson(query, new URL(urlString.replaceAll('___MAX___', String.valueOf(max)).replaceAll('___OFFSET___', String.valueOf(offset))).text))?.size()) {
                    offset += max

                    try {
                        def latestValue = JsonPath.read(result, query.recordJsonPath)
                        if (latestValue.size() == 0) {
                            finished = true
                        } else {
                            processedJson = result
                            allLists.addAll(latestValue)
                        }
                    } catch (Exception e) {
                        //expected behaviour for missing properties
                        finished = true
                    }
                }
                // only for species lists
                def json = JSON.parse(processedJson) as JSONObject
                if (json.lists) {
                    // set json.lists to allLists such that json.toString() does not convert allLists items to strings
                    json.lists = JSON.parse((allLists as JSON).toString()) as JSONArray
                    processedJson = json.toString()
                }
            }

            //update the stored properties
            qcr.response = processedJson

            //update the stored properties
            def propertyPaths = compareProperties(lastQueryResult, processedJson)

            //decompress the last result
            def previousJson = diffService.decompressZipped(lastQueryResult.lastResult)

            //set the has changed
            qcr.queryResult.hasChanged = hasPropertiesChanged(query, propertyPaths, previousJson, processedJson)

        } catch (Exception e) {
            log.error("QUERY " + query.id + " URL:" + urlString + " " + e.message)
            qcr.errored = true
        }
        qcr.timeTaken = System.currentTimeMillis() - start
        qcr
    }

    byte[] gzipResult(String json) {
        //store the last result from the webservice call
        ByteArrayOutputStream bout = new ByteArrayOutputStream()
        GZIPOutputStream gzout = new GZIPOutputStream(bout)
        gzout.write(json.toString().getBytes())
        gzout.flush()
        gzout.finish()
        bout.toByteArray()
    }

    def String[] getQueryUrl(Query query, Frequency frequency) {
        def queryPath = query.queryPath
        def queryPathForUI = query.queryPathForUI

        //if there is a date format, then there's a param to replace
        if (query.dateFormat) {
            def additionalTimeoffset = grailsApplication.config.getProperty('mail.details.forceAllAlertsGetSent', Boolean, false) ? 24 * 180 : 1
            def dateToUse = DateUtils.addSeconds(new Date(), -1 * frequency.periodInSeconds * additionalTimeoffset)
            // date one year prior from today.
            def dateLastYear = DateUtils.addYears(new Date(), -1)
            //insert the date to query with
            SimpleDateFormat sdf = new SimpleDateFormat(query.dateFormat)
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            def dateValue = sdf.format(dateToUse)
            queryPath = queryPath.replaceAll("___DATEPARAM___", dateValue)
            queryPathForUI = queryPathForUI.replaceAll("___DATEPARAM___", dateValue)

            // replace variable with formatted date from 1 year ago.
            def dateLastYearFormatted = sdf.format(dateLastYear)
            queryPath = queryPath.replaceAll("___LASTYEARPARAM___", dateLastYearFormatted)
            queryPathForUI = queryPathForUI.replaceAll("___LASTYEARPARAM___", dateLastYearFormatted)

        }

        [cleanUpUrl(query.baseUrl + queryPath), cleanUpUrl(query.baseUrlForUI + queryPathForUI)]
    }

    def cleanUpUrl(url) {
        def queryStart = url.indexOf("?")
        if (queryStart > 0) {
            def queryString = url.substring(queryStart + 1)
            url.substring(0, queryStart + 1) + queryString.replaceAll(" ", "%20").replaceAll(":", "%3A").replaceAll("\"", "%22")
        } else {
            url
        }
    }

    /**
     * Indicates if the result of a query has changed by checking its properties.
     *
     * @param queryResult
     * @return
     */
    Boolean hasChanged(QueryResult queryResult) {
        Boolean changed = false

        //if there is a fireWhenNotZero or fireWhenChange ignore  idJsonPath
        log.debug("[QUERY " + queryResult.query.id + "] Checking query: " + queryResult.query.name)

        queryResult.propertyValues.each { pv ->
            log.debug("[QUERY " + queryResult.query.id + "] " +
                    " Has changed check:" + pv.propertyPath.name
                    + ", value:" + pv.currentValue
                    + ", previous:" + pv.previousValue
                    + ", fireWhenNotZero:" + pv.propertyPath.fireWhenNotZero
                    + ", fireWhenChange:" + pv.propertyPath.fireWhenChange
            )
            if (pv.propertyPath.fireWhenNotZero) {
                changed = pv.currentValue?.toInteger() ?: 0 > 0
            } else if (pv.propertyPath.fireWhenChange) {
                changed = pv.previousValue != pv.currentValue
            }
        }

        if (queryService.checkChangeByDiff(queryResult.query)) {
            log.debug("[QUERY " + queryResult.query.id + "] Has change check. Checking JSON for query : " + queryResult.query.name)
            changed = diffService.hasChangedJsonDiff(queryResult)
        }

        changed
    }

    /**
     * Indicates if the result of a query has changed by checking its properties.
     *
     * @param queryResult
     * @return
     */
    Boolean hasPropertiesChanged(def query, def propertyPathMap, def jsonPrevious, def jsonCurrent) {
        Boolean changed = false

        //if there is a fireWhenNotZero or fireWhenChange ignore  idJsonPath
        log.debug("[QUERY " + query.id + "] Checking query: " + query.name)

        propertyPathMap.each { propertyPath, value ->
            log.debug("[QUERY " + query.id + "] Has change check:" + propertyPath.name
                    + ", value:" + value.current
                    + ", previous:" + value.previous
                    + ", fireWhenNotZero:" + propertyPath.fireWhenNotZero
                    + ", fireWhenChange:" + propertyPath.fireWhenChange
            )
            if (propertyPath.fireWhenNotZero) {
                changed = value.current.toInteger() > 0
            } else if (propertyPath.fireWhenChange) {
                changed = value.previous != value.current
            }
        }

        if (queryService.checkChangeByDiff(query)) {
            log.debug("[QUERY " + query.id + "] Has change check. Checking JSON for query : " + query.name)
            changed = diffService.hasChangedJsonDiff(jsonPrevious, jsonCurrent, query)
        }
        log.debug("[QUERY " + query.id + "] Has changed: " + changed)
        changed
    }

    /**
     * Compares the stored values with the values in the JSON returning a map of
     *
     * propertyPath -> [current, previous]
     *
     * @param queryResult
     * @param json
     * @return
     */
    private def compareProperties(QueryResult queryResult, json) {

        def propertyPaths = [:]

        log.debug("[QUERY " + queryResult?.query?.id ?: 'NULL' + "] Refreshing properties for query: " + queryResult.query.name + " : " + queryResult.frequency)

        queryResult.query.propertyPaths.each { propertyPath ->
            //read the value from the request
            def latestValue = null
            try {
                latestValue = JsonPath.read(json, propertyPath.jsonPath)
            } catch (ignored) {
                // Do not throw an exception here. All current exceptions are due to the JSON containing no
                // records, e.g. jsonPath is 'occurrences[0].uuid' and occurrences are empty
            }

            try {
                def currentValue = null
                if (latestValue != null && latestValue instanceof List) {
                    currentValue = latestValue.size().toString()
                } else {
                    currentValue = latestValue
                }

                //get property value for this property path
                PropertyValue propertyValue = getPropertyValue(propertyPath, queryResult)
                //add to the map
                propertyPaths.put(propertyPath, [previous: propertyValue.currentValue, current: currentValue])
            } catch (Exception e) {
                log.warn("query:" + queryResult?.query?.id + " cannot read ${propertyPath.name} : [${propertyPath.jsonPath}] the supplied JSON of : Query ${queryResult?.query?.id} : [${queryResult?.query?.name}]")
            }
        }

        propertyPaths
    }

    private def processQueryReturnedJson(Query query, String json) {
        if (!queryService.isMyAnnotation(query) || !queryService.getUserId(query)) {
            return json
        }

        JSONObject rslt = JSON.parse(json) as JSONObject

        // all the occurrences user has made annotations to
        if (rslt.occurrences) {
            // reconstruct occurrences so that only those records with specified annotations are put into the list
            JSONArray reconstructedOccurrences = []
            for (JSONObject occurrence : rslt.occurrences) {
                if (occurrence.uuid) {
                    // all the verified assertions of this occurrence record
                    def (openAssertions, verifiedAssertions, correctedAssertions) = filterAssertionsForAQuery(getAssertionsOfARecord(query.baseUrl, occurrence.uuid), queryService.getUserId(query))

                    // only include record has at least 1 (50001/50002/50003) assertion
                    if (!openAssertions.isEmpty() || !verifiedAssertions.isEmpty() || !correctedAssertions.isEmpty()) {

                        openAssertions.sort { it.uuid }
                        verifiedAssertions.sort { it.uuid }
                        correctedAssertions.sort { it.uuid }

                        occurrence.put('open_assertions', openAssertions.collect { it.uuid }.join(','))
                        occurrence.put('verified_assertions', verifiedAssertions.collect { it.uuid }.join(','))
                        occurrence.put('corrected_assertions', correctedAssertions.collect { it.uuid }.join(','))
                        reconstructedOccurrences.push(occurrence)
                    }
                }
            }
            reconstructedOccurrences.sort { it.uuid }

            // reconstruct occurrences which will be used to retrieve diff (records that will be included in alert email)
            rslt.put('occurrences', reconstructedOccurrences)
        }

        return rslt.toString()
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
            return ([occurrences: occurrences.values(), totalRecords: occurrences.values().size()] as JSON).toString()
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

    JSONElement getJsonElements(String url) {
        log.debug "(internal) getJson URL = " + url
        def conn = new URL(url).openConnection()
        try {
            conn.setConnectTimeout(10000)
            conn.setReadTimeout(50000)
            return JSON.parse(conn.getInputStream(), "UTF-8")
        } catch (Exception e) {
            def error = "Failed to get json from web service (${url}). ${e.getClass()} ${e.getMessage()}, ${e}"
            log.error error
            return new JSONObject();
        }
    }

    JSONArray getAssertionsOfARecord(String baseUrl, String uuid) {
        def url = baseUrl + '/occurrences/' + uuid + '/assertions'
        return getJsonElements(url) as JSONArray
    }

    // of all the assertions user has made return those have been open-issued, verified or corrected
    private static def filterAssertionsForAQuery(JSONArray assertions, String userId) {
        def openAssertions = []
        def verifiedAssertions = []
        def correctedAssertions = []
        if (assertions) {
            // all the original user assertions (issues users flagged)
            def origUserAssertions = assertions.findAll { it.uuid && !it.relatedUuid && it.userId == userId }

            // all the 50001 (open issue) assertions (could belong to userId or other users)
            def openIssueIds = assertions.findAll { it.uuid && it.relatedUuid && it.code == 50000 && it.qaStatus == 50001 }.collect { it.relatedUuid }

            // all the 50002 (verified) assertions (could belong to userId or other users)
            def verifiedIds = assertions.findAll { it.uuid && it.relatedUuid && it.code == 50000 && it.qaStatus == 50002 }.collect { it.relatedUuid }

            // all the 50003 (corrected) assertions (could belong to userId or other users)
            def correctedIds = assertions.findAll { it.uuid && it.relatedUuid && it.code == 50000 && it.qaStatus == 50003 }.collect { it.relatedUuid }

            openAssertions = origUserAssertions.findAll { openIssueIds.contains(it.uuid) }
            verifiedAssertions = origUserAssertions.findAll { verifiedIds.contains(it.uuid) }
            correctedAssertions = origUserAssertions.findAll { correctedIds.contains(it.uuid) }
        }
        [openAssertions, verifiedAssertions, correctedAssertions]
    }

    @NotTransactional
    private def refreshProperties(QueryResult queryResult, json) {

        log.debug("[QUERY " + queryResult?.query?.id ?: 'NULL' + "] Refreshing properties for query: " + queryResult.query.name + " : " + queryResult.frequency)

        try {
            QueryResult.withTransaction {
                queryResult.query.propertyPaths.each { propertyPath ->

                    //read the value from the request
                    def latestValue = null
                    try {
                        latestValue = JsonPath.read(json, propertyPath.jsonPath)
                    } catch (Exception e) {
                        //expected behaviour if JSON doesnt contain the element
                    }

                    //get property value for this property path
                    PropertyValue propertyValue = getPropertyValue(propertyPath, queryResult)

                    propertyValue.previousValue = propertyValue.currentValue

                    if (latestValue != null && latestValue instanceof List) {
                        propertyValue.currentValue = latestValue.size().toString()
                    } else {
                        propertyValue.currentValue = latestValue
                    }

                    if (!propertyValue.save(flush: true)) {
                        propertyValue.errors.allErrors.each {
                            log.error(it)
                        }
                    }
                    queryResult.addToPropertyValues(propertyValue)
                }
                if (!queryResult.save(flush: true)) {
                    queryResult.errors.allErrors.each {
                        log.error(it)
                    }
                }
            }
        } catch (Exception e) {
            log.error("[QUERY " + queryResult?.query?.id ?: 'NULL' + "] There was a problem reading the supplied JSON.", e)
        }
    }

    PropertyValue getPropertyValue(PropertyPath pp, QueryResult queryResult) {
        PropertyValue pv = queryResult.id == null ? null : PropertyValue.findByPropertyPathAndQueryResult(pp, queryResult)
        if (pv == null) {
            if (queryResult.id == null) {
                // must save QueryResult first
                if (!queryResult.save(flush: true)) {
                    queryResult.errors.allErrors.each {
                        log.error(it)
                    }
                }
            }
            pv = new PropertyValue([propertyPath: pp, queryResult: queryResult])
            if (!pv.save(flush: true)) {
                pv.errors.allErrors.each {
                    log.error(it)
                }
            }
        }
        pv
    }

    def checkQueryById(queryId, freqStr) {

        log.debug("[QUERY " + queryId + "] Running query...")

        def checkedCount = 0
        def checkedAndUpdatedCount = 0
        def query = Query.get(queryId)
        def frequency = Frequency.findByName(freqStr)

        QueryCheckResult qcr = checkStatusDontUpdate(query, frequency)

        checkedCount++
        if (qcr.queryResult.hasChanged) {
            checkedAndUpdatedCount++
        }
        log.debug("Query checked: " + checkedCount + ", updated: " + checkedAndUpdatedCount)
        qcr
    }

    // used in debug all alerts
    def checkAllQueries(PrintWriter writer) {
        //iterate through all queries
        def checkedCount = 0
        def checkedAndUpdatedCount = 0
        log.debug("Starting the running of all queries enabled.....")

        // TODO: placeholder to toggle between one of the several test types that are required
        //  readOnlyTest == true to run a test that does not update the database
        //  readOnlyTest == false to run all queries twice
        //   - look for exceptions
        //   - look for emails of 0 records
        //   - look for emails on the 2nd run that should not be triggered again
        //  readOnlyTest == false to run all queries with modified date ranges
        //   - look for q/fq that return nothing or are wrong, e.g. obsolete taxonId or qid
        //   - look for services that are not finding results
        //   - look for queries that return no email on their first run when they should
        boolean readOnlyTest = true

        for (Frequency frequency : Frequency.findAll()) {
            def queries = Query.executeQuery(
                """select q from Query q
                  inner join q.notifications n
                  inner join n.user u
                  where u.frequency = :frequency
                  group by q""", [frequency: frequency])

            queries.each { query ->
                if (queryService.isBioSecurityQuery(query)) {
                    return
                }

                try {
                    long timeTaken = System.currentTimeMillis()

                    boolean hasFireProperty = query.propertyPaths.any { it.fireWhenChange || it.fireWhenNotZero }

                    QueryCheckResult qcr
                    boolean hasUpdated
                    if (readOnlyTest) {
                        qcr = checkStatusDontUpdate(query, frequency)
                        hasUpdated = qcr.queryResult.hasChanged

                        writer.write("\n" + (qcr.errored ? "ERROR:" : "") + query.id + " " + frequency.name + ":" + hasUpdated + "(diff=" + !hasFireProperty + ")" + ":" + ": " + query.toString() + " " + qcr.timeTaken / 1000 + "s")
                    } else {
                        hasUpdated = checkStatus(query, frequency)

                        // these query and queryResult read/write methods are called by the scheduled jobs
                        Integer totalRecords = null
                        QueryResult queryResult = QueryResult.findByQueryAndFrequency(query, frequency)
                        def records = emailService.retrieveRecordForQuery(query, queryResult)
                        Integer fireWhenNotZero = queryService.fireWhenNotZeroProperty(queryResult)
                        totalRecords = records.size()

                        writer.write("\n" + query.id + " " + frequency.name + ":" + hasUpdated + "(diff=" + !hasFireProperty + ")" + ":" + totalRecords + ": " + query.toString() + " " + (System.currentTimeMillis() - timeTaken) / 1000 + "s")
                    }

                    checkedCount++
                    if (hasUpdated) {
                        checkedAndUpdatedCount++
                    }

                    writer.flush()
                } catch (err) {
                    writer.write("\nERROR:" + query.id + " " + frequency.name + ": " + ": " + query.toString() + ": " + err.getMessage())
                    log.error(err.getMessage())
                }
            }
        }
        writer.write("\nQueries checked: " + checkedCount + ", updated: " + checkedAndUpdatedCount)
        writer.flush()
        log.debug("Queries checked: " + checkedCount + ", updated: " + checkedAndUpdatedCount)
    }

    def debugQueriesForUser(User user, PrintWriter writer) {
        log.debug("Checking queries for user: " + user)
        def checkedCount = 0
        def checkedAndUpdatedCount = 0
        def queries = (List<Query>) Query.executeQuery(
                """select q from Query q
                  inner join q.notifications n
                  inner join n.user u
                  where u = :user
                  group by q""", [user: user])

        queries.each { query ->
            QueryCheckResult qcr = checkStatusDontUpdate(query, user.frequency)
            checkedCount++
            if (qcr.queryResult.hasChanged) {
                checkedAndUpdatedCount++
            }
            writer.write(query.id + ": " + query.toString())
            writer.write("\nUpdated (" + user.frequency.name + "):" + qcr.queryResult.hasChanged)
            writer.write("\nTime taken: " + qcr.timeTaken / 1000 + ' secs \n')
            writer.write(("-" * 80) + "\n")
            writer.flush()
        }
    }

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
        QueryResult qr = getQueryResult(query, Frequency.findByName(frequency))

        try {
            def processedJson = processQueryBiosecurity(query, since, now)

            def recordsFound = JsonPath.read(processedJson, '$.totalRecords')
            result.logs << "${recordsFound} record(s) found since ${sdf.format(since)}."

            // set check time
            qr.previousCheck = since

            // store the last result from the webservice call
            qr.previousResult = qr.lastResult
            qr.lastResult = gzipResult(processedJson)
            qr.lastChecked = now
            qr.hasChanged = diffService.hasChangedJsonDiff(qr)
            qr.logs = ""

            log.debug("[QUERY " + query.id + "] Has changed?: " + qr.hasChanged)
            if (qr.hasChanged) {
                qr.lastChanged = since
            }

            // todo Need to be reviewed, the queryUrlUIUsed does not work with biosecurity query

//             copied from R code
//            def todayMinus7 = DateUtils.addDays(qr.previousCheck, -1 * grailsApplication.config.getProperty("biosecurity.legacy.firstLoadedDateAge", Integer, 7))
//            def todayNDaysAgo = DateUtils.addDays(qr.previousCheck, -1 * grailsApplication.config.getProperty("biosecurity.legacy.eventDateAge", Integer, 29))
//
//            def firstLoadedDate = sdf.format(todayMinus7) + 'T00:00:00Z'
//            def occuranceDate = sdf.format(todayNDaysAgo) + 'T00:00:00Z'
//            String queryPath = query.queryPathForUI
//            String modifiedPath = queryPath.replaceAll('___DATEPARAM___', firstLoadedDate).replaceAll('___LASTYEARPARAM___', occuranceDate)
//            qr.queryUrlUIUsed = query.baseUrlForUI + modifiedPath

            //todo experimental code, need to be reviewed and updated
            def todayNDaysAgo = DateUtils.addDays(since, -1 * grailsApplication.config.getProperty("biosecurity.legacy.eventDateAge", Integer, 150))
            def firstLoadedDate = sdf.format(since)
            def occuranceDate = sdf.format(todayNDaysAgo)
            String queryPath = query.queryPathForUI
            String modifiedPath = queryPath.replaceAll('___DATEPARAM___', firstLoadedDate).replaceAll('___LASTYEARPARAM___', occuranceDate)
            qr.queryUrlUIUsed = query.baseUrlForUI + modifiedPath

            //Do not remove, it breaks the refreshProperties method if it is a new subscription
            if (!qr.save(validate: true, flush: true)) {
                qr.errors.allErrors.each {
                    log.error(it)
                }
            }
            // save QueryResult
            refreshProperties(qr, processedJson)


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
            result.logs << "Completed!"
            result.message = "Completion of Subscription check: ${query?.id}. ${query?.name}."

        } catch (Exception e) {
            log.error("Failed to trigger subscription [ ${query?.id}  ${query?.name} ]", e)
            result = [status: 1, message: "Failed to trigger subscription [ ${query?.id}  ${query?.name} ]" + e.message]
        } finally {
            log.info("[Status: ${result.status}] - ${result.message}")

            qr.appendLogs(result.logs.join("\n"))
            if (!qr.save(validate: true, flush: true)) {
                qr.errors.allErrors.each {
                    log.error(it)
                }
            }
            return result
        }
    }

    def checkQueryForFrequency(String frequencyName) {
        log.debug("Checking frequency : " + frequencyName)
        Date now = new Date()
        Frequency frequency = Frequency.findByName(frequencyName)
        checkQueryForFrequency(frequency, true)
        //update the frequency last checked
        frequency = Frequency.findByName(frequencyName)
        if (frequency) {
            frequency.lastChecked = now
            if (!frequency.save(validate: true, flush: true)) {
                frequency.errors.allErrors.each {
                    log.error(it)
                }
            }
        } else {
            log.warn "Frequency not found for ${frequencyName}"
        }
    }

    //select q.id, u.frequency from query q inner join notification n on n.query_id=q.id inner join user u on n.user_id=u.id;
    List<Map> checkQueryForFrequency(Frequency frequency, Boolean sendEmails) {
        List<Map> recipients = []
        def queries = Query.executeQuery(
                """select q from Query q
                  inner join q.notifications n
                  inner join n.user u
                  where u.frequency = :frequency
                  group by q""", [frequency: frequency])

        queries.each { query ->
            // biosecurity queries are handled elsewhere
            if (!queryService.isBioSecurityQuery(query)) {
                log.debug("Running query: " + query.name)
                boolean hasUpdated = checkStatus(query, frequency)
                Boolean forceUpdate = grailsApplication.config.getProperty('mail.details.forceAllAlertsGetSent', Boolean, false)

                if (forceUpdate || hasUpdated && sendEmails) {
                    log.debug("Query has been updated. Sending emails....")
                    //send separate emails for now
                    //if there is a change, generate an email list
                    //send an email

                    def users = Query.executeQuery(
                            """select u.email, max(u.unsubscribeToken), max(n.unsubscribeToken)
                      from User u
                      inner join u.notifications n
                      where n.query = :query
                      and u.frequency = :frequency
                      and (u.locked is null or u.locked != 1)
                      group by u""", [query: query, frequency: frequency])

                    recipients = users.collect { user ->
                        [email: user[0], userUnsubToken: user[1], notificationUnsubToken: user[2]]
                    }
                    log.debug("Sending emails to...." + recipients*.email.join(","))
                    if (!users.isEmpty()) {
                        emailService.sendGroupNotification(query, frequency, recipients)
                    }
                }
            }
        }
        recipients
    }

    /**
     * Check the queries of a specific user.
     *
     * @param user
     * @param sendEmails
     * @return
     */
    def checkQueriesForUser(User user, Boolean sendEmails) {

        log.debug("Checking queries for user: " + user)

        def queries = (List<Query>) Query.executeQuery(
                """select q from Query q
                  inner join q.notifications n
                  inner join n.user u
                  where u = :user
                  group by q""", [user: user])

        queries.each { query ->
            log.debug("Running query: " + query.name)
            boolean hasUpdated = checkStatus(query, user.frequency)
            if (hasUpdated && sendEmails) {
                log.debug("Query has been updated. Sending emails to...." + user)
                //send separate emails for now
                //if there is a change, generate an email list
                //send an email
                emailService.sendGroupNotification(query, user.frequency, [[email: user.email, userToken: user.unsubscribeToken, notificationToken: ""]])
            }
        }
    }

    def addAlertForUser(User user, Long queryId) {
        log.debug('add my alert :  ' + queryId + ' for user : ' + user)
        def notificationInstance = new Notification()
        notificationInstance.query = Query.findById(queryId)
        notificationInstance.user = user
        //does this already exist?
        def exists = Notification.findByQueryAndUser(notificationInstance.query, notificationInstance.user)
        if (!exists) {
            log.info("Adding alert for user: " + notificationInstance.user + ", query id: " + queryId)
            if (!notificationInstance.save(validate: true, flush: true)) {
                notificationInstance.errors.allErrors.each {
                    log.error(it)
                }
            }
        } else {
            log.info("NOT Adding alert for user: " + notificationInstance.user + ", query id: " + queryId + ", already exists...")
        }
    }

    def deleteAlertForUser(User user, Long queryId) {
        log.debug('Deleting my alert :  ' + queryId + ' for user : ' + user)
        def query = Query.findById(queryId)

        def notificationInstance = Notification.findByUserAndQuery(user, query)
        if (notificationInstance) {
            log.debug('Deleting my notification :  ' + queryId)
            notificationInstance.each { it.delete(flush: true) }
        } else {
            log.error('*** Unable to find  my notification - no delete :  ' + queryId)
        }
    }

    def subscribeMyAnnotation(User user) {
        Query myAnnotationQuery = queryService.createMyAnnotationQuery(user?.userId)
        boolean newQueryCreated = queryService.createQueryForUserIfNotExists(myAnnotationQuery, user, false)
        // trigger a check for this query to generate query result
        // user could call multiple subscribeMyAnnotation, only the first one will create a new query so it's
        // triggered only once.
        if (newQueryCreated) {
            Query savedQuery = Query.findByBaseUrlAndQueryPath(myAnnotationQuery.baseUrl, myAnnotationQuery.queryPath)
            checkStatus(savedQuery, user.frequency, true)
        }
    }

    def unsubscribeMyAnnotation(User user) {
        String myAnnotationQueryPath = queryService.constructMyAnnotationQueryPath(user?.userId)
        Query retrievedQuery = Query.findByQueryPath(myAnnotationQueryPath)

        if (retrievedQuery != null) {
            // delete the notification
            def notification = Notification.findByQueryAndUser(retrievedQuery, user)
            if (notification) {
                notification.delete(flush: true)
            }

            // delete the query result
            QueryResult qr = QueryResult.findByQueryAndFrequency(retrievedQuery, user?.frequency)
            if (qr) {
                qr.delete(flush: true)
            }

            // delete query
            retrievedQuery.delete(flush: true)
        }
    }

    // update user to new frequency
    // there are some special work if user is subscribed to 'My Annotation' alert
    def updateFrequency(User user, String newFrequency) {
        def oldFrequency = user.frequency
        user.frequency = Frequency.findByName(newFrequency)

        String myAnnotationQueryPath = queryService.constructMyAnnotationQueryPath(user?.userId)
        Query query = Query.findByQueryPath(myAnnotationQueryPath)

        // my annotation generates alert(diff) by comparing QueryResult at 2 time points.
        // first QueryResult will be inserted when user subscribes to my annotation
        // every time user changes the frequency, we also need to create a new QueryResult
        // here we update the frequency of existing QueryResult instead of delete old + create new for below reason
        // suppose
        // 1. we have an hourly QueryResult
        // 2. some changes happened
        // 3. user changes frequency to daily
        // 4. if now we create a daily QueryResult which reflects current verifications status (at time position 4)
        //  and is used to do diff, then changes in 2 will be lost. So we directly update existing hourly QueryResult to be daily
        //  so next time scheduled daily task runs, it compares with status at time 1 so changes at time 2 will be captured
        if (query) {
            QueryResult qr = QueryResult.findByQueryAndFrequency(query, oldFrequency)
            if (qr) {
                qr.frequency = user.frequency
                if (!qr.save(validate: true, flush: true)) {
                    qr.errors.allErrors.each {
                        log.error(it)
                    }
                }
            }
        }

        if (!user.save(validate: true, flush: true)) {
            user.errors.allErrors.each {
                log.error(it)
            }
        }
    }

    def getUnsubscribeToken(user, query) {
        if (user && query) {
            def notification = Notification.findByUserAndQuery(user, query)
            if (notification) {
                return notification.unsubscribeToken
            }
        } else {
            log.error("User or query not found for userId: " + user?.id + ", queryId: " + query?.name)
            return null;
        }
    }

}
