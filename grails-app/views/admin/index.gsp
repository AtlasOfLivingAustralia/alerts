<html>
<head>
    <title>Notification service | ${grailsApplication.config.skin.orgNameLong}</title>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <meta name="breadcrumb" content="Admin functions" />
    <meta name="breadcrumbParent" content="${grailsApplication.config.grails.serverURL?:'/notification/myAlerts'},My alerts" />
    <asset:stylesheet href="alerts.css"/>
</head>

<body>

<g:if test="${message || flash.message}">
    <div class="alert alert-info">${message}${flash.message}</div>
</g:if>
<g:if test="${flash.errorMessage}">
    <div class="alert alert-danger">${flash.errorMessage}</div>
</g:if>
<div id="admin-functions">
    <div class="mb-4">
        <h4>User Management</h4>
        <ul>
            <li><g:link controller="admin" action="updateUserEmails">Update user emails with CAS</g:link> - synchronise alerts user database with  users from CAS.</li>
            <li class="controller"><a href="${request.contextPath}/admin/user">Manage alerts for users (find user)</a> - Find user(s) and manage their subscriptions.</li>
        </ul>
    </div>
    <div class="mb-4">
        <h4>Alert and Query Management</h4>
        <ul>
            <li><g:link controller="notification" action="myAlerts">View my alerts</g:link> - View my current subscriptions.</li>
            <li><g:link controller="query" action="list">View list of alert types</g:link>  - View the list of all available custom and default alerts.</li>
            <li class="controller"><g:link controller="admin" action="notificationReport">
                View each alert type with counts for users</g:link> - View the list of all available custom and default alerts with user subscription count.</li>
            <li class="admin"><a class="btn btn-primary" href="${request.contextPath}/admin/query">Debug and Test</a> - For testers and developers</li>
            <li class="admin">
                <div class="d-flex flex-wrap align-items-center">
                Simulating a
                <select id="frequencySimulated" class="form-select w-auto mx-2">
                    <option value="hourly">Hourly</option>
                    <option value="daily" selected>Daily</option>
                    <option value="weekly">Weekly</option>
                    <option value="monthly">Monthly</option>
                </select>
                Scheduled Job
                <a class="btn btn-primary ms-2 " id="simulatedFrequencyLink" href="${g.createLink(controller: 'admin', action: 'triggerQueriesByFrequency', params: [frequency: 'daily'])}" target="_blank">Run</a>
                <label>  <g:checkBox name="testMode" class="mx-2" checked="${grailsApplication.config.testMode ?: false}" />  Email me a copy </label>
                </div>
                <div class="mt-2">
                    <i>- Will NOT update the database, and emails will ONLY be sent in the Development environment.</i>
                </div>
            </li>
        </ul>
        <p>
        <h4>Fix empty/invalid notification_token</h4>
        <ul>
            <li><g:link controller="admin" action="repairNotificationsWithoutUnsubscribeToken"> Fix empty notification_token values in Notification table (unsubscribe links with '?token=NULL')</g:link></li>
            <li><g:link controller="admin" action="repairUsersWithoutUnsubscribeToken"> Fix empty notification_token values in user table (unsubscribe all links with '?token=NULL')</g:link></li>
            <li><g:link controller="admin" action="deleteOrphanAlerts">Delete orphaned queries</g:link> - Remove queries no longer associated with Alert Notification/Subscription.</li>
            %{--<li><g:link controller="admin" action="dryRunAllQueriesForFrequency" params="[frequency: 'daily']" target="_blank">Debug daily alerts</g:link> - Dry-run of daily alerts to determine if emails will be triggered on the next schedule. </li>--}%

        </ul>
    </div>
    <div class="mb-3">
        <h4>Manage Scheduling</h4>
        <ul>
            <li class="controller"><g:link controller="quartz">View scheduling</g:link> - Run and/or reschedule alerts.</li>
        </ul>
    </div>

%{--    <div class="mb-4">--}%
%{--        <h4>Email Management </h4>--}%
%{--        <ul>--}%
%{--            <li class="controller"><g:link controller="admin" action="createBulkEmailForRegisteredUsers">--}%
%{--                Ad hoc bulk email to registered users</g:link> - Create and send custom email to registered users.</li>--}%
%{--            <li><g:link controller="admin" action="sendTestEmail">Send test email to yourself (tests server can send emails)</g:link>- Empty alert email to current user.</li>--}%
%{--        </ul>--}%
%{--    </div>--}%

    <div class="mb-4">
        <h4>BioSecurity Alerts</h4>
        <ul>
            <li class="controller"><a href="${request.contextPath}/admin/biosecurity">Manage BioSecurity alerts</a> - Add, update, or remove BioSecurity alerts and users.</li>
        </ul>
    </div>

    <g:if test="${grailsApplication.config.getProperty('useBlogsAlerts', Boolean, true)}">
        <div class="mb-4">
            <h4>Alerts for News and Blogs</h4>
            <ul>
                <li class="controller"><a href="${request.contextPath}/admin/previewBlogAlerts">Preview alerts for the five most recent blogs</a></li>
            </ul>
        </div>
    </g:if>

    <div class="mb-4">
        <h4>Application Management </h4>
        <ul>
            <plugin:isAvailable name="alaAdminPlugin">
                <li style="margin-top:1em;"><g:link controller="alaAdmin" action="index">ALA admin plugin page (system message, app config functions, build info)</g:link>
            </plugin:isAvailable>
        </ul>
    </div>

    <div class="mb-4">
        <h4>Email testing </h4>
        <ul>
            <li class="controller"><g:link controller="admin" action="sendTestEmail">Send test email to yourself (tests server can send emails)</g:link></li>
        </ul>
    </div>
</div>

<script>
    $(document).ready(function() {
        // Update the link simulating the Quartz job
        function updateSimulationQueryLink() {
            let selectedValue = $('#frequencySimulated').val();
            let testModeChecked = $('[name=testMode]').is(':checked'); // Check if the checkbox is checked
            let queryLink = $('#simulatedFrequencyLink');

            let url = "${g.createLink(controller: 'admin', action: 'triggerQueriesByFrequency')}";
            url += "?frequency=" + selectedValue;
            if (testModeChecked) {
                url += "&testMode=true"; // Append testMode only if checked
            }
            queryLink.attr('href', url);
        }

        $('#frequencySimulated, [name=testMode]').change(updateSimulationQueryLink);
    });
</script>
</body>
</html>
