
<%@ page import="ala.postie.Query" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'query.label', default: 'Query')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<a href="#list-query" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
                <li><g:link controller="admin" class="home">Admin</g:link></li>
				<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
			</ul>
		</div>
		<div id="list-query" class="content scaffold-list" role="main">
			<h1><g:message code="default.list.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<table class="table">
				<thead>
					<tr>
					    <g:sortableColumn property="id" title="${message(code: 'query.description.label', default: 'ID')}" />
                        <g:sortableColumn property="resourceName" title="${message(code: 'query.description.label', default: 'Resource')}" />
						<g:sortableColumn property="description" title="${message(code: 'query.description.label', default: 'Description')}" />
                        <th></th>
					</tr>
				</thead>
				<tbody>
				<g:each in="${queryInstanceList}" status="i" var="queryInstance">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
						<td>${fieldValue(bean: queryInstance, field: "id")}</td>
                        <td>${fieldValue(bean: queryInstance, field: "resourceName")}</td>
						<td><g:link action="show" id="${queryInstance.id}">${fieldValue(bean: queryInstance, field: "description")}</g:link></td>
                        <td><g:link class="btn" action="debugAlert" controller="admin" id="${queryInstance.id}">Debug alert</g:link></td>
					</tr>
				</g:each>
				</tbody>
			</table>
			<div class="pagination">
				<g:paginate total="${queryInstanceTotal}" />
			</div>
		</div>
	</body>
</html>
