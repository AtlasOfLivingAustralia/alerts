// List CSV files in S3 by key prefix and date folder.
// Usage:
//   run-script src/main/scripts/listBiosecurityS3CsvByDate.groovy --args="s3://ala-alerts-testing biosecurity/ 2026-02-23 [ap-southeast-2]"

import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3ClientBuilder
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response
import software.amazon.awssdk.services.s3.model.S3Object
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

if (cliArgs.size() < 3) {
    println "Usage: listBiosecurityS3CsvByDate <s3-uri> <file-prefix> <cutoff-date> [region]"
    return
}

String s3Uri = cliArgs[0]?.trim()
String filePrefix = cliArgs[1]?.trim()
String cutoffDate = cliArgs[2]?.trim()
String regionArg = cliArgs.size() > 3 ? cliArgs[3]?.trim() : "ap-southeast-2"

// Strip optional shell/IDE quotes so ISO dates like "2026-03-04" still parse cleanly.
cutoffDate = cutoffDate?.replaceAll(/^["']|["']$/, '')

if (!s3Uri?.startsWith('s3://')) {
    println "Invalid s3-uri '${s3Uri}'. Expected format: s3://bucket/path/"
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

LocalDate parsedCutoff
try {
    parsedCutoff = LocalDate.parse(cutoffDate)
} catch (Exception e) {
    println "Invalid cutoff-date '${cutoffDate}'. Expected format: yyyy-MM-dd"
    return
}

def withoutScheme = s3Uri.substring('s3://'.length())
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

    println "Listing CSV files in s3://${bucket}/ with key prefix '${effectivePrefix}' after '${cutoffDate}'"

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
                println "s3://${bucket} - ${key}"
            }
        } catch (Exception ignored) {
            // Skip keys whose first path segment is not a date.
        }
    }


    println "Total CSV files found: ${matched}"
} finally {
    s3.close()
}

