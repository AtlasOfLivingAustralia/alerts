
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

import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.PathNotFoundException
import org.apache.commons.lang3.builder.Diff
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import groovy.json.JsonOutput
import java.text.SimpleDateFormat

/**
 *   A diff service for annotations
 */
class AnnotationsService {
    def httpService
    def sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")  // Adjust the pattern as needed

    /**
     * Append assertions to the occurrences
     * @param query
     * @param occurrences
     * @return
     */
    String preProcess(Query query, JSONObject occurrences) {
        String baseUrl = query.baseUrl

        if (occurrences.occurrences) {
            // reconstruct occurrences so that only those records with specified annotations are put into the list
            JSONArray reconstructedOccurrences = []
            for (JSONObject occurrence : occurrences.occurrences) {
                if (occurrence.uuid) {
                    // all the verified assertions of this occurrence record
                    String assertionUrl = baseUrl + '/occurrences/' + occurrence.uuid + '/assertions'
                    def assertionResp = httpService.getJson(assertionUrl)
                    if (assertionResp.status == 200) {
                        def assertions = assertionResp.json
                        def sortedAssertions = assertions.sort { a, b ->
                            sdf.parse(b.created) <=> sdf.parse(a.created)
                        }
                        occurrence.put('user_assertions', sortedAssertions)
                    }
                    reconstructedOccurrences.push(occurrence)
                }
            }
            reconstructedOccurrences.sort { it.uuid }

            // reconstruct occurrences which will be used to retrieve diff (records that will be included in alert email)
            occurrences.put('occurrences', reconstructedOccurrences)
        }

        return occurrences.toString()
    }
}
