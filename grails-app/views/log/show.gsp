<!doctype html>
<html>
    <head>
        <title>Show ErrorLog</title>
        <meta name="layout" content="${grailsApplication.config.skin.layout}" />
        <meta name="breadcrumb" content="${errorLog.id}"/>
        <meta name="breadcrumbParent" content="${grailsApplication.config.grails.serverURL?:'/Log'},Logs" />
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
    </head>
    <body class="bg-light">
        <div class="container py-5">
            <h1>Show ErrorLog</h1>
            <ul>
                <li>ID: ${errorLog.id}</li>
                <li>Executed At: ${errorLog.executedAt}</li>
                <li>Query Type: ${errorLog.queryType}</li>
                <li>Query Name: ${errorLog.queryName}</li>
                <li>Reviewed: ${errorLog.reviewed}</li>
                <li>Context: ${errorLog.context}</li>
                <li>Stack Trace: <pre>${errorLog.stackTrace}</pre></li>
            </ul>
            <g:link action="index">Back to List</g:link>
        </div>
    </body>
</html>

