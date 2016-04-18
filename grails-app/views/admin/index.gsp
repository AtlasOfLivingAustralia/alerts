<html>
<head>
    <title>Notification service | ${grailsApplication.config.skin.orgNameLong}</title>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
</head>

<body>
<h1>Admin functions - Alert service</h1>

<g:if test="${message}">
    <div class="alert alert-info">${message}</div>
</g:if>

<div>
    <ul>
        <li><g:link controller="notification" action="myAlerts">View my alerts</g:link></li>
        <li><g:link controller="admin" action="debugAllAlerts">Debug all alerts</g:link></li>
        <li><g:link controller="admin" action="updateUserEmails">Update user emails with CAS</g:link></li>
        <li><g:link controller="query" action="list">View list of alert types</g:link></li>
        <li><g:link controller="admin" action="deleteOrphanAlerts">Delete orphaned queries</g:link></li>
        <li class="controller"><g:link controller="quartz">View scheduling</g:link></li>
        <li class="controller"><g:link controller="admin" action="createBulkEmailForRegisteredUsers">
            Ad hoc bulk email to registered users</g:link></li>
        <li class="controller"><g:link controller="admin" action="notificationReport">
            Each query type with counts for users</g:link></li>
        <li class="controller"><a href="${request.contextPath}/admin/user">Manage alerts for users</a></li>
    </ul>
</div>
</body>
</html>
