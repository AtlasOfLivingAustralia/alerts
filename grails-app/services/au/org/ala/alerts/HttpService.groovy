package au.org.ala.alerts

import org.apache.commons.io.IOUtils
import groovy.json.JsonSlurper

class HttpService {

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
        int responseCode = connection.responseCode
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

    def get(String url) {
        log.debug "(internal) getJson URL = " + url
        def conn = new URL(url).openConnection()
        try {
            conn.setConnectTimeout(10000)
            conn.setReadTimeout(50000)
            conn.setRequestProperty('User-Agent', grailsApplication.config.getProperty("customUserAgent", "ALA-alerts"))
            return IOUtils.toString(conn.getInputStream(), "UTF-8")
        } catch (Exception e) {
            def error = "Failed to get json from web service (${url}). ${e.getClass()} ${e.getMessage()}, ${e}"
            log.error error
            return ""
        }
    }

}
