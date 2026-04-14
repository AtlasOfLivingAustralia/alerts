<!doctype html>
<html>
    <head>
        <title>Error Logs</title>
        <meta name="layout" content="${grailsApplication.config.skin.layout}" />
        <meta name="breadcrumb" content="Error Logs"/>
        <meta name="breadcrumbParent" content="${grailsApplication.config.grails.serverURL?:'/admin'},Admin" />
    </head>
    <body class="bg-light">
        <div class="container py-5">
            <h1 class="mb-4">Error Logs</h1>
            <table class="table table-striped">
                <thead>
                    <tr>
                        <th>Executed At</th>
                        <th>Query Type</th>
                        <th>Query Name</th>
                        <th>Reviewed</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                <g:each in="${errorLogList}" var="errorLog">
                    <tr>
                        <td>${errorLog.executedAt}</td>
                        <td>${errorLog.queryType}</td>
                        <td><g:link action="show" id="${errorLog.id}">${errorLog.queryName}</g:link></td>
                        <td>
                            <g:form action="update" method="POST" style="display:inline;">
                                <g:hiddenField name="id" value="${errorLog.id}"/>
                                <input type="hidden" name="_method" value="PUT"/>
                                <input type="checkbox" name="reviewed" value="true" onchange="this.form.submit()" ${errorLog.reviewed ? 'checked' : ''}/>
                            </g:form>
                        </td>
                        <td>
                            <g:form action="delete" id="${errorLog.id}" method="DELETE">
                                <g:submitButton name="delete" value="Delete"/>
                            </g:form>
                        </td>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </div>
    </body>
</html>
