package au.org.ala.alerts.biosecurity

import au.org.ala.alerts.QueryResult
import au.org.ala.web.AlaSecured
import grails.converters.JSON
import java.text.SimpleDateFormat

class CsvController {
    static namespace = "biosecurity"

    def biosecurityLocalCSVService
    def biosecurityS3CSVService
    def queryResultService

    private def getCsvService() {
        return  grailsApplication.config.getProperty('biosecurity.csv.s3.enabled', Boolean, false) ? biosecurityS3CSVService : biosecurityLocalCSVService
    }

    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true, redirectController = 'notification', redirectAction = 'myAlerts', message = "You don't have permission to view that page.")
    def list() {
        def result = [:]
        def csvService = getCsvService()
        try {
            result  = csvService.list()
        } catch (Exception e) {
            log.error("Error in listing Biosecurity CSV files: ${e.message}")
            result = [status: 1, message: "Error in listing Biosecurity CSV files: ${e.message}"]
        }
        render(view: '/biosecurity/csv', model: result)
    }

    /**
     * todo handle exception occurred during the streaming process
     * @param folderName
     * @return
     */
    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true,redirectController = 'notification', redirectAction = 'myAlerts', message = "You don't have permission to view that page.")
    def aggregate(String folderName) {
        def csvService = getCsvService()

        if (!csvService.folderExists(folderName)) {
            render(status: 404, text: 'Data not found')
            return
        }
        def outputFilename = folderName
        if (!folderName || folderName == "/") {
            outputFilename = "biosecurity_alerts"
        }

        response.contentType = "text/csv"
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=\"${outputFilename}.csv\""
        )

        csvService.aggregateCSVFiles(folderName, response.outputStream)

        response.outputStream.flush()
    }

    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true,redirectController = 'notification', redirectAction = 'myAlerts', message = "You don't have permission to view that page.")
    def download(String filename) {
        def csvService = getCsvService()

        String contents = csvService.getFile(filename)
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
    def downloadLastResult() {
        def csvService = getCsvService()
        // Gorm object QueryResult does not fetch Query object, so we need to fetch it manually
        QueryResult qs = queryResultService.get(params.id)
        if (qs) {
            File tempFile = csvService.createTempCSVFromQueryResult(qs)
            if (!tempFile.exists() || tempFile.isDirectory()) {
                render(status: 404, text: "File not found")
                return
            }

            def saveToFile = csvService.sanitizeFileName(qs.query?.name + "-" + (qs.lastChecked?new SimpleDateFormat("yyyy-MM-dd").format(qs.lastChecked):"") + ".csv")
            response.contentType = 'application/octet-stream'
            response.setHeader('Content-Disposition', "attachment; filename=\"${saveToFile}\"")
            response.outputStream << tempFile.bytes
        } else {
            render(status: 200, text: "QueryResult not found")
        }
    }

    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true)
    def delete(String filename) {
        def csvService = getCsvService()
        Map message = csvService.deleteFile(filename)
        render(status: 200, contentType: 'application/json', text: message as JSON)
    }

    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true)
    def moveLocalFilesToS3() {
        def csvService = getCsvService()

        Boolean dryRun = params.boolean('dryRun', true)

        if (!csvService?.respondsTo('moveLocalFilesToS3', Boolean)) {
            Map unsupportedMessage = [
                    success: false,
                    message: 'Moving local files to S3 is not supported for the current CSV storage configuration.'
            ]
            render(status: 501, contentType: 'application/json', text: unsupportedMessage as JSON)
            return
        }
        Map message = csvService.moveLocalFilesToS3(dryRun)
        render(status: 200, contentType: 'application/json', text: message as JSON)
    }
}
