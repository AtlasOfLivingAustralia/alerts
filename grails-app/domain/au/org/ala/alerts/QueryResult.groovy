package au.org.ala.alerts

import com.jayway.jsonpath.JsonPath

import java.text.SimpleDateFormat
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

class QueryResult {

    Query query
    Frequency frequency
    Date lastChecked          // timestamp of last check
    Date previousCheck        // timestamp of previous check
    String queryUrlUsed       //query URL used
    String queryUrlUIUsed       //query URL used
    Boolean hasChanged = false
    Date lastChanged
    byte[] lastResult
    byte[] previousResult
    String logs
    transient boolean succeed = true

    String[] getLog() {
        return logs ? logs.split("\n") : []
    }

    void newLog(String log) {
        logs = log
    }

    void addLog(String log) {
        logs = logs ? logs + "\n" + log : log
    }


    String getQueryUrlUIUsed() {
        if (!queryUrlUIUsed) {
            Date since = lastChecked ?: (previousCheck ?: (lastChanged ?: new Date()))
            SimpleDateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
            utcFormat.setTimeZone(TimeZone.getTimeZone("UTC")); // Set the UTC timezone
            String formattedUtcDate = utcFormat.format(since)

            String queryPath = query.queryPathForUI
            String modifiedPath = queryPath.replaceAll('___DATEPARAM___', formattedUtcDate).replaceAll('___LASTYEARPARAM___', formattedUtcDate)
            String currentBiocacheUrl = query.baseUrlForUI + modifiedPath

            currentBiocacheUrl
        } else {
            queryUrlUIUsed
        }
    }

    static hasMany = [propertyValues: PropertyValue]

    static constraints = {
        lastResult nullable: true //, minSize:0, maxSize:200000
        previousResult nullable: true //, minSize:0, maxSize:200000
        previousCheck nullable: true
        lastChanged nullable: true
        lastChecked nullable: true
        queryUrlUsed nullable: true
        queryUrlUIUsed nullable: true
        logs nullable: true
    }

    static mapping = {
        propertyValues cascade: 'all-delete-orphan'

        lastResult sqlType: 'longblob' //,  minSize:0, maxSize: 200000
        previousResult sqlType: 'longblob' //,  minSize:0, maxSize: 200000
        queryUrlUsed sqlType: 'text'
        queryUrlUIUsed sqlType: 'text'
        logs sqlType: 'text' , defaultValue: ""
    }

    String toString() {
        "${frequency?.name}: ${lastChanged ? 'Changed' : 'No Change'} on ${lastChecked}"
    }

    Map brief() {
        [queryId: query?.id, query: query?.name, frequency: frequency?.name, queryResultId: id, lastChecked: lastChecked, hasChanged: hasChanged, lastChanged: lastChanged,
         property: displayProperties()
        ]
    }

    Map details() {
        [queryId: query?.id, query: query?.name, frequency: frequency?.name, queryResultId: id, lastChecked: lastChecked, hasChanged: hasChanged, lastChanged: lastChanged,
         property: displayProperties(),
         currentResult: lastResult ? decompress(lastResult) : null, previousResult: previousResult ? decompress(previousResult) : null
        ]
    }

    private def displayProperties() {
        def propertyPaths = []
        query?.propertyPaths.each { propertyPath ->
            propertyPaths.add(propertyPath.toString())
        }

        def propertyValue = []
        propertyValues.each { pv ->
            propertyValue.add(pv.toString())
        }

        ["Properties in Query", propertyPaths, "Property Values in Query Result", propertyValue]
    }

    String decompress(byte[] zipped) {
        if (zipped) {
            GZIPInputStream input = new GZIPInputStream(new ByteArrayInputStream(zipped))
            StringBuffer sb = new StringBuffer()
            Reader decoder = new InputStreamReader(input, "UTF-8");
            BufferedReader buffered = new BufferedReader(decoder);
            try {
                def currentLine = buffered.readLine()
                while (currentLine != null) {
                    sb.append(currentLine)
                    currentLine = buffered.readLine()
                }
            } catch (Exception e) {
                log.error(e.getMessage() + ", zipped content length " + zipped.length, e)
            }
            buffered.close()
            sb.toString()
        } else {
            null
        }
    }

    byte[] compress(String json) {
        //store the last result from the webservice call
        ByteArrayOutputStream bout = new ByteArrayOutputStream()
        GZIPOutputStream gzout = new GZIPOutputStream(bout)
        gzout.write(json.toString().getBytes())
        gzout.flush()
        gzout.finish()
        bout.toByteArray()
    }
}
