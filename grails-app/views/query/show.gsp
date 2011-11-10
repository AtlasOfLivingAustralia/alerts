<%@ page import="org.codehaus.groovy.grails.web.json.JSONObject; ala.postie.Query" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="main"/>
  <g:set var="entityName" value="${message(code: 'query.label', default: 'Query')}"/>
  <title><g:message code="default.show.label" args="[entityName]"/></title>
</head>

<body>
<div class="nav">
  <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a>
  </span>
  <span class="menuButton"><g:link class="list" action="list"><g:message code="default.list.label"
                                                                         args="[entityName]"/></g:link></span>
  <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label"
                                                                             args="[entityName]"/></g:link></span>
</div>

<div class="body">
  <h1><g:message code="default.show.label" args="[entityName]"/></h1>
  <g:if test="${flash.message}">
    <div class="message">${flash.message}</div>
  </g:if>
  <div class="dialog">
    <table>
      <tbody>

      <tr class="prop">
        <td valign="top" class="name"><g:message code="query.name.label" default="Name"/></td>
        <td valign="top" class="value">${fieldValue(bean: queryInstance, field: "name")}</td>
      </tr>

      <tr class="prop">
        <td valign="top" class="name"><g:message code="query.uiurl.label" default="Url for humans"/></td>
        <td valign="top" class="value">
          <a href="${fieldValue(bean: queryInstance, field: "baseUrl")}${fieldValue(bean: queryInstance, field: "queryPathForUI")}">User interface URL</a>
        </td>
      </tr>

      <tr class="prop">
        <td valign="top" class="name"><g:message code="query.webserviceurl.label" default="Url for machines"/></td>
        <td valign="top" class="value">
          <a href="${fieldValue(bean: queryInstance, field: "baseUrl")}${fieldValue(bean: queryInstance, field: "queryPath")}">Webservice URL</a>
        </td>
      </tr>

      <tr class="prop">
          <td valign="top" class="name"><g:message code="notification.lastResult.label" default="Last Result" /></td>
          <td valign="top" class="value">
            <g:textArea rows="30" cols="100" name="lastResult" readonly="true">
              <g:if test="${queryInstance?.lastResult}">${new JSONObject(queryInstance?.lastResult).toString(2)}</g:if>
            </g:textArea>
       </td>
      </tr>
      </tbody>
    </table>
  </div>

  <div class="buttons">
    <g:form>
      <g:hiddenField name="id" value="${queryInstance?.id}"/>
      <span class="button"><g:actionSubmit class="edit" action="edit"
                                           value="${message(code: 'default.button.edit.label', default: 'Edit')}"/></span>
      <span class="button"><g:actionSubmit class="delete" action="delete"
                                           value="${message(code: 'default.button.delete.label', default: 'Delete')}"
                                           onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"/></span>
    </g:form>
  </div>
</div>
</body>
</html>
