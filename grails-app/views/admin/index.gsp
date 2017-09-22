<html>
<head>
    <title>Notification service | ${grailsApplication.config.skin.orgNameLong}</title>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <meta name="breadcrumb" content="Admin functions" />
    <meta name="breadcrumbParent" content="${grailsApplication.config.grails.serverURL?:'/'},Alerts" />
    <asset:stylesheet href="alerts.css"/>
</head>

<body>
<h1>Admin functions - Alert service</h1>

<g:if test="${message || flash.message}">
    <div class="alert alert-info">${message}${flash.message}</div>
</g:if>
<g:if test="${flash.errorMessage}">
    <div class="alert alert-danger">${flash.errorMessage}</div>
</g:if>

<div id="admin-functions">
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
        <li class="controller"><a href="${request.contextPath}/admin/user">Manage alerts for users (find user)</a></li>
        <li><g:link controller="admin" action="sendTestEmail">Send test email to yourself (tests server can send emails)</g:link></li>
        <plugin:isAvailable name="alaAdminPlugin">
            <li style="margin-top:1em;"><g:link controller="alaAdmin" action="index">ALA admin plugin page (system message, app config functions, build info)</g:link>
        </plugin:isAvailable>
    </ul>
</div>
</body>
</html>
