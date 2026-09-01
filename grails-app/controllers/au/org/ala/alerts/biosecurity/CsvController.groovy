package au.org.ala.alerts.biosecurity

import au.org.ala.alerts.DownloadToken
import au.org.ala.alerts.QueryResult
import au.org.ala.web.AlaSecured
import grails.converters.JSON
import grails.gorm.transactions.Transactional

import java.text.SimpleDateFormat
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

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
            outputFilename = "biosecurity_alerts_before_${new SimpleDateFormat("yyyy-MM-dd").format(new Date())}"
        }

        response.contentType = "application/zip"
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=\"${outputFilename}.zip\""
        )
        ZipOutputStream zipOut = new ZipOutputStream(response.outputStream)
        zipOut.putNextEntry(new ZipEntry("${outputFilename}.csv"))
        csvService.aggregateCSVFiles(name, zipOut)
        zipOut.closeEntry()
        zipOut.finish()
    }

    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true,redirectController = 'notification', redirectAction = 'myAlerts', message = "You don't have permission to view that page.")
    def asyncAggregate(String name) {
        name = name ?: '/'
        def csvService = getCsvService()
        if (!csvService.folderExists(name)) {
            render (view: "/info",model:[staus:1, message: "Data not found",
                                         redirectUrl: grailsLinkGenerator.link(namespace: 'biosecurity',controller: 'csv', action: 'list')])
        } else {
            Map result= csvService.asyncAggregateCSVFiles(name)
            render (view: "/info",model:result)
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
        
        file.withInputStream { inputStream ->
            String csvFilename = "biosecurity_alerts_before_${new SimpleDateFormat('yyyy-MM-dd').format(dt.createdAt)}.csv"
            String zipFilename = csvFilename.replace(".csv", ".zip")

            response.setHeader(
                    "Content-Disposition",
                    "attachment; filename=${zipFilename}"
            )
            response.contentType = "application/zip"
            ZipOutputStream zipOut = new ZipOutputStream(response.outputStream)
            zipOut.putNextEntry(new ZipEntry(csvFilename))
            inputStream.transferTo(zipOut)
            zipOut.closeEntry()
            zipOut.finish()
            zipOut.flush()
        }
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

    /**
     * Archive all CSV files in S3 for a given year by renaming them with .archived suffix.
     * File pattern: biosecurity/yyyy-MM-dd/*.csv -> biosecurity/yyyy-MM-dd/*.csv.archived
     *
     * @param year 4-digit year (required)
     * @return JSON with status and count of archived files
     */
    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true)
    def archiveByYear() {
        if (!params.year) {
            render(status: 400, contentType: 'application/json', text: ([status: 1, error: "Year parameter is required"] as JSON))
            return
        }

        int year
        try {
            year = params.year.toInteger()
        } catch (NumberFormatException ignored) {
            render(status: 400, contentType: 'application/json', text: ([status: 1, error: "Invalid year format"] as JSON))
            return
        }

        if (year < 1900 || year > 3000) {
            render(status: 400, contentType: 'application/json', text: ([status: 1, error: "Invalid year. Expected a 4-digit year."] as JSON))
            return
        }

        try {
            def csvService = getCsvService()
            int archivedCount = csvService.archiveCSVFilesByYear(year)
            render(status: 200, contentType: 'application/json', text: ([
                status: 0,
                year: year,
                archivedCount: archivedCount,
                message: "Successfully archived ${archivedCount} CSV file(s) for year ${year}"
            ] as JSON))
        } catch (Exception e) {
            log.error("Error archiving CSV files for year ${year}: ${e.message}", e)
            render(status: 500, contentType: 'application/json', text: ([status: 1, error: "Failed to archive CSV files: ${e.message}"] as JSON))
        }
    }

    /**
     * Unarchive all CSV files for a given year — restores .csv.archived back to .csv
     */
    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true)
    def unarchiveByYear() {
        if (!params.year) {
            render(status: 400, contentType: 'application/json', text: ([status: 1, error: "Year parameter is required"] as JSON))
            return
        }

        int year
        try {
            year = params.year.toInteger()
        } catch (NumberFormatException ignored) {
            render(status: 400, contentType: 'application/json', text: ([status: 1, error: "Invalid year format"] as JSON))
            return
        }

        if (year < 1900 || year > 3000) {
            render(status: 400, contentType: 'application/json', text: ([status: 1, error: "Invalid year. Expected a 4-digit year."] as JSON))
            return
        }

        try {
            def csvService = getCsvService()
            int unarchivedCount = csvService.unarchiveCSVFilesByYear(year)
            render(status: 200, contentType: 'application/json', text: ([
                status: 0,
                year: year,
                unarchivedCount: unarchivedCount,
                message: "Successfully unarchived ${unarchivedCount} CSV file(s) for year ${year}"
            ] as JSON))
        } catch (Exception e) {
            log.error("Error unarchiving CSV files for year ${year}: ${e.message}", e)
            render(status: 500, contentType: 'application/json', text: ([status: 1, error: "Failed to unarchive CSV files: ${e.message}"] as JSON))
        }
    }

    /**
     * Aggregate all CSV files for a given year into a single merged file and upload to S3
     * as biosecurity/<year>/full-records.csv
     */
    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true)
    def aggregateByYear() {
        if (!params.year) {
            render(status: 400, contentType: 'application/json', text: ([status: 1, error: "Year parameter is required"] as JSON))
            return
        }

        int year
        try {
            year = params.year.toInteger()
        } catch (NumberFormatException ignored) {
            render(status: 400, contentType: 'application/json', text: ([status: 1, error: "Invalid year format"] as JSON))
            return
        }

        if (year < 1900 || year > 3000) {
            render(status: 400, contentType: 'application/json', text: ([status: 1, error: "Invalid year. Expected a 4-digit year."] as JSON))
            return
        }

        try {
            def csvService = getCsvService()
            String s3Key = csvService.aggregateAndUploadByYear(year)
            if (s3Key) {
                render(status: 200, contentType: 'application/json', text: ([
                    status: 0,
                    year: year,
                    s3Key: s3Key,
                    message: "Successfully aggregated CSV files for year ${year} → ${s3Key}"
                ] as JSON))
            } else {
                render(status: 200, contentType: 'application/json', text: ([
                    status: 1,
                    year: year,
                    message: "No CSV files found for year ${year} — nothing was aggregated."
                ] as JSON))
            }
        } catch (Exception e) {
            log.error("Error aggregating CSV files for year ${year}: ${e.message}", e)
            render(status: 500, contentType: 'application/json', text: ([status: 1, error: "Failed to aggregate CSV files: ${e.message}"] as JSON))
        }
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

    @Transactional
    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true)
    def downloads() {
        List<DownloadToken> tokens = DownloadToken.list(sort: 'createdAt', order: 'desc')
        tokens = tokens.findAll { token ->
            def file = new File(token.fileKey)
            if (!file.exists()) {
                token.delete()
                return false
            }
            //remove tokens that are expired for more than 1 month, as the file is likely to be deleted by then
            long oneMonthMillis = 30L * 24 * 60 * 60 * 1000
            if (new Date().time > token.expiresAt.time + oneMonthMillis) {
                file.delete()
                token.delete()
                return false
            }
            return true
        }
        render(view: "/biosecurity/downloads", model:[downloads: tokens])
    }
}
