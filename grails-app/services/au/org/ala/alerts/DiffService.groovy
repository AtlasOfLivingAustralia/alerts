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
import com.jayway.jsonpath.PathNotFoundException

import java.util.zip.GZIPInputStream

/**
 * Service to compare JSON results stored in QueryResult
 */
class DiffService {
    def queryService
    def myAnnotationService
    def annotationService
    def datasetService

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

    /**
     * Indicates if the result of a query has changed by checking its properties.
     *
     * @param queryResult
     * @return
     */

    Boolean hasChanged(QueryResult queryResult) {
        Boolean changed = false

        //if there is a fireWhenNotZero or fireWhenChange ignore  idJsonPath
        log.debug("[QUERY " + queryResult.query.id + "] Checking query: " + queryResult.query.name)

        // PropertyValues in a Biocache Query 'usually' has two properties: totalRecords and last_loaded_records (uuid)
        //Both have the possible null value
        //The following check is determined by the last propertyValue, since it overwrites the previous one

        queryResult.propertyValues.each { pv ->
            log.debug("[QUERY " + queryResult.query.id + "] " +
                    " Has changed check:" + pv.propertyPath.name
                    + ", value:" + pv.currentValue
                    + ", previous:" + pv.previousValue
                    + ", fireWhenNotZero:" + pv.propertyPath.fireWhenNotZero
                    + ", fireWhenChange:" + pv.propertyPath.fireWhenChange
            )

            // Two different types of queries: Biocache and Blog/News
            // Biocache: totalRecords and last_loaded_records
            // Blog/News: last_blog_id
            if (pv.propertyPath.fireWhenNotZero) {
                changed = pv.currentValue?.toInteger() ?: 0 > 0
            } else if (pv.propertyPath.fireWhenChange) {
                changed = pv.previousValue != pv.currentValue
            }
        }

        //Example, in a blog/news query,fireWhenNotZero and fireWhenChange both are false

        if (queryService.checkChangeByDiff(queryResult.query)) {
            log.debug("[QUERY " + queryResult.query.id + "] Has change check. Checking JSON for query : " + queryResult.query.name)
            changed = hasChangedJsonDiff(queryResult)
        }

        changed
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
                previous = decompressZipped(queryResult.previousResult)
            }

            try {
                if (!last.startsWith("<") && !previous.startsWith("<")) {
                    // Don't try and process 401, 301, 500, etc., responses that contain HTML
                    if (queryService.isMyAnnotation(queryResult.query)) {
                        // for normal alerts, comparing occurrence uuid is enough to show the difference.
                        // for my annotation alerts, same occurrence record could exist in both result but have different assertions.
                        // so comparing occurrence uuid is not enough, we need to compare 50001/50002/50003 sections inside each occurrence record
                        records = myAnnotationService.diff(previous, last, queryResult.query.recordJsonPath)
                    } else if (queryService.isAnnotation(queryResult.query)) {
                        records = annotationService.diff(previous, last, queryResult.query.recordJsonPath)
                    } else if (queryService.isDatasetQuery(queryResult.query)) {
                        records = datasetService.diff(queryResult)
                    } else {
                        records = differentiateRecordsById(previous, last, queryResult.query.recordJsonPath, queryResult.query.idJsonPath)
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

    /**
     * General method to differentiate records by id
     * @param previous
     * @param last
     * @param idJsonPath
     * @return
     */
    def differentiateRecordsById(previous, last, recordJsonPath, idJsonPath) {
        def records = []
        String fullRecordJsonPath = recordJsonPath + "." + idJsonPath
        List<String> ids1 = []
        List<String> ids2 = []
        try {
            ids1 = JsonPath.read(last, fullRecordJsonPath)
            ids2 = JsonPath.read(previous, fullRecordJsonPath)
        }catch (PathNotFoundException e){
            log.info("it's not an error. Result doesn't contain any records since the returned json does not contain any 'recordJsonPath', if result is empty.")
        }

        List<String> diff = ids1.findAll { !ids2.contains(it) }
        //pull together the records that have been added

        def allRecords = JsonPath.read(last, recordJsonPath)
        allRecords.each { record ->
            if (diff.contains(record.get(idJsonPath))) {
                records.add(record)
            }
        }
        return records
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
