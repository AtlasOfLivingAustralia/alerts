package ala.postie

//@Grab(group = 'org.codehaus.groovy.modules.http-builder', module = 'http-builder', version = '0.5.1')
//import groovyx.net.http.*
//import static groovyx.net.http.Method.GET
//import static groovyx.net.http.ContentType.JSON
import org.apache.commons.io.IOUtils
import com.jayway.jsonpath.JsonPath
import java.text.SimpleDateFormat
import java.util.zip.GZIPOutputStream
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import java.util.zip.GZIPInputStream

class NotificationService {

  static transactional = true
  def emailService

  def serviceMethod() {}

  def boolean checkStatus(Query query) {

    //boolean hasUpdated = false
    def now = new Date()

    //def url = new URL("http://biocache.ala.org.au/ws/occurrences/search?q=*:*&pageSize=1")
    def urlString = getQueryUrl(query)

    println("Querying URL: " + urlString)

    def json = IOUtils.toString(new URL(urlString).newReader())

    //update the stored properties
    refreshProperties(query, json)

    query.previousCheck = query.lastChecked
    query.lastChecked = now

    //store the last result from the webservice call
    ByteArrayOutputStream bout = new ByteArrayOutputStream()
    GZIPOutputStream gzout = new java.util.zip.GZIPOutputStream(bout)
    gzout.write(json.toString().getBytes())
    gzout.flush()
    gzout.finish()
    query.previousResult = query.lastResult
    query.lastResult = bout.toByteArray()

    println("Last result size: " + query.lastResult.length)

    if(query.hasErrors()){
      query.errors.allErrors.each { println it }

    }

    //check the derived properties
    boolean hasChangedValue = hasChanged(query)
    println("Has changed? : " + hasChangedValue)
    if(hasChangedValue){
      query.lastChanged = now
    }

    query.save(true)
    hasChangedValue
  }

  private String getQueryUrl(Query query){

    //if there is a date format, then there's a param to replace
    if (query.dateFormat) {

      //if empty, set to 5 minutes ago, so we can catch latest things
      if (!query.lastChecked) {
        query.lastChecked = org.apache.commons.lang.time.DateUtils.addMinutes(new Date(), -5)
      }

      SimpleDateFormat sdf = new SimpleDateFormat(query.dateFormat)
      //sdf.setTimeZone(TimeZone.getTimeZone(ConfigurationHolder.config.postie.timezone))

      //insert the date to query with
      def dateValue = sdf.format(query.lastChecked)
      query.baseUrl + query.queryPath.replaceAll("___DATEPARAM___", dateValue)

    } else {

      query.baseUrl + query.queryPath
    }
  }

  private Boolean hasChanged(Query query) {
    Boolean changed = false
    query.propertyValues.each { pv ->
      if (pv.fireWhenNotZero && pv.currentValue.toInteger() > 0) changed = true
      else if (pv.fireWhenChange && pv.previousValue != pv.currentValue) changed = true
      else if (query.idJsonPath){
        //if an id json path supplied, do a comparison of previous and current
        if(query.lastResult != null && query.previousResult != null){
          //decompress both and compare lists
          String last = decompressZipped(query.lastResult)
          String previous = decompressZipped(query.previousResult)

          //println(JsonPath.read(last, query.idJsonPath))
          //println(JsonPath.read(previous, query.idJsonPath))
          List<String> ids1 = JsonPath.read(last,  query.idJsonPath)
          List<String> ids2 = JsonPath.read(previous,  query.idJsonPath)

          List<String> diff = ids1.findAll { !ids2.contains(it) }

          diff.each { println it }

          println("has the layer list changed: " + !diff.empty)
          changed = !diff.empty
        }
      }
    }
    changed
  }


  public static String decompressZipped(byte[] zipped){
    GZIPInputStream input = new GZIPInputStream(new ByteArrayInputStream(zipped))
    StringBuffer sb = new StringBuffer()
    List<String> readed = null

    try {
      while (input.available() && !(readed = input.readLines()).isEmpty()) {
        //println(readed.join(""))
        sb.append(readed.join(""))
      }
    } catch (Exception e) {
      //e.printStackTrace()
    }
    input.close()
    sb.toString()
  }


  private def refreshProperties(Query query, json) {
    query.propertyValues.each { propertyValue ->

      //read the value from the request
      def latestValue = JsonPath.read(json, propertyValue.jsonPath)
      propertyValue.previousValue = propertyValue.currentValue

      if (latestValue instanceof List) {
        propertyValue.currentValue = latestValue.size().toString()
      } else {
        propertyValue.currentValue = latestValue
      }

      propertyValue.save(true)
    }
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
        notifications.each { n -> emailAddresses.add(n.userEmail) }
        println("Sending emails to...." + emailAddresses.join(","))
        emailService.sendGroupNotification(query, emailAddresses)
      }
    }
  }
}
