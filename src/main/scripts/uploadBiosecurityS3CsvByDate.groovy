// Upload locally downloaded Biosecurity CSV files back to S3.
// Local file names are expected to use '~' instead of '/' in the S3 key.
// Usage:
//   run-script src/main/scripts/uploadBiosecurityS3CsvByDate.groovy --args="s3://ala-alerts-testing /data/biosecurity-download [ap-southeast-2]"

import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3ClientBuilder
import software.amazon.awssdk.services.s3.model.PutObjectRequest

import java.nio.file.Files
import java.nio.file.Path

S3Client getS3Client(String regionArg) {
    S3ClientBuilder builder = S3Client.builder()
    if (regionArg) {
        builder = builder.region(Region.of(regionArg))
    }
    builder.build()
}

List<String> cliArgs = []
if (binding.hasVariable('argsMap') && argsMap?.params instanceof List) {
    cliArgs.addAll(argsMap.params.collect { it?.toString() })
}
if (binding.hasVariable('args') && args instanceof String[]) {
    cliArgs.addAll((args as String[]).collect { it?.toString() })
}

if (cliArgs.size() < 2) {
    println "Usage: uploadBiosecurityS3CsvByDate <s3-uri> <local-folder> [region]"
    return
}

String s3Uri = cliArgs[0]?.trim()
String localFolder = cliArgs[1]?.trim()
String regionArg = cliArgs.size() > 2 ? cliArgs[2]?.trim() : "ap-southeast-2"

if (!s3Uri?.startsWith('s3://')) {
    println "Invalid s3-uri '${s3Uri}'. Expected format: s3://bucket[/optional/prefix]"
    return
}
if (!localFolder) {
    println "local-folder is required"
    return
}

Path sourceRoot = Path.of(localFolder)
if (!Files.exists(sourceRoot) || !Files.isDirectory(sourceRoot)) {
    println "local-folder '${localFolder}' does not exist or is not a directory"
    return
}

String withoutScheme = s3Uri.substring('s3://'.length())
int firstSlash = withoutScheme.indexOf('/')
String bucket = firstSlash >= 0 ? withoutScheme.substring(0, firstSlash) : withoutScheme
String s3BasePrefix = firstSlash >= 0 ? withoutScheme.substring(firstSlash + 1).trim() : ""

if (!bucket) {
    println "Invalid s3-uri '${s3Uri}': missing bucket"
    return
}
if (s3BasePrefix.startsWith('/')) {
    s3BasePrefix = s3BasePrefix.substring(1)
}
if (s3BasePrefix.endsWith('/')) {
    s3BasePrefix = s3BasePrefix.substring(0, s3BasePrefix.length() - 1)
}

S3Client s3 = getS3Client(regionArg)

try {
    int matched = 0
    int uploaded = 0

    println "Uploading CSV files from '${sourceRoot}' to s3://${bucket}/${s3BasePrefix ? s3BasePrefix + '/' : ''}"
    println "Rule: local '~' is converted back to '/' in S3 key (PutObject overwrites existing objects)."

    Files.list(sourceRoot).forEach { path ->
        if (!Files.isRegularFile(path)) {
            return
        }

        String localName = sourceRoot.relativize(path).toString().replace('\\', '/')
        if (!localName.toLowerCase().endsWith('.csv')) {
            return
        }

        matched++

        String reconstructedKey = localName.replace('~', '/')
        String finalKey = s3BasePrefix ? "${s3BasePrefix}/${reconstructedKey}" : reconstructedKey

        PutObjectRequest putReq = PutObjectRequest.builder()
                .bucket(bucket)
                .key(finalKey)
                .contentType("text/csv")
                .build()

        s3.putObject(putReq, RequestBody.fromFile(path))
        uploaded++
        println "Uploaded ${path} -> s3://${bucket}/${finalKey}"
    }

    println "Matched CSV files: ${matched}"
    println "Uploaded CSV files: ${uploaded}"
} finally {
    s3.close()
}

