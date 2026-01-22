<!DOCTYPE html>
<html>

<head>
    <title>Notification service | ${grailsApplication.config.skin.orgNameLong}</title>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <meta name="breadcrumb" content="Quartz management" />
    <meta name="breadcrumbParent" content="${request.contextPath}/admin, Admin"/>
    <asset:stylesheet href="alerts.css"/>
</head>
<body>

<div class="container mt-4">
    <g:if test="${flash.message}">
        <div class="alert alert-info alert-dismissible fade show mt-3" role="alert">
            ${flash.message}
        </div>
    </g:if>
    <h2 class="mb-4">Scheduled Quartz Jobs</h2>
    <g:if test="${flash.message}">
        <div class="flash-message" style="margin-bottom:20px">
            ${raw(flash.message)}
        </div>
    </g:if>

    <table class="table table-striped table-bordered table-hover">
        <thead class="table-light">
        <tr>
            <th>Job Name</th>
            <th>Job Group</th>
            <th>Trigger Name</th>
            <th>Trigger Group</th>
            <th>Next Fire Time</th>
            <th>Previous Fire Time</th>
            <th>State</th>
            <th>Schedule</th>
            <th>Developer</th>
        </tr>
        </thead>

        <tbody>
        <g:each in="${jobs}" var="job">
            <tr>
                <td>${job.jobName}</td>
                <td>${job.jobGroup}</td>
                <td>${job.triggerName}</td>
                <td>${job.triggerGroup}</td>
                <td><g:formatDate date="${job.nextFireTime}" format="EEE, dd MMM yyyy HH:mm z" timeZone="${session.timeZone}" /></td>
                <td><g:formatDate date="${job.previousFire}" format="EEE, dd MMM yyyy HH:mm z" timeZone="${session.timeZone}" /></td>
                <td>
                    <span class="badge ${job.state == 'NORMAL' ? 'badge-success' : job.state == 'PAUSED' ? 'badge-warning' : 'badge-danger'}">
                        ${job.state}
                    </span>
                </td>
                <td>
                    <g:link controller="quartz" action="pause" params="[jobName: job.jobName, jobGroup: job.jobGroup]">Pause</g:link> |
                    <g:link controller="quartz" action="resume" params="[jobName: job.jobName, jobGroup: job.jobGroup]">Resume</g:link>
                </td>
                <td>
                    <g:link controller="quartz" action="runNow" params="[jobName: job.jobName, jobGroup: job.jobGroup]">Run now</g:link>
                </td>
            </tr>
        </g:each>
        </tbody>
    </table>

</div>

</body>
</html>