<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="au.org.ala.alerts.Notification" %>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <meta name="breadcrumb" content="Unsubscribe alerts" />
    <meta name="breadcrumbParent" content="${request.contextPath?:'/'},Alerts" />
    <title><g:message code="unsubscribe.title" /></title>
</head>

<body>
    <div id="content">
        <header id="page-header" class="container mt-4">
            <div class="row">
                <div class="col">
                    <h1 class="h3">
                        <g:message code="unsubscribe.title" />
                    </h1>
                </div>
            </div>
        </header>

        <div id="page-body" class="container mt-2"  role="main">
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
                    <g:actionSubmit value="Unsubscribe" class="btn btn-outline-primary" action="unsubscribe"/>
                    <g:actionSubmit value="Cancel" class="btn btn-disable-outline" action="cancel"/>
                </g:form>
            </g:if>
        </div>
    </div>

</body>
</html>