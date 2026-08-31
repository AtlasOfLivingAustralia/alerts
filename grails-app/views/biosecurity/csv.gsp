<!DOCTYPE html>
<html lang="en">
<head>
    <title>Biosecurity Alerts Reporting</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <meta name="breadcrumb" content="CSV"/>
    <meta name="breadcrumbParent" content="${request.contextPath}/admin,Alerts admin"/>
    <meta name="breadcrumb" content="CSV"/>
    <meta name="breadcrumbParent" content="${request.contextPath}/biosecurity,BioSecurity"/>
    <style>
    .folder {
        cursor: pointer;
        margin: 10px 0;
    }

    .folder-icon {
        color: #4A90E2;
    }
    .file-list {
        display: none;
        margin-left: 20px;
    }
    </style>
    <script>
        $(document).ready(function() {
            $('.folder').click(function() {
                var folderName = $(this).data('folder');
                $('#files-' + folderName).toggle();

                var icon = $(this).find('i.fa.folder');
                icon.toggleClass('far fa-folder-open');
            });
        });

        function confirmDownload() {
            return confirm(
                "This download may take some time.\n\n" +
                "Please keep an eye on the download status in the top-right toolbar of your browser."
            );
        }

        function deleteFile(filename) {
            $.ajax({
                url: "${createLink( namespace: 'biosecurity', controller: 'csv', action: 'delete')}",
                type: 'POST',
                data: {
                    filename: filename
                },
                success: function(response) {
                    // Assuming the response is a JSON object with a message
                    alert(response.message);
                    location.reload();
                },
                error: function(xhr, status, error) {
                    alert("Error: " + xhr.responseText);
                }
            });
        }

        function setYearButtonsDisabled(disabled, statusMsg) {
            ['archiveBtn','unarchiveBtn','aggregateBtn'].forEach(function(id) {
                document.getElementById(id).disabled = disabled;
            });
            document.getElementById('archiveYear').disabled = disabled;
            var statusEl = document.getElementById('yearOpStatus');
            statusEl.textContent = statusMsg || '';
            statusEl.style.display = statusMsg ? 'block' : 'none';
        }

        function archiveByYear() {
            var year = document.getElementById('archiveYear').value;
            if (!year) { alert("Please enter a year to archive."); return; }
            if (!confirm("Archive all CSV files for year " + year + "?\nThey will no longer appear in the list.")) return;
            setYearButtonsDisabled(true, 'Archiving ' + year + '… It may take a few minutes, please wait.');
            $.ajax({
                url: "${createLink( namespace: 'biosecurity', controller: 'csv', action: 'archiveByYear')}",
                type: 'POST',
                data: { year: year },
                success: function(response) {
                    setYearButtonsDisabled(false, '');
                    if (response.status === 0) {
                        alert("Archived " + response.archivedCount + " file(s) for year " + year + ".");
                        location.reload();
                    } else {
                        alert("Error: " + (response.failed || "Unknown error"));
                    }
                },
                error: function(xhr) {
                    setYearButtonsDisabled(false, '');
                    alert("Error: " + xhr.responseText);
                }
            });
        }

        function unarchiveByYear() {
            var year = document.getElementById('archiveYear').value;
            if (!year) { alert("Please enter a year to unarchive."); return; }
            if (!confirm("Unarchive all CSV files for year " + year + "?\nThey will be restored and appear in the list again.")) return;
            setYearButtonsDisabled(true, 'Unarchiving ' + year + '… It may take a few minutes, please wait.');
            $.ajax({
                url: "${createLink( namespace: 'biosecurity', controller: 'csv', action: 'unarchiveByYear')}",
                type: 'POST',
                data: { year: year },
                success: function(response) {
                    setYearButtonsDisabled(false, '');
                    if (response.status === 0) {
                        alert("Unarchived " + response.unarchivedCount + " file(s) for year " + year + ".");
                        location.reload();
                    } else {
                        alert("Error: " + (response.failed || "Unknown error"));
                    }
                },
                error: function(xhr) {
                    setYearButtonsDisabled(false, '');
                    alert("Error: " + xhr.responseText);
                }
            });
        }

        function aggregateByYear() {
            var year = document.getElementById('archiveYear').value;
            if (!year) { alert("Please enter a year to aggregate."); return; }
            if (!confirm("Aggregate all CSV files for year " + year + " into a single annual file?\nThis will create biosecurity/" + year + "/annual-records.csv in S3.")) return;
            setYearButtonsDisabled(true, 'Aggregating ' + year + '… It may take a few minutes, please wait.');
            $.ajax({
                url: "${createLink( namespace: 'biosecurity', controller: 'csv', action: 'aggregateByYear')}",
                type: 'POST',
                data: { year: year },
                success: function(response) {
                    setYearButtonsDisabled(false, '');
                    if (response.status === 0) {
                        alert("Aggregation complete for year " + year + ".\nUploaded to: " + response.s3Key);
                        location.reload();
                    } else {
                        alert(response.message || "Unknown error");
                    }
                },
                error: function(xhr) {
                    setYearButtonsDisabled(false, '');
                    alert("Error: " + xhr.responseText);
                }
            });
        }
    </script>
</head>
<body>
    <div>
        <h4 class="float-end">
            <span class="badge bg-info">
                <g:if test="${totalFiles}">${totalFiles} files </g:if>
                <g:if test="${totalSize}">, ${totalSize} in total, </g:if>
                %{-- Indicate the storage type being used with a BS label --}%
                ${grailsApplication.config.getProperty('biosecurity.csv.s3.enabled', Boolean) == true
                    ? "s3://${grailsApplication.config.getProperty('grails.plugin.awssdk.s3.bucket')}/${grailsApplication.config.getProperty('biosecurity.csv.s3.directory')}/"
                    : "/${grailsApplication.config.getProperty('biosecurity.csv.local.directory')}"}
            </span>
        </h4>
        <h2>Biosecurity Alerts Reports</h2>
        <p>Download a comprehensive CSV file detailing all occurrence records from every biosecurity alert sent. This includes both scheduled and manually triggered emails</p>

        <div class="row" >
            <div class="col-auto">
                <a class="btn btn-primary " href="${createLink( namespace: 'biosecurity', controller: 'csv', action: 'aggregate', params: [name:'/'])}" onclick="return confirmDownload();">
                    <i class="fas fa-cloud-arrow-down" aria-hidden="true" ></i>&nbsp;&nbsp;Download Full CSV Report
                </a>
            </div>
            <g:if test="${grailsApplication.config.getProperty('biosecurity.csv.s3.enabled', Boolean) == true}">
                <div class="col-auto ms-auto">
                    <a class = "btn btn-outline-primary" href="${createLink( namespace: 'biosecurity', controller: 'csv', action: 'asyncAggregate', absolute: true)}">
                        <i class="fas fa-shipping-fast"></i>&nbsp;&nbsp;Email Me Full CSV Report (!Beta)</a>
                    <a class="ms-3" href="${createLink( namespace: 'biosecurity', controller: 'csv', action: 'downloads', absolute: true)}" >
                        <i class="fas fa-history"></i> logs
                    </a>
                </div>
            </g:if>
        </div>
        <g:if test="${grailsApplication.config.getProperty('biosecurity.csv.s3.enabled', Boolean) == true}">
        <div class="row" >
            <div class="d-flex col-auto gap-2 mt-2">
                <input type="number" id="archiveYear" class="form-control form-control-sm"
                       placeholder="e.g. 2025" min="2023" max="${new Date().year + 1900 - 1}" style="width:120px;"
                       value="${new Date().year + 1900 - 1}"
                       oninput="document.getElementById('archiveBtn').textContent = 'Archive ' + (this.value || '?') + ' CSV files';
                                document.getElementById('unarchiveBtn').textContent = 'Unarchive ' + (this.value || '?') + ' CSV files';
                                document.getElementById('aggregateBtn').textContent = 'Annual Aggregation for ' + (this.value || '?');"/>
                <button id="aggregateBtn" class="btn btn-primary" onclick="aggregateByYear(); return false;">
                    <i class="fas fa-layer-group"></i>&nbsp;Annual Aggregation for ${new Date().year + 1900 - 1}
                </button>

                <button id="archiveBtn" class="btn btn-outline-primary" onclick="archiveByYear(); return false;">
                    <i class="fas fa-archive"></i>&nbsp;Archive ${new Date().year + 1900 - 1} CSV files
                </button>
                <button id="unarchiveBtn" class="btn btn-outline-primary" onclick="unarchiveByYear(); return false;">
                    <i class="fas fa-box-open"></i>&nbsp;Unarchive ${new Date().year + 1900 - 1} CSV files
                </button>
                <small class="text-muted ms-2 mt-3">
                    <a href="#" data-bs-toggle="modal" data-bs-target="#yearOpsHelpModal">
                        <i class="fas fa-question-circle"></i> Help
                    </a>
                </small>

            </div>
            <div id="yearOpStatus" class="text-muted fst-italic mt-1 w-100 text-center" style="display:none;"></div>

        </div>
        </g:if>

        <!-- Year Operations Help Modal -->
        <div class="modal fade" id="yearOpsHelpModal" tabindex="-1" aria-labelledby="yearOpsHelpModalLabel" aria-hidden="true">
            <div class="modal-dialog modal-lg">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="yearOpsHelpModalLabel"><i class="fas fa-question-circle"></i> Annual CSV Operations — Help</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <h6 class="fw-bold">Recommended Workflow</h6>
                        <ol>
                            <li>Run <strong>Annual Aggregation</strong> for the target year first.</li>
                            <li>Verify the aggregated file looks correct.</li>
                            <li>Run <strong>Archive</strong> to remove the weekly CSV files for that year.</li>
                            <li>If anything goes wrong, use <strong>Unarchive</strong> to restore the files — but remember to manually delete the aggregated file after.</li>
                        </ol>
                        <hr/>
                        <h6 class="fw-bold"><i class="fas fa-layer-group"></i> Annual Aggregation</h6>
                        <p>Combines all weekly CSV files for the selected year into a single summary file. The merged file is uploaded to S3 as:</p>
                        <code>biosecurity/&lt;year&gt;/annual-records.csv</code>
                        <p class="mt-2">This file will appear in the <strong>year folder</strong> in the file listing below. The weekly CSV files are <strong>not</strong> removed by this step.</p>
                        <hr/>
                        <h6 class="fw-bold"><i class="fas fa-archive"></i> Archive</h6>
                        <p>Archived files are <strong>invisible</strong> — they no longer appear in the file listing and are excluded from all operations, statistics, and aggregations.</p>
                        <hr/>
                        <h6 class="fw-bold"><i class="fas fa-box-open"></i> Unarchive</h6>
                        <p>Reverses the archive operation, restoring those CSVs for the selected year in the file listing.</p>
                        <div class="alert alert-warning mt-2">
                            <i class="fas fa-exclamation-triangle"></i> <strong>Important:</strong> If you need to unarchive after running Annual Aggregation, remember to <strong>manually delete the aggregated file</strong>
                            (<code>biosecurity/&lt;year&gt;/annual-records.csv</code>) once the unarchive is complete — otherwise both the weekly files and the merged file will coexist.
                        </div>
                        <hr/>
                        <div class="alert alert-info">
                            <i class="fas fa-clock"></i> <strong>Please note:</strong> These operations may take several minutes to complete as they process many files in S3.
                            <strong>Do not refresh the page</strong> while an operation is in progress — wait for the confirmation message before navigating away.
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
                    </div>
                </div>
            </div>
        </div>
%{--        <g:if test="${grailsApplication.config.getProperty('biosecurity.csv.s3.enabled', Boolean) == true}">--}%
%{--            &nbsp;&nbsp;--}%
%{--            <a class="btn btn-default pull-right" href="${createLink(controller: 'admin', action: 'moveLocalFilesToS3')}">--}%
%{--                <i class="fa fa-copy" aria-hidden="true" ></i>&nbsp;&nbsp;Copy all local files to S3--}%
%{--            </a>--}%
%{--        </g:if>--}%
        <hr>
    </div>

    <g:if test="${status == 0}">
        <div>
            <h2>Individual Biosecurity Alerts Data</h2>
            Download individual CSV files for each biosecurity alert email, detailing all occurrence records. Files are sorted by the date the alert was sent.
            <g:each in="${foldersAndFiles}" var="folder">
                <div class="folder" data-folder="${folder.name}">
                    <i class="fa fa-folder folder-icon folder" aria-hidden="true"></i> ${folder.name}
                    <a href="${createLink(
                        namespace: 'biosecurity',
                        controller: 'csv',
                        action: 'aggregate',
                        params: [name: folder.name]
                      )}">
                        <i class="fa fa-cloud-download" aria-hidden="true" title="Download as one CSV for the date."></i>
                    </a>
                </div>
                <div class="file-list" id="files-${folder.name}">
                    <g:each in="${folder.files}" var="file">
                        <div>
                            <a href="${createLink(namespace: 'biosecurity', controller: 'csv', action: 'download', params: [filename: folder.name + '/' + file.name])}">
                                <i class="fa fa-download" aria-hidden="true"></i>  ${file.name}
                            </a>
                            <span class="text-muted ms-2">(
                                ${file.formattedSize},
                                <g:if test="${file.lastUpdated}">
                                    <g:formatDate date="${new Date(file.lastUpdated as Long)}" format="yyyy-MM-dd HH:mm"/>
                                </g:if>
                                )
                            </span>
                            <a href="#" onclick="deleteFile('${folder.name}/${file.name}'); return false;">
                                <i class="fa fa-trash-o" aria-hidden="true"></i>
                            </a>
                        </div>
                    </g:each>
                </div>
            </g:each>
        </div>
    </g:if>
    <g:else>
        <h4>Error</h4>
        <code>${message}</code>
    </g:else>

</body>
</html>