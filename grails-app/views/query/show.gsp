
<%@ page import="au.org.ala.alerts.Query" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="${grailsApplication.config.skin.layout}" />
		<g:set var="entityName" value="${message(code: 'query.label', default: 'Query')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
		<meta name="breadcrumbParent" content="${request.contextPath}/query, Query List"/>
		<meta name="breadcrumb" content=" Query - ${queryInstance.name}"/>
		<asset:stylesheet href="alerts.css"/>
	</head>
	<body>
		<div id="show-query" class="container mt-4" role="main">
			<g:if test="${flash.message}">
				<div class="alert alert-info" role="alert">
					${flash.message}
				</div>
			</g:if>

			<dl class="row col-md-10 mx-auto">
				<g:if test="${queryInstance?.name}">
					<dt class="col-sm-3"><g:message code="query.name.label" default="Name" /></dt>
					<dd class="col-sm-9">${queryInstance.name}</dd>
				</g:if>

				<g:if test="${queryInstance?.description}">
					<dt class="col-sm-3"><g:message code="query.description.label" default="Description" /></dt>
					<dd class="col-sm-9">${queryInstance.description}</dd>
				</g:if>

				<g:if test="${queryInstance?.emailTemplate}">
					<dt class="col-sm-3"><g:message code="query.emailTemplate.label" default="Email Template" /></dt>
					<dd class="col-sm-9">${queryInstance.emailTemplate}</dd>
				</g:if>

				<g:if test="${queryInstance?.dateFormat}">
					<dt class="col-sm-3"><g:message code="query.dateFormat.label" default="Date Format" /></dt>
					<dd class="col-sm-9"><g:fieldValue bean="${queryInstance}" field="dateFormat"/></dd>
				</g:if>

				<g:if test="${queryInstance?.idJsonPath}">
					<dt class="col-sm-3">Id JSON Path</dt>
					<dd class="col-sm-9">${queryInstance.idJsonPath}</dd>
				</g:if>

				<g:if test="${queryInstance?.recordJsonPath}">
					<dt class="col-sm-3">Record JSON Path</dt>
					<dd class="col-sm-9">${queryInstance.recordJsonPath}</dd>
				</g:if>

				<g:if test="${queryInstance?.updateMessage}">
					<dt class="col-sm-3"><g:message code="query.updateMessage.label" default="Update Message" /></dt>
					<dd class="col-sm-9">${queryInstance.updateMessage}</dd>
				</g:if>

				<g:if test="${queryInstance?.queryPathForUI}">
					<dt class="col-sm-3"><g:message code="query.queryPathForUI.label" default="Query Path For UI" /></dt>
					<dd class="col-sm-9">${queryInstance.queryPathForUI}</dd>
				</g:if>

				<g:if test="${queryInstance?.queryPath}">
					<dt class="col-sm-3"><g:message code="query.queryPath.label" default="Query Path" /></dt>
					<dd class="col-sm-9">${queryInstance.queryPath}"</dd>
				</g:if>

				<g:if test="${queryInstance?.baseUrl}">
					<dt class="col-sm-3"><g:message code="query.baseUrl.label" default="Base Url" /></dt>
					<dd class="col-sm-9">${queryInstance.baseUrl}</dd>
				</g:if>

				<g:if test="${queryInstance?.baseUrlForUI}">
					<dt class="col-sm-3"><g:message code="query.baseUrlForUI.label" default="Base Url For UI" /></dt>
					<dd class="col-sm-9">${queryInstance.baseUrlForUI}"</dd>
				</g:if>

				<g:if test="${queryInstance?.custom}">
					<dt class="col-sm-3"><g:message code="query.custom.label" default="Custom" /></dt>
					<dd class="col-sm-9">${queryInstance.custom}</dd>
				</g:if>

				<g:if test="${queryInstance?.propertyPaths}">
					<dt class="col-sm-3"><g:message code="query.propertyPaths.label" default="Property Paths" /></dt>
					<dd class="col-sm-9">${queryInstance.propertyPaths}</dd>
				</g:if>

				<g:if test="${queryInstance?.queryResults}">
					<dt class="col-sm-3"><g:message code="query.queryResults.label" default="Query Results" /></dt>
					<dd class="col-sm-9">${queryInstance.queryResults}</dd>
				</g:if>

				<g:if test="${queryInstance?.resourceName}">
					<dt class="col-sm-3"><g:message code="query.resourceName.label" default="Resource Name" /></dt>
					<dd class="col-sm-9">${queryInstance.resourceName}</dd>
				</g:if>


				<dt class="col-sm-3"><g:message code="query.nousers.label" default="Number of users registered for alert" /></dt>
				<dd class="col-sm-9">${queryInstance.notifications?.size()}</dd>


				<dt class="col-sm-3">Custom</dt>
				<dd class="col-sm-9">
					<g:formatBoolean boolean="${queryInstance.custom}" />
				</dd>
			</dl>

			<g:form url="[resource:queryInstance, action:'delete']" method="POST">
				<div class="d-flex gap-2 mt-3 justify-content-center">
				<!-- Edit button -->
					<g:link class="btn btn-primary" action="edit" resource="${queryInstance}">
						<g:message code="default.button.edit.label" default="Edit" />
					</g:link>

				<!-- Delete button (only if no notifications) -->
					<g:if test="${queryInstance?.notifications?.size() == 0}">
						<button type="submit" class="btn btn-danger"
								onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
							${message(code: 'default.button.delete.label', default: 'Delete')}
						</button>
					</g:if>

				<!-- Debug button -->
					<g:link class="btn btn-secondary" controller="admin" action="debugAlert" id="${queryInstance.id}">
						Debug this query
					</g:link>
				</div>
			</g:form>
		</div>
	</body>
</html>
