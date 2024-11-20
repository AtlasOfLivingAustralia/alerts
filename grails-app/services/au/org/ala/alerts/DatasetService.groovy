package au.org.ala.alerts

class DatasetService {
    def diffService
    def grailsApplication
    def collectionService

    def diff(QueryResult qs) {
        def records = diffService.findNewRecordsById(qs)
        String[] uids = records.collect { it.uid }
        String baseURL = grailsApplication.config.collectoryService.baseURL
        String collectionUrl = baseURL + "/find/dataResource"

        def collectionMap = collectionService.findResourceByUids(collectionUrl, uids)

        records.each { record ->
            record.details = collectionMap.get(record.uid)
        }
        return records
    }
}

