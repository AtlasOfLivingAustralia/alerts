<html>
<head>
    <title>Notification service | ${grailsApplication.config.skin.orgNameLong}</title>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <meta name="breadcrumb" content="Admin functions" />
    <meta name="breadcrumbParent" content="${grailsApplication.config.grails.serverURL?:'/notification/myAlerts'},My alerts" />
    <asset:stylesheet href="alerts.css"/>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css" rel="stylesheet">
</head>

<body>

<g:if test="${message || flash.message}">
    <div class="alert alert-info">${message}${flash.message}</div>
</g:if>
<g:if test="${flash.errorMessage}">
    <div class="alert alert-danger">${flash.errorMessage}</div>
</g:if>
<div id="admin-functions">
    <div class="row mb-4 align-items-center">
        <div class="col-auto" ><i class="fa-solid fa-user-gear fa-2x text-primary"></i></div>
        <div class="col-auto">
            <h2> Admin Dashboard</h2>
            <div class="text-muted">
            Manage users, alerts and system settings.
            </div>
        </div>
    </div>
    <div class="two-col-masonry">
        <div class="box">
            <div class="shadow card card-body ">
                <div class="row">
                    <div class="fw-bold fs-5"><i class="fa-solid fa-user-group text-primary"></i> Users Management</div>
                    <div>
                        <small class="text-muted ps-4">&nbsp; Manage user accounts ans subscriptions</small>
                    </div>
                </div>

                <div class="mt-3">
                <g:link controller="admin" action="updateUserEmails">Update user emails with CAS</g:link>
                <small class="text-muted ms-2"> - synchronise alerts user database with users from CAS.</small>
                </div>
                <div>
                <a href="${request.contextPath}/admin/user">Manage alerts for users (find user)</a>
                <small class="text-muted ms-2"> - find user(s) and manage their subscriptions.</small>
                </div>
              </div>
        </div>

        <div class="box">
            <div class="shadow card card-body ">
                <div class="row">
                    <div class="fw-bold fs-5"><i class="fa-regular fa-calendar-days text-primary"></i> Manage Scheduling</div>
                    <div>
                        <small class="text-muted ps-4">Control when alerts are sent</small>
                    </div>
                </div>
             <div class="mt-3">
                 <g:link controller="quartz">View scheduling</g:link> <small class="text-muted ms-2"> - Run and/or reschedule alerts.</small>
             </div>
            </div>
        </div>

        <div class="box">
            <div class="shadow card card-body ">
                <div class="row">
                    <div class="fw-bold fs-5"><i class="fa-regular fa-bell text-primary"></i> Manage Alerts</div>
                    <div>
                        <small class="text-muted ps-4">View and test all alerts</small>
                    </div>
                </div>
                <div class="mt-3">
                    <div><g:link controller="notification" action="myAlerts">View my alerts</g:link> - my subscriptions.</div>
                    <div><g:link controller="query" action="list">View all alerts</g:link>  - list all available custom and default alerts.</div>
                    <div><g:link controller="admin" action="notificationReport">
                        View alerts with users</g:link> - view the user subscriptions per alert.</div>
                </div>
            </div>
        </div>

        <div class="box">
            <div class="shadow card card-body">
                <div class="fw-bold fs-5"><i class="fa-solid fa-bug text-primary"></i> Developer tools</div>
                <div  class="mt-4"><a class="btn btn-primary" href="${request.contextPath}/admin/query">Debug and Test</a></div>
                <hr>
                <div class="mt-2">
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
                </div>

            </div>
        </div>

        <div class="box">
            <div class="shadow card card-body">
                <div class="row">
                    <div class="fw-bold fs-5"><i class="fa-solid fa-shield-halved text-primary"></i> BioSecurity</div>
                    <div>
                        <small class="text-muted ps-4">Manage / reschedule Biosecurity alerts</small>
                    </div>
                </div>
                <div class="mt-2">
                    <a href="${request.contextPath}/admin/biosecurity">Manage BioSecurity alerts</a><small class="text-muted ms-2"> - Add, update, remove or reschedule BioSecurity alerts and users.</small>
                </div>
            </div>
        </div>

        <div class="box">
            <div class="shadow card card-body">
                <div class="row">
                    <div class="fw-bold fs-5"><i class="fa-solid fa-wrench text-primary"></i> Maintenance and fixes</div>
                    <div>
                        <small class="text-muted ps-4">Repair broken data</small>
                    </div>
                </div>
                <div class="mt-2">
                    <g:link controller="admin" action="repairNotificationsWithoutUnsubscribeToken">
                        Fix missing tokens in notification unsubscribe links
                    </g:link>
                    <small class="text-muted ms-2">
                        – Repairs notification unsubscribe links that are missing tokens
                    </small>
                </div>
                <div>
                    <g:link controller="admin" action="repairUsersWithoutUnsubscribeToken">
                        Fix user unsubscribe-all links with missing tokens
                    </g:link>
                    <small class="text-muted ms-2">
                        – Repairs links used to unsubscribe a user from all notifications
                    </small>
                </div>
                <div>
                    <g:link controller="admin" action="deleteOrphanAlerts">
                        Delete orphaned queries
                    </g:link>
                    <small class="text-muted ms-2">
                        – Removes queries without notifications or subscriptions
                    </small>
                    %{--<li><g:link controller="admin" action="dryRunAllQueriesForFrequency" params="[frequency: 'daily']" target="_blank">Debug daily alerts</g:link> - Dry-run of daily alerts to determine if emails will be triggered on the next schedule. </li>--}%
                </div>
            </div>
        </div>

        <div class="box">
            <div class="shadow card card-body">
                <div class="fw-bold fs-5"><i class="fa-regular fa-file-lines text-primary"></i> Alerts for News and Blogs</div>
                <div class="mt-2">
                    <a href="${request.contextPath}/admin/previewBlogAlerts">Preview alerts for the five most recent blogs</a>
                </div>
            </div>
        </div>

        <div class="box">
            <div class="shadow card card-body">
                <h5><i class="fa-solid fa-gear text-primary"></i> Application Management </h5>
                <div>
                    <plugin:isAvailable name="alaAdminPlugin">
                        <g:link controller="alaAdmin" action="index">ALA admin plugin page </g:link> <small class="text-muted ms-2"> - system message, app config functions, build info</small>
                    </plugin:isAvailable>
                </div>
            </div>
        </div>

        <div class="box">
            <div class="shadow card card-body">
                <h5><i class="fa-regular fa-envelope text-primary"></i> Email testing </h5>
                <div>
                    <g:link controller="admin" action="sendTestEmail">Send an email to yourself</g:link> <small class="text-muted ms-2"> - Test if emails server works </small>
                </div>
            </div>
        </div>


    </div>





%{--    <div class="mb-4">--}%
%{--        <h4>Email Management </h4>--}%
%{--        <ul>--}%
%{--            <li class="controller"><g:link controller="admin" action="createBulkEmailForRegisteredUsers">--}%
%{--                Ad hoc bulk email to registered users</g:link> - Create and send custom email to registered users.</li>--}%
%{--            <li><g:link controller="admin" action="sendTestEmail">Send test email to yourself (tests server can send emails)</g:link>- Empty alert email to current user.</li>--}%
%{--        </ul>--}%
%{--    </div>--}%
    <div class="row mb-4">

    </div>


    <div class="row mb-4">

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
