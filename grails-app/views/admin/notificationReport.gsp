<!doctype html>
<html>
	<head>
		<meta name="layout" content="${grailsApplication.config.skin.layout}" />
		<g:set var="entityName" value="${message(code: 'query.label', default: 'Query')}" />
		<title>Notification report | ${grailsApplication.config.skin.orgNameLong?: 'Atlas of Living Australia'}</title>
		<meta name="breadcrumb" content="Notification report"/>
		<meta name="breadcrumbParent" content="${request.contextPath}/admin, Admin"/>
	</head>
	<body>
		<div id="list-query" class="content scaffold-list" role="main">
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<table class="table">
				<thead>
					<tr>
					    <g:sortableColumn property="id" title="Alert ID" />
                        <g:sortableColumn property="resourceName" title="Resource" />
						<g:sortableColumn property="description" title="Alert name" />
                        <th>Number of users</th>
					</tr>
				</thead>
				<tbody>
				<g:each in="${queryInstanceList}" status="i" var="queryInstance">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
						<td>${fieldValue(bean: queryInstance, field: "id")}</td>
                        <td>${fieldValue(bean: queryInstance, field: "resourceName")}</td>
						<td><g:link action="show" controller="query" id="${queryInstance.id}">${fieldValue(bean: queryInstance, field: "description")}</g:link></td>
                        <td>${queryInstance.notifications.size()}</td>
					</tr>
				</g:each>
				</tbody>
			</table>
		</div>
	</body>
</html>
