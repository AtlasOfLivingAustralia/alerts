
<%@ page import="au.org.ala.alerts.Query" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="${grailsApplication.config.skin.layout}" />
		<meta name="breadcrumb" content="Queries" />
		<meta name="breadcrumbParent" content="${createLink(controller:'admin')},Admin" />
		<g:set var="entityName" value="${message(code: 'query.label', default: 'Query')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
		<asset:stylesheet href="alerts.css"/>
	</head>
	<body>
		<div class="d-flex justify-content-end align-items-center">
			<a href="${createLink(action:'create')}"
			   class="btn btn-outline-primary">
				<g:message code="default.new.label" args="[entityName]" />
			</a>
		</div>
		<div id="list-query" class="content scaffold-list" role="main">
			<h3><g:message code="default.list.label" args="[entityName]" /> :  ${queryInstanceTotal}</h3>
			<g:if test="${flash.message}">
			<div class="alert alert-danger" role="status">${flash.message}</div>
			</g:if>
			<table class="table table-bordered table-striped">
				<thead>
					<tr>
					    <g:sortableColumn property="id" title="${message(code: 'query.description.label', default: 'ID')}" />
                        <g:sortableColumn property="description" title="${message(code: 'query.description.label', default: 'Query title')}" />
                        %{--<g:sortableColumn property="resourceName" title="${message(code: 'query.description.label', default: 'Resource')}" />--}%
                        <th></th>
					</tr>
				</thead>
				<tbody>
				<g:each in="${queryInstanceList}" status="i" var="queryInstance">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
						<td>${fieldValue(bean: queryInstance, field: "id")}</td>
                        %{--<td>${fieldValue(bean: queryInstance, field: "resourceName")}</td>--}%
						<td><g:link action="show" id="${queryInstance.id}">${fieldValue(bean: queryInstance, field: "description")}</g:link></td>
                        <td><g:link class="btn btn-info btn-xs" action="debugAlert" controller="admin" id="${queryInstance.id}"><g:message code="query.list.debug" /></g:link></td>
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
