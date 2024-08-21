<!DOCTYPE html>
<html lang="en">
<head>
    <title>Biosecurity Alerts Reporting</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <meta name="breadcrumb" content="BioSecurity CSV"/>
    <meta name="breadcrumbParent" content="${request.contextPath}/admin,Alerts admin"/>
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
    </script>
</head>
<body>
    <div>
        <h2>All Biosecurity Alerts Data</h2>
        Download a comprehensive CSV file detailing all occurrence records from every biosecurity alert sent. This includes both scheduled and manual sends.
        <br/>
        <a class="btn btn-primary " href="${createLink(controller: 'admin', action: 'aggregateBiosecurityAuditCSV', params: [folderName:'/'])}">
        <i class="fa fa-cloud-download" aria-hidden="true" ></i>  Download Full CSV Report
        </a>
        <hr>
    </div>

    <g:if test="${status == 0}">
        <div>
            <h2>Individual Biosecurity Alerts Data</h2>
            Download individual CSV files for each biosecurity alert email, detailing all occurrence records. Files are sorted by the date the alert was sent.
            <g:each in="${foldersAndFiles}" var="folder">
                <div class="folder" data-folder="${folder.name}">
                    <i class="fa fa-folder folder-icon folder" aria-hidden="true"></i> ${folder.name}
                    <a href="${createLink(controller: 'admin', action: 'aggregateBiosecurityAuditCSV', params: [folderName:folder.name])}">
                        <i class="fa fa-cloud-download" aria-hidden="true" title="Download as one CSV file for the date."></i>
                    </a>
                </div>
                <div class="file-list" id="files-${folder.name}">
                    <g:each in="${folder.files}" var="file">
                        <div><a href="${createLink(controller: 'admin', action: 'downloadBiosecurityAuditCSV', params: [filename:folder.name +'/' + file])}"><i class="fa fa-download" aria-hidden="true"></i>  ${file}</a></div>
                    </g:each>
                </div>
            </g:each>
        </div>
    </g:if>
    <g:else>
        ${message}
    </g:else>

</body>
</html>