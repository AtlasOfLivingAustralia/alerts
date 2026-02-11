package au.org.ala.alerts.biosecurity

import au.org.ala.alerts.QueryResult
import au.org.ala.web.AlaSecured
import grails.converters.JSON

import java.text.SimpleDateFormat

class CsvController {
    static namespace = "biosecurity"

    def biosecurityCSVService

    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true, redirectController = 'notification', redirectAction = 'myAlerts', message = "You don't have permission to view that page.")
    def list() {
        def result = [:]
        try {
            result  = biosecurityCSVService.list()
        } catch (Exception e) {
            log.error("Error in listing Biosecurity CSV files: ${e.message}")
            result = [status: 1, message: "Error in listing Biosecurity CSV files: ${e.message}"]
        }
        render(view: '/biosecurity/csv', model: result)
    }

    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true,redirectController = 'notification', redirectAction = 'myAlerts', message = "You don't have permission to view that page.")
    def aggregate(String folderName) {
        if (!biosecurityCSVService.folderExists(folderName)) {
            render(status: 404, text: 'Data not found')
            return
        }

        // Get a list of all CSV files in the folder
        String mergedCSVFile = biosecurityCSVService.aggregateCSVFiles(folderName)
        if (folderName == "/" || folderName.isEmpty()) {
            folderName = "biosecurity_alerts"
        }
        def saveToFile = folderName +".csv"
        response.contentType = 'application/octet-stream'
        response.setHeader('Content-Disposition', "attachment; filename=\"${saveToFile}\"")
        response.outputStream << new File(mergedCSVFile).bytes
    }

    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true,redirectController = 'notification', redirectAction = 'myAlerts', message = "You don't have permission to view that page.")
    def download(String filename) {
        String contents = biosecurityCSVService.getFile(filename)
        if (!contents) {
            render(status: 404, text: "Data not found")
            return
        }
        response.contentType = 'text/csv'
        response.setHeader("Content-disposition", "attachment; filename=\"${filename.tokenize(File.separator).last()}\"")
        response.outputStream.withWriter("UTF-8") { writer ->
            writer << contents
        }
        response.outputStream.flush()
    }

    /**
     * params.id is the QueryResult id
     * @return
     */
    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true)
    def downloadLastBiosecurityResult() {
        // Gorm object QueryResult does not fetch Query object, so we need to fetch it manually
        QueryResult qs = queryResultService.get(params.id)
        if (qs) {
            File tempFile = biosecurityCSVService.createTempCSVFromQueryResult(qs)
            if (!tempFile.exists() || tempFile.isDirectory()) {
                render(status: 404, text: "File not found")
                return
            }

            def saveToFile = biosecurityCSVService.sanitizeFileName(qs.query?.name + "-" + (qs.lastChecked?new SimpleDateFormat("yyyy-MM-dd").format(qs.lastChecked):"") + ".csv")
            response.contentType = 'application/octet-stream'
            response.setHeader('Content-Disposition', "attachment; filename=\"${saveToFile}\"")
            response.outputStream << tempFile.bytes
        } else {
            render(status: 200, text: "QueryResult not found")
        }
    }

    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true)
    def delete(String filename) {
        Map message = biosecurityCSVService.deleteFile(filename)
        render(status: 200, contentType: 'application/json', text: message as JSON)
    }

    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true)
    def moveLocalFilesToS3() {
        Boolean dryRun = params.boolean('dryRun', true)
        Map message = biosecurityCSVService.moveLocalFilesToS3(dryRun)
        render(status: 200, contentType: 'application/json', text: message as JSON)
    }
}
