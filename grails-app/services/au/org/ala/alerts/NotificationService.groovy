package au.org.ala.alerts
import com.jayway.jsonpath.JsonPath
import org.apache.commons.io.IOUtils

import java.text.SimpleDateFormat
import java.util.zip.GZIPOutputStream

class NotificationService {

    static transactional = true
    def emailService
    def diffService
    def queryService

    QueryResult getQueryResult(Query query, Frequency frequency){
        QueryResult qr = QueryResult.findByQueryAndFrequency(query, frequency)
        if(qr == null){
            qr = new QueryResult([query:query, frequency:frequency])
            qr.save(flush:true)
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
    boolean checkStatus(Query query, Frequency frequency) {

        QueryResult qr = getQueryResult(query, frequency)

        //def url = new URL("http://biocache.ala.org.au/ws/occurrences/search?q=*:*&pageSize=1")
        def urls = getQueryUrl(query, frequency)

        def urlString = urls.first()
        def urlStringForUI = urls.last()

        log.debug("[QUERY " + query.id + "] Querying URL: " + urlString)

        try {
            def json = IOUtils.toString(new URL(urlString).newReader())

            //update the stored properties
            refreshProperties(qr, json)

            qr = QueryResult.findById(qr.id)
            qr.previousCheck = qr.lastChecked

            //store the last result from the webservice call
            qr.previousResult = qr.lastResult
            qr.lastResult = gzipResult(json)
            qr.lastChecked = new Date()
            qr.hasChanged = hasChanged(qr)
            qr.queryUrlUsed = urlString
            qr.queryUrlUIUsed = urlStringForUI

            log.debug("[QUERY " + query.id + "] Has changed?: " + qr.hasChanged)
            if(qr.hasChanged){
                qr.lastChanged = new Date()
            }

            qr.save(true)
            qr.hasChanged
        } catch (Exception e){
            log.error("[QUERY " + query.id + "] There was a problem checking the URL: " + urlString, e)
        }
    }

    QueryCheckResult checkStatusDontUpdate(Query query, Frequency frequency) {

        //get the previous result
        QueryResult lastQueryResult = getQueryResult(query, frequency)
        QueryCheckResult qcr = new QueryCheckResult()

        //get the urls to query
        def urls = getQueryUrl(query, frequency)
        def urlString = urls.first()
        qcr.frequency = frequency
        qcr.urlChecked = urlString
        qcr.query = query
        qcr.queryResult = QueryResult.findByQueryAndFrequency(query, frequency)

        log.debug("[QUERY " + query.id + "] Querying URL: " + urlString)

        try {
            def json = IOUtils.toString(new URL(urlString).newReader())
            qcr.response = json

            //update the stored properties
            def propertyPaths = compareProperties(lastQueryResult, json)

            //decompress the last result
            def previousJson = diffService.decompressZipped(lastQueryResult.lastResult)

            //set the has changed
            qcr.queryResult.hasChanged = hasPropertiesChanged(query, propertyPaths, previousJson, json)

            //set the last result
            qcr.queryResult.lastResult = gzipResult(json)

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

    private String[] getQueryUrl(Query query, Frequency frequency){

        def queryURL = ""
        def queryURLForUI = ""

        //if there is a date format, then there's a param to replace
        if (query.dateFormat) {
            def dateToUse = org.apache.commons.lang.time.DateUtils.addSeconds(new Date(), -1 * frequency.periodInSeconds)
            //insert the date to query with
            SimpleDateFormat sdf = new SimpleDateFormat(query.dateFormat)
            def dateValue = sdf.format(dateToUse)
            queryURL = query.baseUrl + query.queryPath.replaceAll("___DATEPARAM___", dateValue)
            queryURLForUI = query.baseUrlForUI + query.queryPathForUI.replaceAll("___DATEPARAM___", dateValue)
        } else {
            queryURL = query.baseUrl + query.queryPath
            queryURLForUI = query.baseUrlForUI + query.queryPathForUI
        }

        [cleanUpUrl(queryURL), cleanUpUrl(queryURLForUI)]
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
        Boolean hasFireProperty = queryService.hasAFireProperty(queryResult.query)

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

        //if there isnt a 'fire-on' property, use json diff if configured
        if (!hasFireProperty && queryResult.query.idJsonPath){
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
        Boolean hasFireProperty = queryService.hasAFireProperty(query)

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

        //if there isnt a 'fire-on' property, use json diff if configured
        if (!hasFireProperty && query.idJsonPath){
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


    private def refreshProperties(QueryResult queryResult, json) {

        log.debug("[QUERY " + queryResult?.query?.id?:'NULL' + "] Refreshing properties for query: " + queryResult.query.name + " : " +  queryResult.frequency)

        try {

            queryResult.query.propertyPaths.each { propertyPath ->

                //read the value from the request
//        println(propertyPath.jsonPath)
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

    def checkQueryById(queryId, freqStr) {

        log.debug("[QUERY " + queryId +"] Running query...")

        def checkedCount = 0
        def checkedAndUpdatedCount = 0
        def query = Query.get(queryId)
        def frequency = Frequency.findByName(freqStr)

        long start = System.currentTimeMillis()
        QueryCheckResult qcr = checkStatusDontUpdate(query, frequency)
        long finish = System.currentTimeMillis()
        checkedCount++
        if (qcr.queryResult.hasChanged) {
            checkedAndUpdatedCount++
        }
        log.debug("Query checked: " + checkedCount + ", updated: " + checkedAndUpdatedCount)
        qcr.timeTaken = finish - start
        qcr
    }

    def checkAllQueries(PrintWriter writer) {
        //iterate through all queries
        def checkedCount = 0
        def checkedAndUpdatedCount = 0
        def frequency =  Frequency.findAll().first()
        log.debug("Starting the running of all queries.....")
        Query.list().each { query ->
            long start = System.currentTimeMillis()
            boolean hasUpdated = checkStatusDontUpdate(query, Frequency.findAll().first())
            long finish = System.currentTimeMillis()
            checkedCount++
            if (hasUpdated) {
                checkedAndUpdatedCount++
            }
            writer.write(query.id +": " + query.toString())
            writer.write("\nUpdated (" + frequency.name+ "):" + hasUpdated)
            writer.write("\nTime taken: " + (finish - start)/1000 +' secs \n')
            writer.write(("-" * 80) + "\n")
            writer.flush()
        }
        log.debug("Queries checked: " + checkedCount + ", updated: " + checkedAndUpdatedCount)
    }

    def debugQueriesForUser(User user, PrintWriter writer){
        log.debug("Checking queries for user: " + user)
        def checkedCount = 0
        def checkedAndUpdatedCount = 0
        def queries = Query.executeQuery(
                """select q from Query q
                  inner join q.notifications n
                  inner join n.user u
                  where u = :user
                  group by q""", [user: user])

        queries.each { query ->
            long start = System.currentTimeMillis()
            boolean hasUpdated = checkStatusDontUpdate(query, user.frequency)
            long finish = System.currentTimeMillis()
            checkedCount++
            if (hasUpdated) {
                checkedAndUpdatedCount++
            }
            writer.write(query.id +": " + query.toString())
            writer.write("\nUpdated (" + user.frequency.name+ "):" + hasUpdated)
            writer.write("\nTime taken: " + (finish - start)/1000 +' secs \n')
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

        def queries = Query.executeQuery(
                """select q from Query q
                  inner join q.notifications n
                  inner join n.user u
                  where u.frequency = :frequency
                  group by q""", [frequency: frequency])

        queries.each { query ->
            log.debug("Running query: " + query.name)
            boolean hasUpdated = checkStatus(query,frequency)
            if (hasUpdated && sendEmails) {
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
                  and u.locked is null
                  group by u""", [query: query, frequency: frequency])

                List<Map> recipients = users.collect { user ->
                    [email: user[0], userUnsubToken: user[1], notificationUnsubToken: user[2]]
                }
                log.debug("Sending emails to...." + recipients*.email.join(","))
                if(!users.isEmpty()){
                    emailService.sendGroupNotification(query, frequency, recipients)
                }
            }
        }
    }

    /**
     * Check the queries of a specific user.
     *
     * @param user
     * @param sendEmails
     * @return
     */
    def checkQueriesForUser(User user, Boolean sendEmails){

        log.debug("Checking queries for user: " + user)

        def queries = Query.executeQuery(
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
}
