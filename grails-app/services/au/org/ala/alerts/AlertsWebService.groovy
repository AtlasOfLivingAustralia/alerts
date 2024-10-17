/*
 * Copyright (C) 2021 Atlas of Living Australia
 * All Rights Reserved.
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 */

package au.org.ala.alerts

import grails.converters.JSON
import org.grails.web.json.JSONElement
import org.springframework.web.client.RestClientException

class AlertsWebService {
    /**
     * Perform HTTP GET on a JSON web service
     *
     * @param url
     * @return the object we request or an JSON object containing error info in case of error
     */
    JSONElement getJsonElements(String url, String apiKey = null) {
        log.debug "(internal) getJson URL = " + url
        def conn = new URL(url).openConnection()
        try {
            conn.setConnectTimeout(10000)
            conn.setReadTimeout(50000)
            conn.setRequestProperty('User-Agent', grailsApplication.config.getProperty("customUserAgent", "ALA-alerts"))

            if (apiKey != null) {
                conn.setRequestProperty('apiKey', apiKey)
            }

            InputStream stream = null;
            if (conn instanceof HttpURLConnection) {
                conn.getResponseCode() // this line required to trigger parsing of response
                stream = conn.getErrorStream() ?: conn.getInputStream()
            } else { // when read local files it's a FileURLConnection which doesn't have getErrorStream
                stream = conn.getInputStream()
            }
            return JSON.parse(stream, "UTF-8")
        } catch (Exception e) {
            def error = "Failed to get json from web service (${url}). ${e.getClass()} ${e.getMessage()}, ${e}"
            log.error error
            throw new RestClientException(error, e)
        }
    }
}
