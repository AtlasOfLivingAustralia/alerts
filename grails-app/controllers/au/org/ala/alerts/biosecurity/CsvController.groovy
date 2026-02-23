package au.org.ala.alerts.biosecurity

import au.org.ala.alerts.DownloadToken
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
     * @param name
     * @return
     */
    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true,redirectController = 'notification', redirectAction = 'myAlerts', message = "You don't have permission to view that page.")
    def aggregate(String name) {
        def csvService = getCsvService()

        if (!csvService.folderExists(name)) {
            render(status: 404, text: 'Data not found')
            return
        }
        def outputFilename = name
        if (!name || name == "/") {
            outputFilename = "biosecurity_alerts"
        }

        response.contentType = "text/csv"
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=\"${outputFilename}.csv\""
        )

        csvService.aggregateCSVFiles(name, response.outputStream)

        response.outputStream.flush()
    }

    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true,redirectController = 'notification', redirectAction = 'myAlerts', message = "You don't have permission to view that page.")
    def asyncAggregate(String name) {
        name = name ?: '/'
        def csvService = getCsvService()
        if (!csvService.folderExists(name)) {
            render(status: 404, text: 'Data not found')
        } else {
            Map result= csvService.asyncAggregateCSVFiles(name)
            render(result as JSON)
        }
    }

    /**
     * Download CSV files from S3
     * @param token
     * @return
     */
    def downloadWithToken(String token) {
        if (!token) {
            render(status: 400, text: "Token is missing")
            return
        }

        // Lookup the file by token
        DownloadToken dt = DownloadToken.findByToken(token)
        if (!dt || dt.expiresAt.before(new Date()) ) {
            render(status: 404, text: "Invalid or expired token")
            return
        }

        // Serve the file
        File file = new File(dt.fileKey)
        if (!file.exists()) {
            render(status: 404, text: "File not found")
            return
        }

        response.setHeader("Content-disposition", "attachment; filename=\"biosecurity_all.csv\"")
        response.contentType = "text/csv"
        file.withInputStream { stream ->
            response.outputStream << stream
        }
        response.outputStream.flush()
    }

    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true,redirectController = 'notification', redirectAction = 'myAlerts', message = "You don't have permission to view that page.")
    def download(String filename) {
        def csvService = getCsvService()

        InputStream fileStream = csvService.getFile(filename)
        if (!fileStream) {
            render(status: 404, text: "Data not found")
            return
        }

        response.contentType = 'text/csv'
        response.setHeader(
                "Content-disposition",
                "attachment; filename=\"${filename.tokenize(File.separator).last()}\""
        )

        // stream the content to the response output
        fileStream.withCloseable { stream ->
            response.outputStream << stream
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
