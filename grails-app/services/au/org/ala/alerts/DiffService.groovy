/*
 * Copyright (C) 2017 Atlas of Living Australia
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

import com.jayway.jsonpath.JsonPath

import java.util.zip.GZIPInputStream
import grails.gorm.transactions.NotTransactional


class DiffService {
    def queryService

    Boolean hasChangedJsonDiff(QueryResult queryResult) {
        if (queryResult.lastResult != null) {
            if (queryResult.previousResult != null) {
                String last = decompressZipped(queryResult.lastResult)
                String previous = decompressZipped(queryResult.previousResult)
                hasChangedJsonDiff(previous, last, queryResult.query)
            } else {
                true
            }
        } else {
            false
        }
    }

    boolean isCollectionOrArray(object) {
        [Collection, Object[]].any { it.isAssignableFrom(object.getClass()) }
    }

    Boolean hasChangedJsonDiff(String previous, String current, Query query, Boolean debugDiff = false) {
        if (current != null && previous != null) {
            try {
                if (!queryService.isMyAnnotation(query)) {
                    def ids1 = JsonPath.read(current, query.recordJsonPath + "." + query.idJsonPath)
                    if (!isCollectionOrArray(ids1)) {
                        ids1 = [ids1]
                    }

                    def ids2 = JsonPath.read(previous, query.recordJsonPath + "." + query.idJsonPath)
                    if (!isCollectionOrArray(ids2)) {
                        ids2 = [ids2]
                    }
                    List<String> diff = ids1.findAll { !ids2.contains(it) }
                    if (debugDiff) {
                        log.info "checking json diff for: ${query.name}"
                        log.info "query URL: ${query.baseUrl}${query.queryPath}"
                        log.info "jsonpath = ${query.recordJsonPath}.${query.idJsonPath}"
                        log.info "ids1 = ${ids1}"
                        log.info "ids2 = ${ids2}"
                        log.info "diff = ${diff}"
                    }
                    !diff.empty
                } else {
                    // 'occurrences' field in json has been processed in NotificationService.processQueryReturnedJson
                    // so that it only contains records that have at least 1 50001/50002/50003 assertion
                    // and
                    // 1. the open assertion ids have been put into 'open_assertions'
                    // 2. the verified assertion ids have been put into 'verified_assertions'
                    // 3. the corrected assertion ids have been put into 'corrected_assertions'
                    //
                    //
                    // we compare previous and current record list.
                    // 1. if records number different that means records been added or deleted
                    // 2. if same records number, we compare them one by one. If 2 records at same position have different uuid,
                    //    there's a change. If 2 have same uuid but different 'open_assertions' or 'verified_assertions' or 'corrected_assertions'
                    //    that means assertions state have changed and should trigger an alert

                    def oldRecords = JsonPath.read(previous, query.recordJsonPath)
                    def curRecords = JsonPath.read(current, query.recordJsonPath)

                    // if not same number of records, there's a diff
                    if (oldRecords.size() != curRecords.size()) return true

                    oldRecords.sort { it.uuid }
                    curRecords.sort { it.uuid }

                    // compare records one by one
                    for (int i = 0; i < oldRecords.size(); i++) {
                        def oldRecord = oldRecords.get(i)
                        def curRecord = curRecords.get(i)

                        if (oldRecord.uuid != curRecord.uuid || oldRecord.open_assertions != curRecord.open_assertions ||
                                oldRecord.verified_assertions != curRecord.verified_assertions || oldRecord.corrected_assertions != curRecord.corrected_assertions) {
                            return true
                        }
                    }

                    false
                }
            } catch (Exception ex) {
                log.warn "JSONPath exception: ${ex} for query ${query.name} (id: ${query.id}) | URL: ${query.baseUrl}${query.queryPath}"
                log.info "JSONPath exception stacktrace: ", ex
                log.debug "Diff values: previous = ${previous} || last = ${current} || json path = ${query.recordJsonPath}.${query.idJsonPath}"
                false
            }
        } else {
            false
        }
    }

    def getNewRecords(QueryResult queryResult) {

        //decompress both and compare lists
        if (queryResult.query.recordJsonPath) {
            String last = decompressZipped(queryResult.lastResult)
            if (last) {
                JsonPath.read(last, queryResult.query.recordJsonPath)
            } else {
                []
            }
        } else {
            []
        }
    }

    def getNewRecordsFromDiff(QueryResult queryResult) {

        def records = []

        if (queryResult.lastResult != null ) {
            String last = decompressZipped(queryResult.lastResult)
            String previous = "{}"
            // If previous result is null, assign an empty Json object
            if ( queryResult.previousResult != null) {
                revious = decompressZipped(queryResult.previousResult)
            }

            try {
                if (!last.startsWith("<") && !previous.startsWith("<")) {
                    // Don't try and process 401, 301, 500, etc., responses that contain HTML
                    if (!queryService.isMyAnnotation(queryResult.query)) {
                        List<String> ids1 = JsonPath.read(last, queryResult.query.recordJsonPath + "." + queryResult.query.idJsonPath)
                        List<String> ids2 = JsonPath.read(previous, queryResult.query.recordJsonPath + "." + queryResult.query.idJsonPath)
                        List<String> diff = ids1.findAll { !ids2.contains(it) }
                        //pull together the records that have been added

                        def allRecords = JsonPath.read(last, queryResult.query.recordJsonPath)
                        allRecords.each { record ->
                            if (diff.contains(record.get(queryResult.query.idJsonPath))) {
                                records.add(record)
                            }
                        }
                    } else {
                        // for normal alerts, comparing occurrence uuid is enough to show the difference.
                        // for my annotation alerts, same occurrence record could exist in both result but have different assertions.
                        // so comparing occurrence uuid is not enough, we need to compare 50001/50002/50003 sections inside each occurrence record

                        // uuid -> occurrence record map
                        def oldRecordsMap = JsonPath.read(previous, queryResult.query.recordJsonPath).collectEntries { [(it.uuid): it] }
                        def curRecordsMap = JsonPath.read(last, queryResult.query.recordJsonPath).collectEntries { [(it.uuid): it] }

                        // if an occurrence record doesn't exist in previous result (added) or has different open_assertions or verified_assertions or corrected_assertions than previous (changed).
                        records = curRecordsMap.values().findAll {
                            !oldRecordsMap.containsKey(it.uuid) ||
                                    it.open_assertions != oldRecordsMap.get(it.uuid).open_assertions ||
                                    it.verified_assertions != oldRecordsMap.get(it.uuid).verified_assertions ||
                                    it.corrected_assertions != oldRecordsMap.get(it.uuid).corrected_assertions
                        }

                        // if an occurrence record exists in previous result but not in current (deleted).
                        records.addAll(oldRecordsMap.findAll { !curRecordsMap.containsKey(it.value.uuid) }.values())
                    }
                } else {
                    log.warn "queryId: " + queryResult.query.id + ", queryResult:" + queryResult.id + " last or previous objects contains HTML and not JSON"
                }
            } catch (Exception ex) {
                log.error "queryId: " + queryResult.query.id + ", JsonPath error: ${ex}"
            }
        }
        records
    }

    String decompressZipped(byte[] zipped) {
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
}
