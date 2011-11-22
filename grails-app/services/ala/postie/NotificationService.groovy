package ala.postie

//@Grab(group = 'org.codehaus.groovy.modules.http-builder', module = 'http-builder', version = '0.5.1')
//import groovyx.net.http.*
//import static groovyx.net.http.Method.GET
//import static groovyx.net.http.ContentType.JSON
import org.apache.commons.io.IOUtils
import com.jayway.jsonpath.JsonPath
import java.text.SimpleDateFormat
import java.util.zip.GZIPOutputStream

import java.util.zip.GZIPInputStream

class NotificationService {

  static transactional = true
  def emailService
  def diffService

  def serviceMethod() {}

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
  def boolean checkStatus(Query query, Frequency frequency) {

    QueryResult qr = getQueryResult(query, frequency)

    //def url = new URL("http://biocache.ala.org.au/ws/occurrences/search?q=*:*&pageSize=1")
    def urlString = getQueryUrl(query, frequency)

    println("Querying URL: " + urlString)

    def json = IOUtils.toString(new URL(urlString).newReader())

    //update the stored properties
    refreshProperties(qr, json)

    qr.previousCheck = qr.lastChecked

    //store the last result from the webservice call
    qr.previousResult = qr.lastResult
    qr.lastResult = gzipResult(json)
    qr.lastChecked = new Date()
    qr.hasChanged = hasChanged(qr)

    println("Has changed? : " + qr.hasChanged)
    if(qr.hasChanged){
      qr.lastChanged = new Date()
    }

    qr.save(true)
    qr.hasChanged
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

  private String getQueryUrl(Query query, Frequency frequency){

    //if there is a date format, then there's a param to replace
    if (query.dateFormat) {
      def dateToUse = org.apache.commons.lang.time.DateUtils.addSeconds(new Date(), -1 * frequency.periodInSeconds)
      //insert the date to query with
      SimpleDateFormat sdf = new SimpleDateFormat(query.dateFormat)
      def dateValue = sdf.format(dateToUse)
      query.baseUrl + query.queryPath.replaceAll("___DATEPARAM___", dateValue)

    } else {

      query.baseUrl + query.queryPath
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
    queryResult.propertyValues.each { pv ->
      if (pv.propertyPath.fireWhenNotZero){
        changed = pv.currentValue.toInteger() > 0
      }
      else if (pv.propertyPath.fireWhenChange){
        changed = pv.previousValue != pv.currentValue
      }
      else if (queryResult.query.idJsonPath){
        changed = diffService.hasChangedJsonDiff(queryResult)
      }
    }
    changed
  }


  private def refreshProperties(QueryResult queryResult, json) {

    println("Refreshing properties for query: " + queryResult.query.name + " : " +  queryResult.frequency)

    try {

      queryResult.query.propertyPaths.each { propertyPath ->

        //read the value from the request
        def latestValue = JsonPath.read(json, propertyPath.jsonPath)

        //get property value for this property path
        PropertyValue propertyValue = getPropertyValue(propertyPath, queryResult)

        propertyValue.previousValue = propertyValue.currentValue

        if (latestValue instanceof List) {
          propertyValue.currentValue = latestValue.size().toString()
        } else {
          propertyValue.currentValue = latestValue
        }

        propertyValue.save(true)
      }
    } catch (Exception e){
      println("There was a problem reading the supplied JSON.")
      e.printStackTrace()
    }
  }

  PropertyValue getPropertyValue(PropertyPath pp, QueryResult queryResult){
    PropertyValue pv = PropertyValue.findByPropertyPathAndQueryResult(pp, queryResult)
    if(pv == null){
      pv = new PropertyValue([propertyPath:pp, queryResult:queryResult])
      //pv.save(flush:true)
    }
    pv
  }

  def checkAllQueries() {
    //iterate through all queries
    Query.list().each { query ->
      boolean hasUpdated = checkStatus(query)
      if (hasUpdated) {
        println("Query has been updated. Sending emails....")
        //send separate emails for now
        //if there is a change, generate an email list
        //send an email
        List<Notification> notifications = Notification.findAllByQuery(query)
        List<String> emailAddresses = new ArrayList<String>()
        notifications.each { n -> emailAddresses.add(n.user.email) }
        println("Sending emails to...." + emailAddresses.join(","))
        emailService.sendGroupNotification(query, emailAddresses)
      }
    }
  }

  def checkQueryForFrequency(String frequencyName){
    Date now = new Date()
    Frequency frequency = Frequency.findByName(frequencyName)
    checkQueryForFrequency(frequency, true)
    //update the frequency last checked
    frequency = Frequency.findByName(frequencyName)
    frequency.lastChecked = now
    frequency.save()
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
      boolean hasUpdated = checkStatus(query,frequency)
      if (hasUpdated && sendEmails) {
        println("Query has been updated. Sending emails....")
        //send separate emails for now
        //if there is a change, generate an email list
        //send an email

        def users = Query.executeQuery(
               """select u from User u
                  inner join u.notifications n
                  where n.query = :query
                  and u.frequency = :frequency
                  group by u""", [query: query, frequency: frequency])


        List<String> emailAddresses = new ArrayList<String>()
        users.each { user -> emailAddresses.add(user.email) }
        println("Sending emails to...." + emailAddresses.join(","))
        emailService.sendGroupNotification(query, frequency, emailAddresses)
      }
    }
  }
}
