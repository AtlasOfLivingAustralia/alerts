package au.org.ala.alerts

import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.PathNotFoundException
import grails.converters.JSON
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject

import java.text.SimpleDateFormat

class DatasetService {
    def diffService
    def notificationService
    def grailsApplication

    def diff(previous, last, recordJsonPath, idJsonPath) {
        def records = diffService.differentiateRecordsById(previous, last, recordJsonPath, idJsonPath)
        int maxRecords = grailsApplication.config.getProperty("biosecurity.query.maxRecords", Integer, 500)
        if (records.size() > maxRecords) {
            records = records.subList(0, maxRecords)
        }

        //Iterate over the records and query with uri to get the details
        records.each { record ->
            def uri = record.uri
            def data = notificationService.getWebserviceResults(uri)
            record.details = JSON.parse(data)
        }

        return records
    }

}

