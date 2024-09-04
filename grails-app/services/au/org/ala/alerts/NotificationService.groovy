package au.org.ala.alerts

import com.jayway.jsonpath.JsonPath
import grails.gorm.transactions.NotTransactional
import grails.converters.JSON
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.time.DateUtils
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONElement
import org.grails.web.json.JSONObject

import javax.transaction.Transactional
import java.util.zip.GZIPOutputStream
import java.text.SimpleDateFormat
import groovy.time.TimeCategory
import org.hibernate.FlushMode

class NotificationService {

    int PAGING_MAX = 1000
    def sessionFactory
    def emailService
    def diffService
    def queryService
    def grailsApplication
    def dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")

    @Transactional
    QueryResult getQueryResult(Query query, Frequency frequency) {
        QueryResult qr = QueryResult.findByQueryAndFrequency(query, frequency)
        if (qr == null) {
            qr = new QueryResult([query: query, frequency: frequency])
        }
        qr
    }

    def retrieveRecordForQuery(query, queryResult) {
        if  (query.recordJsonPath) {
            // return all of the new records if query is configured to fire on a non-zero value OR if previous value does not exist.
            if (queryService.firesWhenNotZero(query) || queryResult.previousResult ==  null) {
                diffService.getNewRecords(queryResult)
                // return diff of new and old records for all other cases
            } else {
                diffService.getNewRecordsFromDiff(queryResult)
            }
        } else {
            []
        }
    }

    /**
     * CORE method of searching if there are new records for a given query and frequency.
     * EXCEPT BioSecurity Query which is handled elsewhere.
     *
     * @param query
     * @param frequency
     * @param runLastCheck It will set the date range from N days before the previous check date, based on the given frequency, to the last check date
     * @param dryRun If true, the method will not update the database
     * @return true if the result has changed
     */

    QueryResult executeQuery(Query query, Frequency frequency, boolean runLastCheck=false, boolean dryRun=false) {

        def session = sessionFactory.currentSession
        session.setFlushMode(FlushMode.MANUAL) // Set the flush mode to MANUAL

        QueryResult qr = getQueryResult(query, frequency)
        Date startTime = new Date()
        qr.newLog("Checking: ${frequency?.name} - [${query.id}] - ${query.name} - ${dateFormatter.format(startTime)}.")
        //def url = new URL("http://biocache.ala.org.au/ws/occurrences/search?q=*:*&pageSize=1")
        def urls = buildQueryUrl(query, frequency, runLastCheck)

        def urlString = urls.first()
        def urlStringForUI = urls.last()
        qr.addLog("${urlString}")
        log.debug("[QUERY " + query.id + "] Querying URL: " + urlString)

        try {
            def processedJson

            if (!urlString.contains("___MAX___")) {
                // queries without paging
                if (!queryService.isBioSecurityQuery(query)) {
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
            qr.addLog("Completed. ${qr.hasChanged ? 'Changed' : 'No change'}")

        } catch (Exception e) {
            log.error("[QUERY " + query.id + "] URL: " + urlString + " " + e.getMessage(), e)
            qr.addLog("Failed: ${e.getMessage()}")
            qr.succeed = false
        } finally {
            Date endTime = new Date()
            if (qr.hasChanged) {
                qr.lastChanged = endTime
            }
            def duration = TimeCategory.minus(endTime, startTime)
            qr.addLog("Time cost: ${duration}")
            if(!dryRun) {
                QueryResult.withTransaction {
                    if (!qr.save(validate: true, flush: true)) {
                        qr.errors.allErrors.each {
                            log.error(it)
                        }
                    }
                }
            }
            else {
                // if dryRun, evict the object to avoid being persistent
                session.evict(qr)
            }
        }

        return qr
    }

    /**
     * It has similar code with checkStatus, but not identical
     *
     * @param query
     * @param frequency
     * @return
     */
    @Deprecated
    QueryCheckResult checkStatusDontUpdate(Query query, Frequency frequency) {

        //get the previous result
        long start = System.currentTimeMillis()
        QueryResult lastQueryResult = getQueryResult(query, frequency)
        QueryCheckResult qcr = new QueryCheckResult()

        //get the urls to query
        def urls = buildQueryUrl(query, frequency)
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

    /**
     * if runLastCheck is true, the date range will start from the previous check data to the last check date
     *
     * @param query
     * @param frequency
     * @param runLastCheck
     * @return
     */
    String[] buildQueryUrl(Query query, Frequency frequency, boolean runLastCheck=false) {
        def queryPath = query.queryPath
        def queryPathForUI = query.queryPathForUI

        //if there is a date format, then there's a param to replace
        if (query.dateFormat) {
            def checkDate = new Date()
            if (runLastCheck) {
                QueryResult qs = query.getQueryResult(frequency.name)
                if (qs && qs.lastChecked) {
                    checkDate  = qs.lastChecked
                }
            }

            def additionalTimeoffset = grailsApplication.config.getProperty('mail.details.forceAllAlertsGetSent', Boolean, false) ? 24 * 180 : 1
            def dateToUse = DateUtils.addSeconds(checkDate, -1 * frequency.periodInSeconds * additionalTimeoffset)
            // date one year prior from today.
            def dateLastYear = DateUtils.addYears(checkDate, -1)
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
    //@NotTransactional
    Boolean hasChanged(QueryResult queryResult) {
        Boolean changed = false

        //if there is a fireWhenNotZero or fireWhenChange ignore  idJsonPath
        log.debug("[QUERY " + queryResult.query.id + "] Checking query: " + queryResult.query.name)

         // PropertyValues in a Biocache Query 'usually' has two properties: totalRecords and last_loaded_records (uuid)
         //Both have the possible null value
         //The following check is determined by the last propertyValue, since it overwrites the previous one

        queryResult.propertyValues.each { pv ->
            log.debug("[QUERY " + queryResult.query.id + "] " +
                    " Has changed check:" + pv.propertyPath.name
                    + ", value:" + pv.currentValue
                    + ", previous:" + pv.previousValue
                    + ", fireWhenNotZero:" + pv.propertyPath.fireWhenNotZero
                    + ", fireWhenChange:" + pv.propertyPath.fireWhenChange
            )

            // Two different types of queries: Biocache and Blog/News
            // Biocache: totalRecords and last_loaded_records
            // Blog/News: last_blog_id
            if (pv.propertyPath.fireWhenNotZero) {
                changed = pv.currentValue?.toInteger() ?: 0 > 0
            } else if (pv.propertyPath.fireWhenChange) {
                changed = pv.previousValue != pv.currentValue
            }
        }

        //Example, in a blog/news query,fireWhenNotZero and fireWhenChange both are false

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

    /**
     * Update the values of the properties, e.g. totalRecords, lastLoadedRecords etc defined int PropertyPath
     *
     * @param queryResult
     * @param json
     * @return
     */
    @NotTransactional
    def refreshProperties(QueryResult queryResult, json) {
        log.debug("[QUERY " + queryResult?.query?.id ?: 'NULL' + "] Refreshing properties for query: " + queryResult.query.name + " : " + queryResult.frequency)
        try {
                queryResult.query.propertyPaths.each { propertyPath ->
                    //Used to read the ID from the JSON, if needed
                    String idJsonPath = queryResult.query.idJsonPath?: 'id'

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
                        //Assume that the size of the list is the total number of records
                        //We need to decide to store the total number or the first?/last? ID (Assume it is defined in idJsonPath)
                        //depends on the propertyPath e.g. fireWhenNotZero, fireWhenChange etc
                        if (propertyPath.fireWhenNotZero ) {
                            propertyValue.currentValue = latestValue.size().toString()
                        } else if (propertyPath.fireWhenChange){
                            //idJsonPath is defined in query represents the 'id' of the record
                            propertyValue.currentValue = latestValue.first()[idJsonPath]?.toString()
                        }
                     } else {
                        propertyValue.currentValue = latestValue
                    }
                    queryResult.addToPropertyValues(propertyValue)
                }

        } catch (Exception e) {
            log.error("[QUERY " + queryResult?.query?.id ?: 'NULL' + "] There was a problem reading the supplied JSON.", e)
        }
    }

    PropertyValue getPropertyValue(PropertyPath pp, QueryResult queryResult) {
        PropertyValue pv = queryResult.id == null ? null : PropertyValue.findByPropertyPathAndQueryResult(pp, queryResult)
        if (pv == null) {
            pv = new PropertyValue([propertyPath: pp, queryResult: queryResult])
        }
        pv
    }

    /**
     * We are now using the original query code for testing, instead of using a duplicated version.
     */
    @Deprecated
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

    /**
     * We are now using the original query code for testing, instead of using a duplicated version.
     */
    @Deprecated
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
                        hasUpdated = executeQuery(query, frequency)?.hasChanged

                        // these query and queryResult read/write methods are called by the scheduled jobs
                        Integer totalRecords = null
                        QueryResult queryResult = QueryResult.findByQueryAndFrequency(query, frequency)
                        def records = retrieveRecordForQuery(query, queryResult)
                        Integer fireWhenNotZero = queryService.totalNumberWhenNotZeroPropertyEnabled(queryResult)
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

    @Deprecated
    /**
     * We are now using the original query code for testing, instead of using a duplicated version.
     */
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



    /**
     * Check the queries for a specific frequency EXCEPT for biosecurity queries.
     * @param frequencyName
     * @return
     */
    def execQueryForFrequency(String frequencyName, boolean sendEmails = true) {
        log.debug("Checking frequency : " + frequencyName)
        Date now = new Date()
        Frequency frequency = Frequency.findByName(frequencyName)
        execQueryForFrequency(frequency, sendEmails)
        //update the frequency last checked
        frequency = Frequency.findByName(frequencyName)
        if (frequency) {
            frequency.lastChecked = now
            Frequency.withTransaction {
                if (!frequency.save(validate: true, flush: true)) {
                    frequency.errors.allErrors.each {
                        log.error(it)
                    }
                }
            }
        } else {
            log.warn "Frequency not found for ${frequencyName}"
        }
    }

    //select q.id, u.frequency from query q inner join notification n on n.query_id=q.id inner join user u on n.user_id=u.id;
    List<Map> execQueryForFrequency(Frequency frequency, Boolean sendEmails) {
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
                boolean hasUpdated = executeQuery(query, frequency)?.hasChanged
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
            boolean hasUpdated = executeQuery(query, user.frequency)?.hasChanged
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
            Notification.withTransaction {
                if (!notificationInstance.save(validate: true, flush: true)) {
                    notificationInstance.errors.allErrors.each {
                        log.error(it)
                    }
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
            Notification.withTransaction {
                notificationInstance.each { it.delete(flush: true) }
            }
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
            executeQuery(savedQuery, user.frequency)
        }
    }


    def unsubscribeMyAnnotation(User user) {
        String myAnnotationQueryPath = queryService.constructMyAnnotationQueryPath(user?.userId)
        Query retrievedQuery = Query.findByQueryPath(myAnnotationQueryPath)


        Query.withTransaction {
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
    }

    // update user to new frequency
    // there are some special work if user is subscribed to 'My Annotation' alert
    @Transactional
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
                QueryResult.withTransaction {
                    if (!qr.save(validate: true, flush: true)) {
                        qr.errors.allErrors.each {
                            log.error(it)
                        }
                    }
                }
            }
        }

        User.withTransaction {
            if (!user.save(validate: true, flush: true)) {
                user.errors.allErrors.each {
                    log.error(it)
                }
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

    /**
     * Copied from EmailService
     * Todo : Not full correct, need to be fixed
     * @param queryResult
     * @return
     */
    //@NotTransactional
    def collectUpdatedRecords(queryResult) {
        if  (queryResult.query?.recordJsonPath) {
            // return all of the new records if query is configured to fire on a non-zero value OR if previous value does not exist.
            if (queryService.firesWhenNotZero(queryResult.query) || queryResult.previousResult ==  null) {
                diffService.getNewRecords(queryResult)
                // return diff of new and old records for all other cases
            } else {
                diffService.getNewRecordsFromDiff(queryResult)
            }
        } else {
            []
        }
    }
}
