<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="au.org.ala.alerts.Notification" %>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <meta name="breadcrumb" content="Unsubscribe" />
    <meta name="breadcrumbParent" content="${request.contextPath?:'/'},Alerts" />
    <title><g:message code="unsubscribe.title" /></title>
    <asset:stylesheet href="alerts.css"/>
</head>

<body>
<div id="content">
    <header id="page-header">
        <div class="inner row">
            <hgroup>
                <h1><g:message code="unsubscribe.title" /></h1>
            </hgroup>
        </div>
    </header>

    <div id="page-body" role="main">
        <g:if test="${!notifications}">
            <g:message code="unsubscribe.no.alerts" />
        </g:if>
        <g:elseif test="${notifications.size() == 1}">
            <g:message code="unsubscribe.stop.receiving" args="[notifications[0].query.name]" />
        </g:elseif>
        <g:else>
            <g:message code="unsubscribe.stop.receiving.sure" />
            <ul>
                <g:each in="${notifications.sort { it.query.name }}" var="notification">
                    <li>${notification.query.name}</li>
                </g:each>
            </ul>
        </g:else>

        <g:if test="${notifications}">
            <g:form controller="unsubscribe" action="unsubscribe" method="post">
                <g:hiddenField name="token" value="${params.token}"/>
                <g:actionSubmit value="Unsubscribe" class="btn btn-primary" action="unsubscribe"/>
                <g:actionSubmit value="Cancel" class="btn btn-default" action="cancel"/>
            </g:form>
        </g:if>
    </div>
</div>
<asset:javascript src="alerts.js"/>
</body>
</html>