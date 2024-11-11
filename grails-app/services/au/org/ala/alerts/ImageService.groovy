package au.org.ala.alerts

class ImageService {
    def grailsApplication
    def collectionService

    def diff(def records) {
        def groupedByDataResource = records.groupBy { it.dataResourceUid }

        String[] drIds = records.collect { it.dataResourceUid }
        String baseURL = grailsApplication.config.collectoryService.baseURL
        String collectionUrl = baseURL + "/find/dataResource"
        def dataResourceInfos = collectionService.findResourceByUids(collectionUrl, drIds)
        drIds.each { id ->
            def dataResourceInfo = dataResourceInfos[id]
            groupedByDataResource[id].each { record ->
                record["dataResourceInfo"] = [:]
                record["dataResourceInfo"].lastUpdated = dataResourceInfo.lastUpdated
                record["dataResourceInfo"].alaPublicUrl = dataResourceInfo.alaPublicUrl
            }
        }
        return groupedByDataResource
    }
}

