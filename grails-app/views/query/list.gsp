<%@ page import="ala.postie.Query" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="${grailsApplication.ala.layout}" />
  <g:set var="entityName" value="${message(code: 'query.label', default: 'Query')}"/>
  <title><g:message code="default.list.label" args="[entityName]"/></title>
</head>

<body>
<div class="nav">
  <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a>
  </span>
  <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label"
                                                                             args="[entityName]"/></g:link></span>
</div>

<div class="body">
  <h1><g:message code="default.list.label" args="[entityName]"/></h1>
  <g:if test="${flash.message}">
    <div class="message">${flash.message}</div>
  </g:if>
  <div class="list">
    <table>
      <thead>
      <tr>
        <g:sortableColumn property="id" title="${message(code: 'query.id.label', default: 'Id')}"/>
        <g:sortableColumn property="name" title="${message(code: 'query.name.label', default: 'Name')}"/>
        <g:sortableColumn property="description" title="${message(code: 'query.description.label', default: 'Description')}"/>
      </tr>
      </thead>
      <tbody>
      <g:each in="${queryInstanceList}" status="i" var="queryInstance">
        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
          <td><g:link action="show" id="${queryInstance.id}">${fieldValue(bean: queryInstance, field: "id")}</g:link></td>
          <td>${fieldValue(bean: queryInstance, field: "name")}</td>
          <td>${fieldValue(bean: queryInstance, field: "description")}</td>
        </tr>
      </g:each>
      </tbody>
    </table>
  </div>

  <div class="paginateButtons">
    <g:paginate total="${queryInstanceTotal}"/>
  </div>
</div>
</body>
</html>
