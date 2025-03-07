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
 * A diff service for images
 */
class ImageService {
    def grailsApplication
    def collectionService

    /*
    * Collect the new updated images
    */
    def postProcess(def records) {
        def groupedByDataResource = records.groupBy { it.dataResourceUid }

        String[] drIds = records.collect { it.dataResourceUid }
        String baseURL = grailsApplication.config.collectoryService.baseURL
        String collectionUrl = baseURL + "/find/dataResource"
        def dataResourceInfos = collectionService.findResourceByUids(collectionUrl, drIds)
        drIds.each { id ->
            def dataResourceInfo = dataResourceInfos[id]
            if (dataResourceInfo) {
                groupedByDataResource[id].each { record ->
                    record["dataResourceInfo"] = [:]
                    record["dataResourceInfo"].lastUpdated = dataResourceInfo.lastUpdated
                    record["dataResourceInfo"].alaPublicUrl = dataResourceInfo.alaPublicUrl
                }
            }
        }
        return groupedByDataResource
    }
}

