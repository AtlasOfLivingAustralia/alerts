package au.org.ala.alerts
import com.jayway.jsonpath.JsonPath
import grails.converters.JSON
import org.apache.commons.io.IOUtils
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONElement
import org.grails.web.json.JSONObject

import java.text.SimpleDateFormat
import java.util.zip.GZIPOutputStream

class NotificationService {

    static transactional = true
    def emailService
    def diffService
    def queryService

    // find query result, which may be user specific
    QueryResult getQueryResult(Query query, Frequency frequency, User user = null) {
        QueryResult qr = QueryResult.findByQueryAndFrequencyAndUser(query, frequency, user)

        if (qr == null) {
            qr = new QueryResult([query:query, frequency:frequency, user:user])
            qr.save(flush:true)
        }
        qr
    }

    /**
     * Check the status of queries for this given frequency.
     *
     * @param query
     * @param frequency
     * @return if query is not user specific returns [true]/[false] indicating there's a change or not
     *         otherwise return [true, [uid1 ... uidn]] or [false]
     */
    def checkStatus(Query query, Frequency frequency, User user = null) {
        // if query not user specific
        if (!queryService.isUserSpecific(query)) {
            return [processSingleQuery(query, frequency)]
        } else { // if query is user specific
            // each user stores its own result so we need to run one by one
            def users = (user != null) ? [user] : getAllUsersForAQuery(query, frequency)
            def changedList = []
            for (u in users) {
                if (processSingleQuery(query, frequency, u)) {
                    changedList.push(u)
                }
            }

            return changedList.size() > 0 ? [true, changedList] : [false]
        }
    }

    def processSingleQuery(Query query, Frequency frequency, User user = null) {
        QueryResult qr = getQueryResult(query, frequency, user)

        //def url = new URL("http://biocache.ala.org.au/ws/occurrences/search?q=*:*&pageSize=1")
        def urls = getQueryUrl(query, frequency, user)

        def urlString = urls.first()
        def urlStringForUI = urls.last()

        log.debug("[QUERY " + query.id + "] Querying URL: " + urlString)

        try {
            def processedJson = processQueryReturnedJson(query, user?.userId, IOUtils.toString(new URL(urlString).newReader()))
            //update the stored properties
            refreshProperties(qr, processedJson)

            qr = QueryResult.findById(qr.id)

            // set check time
            qr.previousCheck = qr.lastChecked
            qr.lastChecked = new Date()

            // store the last result from the webservice call
            qr.previousResult = qr.lastResult
            qr.lastResult = gzipResult(processedJson)

            qr.hasChanged = hasChanged(qr)

            // set query url
            qr.queryUrlUsed = urlString
            qr.queryUrlUIUsed = urlStringForUI

            log.debug("[QUERY " + query.id + "] Has changed?: " + qr.hasChanged)
            if (qr.hasChanged) {
                qr.lastChanged = new Date()
            }

            qr.save(true)
            qr.hasChanged
        } catch (Exception e) {
            log.error("[QUERY " + query.id + "] There was a problem checking the URL: " + urlString, e)
        }
    }

    List<QueryCheckResult> checkStatusDontUpdate(Query query, Frequency frequency, User user = null) {
        def qcrList = []

        // if not user specific
        if (!queryService.isUserSpecific(query)) {
            qcrList.push(processSingleQueryNoUpdate(query, frequency))
        } else if (user != null) { // if user specific and user specified
            qcrList.push(processSingleQueryNoUpdate(query, frequency, user))
        } else { // else get all users subscribe to this query
            // each user stores its own result so we need to run one by one
            def users = getAllUsersForAQuery(query, frequency)
            for (u in users) {
                qcrList.push(processSingleQueryNoUpdate(query, frequency, u))
            }
        }

        qcrList
    }

    def processSingleQueryNoUpdate(Query query, Frequency frequency, User user = null) {

        //get the previous result
        long start = System.currentTimeMillis()
        QueryResult lastQueryResult = getQueryResult(query, frequency, user)
        QueryCheckResult qcr = new QueryCheckResult()

        //get the urls to query
        def urls = getQueryUrl(query, frequency, user)
        def urlString = urls.first()
        qcr.frequency = frequency
        qcr.urlChecked = urlString
        qcr.query = query
        qcr.queryResult = QueryResult.findByQueryAndFrequencyAndUser(query, frequency, user)

        log.debug("[QUERY " + query.id + "] Querying URL: " + urlString)

        try {
            def processedJson = processQueryReturnedJson(query, user?.userId, IOUtils.toString(new URL(urlString).newReader()))

            qcr.response = processedJson

            //update the stored properties
            def propertyPaths = compareProperties(lastQueryResult, processedJson)

            //decompress the last result
            def previousJson = diffService.decompressZipped(lastQueryResult.lastResult)

            //set the has changed
            qcr.queryResult.hasChanged = hasPropertiesChanged(query, propertyPaths, previousJson, processedJson)

            //set the last result
            qcr.queryResult.lastResult = gzipResult(processedJson)

            //set the property values
            def propertyValues = []
            propertyPaths.values().each {
//            new PropertyValue()
//            it.
            }


        } catch (Exception e){
            log.error("[QUERY " + query.id + "] There was a problem checking the URL :" + urlString, e)
            qcr.errored = true
        }
        qcr.timeTaken = System.currentTimeMillis() - start
        qcr
    }

    byte[] gzipResult (String json){
        //store the last result from the webservice call
        ByteArrayOutputStream bout = new ByteArrayOutputStream()
        GZIPOutputStream gzout = new java.util.zip.GZIPOutputStream(bout)
        gzout.write(json.toString().getBytes())
        gzout.flush()
        gzout.finish()
        bout.toByteArray()
    }

    private String[] getQueryUrl(Query query, Frequency frequency, User user = null){
        def queryPath = query.queryPath
        def queryPathForUI = query.queryPathForUI

        //if there is a date format, then there's a param to replace
        if (query.dateFormat) {
            def dateToUse = org.apache.commons.lang.time.DateUtils.addSeconds(new Date(), -1 * frequency.periodInSeconds)
            //insert the date to query with
            SimpleDateFormat sdf = new SimpleDateFormat(query.dateFormat)
            def dateValue = sdf.format(dateToUse)
            queryPath = queryPath.replaceAll("___DATEPARAM___", dateValue)
            queryPathForUI = queryPathForUI.replaceAll("___DATEPARAM___", dateValue)
        }

        if (queryService.isUserSpecific(query) && user != null) {
            queryPath = queryPath.replaceAll("___UIDPARAM___", user.getUserId())
            queryPathForUI = queryPathForUI.replaceAll("___UIDPARAM___", user.getUserId())
        }

        [cleanUpUrl(query.baseUrl + queryPath), cleanUpUrl(query.baseUrlForUI + queryPathForUI)]
    }

    def cleanUpUrl(url){
        def queryStart = url.indexOf("?")
        if(queryStart>0){
            def queryString = url.substring(queryStart+1)
            url.substring(0, queryStart+1) + queryString.replaceAll(" ", "%20").replaceAll(":", "%3A").replaceAll("\"","%22")
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
            if (pv.propertyPath.fireWhenNotZero){
                changed = pv.currentValue.toInteger() > 0
            }
            else if (pv.propertyPath.fireWhenChange){
                changed = pv.previousValue != pv.currentValue
            }
        }

        if (queryService.checkChangeByDiff(queryResult.query)) {
            log.debug("[QUERY " + queryResult.query.id + "] Has change check. Checking JSON for query : "  + queryResult.query.name)
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
    Boolean hasPropertiesChanged(Query query, Map<PropertyPath, Map<String,String>> propertyPathMap, String jsonPrevious, String jsonCurrent) {
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
            if (propertyPath.fireWhenNotZero){
                changed = value.current.toInteger() > 0
            } else if (propertyPath.fireWhenChange){
                changed = value.previous != value.current
            }
        }

        if (queryService.checkChangeByDiff(query)) {
            log.debug("[QUERY " + query.id + "] Has change check. Checking JSON for query : "  + query.name)
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

        log.debug("[QUERY " + queryResult?.query?.id?:'NULL' + "] Refreshing properties for query: " + queryResult.query.name + " : " +  queryResult.frequency)

        try {

            queryResult.query.propertyPaths.each { propertyPath ->
                //read the value from the request
                def latestValue = null

                try {
                    latestValue = JsonPath.read(json, propertyPath.jsonPath)
                } catch (Exception e){
                    //expected behaviour for missing properties
                }

                //get property value for this property path
                PropertyValue propertyValue = getPropertyValue(propertyPath, queryResult)
                propertyValue.previousValue = propertyValue.currentValue

                //add to the map
                propertyPaths.put(propertyPath, [previous: propertyValue.currentValue, current: latestValue])
            }
        } catch (Exception e){
            log.error("[QUERY " + queryResult?.query?.id?:'NULL' + "] There was a problem reading the supplied JSON.", e)
        }

        propertyPaths
    }
    

    private def processQueryReturnedJson(Query query, String userId, String json) {
        if (!queryService.isUserSpecific(query)) return json

        JSONObject rslt = JSON.parse(json) as JSONObject

        // all the occurrences user has made annotations to
        if (rslt.occurrences) {
            // reconstruct occurrences so that only those records with specified annotations are put into the list
            JSONArray reconstructedOccurrences = []
            for (JSONObject occurrence : rslt.occurrences) {
                if (occurrence.uuid) {
                    // all the verified assertions of this occurrence record
                    def (openAssertions, verifiedAssertions, correctedAssertions) = filterAssertionsForAQuery(getAssertionsOfARecord(query.baseUrl, occurrence.uuid), userId)

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

    private JSONArray getAssertionsOfARecord(String baseUrl, String uuid) {
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
            def origUserAssertions = assertions.findAll { it.uuid && !it.relatedUuid && it.userId == userId}

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

    private def refreshProperties(QueryResult queryResult, json) {

        log.debug("[QUERY " + queryResult?.query?.id?:'NULL' + "] Refreshing properties for query: " + queryResult.query.name + " : " +  queryResult.frequency)

        try {

            queryResult.query.propertyPaths.each { propertyPath ->

                //read the value from the request
                def latestValue = null
                try {
                    latestValue = JsonPath.read(json, propertyPath.jsonPath)
                } catch (Exception e){
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

                propertyValue.save(true)
            }
        } catch (Exception e){
            log.error("[QUERY " + queryResult?.query?.id?:'NULL' + "] There was a problem reading the supplied JSON.",e)
        }
    }

    PropertyValue getPropertyValue(PropertyPath pp, QueryResult queryResult){
        PropertyValue pv = PropertyValue.findByPropertyPathAndQueryResult(pp, queryResult)
        if(pv == null){
            pv = new PropertyValue([propertyPath:pp, queryResult:queryResult])
        }
        pv
    }

    def checkQueryById(queryId, freqStr, uid = null) {
        log.debug("[QUERY " + queryId +"] Running query...")

        def checkedCount = 0
        def checkedAndUpdatedCount = 0
        def query = Query.get(queryId)
        def frequency = Frequency.findByName(freqStr)
        def user = (uid == null) ? null : User.findById(uid)

        // return a list of result. 'My Annotations' query is user specific
        List<QueryCheckResult> qcrs = checkStatusDontUpdate(query, frequency, user)

        checkedCount += qcrs.size()
        checkedAndUpdatedCount += qcrs.findAll {it.queryResult.hasChanged}.size()

        log.debug("Query checked: " + checkedCount + ", updated: " + checkedAndUpdatedCount)
        qcrs
    }

    // used in debug all alerts
    def checkAllQueries(PrintWriter writer) {
        //iterate through all queries
        def checkedCount = 0
        def checkedAndUpdatedCount = 0
        def frequency =  Frequency.findAll().first()
        log.debug("Starting the running of all queries.....")
        Query.list().each { query ->
            List<QueryCheckResult> qcrs = checkStatusDontUpdate(query, frequency)
            checkedCount += qcrs.size()
            checkedAndUpdatedCount += qcrs.findAll {it.queryResult.hasChanged}.size()

            if (!qcrs.isEmpty()) {
                writer.write(query.id + ": " + query.toString())
                // there could be multiple results for one user specific query
                for (QueryCheckResult qcr : qcrs) {
                    writer.write("\nUpdated (" + frequency.name + "):" + qcr.queryResult.hasChanged + (qcr.queryResult?.user != null ? (" for user " + qcr.queryResult?.user?.email + " id(" + qcr.queryResult?.user?.userId +")") : ""))
                    writer.write("\nTime taken: " + qcr.timeTaken / 1000 + ' secs \n')
                }
                writer.write(("-" * 80) + "\n")
                writer.flush()
            } else {
                writer.write(query.id + ": " + query.toString() + '\n')
                writer.write('No user subscribed to this query' + '\n')
                writer.write(("-" * 80) + "\n")
                writer.flush()
            }
        }
        log.debug("Queries checked: " + checkedCount + ", updated: " + checkedAndUpdatedCount)
    }

    def debugQueriesForUser(User user, PrintWriter writer){
        log.debug("Checking queries for user: " + user)
        def checkedCount = 0
        def checkedAndUpdatedCount = 0
        // queries for this user
        def queries = (List<Query>)Query.executeQuery(
                """select q from Query q
                  inner join q.notifications n
                  inner join n.user u
                  where u = :user
                  group by q""", [user: user])

        queries.each { query ->
            // since it's for a specific user, qcrs will only contain 1 element
            List<QueryCheckResult> qcrs = checkStatusDontUpdate(query, user.frequency, user)
            checkedCount += qcrs.size()
            checkedAndUpdatedCount += qcrs.findAll {it.queryResult.hasChanged}.size()

            // since it's for a specific user, we don't need to show user id, name, etc.
            writer.write(query.id +": " + query.toString())
            writer.write("\nUpdated (" + user.frequency.name+ "):" + qcrs[0].queryResult.hasChanged)
            writer.write("\nTime taken: " +  qcrs[0].timeTaken/1000 +' secs \n')
            writer.write(("-" * 80) + "\n")
            writer.flush()
        }
    }


    def checkQueryForFrequency(String frequencyName){
        log.debug("Checking frequency : " + frequencyName)
        Date now = new Date()
        Frequency frequency = Frequency.findByName(frequencyName)
        checkQueryForFrequency(frequency, true)
        //update the frequency last checked
        frequency = Frequency.findByName(frequencyName)
        if (frequency) {
            frequency.lastChecked = now
            frequency.save(flush:true)
        } else {
            log.warn "Frequency not found for ${frequencyName}"
        }
    }

    //select q.id, u.frequency from query q inner join notification n on n.query_id=q.id inner join user u on n.user_id=u.id;
    def checkQueryForFrequency(Frequency frequency, Boolean sendEmails){

        def queries = (List<Query>)Query.executeQuery(
                """select q from Query q
                  inner join q.notifications n
                  inner join n.user u
                  where u.frequency = :frequency
                  group by q""", [frequency: frequency])

        queries.each { query ->
            log.debug("Running query: " + query.name)
            def (changed, users) = checkStatus(query, frequency)
            if (changed && sendEmails) {
                log.debug("Query has been updated. Sending emails....")
                List<Map> recipients = []
                //send separate emails for now
                //if there is a change, generate an email list
                //send an email
                if (users == null) {
                    users = Query.executeQuery(
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
                } else {
                    recipients = users.collect { user ->
                        def notification = Notification.findByUserAndQuery(user, query)
                        [uid: user.id, email: user.email, userUnsubToken: user.unsubscribeToken, notificationUnsubToken: notification.unsubscribeToken]
                    }
                }
                log.debug("Sending emails to...." + recipients*.email.join(","))
                if(!recipients.isEmpty()){
                    emailService.sendGroupNotification(query, frequency, recipients)
                }
            }
        }
    }

    // get ids of all the users who subscribe to query + frequency
    def getAllUsersForAQuery(Query query, Frequency frequency) {
        Query.executeQuery(
            """select u
            from User u
            inner join u.notifications n
            where n.query = :query
            and u.frequency = :frequency
            and (u.locked is null or u.locked != 1)
            group by u""", [query: query, frequency: frequency])
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

        def queries = (List<Query>)Query.executeQuery(
                """select q from Query q
                  inner join q.notifications n
                  inner join n.user u
                  where u = :user
                  group by q""", [user: user])

        queries.each { query ->
            log.debug("Running query: " + query.name)
            def (hasUpdated) = checkStatus(query, user.frequency, user)
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
            notificationInstance.save(flush: true)
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
}
