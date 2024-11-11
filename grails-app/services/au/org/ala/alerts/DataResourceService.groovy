package au.org.ala.alerts

class DataResourceService {
    def diffService
    def grailsApplication
    def collectionService

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

