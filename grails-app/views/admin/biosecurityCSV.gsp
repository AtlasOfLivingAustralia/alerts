<!DOCTYPE html>
<html lang="en">
<head>
    <title>Biosecurity Audit CSV </title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <meta name="breadcrumb" content="BioSecurity CSV"/>
    <meta name="breadcrumbParent" content="${request.contextPath}/admin,Alerts admin"/>
    <style>
    .folder {
        cursor: pointer;
        margin: 10px 0;
        /*font-weight: bold;*/

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

                var icon = $(this).find('i.fa');
                icon.toggleClass('fa-folder fa-folder-open-o');
            });
        });
    </script>
</head>
<body>
    <g:if test="${status == 0}">
        <g:each in="${foldersAndFiles}" var="folder">
            <div class="folder" data-folder="${folder.name}">
                <i class="fa fa-folder folder-icon" aria-hidden="true"></i> ${folder.name}
            </div>
            <div class="file-list" id="files-${folder.name}">
                <g:each in="${folder.files}" var="file">
                    <div><a href="${createLink(controller: 'admin', action: 'downloadBiosecurityAuditCSV', params: [filename:folder.name +'/' + file])}"><i class="fa fa-download" aria-hidden="true"></i>  ${file}</a></div>
                </g:each>
            </div>
        </g:each>
    </g:if>
    <g:else>
        ${message}
    </g:else>

</body>
</html>