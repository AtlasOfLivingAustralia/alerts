
<%@ page import="au.org.ala.alerts.Notification" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="${grailsApplication.ala.layout}" />
        <g:set var="entityName" value="${message(code: 'notification.label', default: 'Notification')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></span>
            <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="default.list.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                            <g:sortableColumn property="id" title="${message(code: 'notification.id.label', default: 'Id')}" />
                            <th><g:message code="notification.query.label" default="Query" /></th>
                            <g:sortableColumn property="description" title="${message(code: 'notification.description.label', default: 'Description')}" />
                            <g:sortableColumn property="userEmail" title="${message(code: 'notification.userEmail.label', default: 'User')}" />
                            <g:sortableColumn property="lastChanged" title="${message(code: 'notification.lastChanged.label', default: 'Last Changed')}" />
                            <g:sortableColumn property="lastChecked" title="${message(code: 'notification.lastChecked.label', default: 'Last Checked')}" />
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${notificationInstanceList}" status="i" var="notificationInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                            <td><g:link action="show" id="${notificationInstance.id}">${fieldValue(bean: notificationInstance, field: "id")}</g:link></td>
                            <td>${fieldValue(bean: notificationInstance, field: "query")}</td>
                            <td>${notificationInstance.query.description}</td>
                            <td>${fieldValue(bean: notificationInstance, field: "userEmail")}</td>
                            <td><g:formatDate date="${notificationInstance.query.lastChanged}" /></td>
                            <td><g:formatDate date="${notificationInstance.query.lastChecked}" /></td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${notificationInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
