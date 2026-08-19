package au.org.ala.alerts

import com.jayway.jsonpath.JsonPath
import grails.converters.JSON
import org.apache.commons.lang3.time.DateUtils
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
        QueryResult qr = null

        QueryResult.withTransaction {
            qr = QueryResult.where {
                query == query && frequency == frequency
            }.join('query').find()

            if (!qr) {
                qr = new QueryResult(query: query, frequency: frequency)
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


    String cleanUpUrl(url) {
        def queryStart = url.indexOf("?")
        if (queryStart > 0) {
            // If there is a query string, replace spaces, colons, and quotes with their URL-encoded equivalents
            def queryString = url.substring(queryStart + 1)
            url = url.substring(0, queryStart + 1) + queryString.replaceAll(" ", "%20").replaceAll("\"", "%22")
            // Encodes brackets in the URL if the configuration is set to do so
            if (grailsApplication.config.encodeBracketInUrlParam?.enabled) {
               url = url.replace("[", "%5B").replace("]", "%5D")
            }
        }
        return url
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
        if (resp.status >= 200 && resp.status < 300) {
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
     * Check the queries for a specific frequency EXCEPT for biosecurity queries.
     * @param frequencyName
     * @return logs for each query
     */
    def execQueryForFrequency(String frequencyName, boolean sendEmails = true, boolean dryRun = false) {
        log.info("Checking frequency : ${frequencyName}, emails ${sendEmails}, dryRun ${dryRun}")
        def logs = []
        Date now = new Date()
        Frequency.withTransaction {
            Frequency frequency = Frequency.findByName(frequencyName)
            if (frequency) {
                logs = execQueryForFrequency(frequency, sendEmails, dryRun)
                //update the frequency last checked.
                //Refresh in case of another job happens to run simultaneous
                frequency.refresh()
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
        List<Query> queries = Query.createCriteria().listDistinct {
            notifications {
                user {
                    eq('frequency', frequency)
                }
                // keep this if you only want enabled subscriptions
                eq('enabled', true)
            }
        } as List<Query>

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
                    List<Notification> matchedNotifications = Notification.createCriteria().list {
                        eq('query', query)
                        eq('enabled', true)
                        user {
                            eq('frequency', frequency)
                            or {
                                isNull('locked')
                                ne('locked', true)
                            }
                        }
                    } as List<Notification>

                    def recipients = matchedNotifications.collect { Notification n ->
                        [email: n.user.email, userUnsubToken: n.user.unsubscribeToken, notificationUnsubToken: n.unsubscribeToken]
                    }

                    log.debug("Sending emails to...." + recipients*.email.join(","))
                    def emails = recipients*.email
                    info['newRecords'] = qr.newRecords.size()
                    info['recipients'] = emails.size() > 3
                            ? emails.take(3).join(", ") + ", etc"
                            : emails.join(", ")

                    if (!recipients.isEmpty() && sendEmails) {
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

    /**
     * Get all alerts for a user, including enabled and disabled alerts, and mark them as the user's own alerts.
     * Used to display the user's alerts in the UI.
     * @param user
     * @return
     */
    def myAlerts(User user) {
        def myAlerts = getAlerts(user)
        myAlerts["isMyOwnAlerts"] = true
        myAlerts
    }

    /**
     * Only get the enabled queries for a user.
     * @param user
     * @return
     */
    def getEnabledAlerts(User user) {
        def myAlerts = Notification.createCriteria().list {
            eq('user', user)
            eq('enabled', true)
        } as List<Notification>

        def enabledQuries = myAlerts.collect { it.query }.findAll { it != null }.unique { it.id }
        enabledQuries
    }

    def getAlerts(User user) {
        def userAlertsMap = [:]

        if (user) {
            def myAlerts = []

            myAlerts = Notification.createCriteria().list {
                eq('user', user)
            } as List<Notification>

            // myAnnotationQuery is created to fillet out those myAnnotation queries which do not belong to me,
            def myAnnotationQuerySample = queryService.createMyAnnotationQuery(user.getUserId())
            // myAnnotation is a special case. We need to create a myAnnotation query for the user if it does not exist.
            queryService.createQueryForUserIfNotExists(myAnnotationQuerySample, user, false, false)
            // Get all standard (non-custom) queries, but exclude myAnnotation queries except the one for this user
            def standardQueries = Query.createCriteria().list {
                eq('custom', false)
                or {
                    // Non-myAnnotation standard queries
                    ne('emailTemplate', myAnnotationQuerySample.emailTemplate)
                }

            } as List<Query>

            // check if the user has all standard alerts, if not, create and add it as a disabled notification
            Notification.withTransaction {
                standardQueries.each { query ->
                    def existed = myAlerts.find { it.query.id == query.id }
                    if (!existed) {
                        def newAlert = new Notification(query: query, user: user, enabled: false)
                        newAlert.save()
                        myAlerts << newAlert
                    }
                }
            }

            def myAnnotationQueries = Query.createCriteria().list {
                        eq('emailTemplate', myAnnotationQuerySample.emailTemplate)
                        eq('queryPath', myAnnotationQuerySample.queryPath)
                } as List<Query>
            standardQueries << myAnnotationQueries

            // Only the queries are needed, so filter the notifications and collect their queries in one step
            def myEnabledStandardQueries = myAlerts.findAll { it.query && !it.query.custom && it.enabled }*.query.unique { it.id }
            def myDisabledStandardQueries = myAlerts.findAll { it.query && !it.query.custom && !it.enabled }*.query.unique { it.id }
            def myEnabledCustomQueries = myAlerts.findAll { it.query && it.query.custom && it.enabled }*.query.unique { it.id }
            def myDisabledCustomQueries = myAlerts.findAll { it.query && it.query.custom && !it.enabled }*.query.unique { it.id }

//          It is an old return objects. Keep it for reference.
//          def userConfig = [disabledQueries: allAlertTypes,   // all disabled standard queries
//                              enabledQueries : standardQueries, // all enabled standard queries
//                              customQueries  : customQueries,   // all enabled custom queries
//                              frequencies    : Frequency.listOrderByPeriodInSeconds(),
//                              user           : user]

            userAlertsMap = [enabledStandardQueries: myEnabledStandardQueries,
                          disabledStandardQueries: myDisabledStandardQueries,
                          enabledCustomQueries  : myEnabledCustomQueries,
                          disabledCustomQueries: myDisabledCustomQueries,
                          frequencies    : Frequency.listOrderByPeriodInSeconds(),
                          user           : user
            ]

        }

        return userAlertsMap
    }

    def disableAlertForUser(User user, Long queryId) {
        log.debug('disable an alert :  ' + queryId + ' for user : ' + user)
        def notificationInstance = Notification.findByUserAndQuery(user, Query.findById(queryId))
        if (notificationInstance) {
            log.debug('Disabling alert for user: ' + notificationInstance.user + ', query id: ' + queryId)
            Notification.withTransaction {
                notificationInstance.enabled = false
                if (!notificationInstance.save(validate: true, flush: true)) {
                    notificationInstance.errors.allErrors.each {
                        log.error(it)
                    }
                }
            }
        } else {
            log.info("No alert found to disable for user: " + user + ", query id: " + queryId)
        }
    }

    def enableAlertForUser(User user, Long queryId) {
        def notificationInstance = Notification.findByUserAndQuery(user, Query.findById(queryId))
        if (notificationInstance) {
            log.debug('enable alert for user: ' + notificationInstance.user + ', query id: ' + queryId)
            Notification.withTransaction {
                notificationInstance.enabled = true
                if (!notificationInstance.save(validate: true, flush: true)) {
                    notificationInstance.errors.allErrors.each {
                        log.error(it)
                    }
                }
            }
        } else {
            log.info("No alert found to enable for user: " + user + ", query id: " + queryId)
            log.info("Creating a new alert for user: " + user + ", query id: " + queryId)
            addAlertForUser(user, queryId)
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
        Query myAnnotationSampleQuery = queryService.createMyAnnotationQuery(user?.userId)
        boolean newQueryCreated = queryService.createQueryForUserIfNotExists(myAnnotationSampleQuery, user, false, true)
        // trigger a check for this query to generate query result
        // user could call multiple subscribeMyAnnotation, only the first one will create a new query so it's
        // triggered only once.
        if (newQueryCreated) {
            Query savedQuery = Query.findByBaseUrlAndQueryPath(myAnnotationSampleQuery.baseUrl, myAnnotationSampleQuery.queryPath)
            //todo I don't think we need to execute the query here.
            //executeQuery(savedQuery, user.frequency)
        } else {
            //if it is not new created, the related notification may set to disabled, so we need to enable it
            Query retrievedQuery = Query.findByBaseUrlAndQueryPath(myAnnotationSampleQuery.baseUrl, myAnnotationSampleQuery.queryPath)
            def notification = Notification.findByQueryAndUser(retrievedQuery, user)
            if (notification) {
                notification.enabled = true
                Notification.withTransaction {
                    notification.save()
                }
            }
        }
    }

    /**
     * Unsubscribe the user from the "My Annotation" alert by disabling the notification associated with the user's myAnnotation query.
     * @param user
     * @return
     */
    def unsubscribeMyAnnotation(User user) {
        Query myAnnotationQuery = queryService.findMyAnnotationQuery(user?.userId)
        if (myAnnotationQuery) {
            def notification = Notification.findByQueryAndUser(myAnnotationQuery, user)
            if (notification) {
                notification.enabled = false
                Notification.withTransaction {
                    notification.save()
                }
            }
        } else {
            log.error("Query not found for queryPath: " + user.userId)
        }
    }

    /**
     * Completely delete the "My Annotation" alert for the user, including the notification, query result, and query itself.
     * @param user
     * @return
     */
    def deleteMyAnnotation(User user) {
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
    // todo if we do this for MyAnnotation, we may also do this for others
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
