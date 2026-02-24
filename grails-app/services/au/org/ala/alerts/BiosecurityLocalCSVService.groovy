/*
 *   Copyright (c) 2026.  Atlas of Living Australia
 *   All Rights Reserved.
 *   The contents of this file are subject to the Mozilla Public
 *   License Version 1.1 (the "License"); you may not use this file
 *   except in compliance with the License. You may obtain a copy of
 *   the License at http://www.mozilla.org/MPL/
 *   Software distributed under the License is distributed on an "AS
 *   IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 *   implied. See the License for the specific language governing
 *   rights and limitations under the License.
 *
 */

package au.org.ala.alerts

import grails.core.GrailsApplication
import org.apache.commons.lang.NotImplementedException

import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.text.SimpleDateFormat

/**
 * Generate CSV reports for Biosecurity alerts.
 * CSV files are generated from the query results and stored on the local file system.
 * S3 storage is handled separately by {@code BiosecurityS3CSVService}.
 *
 * @author qifeng-bai
 *
 **/
class BiosecurityLocalCSVService  extends BiosecurityCSVService {
    GrailsApplication grailsApplication

    @Override
    def list() throws Exception {
        def BASE_DIRECTORY = grailsApplication.config.getProperty('biosecurity.csv.local.directory', '/tmp')
        def dir = new File(BASE_DIRECTORY)
        if (!dir.exists() || !dir.isDirectory()) {
            return [status: 1, message: "Directory not found"]
        }

        def foldersAndFiles = listFilesRecursively(dir)
        long totalFiles = foldersAndFiles.sum { it.files.size() }
        long totalSize  = foldersAndFiles.sum { it.size }
        return [status:0, foldersAndFiles: foldersAndFiles, totalFiles: totalFiles, totalSize: formatSize(totalSize)]
    }


    /**
     *
     * @param folderName Assure folder exists
     * @return stream reader
     */
    void aggregateCSVFiles(String folderName, OutputStream out) {
        def BASE_DIRECTORY =
                grailsApplication.config.getProperty(
                        'biosecurity.csv.local.directory',
                        '/tmp'
                )

        def folder = new File(BASE_DIRECTORY, folderName)
        if (folder == null || !folder.exists() || !folder.isDirectory()) {
            return
        }

        Collection<File> csvFiles = []

        collectLocalCsvFiles(folder, csvFiles)
        boolean firstFile = true
        csvFiles.each { File csvFile ->
            csvFile.withReader('UTF-8') { reader ->
                reader.eachLine { line, lineNumber ->
                    // keep header only from first file
                    if (firstFile || lineNumber > 1) {
                        out.write((line + System.lineSeparator()).getBytes("UTF-8"))
                    }
                }
            }

            firstFile = false
        }
    }

    @Override
    Map asyncAggregateCSVFiles(String folder) {
        throw new NotImplementedException("This function is not needed for the local CSV storage")
    }

    /**
     * Collect CSV files from the folder and its subfolders
     * @param folder
     * @param collectedFiles
     */
    private void collectLocalCsvFiles(File folder, Collection<File> collectedFiles) {
        folder.listFiles().each { file ->
            if (file.isDirectory()) {
                collectLocalCsvFiles(file, collectedFiles) // Recursively collect CSV files from subfolders
            } else if (file.isFile() && file.name.endsWith('.csv')) {
                collectedFiles.add(file)
            }
        }
    }

    /**
     * Check if folder exists
     *
     * @param folderName
     * @return true if folder exists
     */
    @Override
    boolean folderExists(String folderName) {
        if (!folderName) return false

        def config = grailsApplication.config

        String baseDirectory = config.getProperty('biosecurity.csv.local.directory', String, '/tmp')
        File folder = new File(baseDirectory, folderName)
        return folder.exists() && folder.isDirectory()
    }

    /**
     * Get file content
     *
     * @param filename
     * @return file content as String
     */
    @Override
    InputStream getFile(String filename) {
        if (!filename) return null

        def config = grailsApplication.config

        String BASE_DIRECTORY = config.getProperty('biosecurity.csv.local.directory', String, '/tmp')
        def file = new File(BASE_DIRECTORY, filename)
        if (file.exists()) {
            return new FileInputStream(file)
        }
        return null
    }

    @Override
    Map deleteFile(String filename) {
        if (!filename) return [:]

        def BASE_DIRECTORY = grailsApplication.config.getProperty('biosecurity.csv.local.directory', String, '/tmp')
        def file = new File(BASE_DIRECTORY, filename)
        def message = [:]

        if (!file.exists() || file.isDirectory()) {
            message['status'] = 1
            message['message'] = "File not found"
        } else {
            if (file.renameTo(new File(BASE_DIRECTORY,  filename +'_deleted'))){
                message['status'] = 0
                message['message'] = "The file has been temporarily deleted. You can contact the system administrator to recover it."
            } else {
                message['status'] = 1
                message['message'] = "File deletion failed"
            }
        }

        return message
    }

    void generateAuditCSV(QueryResult qs) {
        def task = {
            File outputFile = createTempCSVFromQueryResult(qs)
            String folderName = new SimpleDateFormat("yyyy-MM-dd").format(new Date())
            String fileName = sanitizeFileName("${qs.query.name}")+ ".csv"
            String destinationFolder = new File(grailsApplication.config.getProperty('biosecurity.csv.local.directory', '/tmp'), folderName).absolutePath
            File destinationFile = new File(destinationFolder, fileName)
            moveToDestination(outputFile, destinationFile)
        }

        Thread.start(task)
    }

    /**
     * Move file from source to destination
     * @param source
     * @param destination
     */
     private static void moveToDestination(File source, File destination) {
        File destDir = new File(destination.parent)
        if (!destDir.exists()) {
            destDir.mkdirs()
        }
        //copy source file to destination
        Files.move(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING)
    }


    /**
     * List all files in the directory recursively, ignoring deleted files
     *
     * @param dir
     * @return Key value pair of folder and files
     * */
    static private List<Map> listFilesRecursively(File rootDir) {
        def foldersAndFiles = rootDir.listFiles().findAll { it.isDirectory() }.collect { folder ->
            def csvFiles = folder.listFiles().findAll { File file ->
                file.isFile() && file.name.endsWith('.csv')
            }
            [
                    name : folder.name,
                    files: csvFiles.collect { it.name } ?: [],
                    size : csvFiles.sum { it.length() } ?: 0  // bytes
            ]
        }
        return foldersAndFiles.sort({ it.name }).reverse()
    }
}
