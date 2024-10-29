package au.org.ala.alerts
import groovy.json.JsonOutput
import groovy.json.JsonSlurper


class DatasetService {
    def diffService
    def httpService

    def diff(QueryResult qs) {
        def records = diffService.findNewRecordsById(qs)
        def uids = records.collect { it.uid }

        String collectionUrl = qs.query.baseUrl + "/find/dataResource"
        String collectionJsonPayload = JsonOutput.toJson(uids)

        def jsonSlurper = new JsonSlurper()
        def collectionResp = httpService.post(collectionUrl, collectionJsonPayload)
        if (collectionResp.status == 200) {

            def collectionJson = collectionResp.data
            def collectionMap = collectionJson.inject([:]) { map, item ->
                // Collection server is supposed to return a JSON array of collection objects with a uid field
                // but it returns a JSON array of String instead
                // So we need to parse the JSON string to get the uid
                if (item instanceof String) {
                    item = jsonSlurper.parseText(item)
                }

                map.put(item.uid, item)
                return map
            }
            records.each { record ->
                record.details = collectionMap.get(record.uid)
            }
        }
        return records
    }



}

