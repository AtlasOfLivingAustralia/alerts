// Download CSV files from S3 by key prefix and date folder into a local folder.
// Usage:
//   run-script src/main/scripts/downloadBiosecurityS3CsvByDate.groovy --args="s3://ala-alerts-testing biosecurity/ 2026-02-23 /data/biosecurity-download [ap-southeast-2]"

import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3ClientBuilder
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response
import software.amazon.awssdk.services.s3.model.S3Object

import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate

S3Client getS3Client(String regionArg) {
    S3ClientBuilder builder = S3Client.builder()
    if (regionArg) {
        builder = builder.region(Region.of(regionArg))
    }
    builder.build()
}

List<S3Object> collectFilesInS3(S3Client s3Client, String bucketName, String prefix) {
    List<S3Object> allObjects = new ArrayList<>()
    String continuationToken = null

    do {
        ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(prefix)
        if (continuationToken != null) {
            requestBuilder.continuationToken(continuationToken)
        }

        ListObjectsV2Response response = s3Client.listObjectsV2(requestBuilder.build())
        allObjects.addAll(response.contents())
        continuationToken = response.nextContinuationToken()
    } while (continuationToken != null)

    allObjects.sort { a, b ->
        long aTime = a.lastModified() != null ? a.lastModified().toEpochMilli() : 0L
        long bTime = b.lastModified() != null ? b.lastModified().toEpochMilli() : 0L
        Long.compare(bTime, aTime)
    }
    allObjects
}

List<String> cliArgs = []
if (binding.hasVariable('argsMap') && argsMap?.params instanceof List) {
    cliArgs.addAll(argsMap.params.collect { it?.toString() })
}
if (binding.hasVariable('args') && args instanceof String[]) {
    cliArgs.addAll((args as String[]).collect { it?.toString() })
}

if (cliArgs.size() < 4) {
    println "Usage: downloadBiosecurityS3CsvByDate <s3-uri> <file-prefix> <cutoff-date> <local-folder> [region]"
    return
}

String s3Uri = cliArgs[0]?.trim()
String filePrefix = cliArgs[1]?.trim()
String cutoffDate = cliArgs[2]?.trim()?.replaceAll(/^[\"']|[\"']$/, '')
String localFolder = cliArgs[3]?.trim()
String regionArg = cliArgs.size() > 4 ? cliArgs[4]?.trim() : "ap-southeast-2"

if (!s3Uri?.startsWith('s3://')) {
    println "Invalid s3-uri '${s3Uri}'. Expected format: s3://bucket"
    return
}
if (!filePrefix) {
    println "file-prefix is required"
    return
}
if (!cutoffDate) {
    println "cutoff-date is required"
    return
}
if (!localFolder) {
    println "local-folder is required"
    return
}

LocalDate parsedCutoff
try {
    parsedCutoff = LocalDate.parse(cutoffDate)
} catch (Exception e) {
    println "Invalid cutoff-date '${cutoffDate}'. Expected format: yyyy-MM-dd"
    return
}

Path destinationRoot = Path.of(localFolder)
Files.createDirectories(destinationRoot)

String withoutScheme = s3Uri.substring('s3://'.length())
int firstSlash = withoutScheme.indexOf('/')
String bucket = firstSlash >= 0 ? withoutScheme.substring(0, firstSlash) : withoutScheme

if (!bucket) {
    println "Invalid s3-uri '${s3Uri}': missing bucket"
    return
}

if (filePrefix.startsWith('/')) {
    filePrefix = filePrefix.substring(1)
}
String effectivePrefix = filePrefix.endsWith('/') ? filePrefix : filePrefix + '/'

S3Client s3 = getS3Client(regionArg)

try {
    int matched = 0
    int downloaded = 0

    println "Downloading CSV files from s3://${bucket}/ with key prefix '${effectivePrefix}' after '${cutoffDate}' into '${destinationRoot}'"

    def allObjects = collectFilesInS3(s3, bucket, effectivePrefix)

    allObjects.each { obj ->
        String key = obj.key()
        if (!key?.startsWith(effectivePrefix) || !key?.toLowerCase()?.endsWith('.csv')) {
            return
        }

        String relativePath = key.substring(effectivePrefix.length())
        String dateFolder = relativePath.contains('/') ? relativePath.substring(0, relativePath.indexOf('/')) : null
        if (!dateFolder) {
            return
        }

        try {
            LocalDate objectDate = LocalDate.parse(dateFolder)
            if (!objectDate.isBefore(parsedCutoff)) {
                matched++

                // Flatten S3 keys into files by replacing path separators.
                String flattenedFileName = key.replace('/', '~')
                Path targetFile = destinationRoot.resolve(flattenedFileName)

                GetObjectRequest getReq = GetObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build()

                s3.getObject(getReq, targetFile)
                downloaded++
                println "Downloaded s3://${bucket} # ${key} -> ${targetFile}"
            }
        } catch (Exception ignored) {
            // Skip keys whose first path segment is not a date.
        }
    }

    println "Matched CSV files: ${matched}"
    println "Downloaded CSV files: ${downloaded}"
} finally {
    s3.close()
}
