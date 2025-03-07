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
import grails.converters.JSON
import org.grails.web.json.JSONObject

import java.util.zip.GZIPInputStream

/**
 * Service to compare JSON results stored in QueryResult
 */
class DiffService {
    def queryService
    def myAnnotationService
    def annotationsService
    def datasetService
    def imageService
    def dataResourceService

    /**
     * Todo:
     * Add preProcess and postProcess methods which can be overwritten by the specific services for data resources, e.g. Annotations, Images, etc.
     *
     * Create a new instance of the diff service based on email template.
     *
     * This is a workaround to allow the service to be created dynamically based on emailTemplate.
     * @param emailTemplate
     * @return
     */
     def createService(String emailTemplate) {
        if (!emailTemplate) {
            def parts = emailTemplate.split("/")
            if (parts.size() == 2) {
                def serviceName = parts[-1].capitalize() + "Service" // Convert 'annotations' -> 'AnnotationsService'
                def beanName = serviceName[0].toLowerCase() + serviceName.substring(1) // Convert to Grails bean naming

                if (grailsApplication.mainContext.containsBean(beanName)) {
                    return grailsApplication.mainContext.getBean(beanName)
                }
            }
        }
        return this
    }


    /**
     * Detects the changes in the records of a query result.
     *
     * This is the only method that should be called directly from external sources.
     *
     * Originally copied from EmailService
     * @param queryResult
     * @return
     */
    def diff(queryResult) {
        if  (queryResult.query?.recordJsonPath) {

            // return all of the new records if query is configured to fire on a non-zero value OR if previous value does not exist.
            if (queryService.firesWhenNotZero(queryResult.query)) {
                def records = getNewRecords(queryResult)
                //Some queries, like bioCache,has a pageSize, so it only returns a subset of the total records
                def jsonResult = JSON.parse( queryResult.decompress(queryResult.lastResult)) as JSONObject
                queryResult.totalRecords = jsonResult.totalRecords !=0 ? jsonResult.totalRecords: records.size()
                return records
            } else {
                return getNewRecordsFromDiff(queryResult)
            }
        } else {
            return []
        }
    }


    /**
     * The entry method for retrieving new records when the query URL contains Date parameters (also known as fireWhenNotZero: true).
     * @param queryResult
     * @return
     */
    def getNewRecords(QueryResult queryResult) {
        def records = []
        // decompress both and compare lists
        if (queryResult.query.recordJsonPath) {
            String last = decompressZipped(queryResult.lastResult)
            if (last) {
                records = JsonPath.read(last, queryResult.query.recordJsonPath)
            }
            if (queryService.isBiocacheImages(queryResult.query)) {
                records = imageService.postProcess(records)
            }
        }
        return records
    }

    /**
     * The entry API method for retrieving new records via full comparison
     * - for those the query URLs without Date parameters (also known as fireWhenNotZero: false).
     *
     * It returns the new or updated records by comparing the last and previous results in QueryResult.
     *
     * @param queryResult
     * @return
     */
    def getNewRecordsFromDiff(QueryResult queryResult) {

        def records = []
        String last = "{}"
        String previous = "{}"
        if (queryResult.lastResult != null ) {
            last = decompressZipped(queryResult.lastResult)
        }

        // If previous result is null, assign an empty Json object String
        if ( queryResult.previousResult != null) {
            previous = decompressZipped(queryResult.previousResult)
        }

        try {
            if (!last.startsWith("<") && !previous.startsWith("<")) {
                // Don't try and process 401, 301, 500, etc., responses that contain HTML
                if (queryService.isMyAnnotation(queryResult.query)) {
                    records = myAnnotationService.diff(queryResult)
                } else if (queryService.isDatasetQuery(queryResult.query)) {
                    records = datasetService.diff(queryResult)
                } else if (queryService.isDatasetResource(queryResult.query)) {
                    records = dataResourceService.diff(queryResult)
                } else if ( queryService.isBiocacheImages(queryResult.query)) {
                    records = imageService.postProcess(queryResult)
                } else {
                    records = findNewRecordsById(previous, last, queryResult.query.recordJsonPath, queryResult.query.idJsonPath)
                }
                queryResult.totalRecords = records.size()
            } else {
                log.warn "queryId: " + queryResult.query.id + ", queryResult:" + queryResult.id + " last or previous objects contains HTML and not JSON"
            }
        } catch (Exception ex) {
            log.error("Diff Exception: [${queryResult.query.id}, ${queryResult.query?.name}]: Runtime error: ${ex.getMessage()}")
        }

        return records
    }


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
     * todo: when fireWhenChange is true, it only compare the last 'ID', UUID, UID etc field of the previous and current result
     * The ID values may be same, but the rest of the records may have changed.
     *
     * @param queryResult
     * @return
     */
    @Deprecated
    Boolean hasChanged(QueryResult queryResult) {
        Boolean changed = false

        // if there is a fireWhenNotZero or fireWhenChange ignore  idJsonPath
        log.debug("[QUERY " + queryResult.query.id + "] Checking query: " + queryResult.query.name)

        // PropertyValues in a Biocache Query 'usually' has two properties: totalRecords and last_loaded_records (uuid)
        // Both have the possible null value
        // The following check is determined by the last propertyValue, since it overwrites the previous one

        queryResult.propertyValues.each { pv ->
            if (pv) {
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
                } else if (pv?.propertyPath.fireWhenChange) {
                    changed = pv.previousValue != pv.currentValue
                }
            }
        }

        // Example, in a blog/news query,fireWhenNotZero and fireWhenChange both are false

        if (queryService.checkChangeByDiff(queryResult.query)) {
            log.debug("[QUERY " + queryResult.query.id + "] Has change check. Checking JSON for query : " + queryResult.query.name)
            changed = hasChangedJsonDiff(queryResult)
        }

        return changed
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


    /**
     *  Find new records by compare the last and previous results in QueryResult
     * @param qs
     * @return
     */
    def findNewRecordsById(QueryResult qs) {
        String last = {}
        // If last result is null, assign an empty Json object
        if (qs.lastResult != null) {
            last = decompressZipped(qs.lastResult)
        }

        String previous = "{}"
        // If previous result is null, assign an empty Json object
        if ( qs.previousResult != null) {
            previous = decompressZipped(qs.previousResult)
        }

        def recordJsonPath = qs.query.recordJsonPath
        def idJsonPath = qs.query.idJsonPath

        return findNewRecordsById(previous, last, recordJsonPath, idJsonPath)
    }

    /**
     * General method to differentiate records by id
     * @param String previous
     * @param String last
     * @param String idJsonPath
     * @return a list of new records
     */
    def findNewRecordsById(String previous,String last, String recordJsonPath,String idJsonPath) {
        def records = []
        String fullRecordJsonPath = recordJsonPath + "." + idJsonPath
        List<String> ids1 = []
        List<String> ids2 = []
        try {
            ids1 = JsonPath.read(last, fullRecordJsonPath)
            ids2 = JsonPath.read(previous, fullRecordJsonPath)
        } catch (PathNotFoundException e){
            log.debug("it's not an error. Result doesn't contain any records since the returned json does not contain any 'recordJsonPath', if result is empty.")
        }

        // pull together the records that have been added
        List<String> diff = ids1.findAll { !ids2.contains(it) }

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