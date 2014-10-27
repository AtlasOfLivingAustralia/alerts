<%@ page import="groovy.json.JsonOutput; groovy.json.JsonSlurper" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'query.label', default: 'Query')}" />
    <title>Notification report | Atlas of Living Australia</title>
</head>
<body>
<a href="#list-query" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
<div class="nav" role="navigation">
    <ul>
        <li><g:link controller="admin" class="home">Admin</g:link></li>
        <li><i class="icon-bell"></i> Debug alert ID: <g:link controller="query" id="${alerts.values().iterator().next().query.id}"> ${alerts.values().iterator().next().query.id} - ${alerts.values().iterator().next().query.name}</g:link></li>
    </ul>
</div>
<h1>${alerts.values().iterator().next().query.name}</h1>
<g:each in="${alerts.keySet()}" var="key">

    <g:set var="qcr" value="${alerts.get(key)}"/>

    <div id="list-query" class="content scaffold-list" role="main">
        <h2> ${key}</h2>
        <ul style="list-style: none; padding:0; margin:0;">
            <li>URL checked: <a href="${qcr.urlChecked}">${qcr.urlChecked}</a></li>
            <li>Frequency: ${qcr.frequency}</li>
            <li>Would send an email: ${qcr.queryResult.hasChanged}</li>
            <li>Errored: ${qcr.errored}</li>
            <li><g:link controller="admin" action="debugAlertEmail" params="[id:qcr.query.id, frequency:key]" class="btn">View debug email - ${key}</g:link></li>
        </ul>
    </div>

</g:each>
</body>
</html>
