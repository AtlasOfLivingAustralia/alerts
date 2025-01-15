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

/**
 * A diff service for DataResource
 */
class DataResourceService {
    def diffService
    def grailsApplication
    def collectionService

    /**
     * Diff the new records by comparing the previous and current records in query result
     * @param qs
     * @return
     */
    def diff(QueryResult qs) {
        def records = diffService.findNewRecordsById(qs)
        String[] uids = records.collect { it.i18nCode?.substring(it.i18nCode.indexOf('.') + 1) }
        //Add uid to the record
        records.eachWithIndex { record, index ->
            record.uid = uids[index]
        }
        String baseURL = grailsApplication.config.collectoryService.baseURL
        String collectionUrl = baseURL + "/find/dataResource"

        def collectionMap = collectionService.findResourceByUids(collectionUrl, uids)

        records.each { record ->
            record.details = collectionMap.get(record.uid)
        }

        return records
    }
}

