/*
 *   Copyright (c) 2024.  Atlas of Living Australia
 *   All Rights Reserved.
 *   The contents of this file are subject to the Mozilla Public
 *   License Version 1.1 (the "License"); you may not use this file
 *   except in compliance with the License. You may obtain a copy of
 *   the License at http://www.mozilla.org/MPL/
 *   Software distributed under the License is distributed on an "AS
 *   IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 *   implied. See the License for the specific language governing
 *   rights and limitations under the License.
 *
 */

package au.org.ala.alerts

import grails.converters.JSON
import org.apache.commons.io.IOUtils
import groovy.json.JsonSlurper

/**
 * A HTTP Helper to make HTTP requests
 */
class HttpService {
    def grailsApplication

    /**
     * Post a JSON payload to a URL, and return status and response JSON
     *
     * @param url
     * @param jsonPayload
     * @return [Status, Json:JSONObject | error:String]
     */
    def post(String url, String jsonPayload) {
        URL apiUrl = new URL(url)
        HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection()
        try {
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
                return [status: 200, "json": new JsonSlurper().parseText(responseText)]
            } else {
                def responseText = connection.errorStream?.withReader('UTF-8') { it.text }
                return [status: responseCode, "error": responseText]
            }
        } catch (Exception e) {
            def error = "Failed to get json from web service (${url}). ${e.getClass()} ${e.getMessage()}, ${e}"
            log.error(error)
            return [status: 500, "error" : error]
        } finally {
            connection.disconnect()
        }
    }

    /**
     * Get JSON from a URL, and return status and response JSON
     * @param url
     * @return [Status, Json:JSONObject/JsonArray | error:String]
     */
    def getJson(String url) {
        log.debug "(internal) getJson URL = " + url
        def connection = new URL(url).openConnection()
        try {
            connection.setConnectTimeout(10000)
            connection.setReadTimeout(50000)
            connection.setRequestProperty('User-Agent', grailsApplication.config.getProperty("customUserAgent", "ALA-alerts"))

            int responseCode = connection.responseCode
            if (responseCode == 200) {
                def responseText = connection.inputStream.withReader('UTF-8') { it.text }
                def resp =[status: 200, "json" :  JSON.parse(responseText) ]
                return resp
            } else {
                def responseText = connection.errorStream?.withReader('UTF-8') { it.text }
                return [status: responseCode, "error" : responseText]
            }
        } catch (Exception e) {
            def error = "Failed to get json from web service (${url}). ${e.getClass()} ${e.getMessage()}, ${e}"
            log.error(error)
            return [status: 500, "error" : error]
        } finally {
            connection.disconnect()
        }
    }

    /**
     * Copied from NotifiationService to avoid circular dependency
     * @param url
     * @return
     */
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
