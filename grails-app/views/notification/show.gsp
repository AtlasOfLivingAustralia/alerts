
<%@ page import="au.org.ala.alerts.Notification" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="${grailsApplication.config.skin.layout}" />
        <g:set var="entityName" value="${message(code: 'notification.label', default: 'Notification')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></span>
            <span class="menuButton"><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></span>
            <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="default.show.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
                <table>
                    <tbody>
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="notification.query.label" default="Query" /></td>
                            <td valign="top" class="value"><g:link controller="query" action="show" id="${notificationInstance?.query?.id}">${notificationInstance?.query?.encodeAsHTML()}</g:link></td>
                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="notification.userEmail.label" default="User Email" /></td>
                            <td valign="top" class="value">${fieldValue(bean: notificationInstance, field: "userEmail")}</td>
                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="notification.lastChanged.label" default="Last Changed" /></td>
                            <td valign="top" class="value"><g:formatDate date="${notificationInstance?.query?.lastChanged}" /></td>
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="notification.lastChecked.label" default="Last Checked" /></td>
                            <td valign="top" class="value"><g:formatDate date="${notificationInstance?.query?.lastChecked}" /></td>
                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="notification.refresh.label" default="Refresh" /></td>

                            <td valign="top" class="value">
                              <g:link controller="notification" action="checkNow" id="${notificationInstance?.id}">
                                <g:message code="show.check.now" />
                              </g:link>
                            </td>
                        </tr>

                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                    <g:hiddenField name="id" value="${notificationInstance?.id}" />
                    <span class="button"><g:actionSubmit class="edit" action="edit" value="${message(code: 'default.button.edit.label', default: 'Edit')}" /></span>
                    <span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
                </g:form>
            </div>
        </div>
    </body>
</html>
