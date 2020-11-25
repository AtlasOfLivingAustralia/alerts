<%@ page import="groovy.json.JsonOutput; groovy.json.JsonSlurper" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="${grailsApplication.config.skin.layout}" />
    <g:set var="entityName" value="${message(code: 'query.label', default: 'Query')}" />
    <title>Notification report | ${grailsApplication.config.skin.orgNameLong}</title>
</head>
<body>
<a href="#list-query" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
<div class="nav" role="navigation">
    <ul>
        <li><g:link controller="admin" class="home">Admin</g:link></li>
        <li><i class="icon-bell"></i> Debug alert ID: <g:link controller="query" action="show" id="${query.id}"> ${query.id} - ${query.name}</g:link></li>
    </ul>
</div>
<h1>${query.name}</h1>
<g:each in="${alerts.keySet()}" var="key">

    <div id="list-query" class="content scaffold-list" role="main">
        <h2>${key}</h2>
        <g:if test="${alerts.get(key).size() > 0}">
            <g:each in="${alerts.get(key)}" var="qcr">
            <div class="${qcr.errored ? 'alert alert-danger' : 'alert alert-info'}">
                <ul style="list-style: none; padding:0; margin:0;">
                    <li>URL checked: <a href="${qcr.urlChecked}">${qcr.urlChecked}</a></li>
                    <li>Would send an email: ${qcr.queryResult.hasChanged}
                    </li>
                    <li>Errored: ${qcr.errored}</li>
                    <li><g:link controller="admin" action="debugAlertEmail" params="[id:qcr.query.id, frequency:key, uid:qcr?.queryResult?.user?.id]" class="btn btn-sm btn-ala">View debug email - ${key}</g:link></li>
                </ul>
            </div>
            </g:each>
        </g:if>
        <g:else>
            No user subscribes to this notification.
        </g:else>
    </div>

</g:each>
</body>
</html>
