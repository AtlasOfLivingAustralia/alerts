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

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

class CollectionService {

    def httpService

    def findResourceByUids(String dataResourceURL, String[] uids) {
        def collectionMap = [:]
        String collectionJsonPayload = JsonOutput.toJson(uids)
        def jsonSlurper = new JsonSlurper()
        def collectionResp = httpService.post(dataResourceURL, collectionJsonPayload)
        if (collectionResp.status == 200) {
            def collectionJson = collectionResp.data
            collectionMap = collectionJson.inject([:]) { map, item ->
                // Collection server is supposed to return a JSON array of collection objects with a uid field
                // but it returns a JSON array of String instead
                // So we need to parse the JSON string to get the uid
                if (item instanceof String) {
                    item = jsonSlurper.parseText(item)
                }

                map.put(item.uid, item)
                return map
            }
        }

        return collectionMap
    }
}
