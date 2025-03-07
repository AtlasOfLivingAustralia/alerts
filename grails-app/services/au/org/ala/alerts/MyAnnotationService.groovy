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

/**
 *  A diff service for annotations
 */
class MyAnnotationService {
    def httpService

    def diff(QueryResult queryResult) {
        def records = []
        String last = "{}"
        String previous = "{}"
        if (queryResult.lastResult != null ) {
            last = queryResult.decompress(queryResult.lastResult)
        }

        // If previous result is null, assign an empty Json object String
        if ( queryResult.previousResult != null) {
            previous = queryResult.decompress(queryResult.previousResult)
        }

        def recordJsonPath = queryResult.query?.recordJsonPath

        records = diff(previous, last, recordJsonPath)

        return records
    }

    /**
     * Diff the new records by comparing the previous and current records in query result
     * @param String previous
     * @param String last
     * @param String recordJsonPath
     * @return a list of records that have been added or changed
     */
    def diff(previous, last, recordJsonPath) {
        // uuid -> occurrence record map
        def oldRecordsMap = [:]
        def curRecordsMap = [:]
        try {
            oldRecordsMap = JsonPath.read(previous, recordJsonPath).collectEntries { [(it.uuid): it] }
        }catch (Exception e){
            log.debug("Previous result is empty or doesn't have any records containing a field ${recordJsonPath} defined in recordJsonPath")
        }
        try {
            curRecordsMap = JsonPath.read(last, recordJsonPath).collectEntries { [(it.uuid): it] }
        }catch (Exception e){
            log.debug("Current result is empty or doesn't have any records containing a field ${recordJsonPath} defined in recordJsonPath")
        }
        // if an occurrence record doesn't exist in previous result (added) or has different verified_assertions than previous (changed).

        // NOTE: values of a linkedHashMap is immutable, so we need to create a new list if we will add new records after
        Collection records = curRecordsMap.findAll {
            def record = it.value
            // if the record has verified assertions and it is a list - some legacy records may have a String instead of a list
            if (!record.verified_assertions?.isEmpty() && record.verified_assertions instanceof List) {
                if (oldRecordsMap.containsKey(record.uuid)) {
                    if (oldRecordsMap.get(record.uuid).verified_assertions?.isEmpty()) {
                        return true
                    } else {
                        return record.verified_assertions.collect { it.uuid }.join(',') != oldRecordsMap.get(record.uuid).verified_assertions.collect { it.uuid }.join(',')
                    }
                } else {
                    return true
                }
            } else {
                //No verified assertions in current records
                return false
            }

        }.values()

        def mutableRecords = new ArrayList(records)

//         // Decision: #381 Do not included those records which their annotation was deleted
//        // make a copy of the records list to make it mutable
//        //if an occurrence record exists in previous result but not in current, it means the annotation is deleted.
//        //We need to add these records as a 'modified' record
//        //mutableRecords.addAll(oldRecordsMap.findAll { !curRecordsMap.containsKey(it.value.uuid) }.values())

        return mutableRecords
    }

    /**
     * Append assertions to the occurrences
     * @param query
     * @param occurrences
     * @return
     */
    String preProcess(Query query, JSONObject occurrences) {
        String baseUrl = query.baseUrl
        // get the user id from the query path
        // NOTE: oder of the query path is important
        String userId = query.queryPath.substring(query.queryPath.indexOf('assertion_user_id:') + 'assertion_user_id:'.length(), query.queryPath.indexOf('&dir=desc'))

        if(occurrences.occurrences) {
            // reconstruct occurrences so that only those records with specified annotations are put into the list
            JSONArray reconstructedOccurrences = []
            for (JSONObject occurrence : occurrences.occurrences) {
                if (occurrence.uuid) {
                    // all the verified assertions of this occurrence record
                    String assertionUrl = baseUrl + '/occurrences/' + occurrence.uuid + '/assertions'
                    def assertionsData = httpService.getJson(assertionUrl)
                    if (assertionsData.status == 200) {
                        JSONArray assertions = assertionsData.json as JSONArray
                        occurrence.put('user_assertions', assertions)

                        def verifiedAssertions = findVerifiedAssertions(assertions, userId)
                        // only include record has at least 1 (50001/50002/50003) assertions that VERIFY the user's assertions
                        // They will be used for diffService (records that will be included in alert email)
                        if ( !verifiedAssertions.isEmpty()) {
                            occurrence.put('verified_assertions', verifiedAssertions)
                            reconstructedOccurrences.push(occurrence)
                        }
                    }
                }
            }
            reconstructedOccurrences.sort { it.uuid }

            // reconstruct occurrences which will be used to retrieve diff (records that will be included in alert email)
            occurrences.put('occurrences', reconstructedOccurrences)
        }

        return occurrences.toString()
    }

    /**
     * Search the assertions which the USER has made
     * return assertions which verify or comment the user's assertions
     * @param assertions
     * @param userId
     * @return
     */
    private static def findVerifiedAssertions(JSONArray assertions, String userId) {
        Map verificationStatus = [50001: 'Unresolved issue, recognised by data custodian',
                                  50002: 'Record has been verified by data custodian as being correct',
                                  50003: 'Corrected via data refresh']
        def sortedAssertions = []
        if (assertions) {
            // all the original user assertions (issues users flagged)
            def origUserAssertions = assertions.findAll { it.uuid && !it.relatedUuid && it.userId == userId }
            // Find assertions which commented on the original user assertions
            def myOriginalUuids = origUserAssertions*.uuid
            def verifiedAssertions = myOriginalUuids.collectMany { uuid ->
                assertions.findAll { it.relatedUuid == uuid && ( it.qaStatus == 50001 || it.qaStatus == 50002 || it.qaStatus == 50003) }
            }.collect { assertion ->
                assertion.status = verificationStatus[assertion.qaStatus]
                return assertion
            }

            try {
                sortedAssertions = verifiedAssertions.sort { a, b ->
                    Date.parse("yyyy-MM-dd'T'HH:mm:ssX", b.created) <=> Date.parse("yyyy-MM-dd'T'HH:mm:ssX", a.created)
                }
            } catch (Exception e) {
                sortedAssertions = verifiedAssertions
            }
        }
        return  sortedAssertions
    }
}
