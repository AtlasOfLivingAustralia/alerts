package au.org.ala.alerts
import groovy.json.JsonOutput
import groovy.json.JsonSlurper


class DatasetService {
    def diffService
    def httpService
    def grailsApplication

    def diff(QueryResult qs) {
        def records = []
        String last = {}
        // If last result is null, assign an empty Json object
        if (qs.lastResult != null) {
            last = diffService.decompressZipped(qs.lastResult)
        }

        String previous = "{}"
        // If previous result is null, assign an empty Json object
        if ( qs.previousResult != null) {
            previous = diffService.decompressZipped(qs.previousResult)
        }

        def recordJsonPath = qs.query.recordJsonPath
        def idJsonPath = qs.query.idJsonPath

        int maxRecords = grailsApplication.config.getProperty("biosecurity.query.maxRecords", Integer, 500)
        records = diffService.differentiateRecordsById(previous, last, recordJsonPath, idJsonPath)
        if (records.size() > maxRecords) {
            records = records.subList(0, maxRecords)
        }
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

