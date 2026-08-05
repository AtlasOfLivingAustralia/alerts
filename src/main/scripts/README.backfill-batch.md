# Biosecurity CSV Scripts

Recommended run order:
1. `listBiosecurityS3CsvByDate.groovy`
2. `downloadBiosecurityS3CsvByDate.groovy`
3. `backfillBiosecurityFirstLoadedDate.groovy`
4. `uploadBiosecurityS3CsvByDate.groovy`

## 1) `src/main/scripts/listBiosecurityS3CsvByDate.groovy`
Function:
- Lists CSV objects in S3 by prefix.
- Uses the first folder segment after prefix as a date (for example `biosecurity/2026-06-04/file.csv`).
- Returns objects where date folder is `>= cutoff-date`.

Usage:
```bash
cd /Users/bai187/src/alerts
./grailsw run-script src/main/scripts/listBiosecurityS3CsvByDate.groovy --args="<s3-uri> <file-prefix> <cutoff-date> [region]"
```

Example:
```bash
cd /Users/bai187/src/alerts
./grailsw run-script src/main/scripts/listBiosecurityS3CsvByDate.groovy --args="s3://ala-alerts-testing biosecurity/ 2026-03-04 ap-southeast-2"
```

## 2) `src/main/scripts/downloadBiosecurityS3CsvByDate.groovy`
Function:
- Downloads matching CSV files from S3.
- Filters by date folder `>= cutoff-date`.
- Stores files in one local folder by flattening S3 key separators (`/`) to `~`.

Usage:
```bash
cd /Users/bai187/src/alerts
./grailsw run-script src/main/scripts/downloadBiosecurityS3CsvByDate.groovy --args="<s3-uri> <file-prefix> <cutoff-date> <local-folder> [region]"
```

Example:
```bash
cd /Users/bai187/src/alerts
./grailsw run-script src/main/scripts/downloadBiosecurityS3CsvByDate.groovy --args="s3://ala-alerts-testing biosecurity/ 2026-03-04 /data/biosecurity-download ap-southeast-2"
```

## 3) `src/main/scripts/backfillBiosecurityFirstLoadedDate.groovy`
Function:
- Reads CSV files in a local folder recursively.
- Finds rows with empty `firstLoadedDate`.
- Looks up values from biocache APIs and writes updates back to the same CSV files.

Usage:
```bash
cd /Users/bai187/src/alerts
./grailsw run-script src/main/scripts/backfillBiosecurityFirstLoadedDate.groovy --args="<folder> <biocacheBaseURL> <jwtToken> [lgaLayer] [pageSize]"
```

Example:
```bash
cd /Users/bai187/src/alerts
./grailsw run-script src/main/scripts/backfillBiosecurityFirstLoadedDate.groovy --args="/data/biosecurity-download https://biocache-ws.example.org eyJhbGciOi... cl11170 100"
```

## 4) `src/main/scripts/uploadBiosecurityS3CsvByDate.groovy`
Function:
- Uploads local CSV files to S3.
- Converts local file names from `~` back to `/` to reconstruct the S3 object key.
- Uses S3 `PutObject`, so existing keys are overwritten.

Usage:
```bash
cd /Users/bai187/src/alerts
./grailsw run-script src/main/scripts/uploadBiosecurityS3Csv.groovy --args="<s3-uri> <local-folder> [region]"
```

Example:
```bash
cd /Users/bai187/src/alerts
./grailsw run-script src/main/scripts/uploadBiosecurityS3Csv.groovy --args="s3://ala-alerts-testing /data/biosecurity-download ap-southeast-2"
```
