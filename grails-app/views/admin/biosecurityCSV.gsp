<!DOCTYPE html>
<html lang="en">
<head>
    <title>Biosecurity Audit CSV </title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <meta name="breadcrumb" content="BioSecurity CSV"/>
    <meta name="breadcrumbParent" content="${request.contextPath}/admin,Alerts admin"/>
</head>
<body>
    <g:if test="${status == 0}">
        <g:each  var="file"  in="${files}">
            <a href="${createLink(controller: 'admin', action: 'downloadBiosecurityAuditCSV', params: [filename: file])}"> ${file}</a> <br/>
        </g:each>
    </g:if>
    <g:else>
        ${message}
    </g:else>

</body>
</html>