
<%@ page import="au.org.ala.alerts.Notification" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="${grailsApplication.config.skin.layout}" />
        <g:set var="entityName" value="${message(code: 'notification.label', default: 'Notification')}" />
        <title><g:message code="default.create.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></span>
            <span class="menuButton"><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="default.create.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${notificationInstance}">
            <div class="errors">
                <g:renderErrors bean="${notificationInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form action="save" >
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="query.id"><g:message code="notification.query.label" default="Query" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: notificationInstance, field: 'query', 'errors')}">
                                    <g:select name="query.id" from="${ala.postie.Query.list()}" optionKey="id" value="${notificationInstance?.query?.id}"  />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="userEmail"><g:message code="notification.userEmail.label" default="User Email" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: notificationInstance, field: 'userEmail', 'errors')}">
                                    <g:textField name="userEmail" value="${notificationInstance?.userEmail}" />
                                </td>
                            </tr>
                        
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><g:submitButton name="create" class="save" value="${message(code: 'default.button.create.label', default: 'Create')}" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
