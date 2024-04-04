package au.org.ala.alerts

import groovy.json.JsonBuilder
import groovyjarjarpicocli.CommandLine

import javax.persistence.Column
import java.text.SimpleDateFormat

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

    String[] retrieveLogs() {
        return logs ? logs.split("\n") : []
    }

    void appendLogs(String log) {
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
        "Last checked: " + lastChecked
    }
}
