package au.org.ala.alerts
import groovy.json.JsonOutput
import groovy.json.JsonSlurper


class ImageService {

    def diff(def records) {
        def groupedByDataResource = records.groupBy { it.dataResourceName }

        return groupedByDataResource
    }



}

