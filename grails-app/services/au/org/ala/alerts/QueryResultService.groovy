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

import javax.transaction.Transactional

/**
 * Database service for QueryResult:
 */
class QueryResultService {
    def queryService
    /**
     * Get QueryResult by id, including cascading objects: query and frequency
     *
     * @param id
     * @return QueryResult
     */
    def get(id){
        QueryResult qs = QueryResult.get(id)
        if (qs) {
            Query query = queryService.get(qs.query.id)
            qs.query = query

            Frequency frequency = Frequency.get(qs.frequency.id)
            qs.frequency = frequency

            PropertyValue pvs = PropertyValue.findByQueryResult(qs)
            qs.propertyValues = [pvs]
        }
        return qs
    }

    /**
     * Reset the QueryResult
     * @param id
     */
    def reset(id){
        QueryResult qs = QueryResult.get(id)
        if (qs) {
            qs.previousResult = null
            qs.lastResult = null
            qs.hasChanged = false
            qs.lastChecked = qs.lastChanged = null
            QueryResult.withTransaction {
                qs.save(flush:true)
            }
        }
    }
}
