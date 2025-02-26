
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
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import groovy.json.JsonOutput
import java.text.SimpleDateFormat

/**
 *   A diff service for annotations
 */
class AnnotationService {
    def httpService
    def sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")  // Adjust the pattern as needed

    /**
     * Append assertions to the occurrences
     * @param query
     * @param occurrences
     * @return
     */
    String appendAssertions(Query query, JSONObject occurrences) {
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

    /**
     * If fireNotZero property is true, this method will not be called
     *
     * This method compare the difference of Annotations between previous and last result.
     * for annotation query, it returns the records that have new annotations after the given DATE. So it does not require to call this method.

     * return a list of records that their annotations have been changed or deleted
     * @param String previous
     * @param String last
     * @param String  recordJsonPath
     * @return a list of records
     */
    Collection diff(String previous,String last,String recordJsonPath) {
        // uuid -> occurrence record map
        def oldRecordsMap = [:]
        def curRecordsMap = [:]
        try {
            oldRecordsMap = JsonPath.read(previous, recordJsonPath).collectEntries { [(it.uuid): it] }
        } catch (PathNotFoundException e) {
            log.warn("Previous result is empty or doesn't have any records containing a field ${recordJsonPath} defined in recordJsonPath")
        }

        try {
             curRecordsMap = JsonPath.read(last, recordJsonPath).collectEntries { [(it.uuid): it] }
        } catch (PathNotFoundException e){
            log.warn("Current result is empty or doesn't have any records containing a field ${recordJsonPath} defined in recordJsonPath")
        }
        // if an occurrence record doesn't exist in previous result (added) or has different open_assertions or verified_assertions or corrected_assertions than previous (changed).
        def records = curRecordsMap.findAll {
            def record = it.value
            def previousRecord = oldRecordsMap.get(record.uuid)
            if (previousRecord) {
                String currentAssertions = JsonOutput.toJson(record.user_assertions)
                String previousAssertions = JsonOutput.toJson(previousRecord.user_assertions)
                currentAssertions != previousAssertions
            } else {
                true
            }
        }.values()

        //Decision: #381 Do not included those records
        //if an occurrence record exists in previous result but not in current, it means the annotation is deleted.
        //We need to add these records as a 'modified' record
        //records.addAll(oldRecordsMap.findAll { !curRecordsMap.containsKey(it.value.uuid) }.values())

        return records
    }
}
