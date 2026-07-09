// Standalone CSV backfill script.
// Usage:
//   run-script src/main/scripts/backfillBiosecurityFirstLoadedDate.groovy --args="<folder> <biocacheBaseURL> <jwtToken> [lgaLayer] [pageSize]"

import groovy.json.JsonSlurper
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVPrinter

import java.text.SimpleDateFormat

List<String> cliArgs = []
if (binding.hasVariable('argsMap') && argsMap?.params instanceof List) {
    cliArgs.addAll(argsMap.params.collect { it?.toString() })
}
if (binding.hasVariable('args') && args instanceof String[]) {
    cliArgs.addAll((args as String[]).collect { it?.toString() })
}

if (cliArgs.size() < 3) {
    println "Usage: backfillBiosecurityFirstLoadedDate <folder> <biocacheBaseURL> <jwtToken> [lgaLayer] [pageSize]"
    return
}

File rootFolder = new File(cliArgs[0])
String biocacheBaseUrl = cliArgs[1]?.trim()?.replaceAll('/+$', '')
String jwtToken = cliArgs[2]?.trim()
String layerId = cliArgs.size() > 3 && cliArgs[3]?.trim() ? cliArgs[3].trim() : 'cl11170'

int pageSize = 100
if (cliArgs.size() > 4 && cliArgs[4]?.trim()) {
    try {
        pageSize = Integer.parseInt(cliArgs[4].trim())
    } catch (Exception ignored) {
        println "Invalid pageSize '${cliArgs[4]}', using default 100"
        pageSize = 100
    }
}

if (!rootFolder.exists() || !rootFolder.isDirectory()) {
    println "Folder not found or not a directory: ${rootFolder.absolutePath}"
    return
}
if (!biocacheBaseUrl) {
    println "biocacheBaseURL is required"
    return
}
if (!jwtToken) {
    println "jwtToken is required"
    return
}
if (pageSize <= 0) {
    println "pageSize must be > 0"
    return
}

String authHeader = jwtToken.toLowerCase().startsWith('bearer ') ? jwtToken : "Bearer ${jwtToken}"

def postForm = { String url, Map<String, String> form ->
    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection()
    conn.setRequestMethod('POST')
    conn.setDoOutput(true)
    conn.setRequestProperty('Content-Type', 'application/x-www-form-urlencoded; charset=UTF-8')
    conn.setRequestProperty('Accept', 'application/json')
    conn.setRequestProperty('Authorization', authHeader)

    String payload = form.collect { k, v ->
        URLEncoder.encode(k, 'UTF-8') + '=' + URLEncoder.encode(v ?: '', 'UTF-8')
    }.join('&')

    conn.outputStream.withWriter('UTF-8') { writer ->
        writer << payload
    }

    int status = conn.responseCode
    String body = ''
    InputStream stream = status >= 200 && status < 300 ? conn.inputStream : conn.errorStream
    if (stream) {
        body = stream.getText('UTF-8')
    }

    def json = null
    if (body) {
        try {
            json = new JsonSlurper().parseText(body)
        } catch (Exception ignored) {
            json = null
        }
    }

    [statusCode: status, resp: json, body: body]
}

def getJson = { String url ->
    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection()
    conn.setRequestMethod('GET')
    conn.setRequestProperty('Accept', 'application/json')
    conn.setRequestProperty('Authorization', authHeader)

    int status = conn.responseCode
    String body = ''
    InputStream stream = status >= 200 && status < 300 ? conn.inputStream : conn.errorStream
    if (stream) {
        body = stream.getText('UTF-8')
    }

    def json = null
    if (body) {
        try {
            json = new JsonSlurper().parseText(body)
        } catch (Exception ignored) {
            json = null
        }
    }

    [statusCode: status, resp: json, body: body]
}

def fetchExtraOccurrenceInfo = { List<Map> records, String baseUrl, String lgaLayer, int limit ->
    String qidUrl = baseUrl + '/qid'

    records.collate(limit).each { batch ->
        def ids = batch.collect { it.uuid }.findAll { it }
        if (!ids) {
            return
        }

        String query = ids.collect { "id:${it}" }.join(' OR ')
        def qidResp = postForm(qidUrl, [q: query])

        if (qidResp.statusCode == 200) {
            def qid = qidResp.resp
            if (qid) {
                String encodedQid = URLEncoder.encode(qid.toString(), 'UTF-8')
                String occurrenceUrl = baseUrl + "/occurrences/search?q=qid:${encodedQid}&pageSize=${limit}&fl=id,firstLoadedDate,${lgaLayer}"
                def occurrencesResp = getJson(occurrenceUrl)

                if (occurrencesResp.statusCode == 200) {
                    def occurrences = occurrencesResp.resp?.occurrences ?: []
                    def occMap = occurrences.collectEntries { occ ->
                        [(occ.uuid): occ]
                    }

                    batch.each { record ->
                        def occ = occMap[record.uuid]
                        if (occ) {
                            //Force to update lga layer and lga name.
                            record['lga layer'] = lgaLayer
                            record['lga'] = occ.otherProperties?[record['lga layer'] as String] ?: ''
                            record['firstLoadedDate'] = occ.otherProperties?.firstLoadedDate
                        }
                    }
                } else {
                    println "WARN occurrences lookup failed status=${occurrencesResp.statusCode} for batch size=${batch.size()}"
                }
            }
        } else {
            println "WARN qid lookup failed status=${qidResp.statusCode} for batch size=${batch.size()}"
        }
    }
}

try {
    List<File> csvFiles = (rootFolder.listFiles() ?: []).findAll { File file ->
        file.isFile() && file.name.toLowerCase().endsWith('.csv')
    }

    println "Found ${csvFiles.size()} CSV file(s) in ${rootFolder.absolutePath}"

    int filesUpdated = 0
    int totalRowsUpdated = 0

    csvFiles.sort { it.absolutePath }.each { File csvFile ->
    List<LinkedHashMap<String, String>> rows = []
    List<String> headers = []

    csvFile.withReader('UTF-8') { reader ->
        CSVParser parser = CSVFormat.RFC4180.builder().setHeader().setSkipHeaderRecord(true).build().parse(reader)
        headers = new ArrayList<>(parser.headerNames)

        parser.each { record ->
            LinkedHashMap<String, String> row = new LinkedHashMap<>()
            headers.each { header ->
                row[header] = record.get(header)
            }
            rows << row
        }
    }

    if (!rows || !headers.contains('firstLoadedDate')) {
        println "Skip ${csvFile.name}: no rows or no firstLoadedDate column"
        return
    }

    String uuidField = headers.contains('uuid') ? 'uuid' : (headers.contains('recordID') ? 'recordID' : null)
    if (!uuidField) {
        println "Skip ${csvFile.name}: no uuid/recordID column"
        return
    }

    List<Map> recordsNeedingLookup = rows.findAll { row ->
        row[uuidField]?.trim()
    }.collect { row ->
        [uuid: row[uuidField]]
    }

    if (!recordsNeedingLookup) {
        println "No records with uuid in ${csvFile.name}"
        return
    }

        fetchExtraOccurrenceInfo(recordsNeedingLookup, biocacheBaseUrl, layerId, pageSize)

    // Match BiosecurityCSVService date handling: try millis first, keep original if parsing fails.
    def formatFirstLoadedDate = { rawValue ->
        if (!rawValue) {
            return ''
        }

        def formatted = rawValue
        try {
            formatted = new SimpleDateFormat('dd/MM/yyyy hh:mm:ss').format(rawValue.toString().toLong())
        } catch (Exception ignored) {
            // keep original value if it cannot be parsed as milliseconds
        }
        return formatted as String
    }

    Map<String, Map> resolvedByUuid = recordsNeedingLookup.findAll { it.uuid }.collectEntries { record ->
        [
                (record.uuid as String): [
                        firstLoadedDate: formatFirstLoadedDate(record.firstLoadedDate),
                        "lga layer"    : (record["lga layer"] ?: '') as String,
                        lga            : (record.lga ?: '') as String
                ]
        ]
    }

    int updatedInFile = 0
    rows.each { row ->
        Map resolved = resolvedByUuid[row[uuidField]]
        if (!resolved) {
            return
        }

        boolean changed = false
        String firstLoadedResolved = resolved.firstLoadedDate as String
        if (firstLoadedResolved) {
            row['firstLoadedDate'] = firstLoadedResolved
            changed = true
        }
        if (resolved["lga layer"]) {
            row['lga layer'] = resolved["lga layer"] as String
            changed = true
        }
        if (resolved.lga) {
            row['lga'] = resolved.lga as String
            changed = true
        }

        if (changed) {
            updatedInFile++
        }
    }

    if (updatedInFile == 0) {
        println "No resolvable firstLoadedDate values for ${csvFile.name}"
        return
    }

    File tempFile = File.createTempFile(csvFile.name, '.tmp', csvFile.parentFile)
    tempFile.withWriter('UTF-8') { writer ->
        CSVPrinter printer = new CSVPrinter(writer, CSVFormat.RFC4180)
        printer.printRecord(headers)
        rows.each { row ->
            printer.printRecord(headers.collect { h -> row[h] ?: '' })
        }
        printer.flush()
    }

    if (!tempFile.renameTo(csvFile)) {
        tempFile.delete()
        throw new RuntimeException("Failed to replace ${csvFile.absolutePath}")
    }

    filesUpdated++
    totalRowsUpdated += updatedInFile
        println "Updated ${updatedInFile} row(s) in ${csvFile.name}"
    }

    println "Completed. Files updated: ${filesUpdated}, rows updated: ${totalRowsUpdated}"
} finally {
}
