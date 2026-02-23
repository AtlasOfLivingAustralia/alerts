<!DOCTYPE html>
<html lang="en">
<head>
    <title>Biosecurity Alerts Reporting</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <meta name="breadcrumb" content="CSV"/>
    <meta name="breadcrumbParent" content="${request.contextPath}/admin/biosecurity,BioSecurity"/>
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
                icon.toggleClass('fa-folder fa-folder-open-o');
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
    </script>
</head>
<body>
    <div>
        <h4 class="pull-right">
            <span class="label label-info">
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

        <a class="btn btn-primary " href="${createLink( namespace: 'biosecurity', controller: 'csv', action: 'aggregate', params: [name:'/'])}" onclick="return confirmDownload();">
            <i class="fa fa-cloud-download" aria-hidden="true" ></i>&nbsp;&nbsp;Download Full CSV Report
        </a>
        <g:if test="${grailsApplication.config.getProperty('biosecurity.csv.s3.enabled', Boolean) == true}">
            &nbsp;
            <a href="${createLink( namespace: 'biosecurity', controller: 'csv', action: 'asyncAggregate', absolute: true)}" >
                <i class="fas fa-shipping-fast"></i>&nbsp;&nbsp;Email Me Full CSV Report (!Beta)
            </a>
            &nbsp;
            <a href="${createLink( namespace: 'biosecurity', controller: 'csv', action: 'downloads', absolute: true)}" >
                <i class="fas fa-history"></i> logs
            </a>

        </g:if>
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
                            <a href="${createLink( namespace: 'biosecurity', controller: 'csv', action: 'download', params: [filename:folder.name +'/' + file])}"><i class="fa fa-download" aria-hidden="true"></i>  ${file}</a>
                            <a href="#" onclick="deleteFile('${folder.name}/${file}'); return false;">
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