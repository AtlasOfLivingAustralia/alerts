
<%@ page import="au.org.ala.alerts.Query" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="${grailsApplication.config.skin.layout}" />
		<g:set var="entityName" value="${message(code: 'query.label', default: 'Query')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<a href="#list-query" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
				<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
			</ul>
		</div>
		<div id="list-query" class="content scaffold-list" role="main">
			<h1><g:message code="default.list.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
				<div class="message" role="status">${flash.message}</div>
			</g:if>
			<table>
			<thead>
					<tr>
					
						<g:sortableColumn property="description" title="${message(code: 'query.description.label', default: 'Description')}" />
					
						<g:sortableColumn property="dateFormat" title="${message(code: 'query.dateFormat.label', default: 'Date Format')}" />
					
						<g:sortableColumn property="idJsonPath" title="${message(code: 'query.idJsonPath.label', default: 'Id Json Path')}" />
					
						<g:sortableColumn property="recordJsonPath" title="${message(code: 'query.recordJsonPath.label', default: 'Record Json Path')}" />
					
						<g:sortableColumn property="updateMessage" title="${message(code: 'query.updateMessage.label', default: 'Update Message')}" />
					
						<g:sortableColumn property="queryPathForUI" title="${message(code: 'query.queryPathForUI.label', default: 'Query Path For UI')}" />
					
					</tr>
				</thead>
				<tbody>
				<g:each in="${queryInstanceList}" status="i" var="queryInstance">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
					
						<td><g:link action="show" id="${queryInstance.id}">${fieldValue(bean: queryInstance, field: "description")}</g:link></td>
					
						<td>${fieldValue(bean: queryInstance, field: "dateFormat")}</td>
					
						<td>${fieldValue(bean: queryInstance, field: "idJsonPath")}</td>
					
						<td>${fieldValue(bean: queryInstance, field: "recordJsonPath")}</td>
					
						<td>${fieldValue(bean: queryInstance, field: "updateMessage")}</td>
					
						<td>${fieldValue(bean: queryInstance, field: "queryPathForUI")}</td>
					
					</tr>
				</g:each>
				</tbody>
			</table>
			<div class="pagination">
				<g:paginate total="${queryInstanceCount ?: 0}" />
			</div>
		</div>
	</body>
</html>
