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
    <div class="panel-heading">
        <h3>User Management</h3>
        <ul>
            <li><g:link controller="admin" action="updateUserEmails">Update user emails with CAS</g:link> - synchronise alerts user database with  users from CAS.</li>
            <li class="controller"><a href="${request.contextPath}/admin/user">Manage alerts for users (find user)</a> - Find user(s) and manage their subscriptions.</li>
        </ul>
    </div>

    <div class="panel-heading">
        <h3>Alert and Query Management</h3>
        <ul>
            <li><g:link controller="notification" action="myAlerts">View my alerts</g:link> - View my current subscriptions.</li>
            <li><g:link controller="query" action="list">View list of alert types</g:link>  - View the list of all available custom and default alerts.</li>
            <li class="controller"><g:link controller="admin" action="notificationReport">
                View each alert type with counts for users</g:link> - View the list of all available custom and default alerts with user subscription count.</li>
            <li><g:link controller="admin" action="debugAllAlerts">Debug all alerts</g:link> - Dry-run of all alerts to determine if emails will be triggered on the next schedule. </li>

            <li><g:link controller="admin" action="deleteOrphanAlerts">Delete orphaned queries</g:link> - Remove queries no longer associated with Alert Notification/Subscription.</li>
        </ul>
        <h4>Fix empty/invalid notification_token</h4>
        <ul>
            <li><g:link controller="admin" action="repairNotificationsWithoutUnsubscribeToken">Fix empty notification_token values in Notification table (unsubscribe links with '?token=NULL')</g:link></li>
            <li><g:link controller="admin" action="repairUsersWithoutUnsubscribeToken">Fix empty notification_token values in user table (unsubscribe all links with '?token=NULL')</g:link></li>
        </ul>
    </div>
    <div class="panel-heading">
        <h3>Manage Scheduling</h3>
        <ul>
            <li class="controller"><g:link controller="quartz">View scheduling</g:link> - Run and/or reschedule alerts.</li>
        </ul>
    </div>

    <div class="panel-heading">
        <h3>Email Management </h3>
        <ul>
            <li class="controller"><g:link controller="admin" action="createBulkEmailForRegisteredUsers">
                Ad hoc bulk email to registered users</g:link> - Create and send custom email to registered users.</li>
            <li><g:link controller="admin" action="sendTestEmail">Send test email to yourself (tests server can send emails)</g:link>- Empty alert email to current user.</li>
        </ul>
    </div>

    <div class="panel-heading">
        <h3>BioSecurity Alerts</h3>
        <ul>
            <li class="controller"><a href="${request.contextPath}/admin/biosecurity">Manage BioSecurity alerts</a> - Add, update, or remove BioSecurity alerts and users.</li>
        </ul>
    </div>

    <div class="panel-heading">
        <h3>Application Management </h3>
        <ul>
            <plugin:isAvailable name="alaAdminPlugin">
                <li style="margin-top:1em;"><g:link controller="alaAdmin" action="index">ALA admin plugin page (system message, app config functions, build info)</g:link>
            </plugin:isAvailable>
        </ul>
    </div>



</div>
</body>
</html>
