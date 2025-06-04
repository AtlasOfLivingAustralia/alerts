package au.org.ala.alerts

import com.jayway.jsonpath.JsonPath
import grails.converters.JSON
import org.apache.commons.lang.time.DateUtils
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import java.text.SimpleDateFormat
import groovy.time.TimeCategory
import org.hibernate.FlushMode

class NotificationService {

    int PAGING_MAX = 500
    def sessionFactory
    def httpService
    def userService
    def emailService
    def diffService
    def queryService
    def myAnnotationService
    def annotationsService
    def grailsApplication
    def dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")

    QueryResult getQueryResult(Query query, Frequency frequency) {
        QueryResult qr = QueryResult.findByQueryAndFrequency(query, frequency)
        if (qr == null) {
            QueryResult.withTransaction {
                qr = new QueryResult([query: query, frequency: frequency])
            }
        }
        qr
    }

    /**
     * CORE method of searching if there are new records for a given query and frequency.
     * EXCEPT BioSecurity Query which is handled elsewhere.
     *
     * @param query
     * @param frequency
     * @param runLastCheck It has the highest priority. It may set the date range from N days before the previous check date to the last check date based on the query.
     * @param dryRun If true, the method will not update the database
     * @param checkDate the date to check the query. It is the current date by default
     * @return true if the result has changed
     */

    QueryResult executeQuery(Query query, Frequency frequency, boolean runLastCheck=false, boolean dryRun=false, Date checkDate= new Date()) {
        def session = sessionFactory.currentSession
        session.setFlushMode(FlushMode.MANUAL) // Set the flush mode to MANUAL

        QueryResult qr = getQueryResult(query, frequency)
        //For log only, the time started to run the query, not the time which query was executed against
        Date startTime = new Date()

        qr.newLog("Checking: ${frequency?.name} - [${query.id}] - ${query.name} - ${dateFormatter.format(checkDate)}.")
        //def url = new URL("http://biocache.ala.org.au/ws/occurrences/search?q=*:*&pageSize=1")
        def urls = buildQueryUrl(query, frequency, runLastCheck, checkDate)

        def urlString = urls.first()
        def urlStringForUI = urls.last()
        qr.addLog("${urlString}")
        log.debug("[QUERY " + query.id + "] Querying URL: " + urlString)

        try {
            def processedJson

            if (urlString.contains("___MAX___")) {
                //Only can handle the species lists
                processedJson = processQuery(query, urlString, PAGING_MAX)
            } else {
                processedJson = processQuery(query, urlString)
            }
            // set check time
            qr.previousCheck = qr.lastChecked
            // store the last result from the webservice call
            qr.previousResult = qr.lastResult
            qr.lastResult = qr.compress(processedJson)
            qr.lastChecked = checkDate
            qr.queryUrlUsed = urlString
            qr.queryUrlUIUsed = urlStringForUI

            // Find the new and updated records
            qr.newRecords = diffService.diff(qr)
            if (qr.newRecords.size() > 0) {
                qr.hasChanged = true
                qr.lastChanged = checkDate
            } else {
                qr.hasChanged = false
            }

            log.debug("[QUERY " + query.id + "] Has changed?: " + qr.hasChanged)
            qr.succeeded = true
            qr.addLog("Completed ${qr.succeeded}. ${qr.hasChanged ? 'Changed' : 'No change'}")
        } catch (Exception e) {
            log.error("Failed: ${query.id}, ${query.name}, ${frequency.name}, URL: ${urlString}")
            log.error("Error: ${e.getMessage()}")
            qr.addLog("Error: ${e.getMessage()}")
            qr.succeeded = false
        } finally {
            Date endTime = new Date()
            def duration = TimeCategory.minus(endTime, startTime)
            String msg = "${qr.succeeded ? (qr.hasChanged ? qr.newRecords.size() + ' new records found' : 'Completed - No new records') : 'Aborted'}. [${query.id}, ${query.name}, ${frequency.name}]. Time cost: ${duration}"
            qr.addLog(msg)
            log.info(msg)

            if(!dryRun ){
                QueryResult.withTransaction {
                    if (!qr.save(validate: true)) {
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
                    processedJson = processQuery(query, urlString)
                } else {
                    // biosecurity query is handled elsewhere
                    Date since = lastQueryResult.lastChecked ?: DateUtils.addDays(new Date(), -1 * grailsApplication.config.getProperty("biosecurity.legacy.firstLoadedDateAge", Integer, 7))
                    Date to = new Date()
                    processedJson = processQueryBiosecurity(query, since, to)
                }
            } else {
                // It is works on species lists request
                int max = PAGING_MAX
                int offset = 0
                def allLists = []
                boolean finished = false

                while (!finished) {
                    // Construct the URL with max and offset values
                    String urlWithParams = urlString.replace('___MAX___', max.toString()).replace('___OFFSET___', offset.toString())

                    // Get the result from the query
                    def result = processQuery(query, urlWithParams)

                    // Check if result is not empty
                    if (result?.size() == 0) {
                        finished = true
                        break
                    }

                    try {
                        // Read latest values from JSON
                        def latestValue = JsonPath.read(result, query.recordJsonPath)

                        if (latestValue.size() == 0) {
                            finished = true
                        } else {
                            processedJson = result
                            allLists.addAll(latestValue)
                            offset += max // Update offset for the next iteration
                        }
                    } catch (Exception e) {
                        // Handle missing properties gracefully
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


    /**
     * runLastCheck has the highest priority, if it is true, the date range will start from the previous check data to the last check date
     *
     * @param query
     * @param frequency
     * @param runLastCheck
     * @param checkDate the date to check the query. It is the current date by default
     * @return
     */
    String[] buildQueryUrl(Query query, Frequency frequency, boolean runLastCheck=false, Date checkDate= new Date()) {
        def queryPath = query.queryPath
        def queryPathForUI = query.queryPathForUI

        //if there is a date format, a param relating with Date needs to be replaced
        if (query.dateFormat) {
            if (runLastCheck) {
                QueryResult qs = query.getQueryResult(frequency.name)
                if (qs && qs.lastChecked) {
                    checkDate  = qs.lastChecked
                }
            }

            def additionalTimeoffset =  1
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
     * todo: this method has been moved to diffService
     * It is not used by a deprecated method: checkStatusDontUpdate
     *
     * Indicates if the result of a query has changed by checking its properties.
     *
     * @param queryResult
     * @return
     */
    @Deprecated
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
     * todo: check if this method could be removed.
     * It is used by a deprecated method: checkStatusDontUpdate
     *
     * Compares the stored values with the values in the JSON returning a map of
     *
     * propertyPath -> [current, previous]
     *
     * @param queryResult
     * @param json
     * @return
     */
    @Deprecated
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

    /**
     * todo : Suspect this method ONLY works for Lists
     *
     * Iterate the query with paging to get all the lists
     * @param query
     * @param url
     * @param pageSize
     * @return
     */
    String processQuery(Query query, String urlString, int pageSize) {
        int offset = 0
        def processedJson = ""
        def result = ""
        boolean finished = false
        def allLists = []

        while (!finished) {
            // Construct the URL
            def url = new URL(urlString
                    .replaceAll('___MAX___', String.valueOf(pageSize))
                    .replaceAll('___OFFSET___', String.valueOf(offset))
            )

            // Process the query
            result = processQuery(query, url.toString()) // API errors will result in an empty string ("")

            // Check if we have results
            if (!result || result?.size() == 0) {
                finished = true
                continue
            }

            // Increment offset for next iteration
            offset += pageSize

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

        // Suspicious code: Seems ONLY for lists.
        // If it is not a list, only return the last result - processedJson
        def json = JSON.parse(processedJson) as JSONObject
        if (json.lists) {
            // set json.lists to allLists such that json.toString() does not convert allLists items to strings
            json.lists = JSON.parse((allLists as JSON).toString()) as JSONArray
            processedJson = json.toString()
        }
        return processedJson
    }

     String processQuery(Query query, String url) {
        String queryResult = "{}"
        def resp = httpService.getJson(url)
        if (resp.status == 200 || resp.status == 201) {
            def occurrences = resp.json
            if (queryService.isMyAnnotation(query)) {
                queryResult = myAnnotationService.preProcess(query, occurrences)
            } else if (queryService.isAnnotation(query)) {
                queryResult = annotationsService.preProcess(query, occurrences)
            } else {
                queryResult = occurrences.toString()
            }
        } else {
            String msg = resp.error.length() > 100 ? resp.error.take(100) + "..." : resp.error
            String error = "Failed to access : ${url}, [${resp.status}]: ${msg}"
            throw new RuntimeException("${error}")
        }
        return queryResult
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
                        def records = diffService.diff(queryResult)
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


    /**
     * Check the queries for a specific frequency EXCEPT for biosecurity queries.
     * @param frequencyName
     * @return logs for each query
     */
    def execQueryForFrequency(String frequencyName, boolean sendEmails = true, boolean dryRun = false) {
        log.info("Checking frequency : ${frequencyName}, emails ${sendEmails}, dryRun ${dryRun}")
        def logs = []
        Date now = new Date()
        Frequency frequency = Frequency.findByName(frequencyName)
        if (frequency) {
            logs = execQueryForFrequency(frequency, sendEmails, dryRun)
            //update the frequency last checked
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
        return logs
    }

    /**
     * Check the queries for a specific frequency EXCEPT for biosecurity queries.
     * @param frequency
     * @param sendEmails
     * @param dryRun
     * @return los for results of each query
     */
    //select q.id, u.frequency from query q inner join notification n on n.query_id=q.id inner join user u on n.user_id=u.id;
    List<Map> execQueryForFrequency(Frequency frequency, Boolean sendEmails, Boolean dryRun = false) {
        def logs = []
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
                QueryResult qr = executeQuery(query, frequency, false, dryRun)
                def info = [id: query.id, name: query.name, succeeded: qr.succeeded, hasChanged: qr.hasChanged]

                // Add error message if the query failed
                if (!qr.succeeded) {
                    info['error'] = qr.logs
                }
                boolean hasUpdated = qr?.succeeded && qr?.hasChanged

                if (hasUpdated) {
                    def users = Query.executeQuery(
                            """select u.email, max(u.unsubscribeToken), max(n.unsubscribeToken)
                      from User u
                      inner join u.notifications n
                      where n.query = :query
                      and u.frequency = :frequency
                      and (u.locked is null or u.locked != 1)
                      group by u""", [query: query, frequency: frequency])

                    def recipients = users.collect { user ->
                        [email: user[0], userUnsubToken: user[1], notificationUnsubToken: user[2]]
                    }
                    log.debug("Sending emails to...." + recipients*.email.join(","))
                    def emails = recipients*.email
                    info['newRecords'] = qr.newRecords.size()
                    info['recipients'] = emails.size() > 3
                            ? emails.take(3).join(", ") + ", etc"
                            : emails.join(", ")

                    if (!users.isEmpty() && sendEmails) {
                        emailService.sendGroupNotification(qr, frequency, recipients)
                    }

                    if (grailsApplication.config.testMode) {
                        try {
                            User currentUser = userService.getUser()
                            def me =
                                    [email: currentUser.email, userUnsubToken: currentUser.unsubscribeToken, notificationUnsubToken: '']
                            emailService.sendGroupNotification(qr, frequency, [me])
                        } catch (Exception e) {
                            log.error("TestMode is on, but failed to sending alerts to the current user: " + e.getMessage())
                        }
                    }
                }
                logs << info
            }
        }
        logs
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
        Query retrievedQuery = queryService.findMyAnnotationQuery(user?.userId)
        if (retrievedQuery != null) {
             Query.withTransaction {

                // delete the notification
                def notification = Notification.findByQueryAndUser(retrievedQuery, user)
                if (notification) {
                    user.removeFromNotifications(notification)
                    retrievedQuery.removeFromNotifications(notification)
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
            return true
        } else {
            log.error("Query not found for queryPath: " + user.userId)
            return false
        }
    }

    // update user to new frequency
    // there are some special work if user is subscribed to 'My Annotation' alert
    // todo if we do this for MyAnnotation, we should also do this for others
    def updateFrequency(User user, String newFrequency) {
        def oldFrequency = user.frequency
        user.frequency = Frequency.findByName(newFrequency)

        Query query =  queryService.findMyAnnotationQuery(user?.userId)
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
}
