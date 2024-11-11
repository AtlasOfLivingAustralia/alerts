package au.org.ala.alerts

import grails.converters.JSON
import org.apache.commons.io.IOUtils
import groovy.json.JsonSlurper
import org.grails.web.json.JSONObject

class HttpService {
    def grailsApplication

    def post(String url, String jsonPayload) {
        URL apiUrl = new URL(url)
        HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection()
        connection.requestMethod = 'POST'
        connection.doOutput = true
        connection.setRequestProperty('Content-Type', 'application/json')
        connection.setRequestProperty('Accept', 'application/json')

        // Write data to the request body
        connection.outputStream.withWriter('UTF-8') { writer ->
            writer.write(jsonPayload)
        }

        // Read the response
        int responseCode = connection.getResponseCode()
        def resp = [:]
        if (responseCode == 200) {
            def responseText = connection.inputStream.withReader('UTF-8') { it.text }
            resp =[status: 200, "data" : new JsonSlurper().parseText(responseText)]
        } else {
            def responseText = connection.errorStream?.withReader('UTF-8') { it.text }
            resp = [status: responseCode, "error" : responseText]
        }

        connection.disconnect()
        return resp
    }

    /**
     * @param url
     * @return [Status, Json:JSONObject | error:String]
     */
    def getJson(String url) {
        log.debug "(internal) getJson URL = " + url
        def connection = new URL(url).openConnection()
        try {
            connection.setConnectTimeout(10000)
            connection.setReadTimeout(50000)
            connection.setRequestProperty('User-Agent', grailsApplication.config.getProperty("customUserAgent", "ALA-alerts"))

            int responseCode = connection.responseCode
            def responseText = ""
            def resp = [:]
            if (responseCode == 200) {
                responseText = connection.inputStream.withReader('UTF-8') { it.text }
            } else {
                responseText = connection.errorStream?.withReader('UTF-8') { it.text }
                return [status: responseCode, "error" : responseText]
            }
            connection.disconnect()

            resp =[status: 200, "json" :  JSON.parse(responseText) as JSONObject]
            return resp
        } catch (Exception e) {
            def error = "Failed to get json from web service (${url}). ${e.getClass()} ${e.getMessage()}, ${e}"
            log.error(error)
            return [status: 500, "error" : error]
        }
    }

    def get(String url) {
        log.debug "(internal) get URL = " + url
        def conn = new URL(url).openConnection()
        try {
            conn.setConnectTimeout(10000)
            conn.setReadTimeout(50000)
            conn.setRequestProperty('User-Agent', grailsApplication.config.getProperty("customUserAgent", "ALA-alerts"))
            return IOUtils.toString(conn.getInputStream(), "UTF-8")
        } catch (Exception e) {
            def error = "Http get calle failed: (${url}). ${e.getClass()} ${e.getMessage()}, ${e}"
            log.error(error)
            return ""
        }
    }

}
