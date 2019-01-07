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

class DiffService {

    static transactional = true

    Boolean hasChangedJsonDiff(QueryResult queryResult) {
        if (queryResult.lastResult != null && queryResult.previousResult != null) {
            String last = decompressZipped(queryResult.lastResult)
            String previous = decompressZipped(queryResult.previousResult)
            hasChangedJsonDiff(previous, last, queryResult.query)
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

        if (queryResult.lastResult != null && queryResult.previousResult != null) {
            try {
                //decompress both and compare lists
                String last = decompressZipped(queryResult.lastResult)
                String previous = decompressZipped(queryResult.previousResult)

                if(!last.startsWith("<") && !previous.startsWith("<")) {
                    // Don't try and process 401, 301, 500, etc., responses that contain HTML
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
                    log.warn "queryResult last or previous objects contains HTML and not JSON - ${last} || ${previous}"
                }
            } catch (Exception ex) {
                log.info "last = ${decompressZipped(queryResult.lastResult)}"
                log.info "previousResult = ${decompressZipped(queryResult.previousResult)}"
                log.info "JsonPath arg = ${queryResult.query.recordJsonPath + "." + queryResult.query.idJsonPath}"
                log.error "JsonPath error: ${ex}"
                log.info "JsonPath exception stacktrace.", ex
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
