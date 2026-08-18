
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
					<dd class="col-sm-9">${queryInstance.queryPath}</dd>
				</g:if>

				<g:if test="${queryInstance?.baseUrl}">
					<dt class="col-sm-3"><g:message code="query.baseUrl.label" default="Base Url" /></dt>
					<dd class="col-sm-9">${queryInstance.baseUrl}</dd>
				</g:if>

				<g:if test="${queryInstance?.baseUrlForUI}">
					<dt class="col-sm-3"><g:message code="query.baseUrlForUI.label" default="Base Url For UI" /></dt>
					<dd class="col-sm-9">${queryInstance.baseUrlForUI}</dd>
				</g:if>

				<g:if test="${queryInstance?.propertyPaths}">
					<dt class="col-sm-3"><g:message code="query.propertyPaths.label" default="Property Paths" /></dt>
					<dd class="col-sm-9">${queryInstance.propertyPaths}</dd>
				</g:if>

				<g:if test="${queryInstance?.queryResults}">
					<dt class="col-sm-3"><g:message code="query.queryResults.label" default="Query Results" /></dt>
					<dd class="col-sm-9">
						<g:each in="${queryInstance.queryResults.sort { it.frequency?.name }}" var="queryResult">
							<div>
								<b>${queryResult.frequency?.name?.toUpperCase()}</b>
								<g:if test="${queryResult.hasChanged}">
									<span class="badge bg-info">Changed</span>
								</g:if>
								<g:else>
									<span class="badge bg-dark">No changes</span>
								</g:else>
								<g:link controller="queryResult" action="getDetails" params="[id: queryResult.id]"
										target="_blank" title="Show the latest query result - QR ID: ${queryResult.id}">#${queryResult.id}</g:link>
								<span class="text-muted">last checked: ${queryResult.lastChecked ?: 'never'}</span>
							</div>
						</g:each>
					</dd>
				</g:if>

				<g:if test="${queryInstance?.resourceName}">
					<dt class="col-sm-3"><g:message code="query.resourceName.label" default="Resource Name" /></dt>
					<dd class="col-sm-9">${queryInstance.resourceName}</dd>
				</g:if>

				<g:set var="subscriberEmails"
					   value="${queryInstance.notifications?.collect { it.user?.email }?.findAll { it }?.unique()?.sort()}"/>
				<dt class="col-sm-3"><g:message code="query.nousers.label" default="Number of users registered for alert" /></dt>
				<dd class="col-sm-9">
					${queryInstance.notifications?.size() ?: 0}
					<g:if test="${subscriberEmails}">
						-- <span class="text-muted" style="word-break: break-word;"
								 title="${subscriberEmails.join(', ')}">${subscriberEmails.take(5).join(', ')}<g:if test="${subscriberEmails.size() > 5}"> ... and ${subscriberEmails.size() - 5} more</g:if></span>
					</g:if>
				</dd>

				<g:if test="${queryInstance?.custom}">
					<dt class="col-sm-3"><g:message code="query.custom.label" default="Custom" /></dt>
					<dd class="col-sm-9">${queryInstance.custom}</dd>
				</g:if>
			</dl>

			<%@ page import="java.time.LocalDate" %>
			<%
				String today = LocalDate.now().toString();  // Format: YYYY-MM-DD
			%>

			<div class="d-flex gap-2 mt-3 justify-content-center align-items-center flex-wrap">
			   <!-- Edit button -->
				<g:link class="btn btn-primary" action="edit" resource="${queryInstance}">
					<g:message code="default.button.edit.label" default="Edit" />
				</g:link>
				<!-- Wipe button: deletes the query AND all of its subscriptions / results -->
				<g:if test="${queryInstance?.custom}">
					<button type="button" class="btn btn-outline-primary" onclick="wipeQuery(${queryInstance.id})">
						<i class="fas fa-trash" aria-hidden="true"></i>
						Delete
					</button>
				</g:if>
				<!-- Debug: run the query against today's date and email the result -->
				<g:form controller="admin" action="emailAlertsOnCheckDate" method="POST" target="_blank" class="m-0">
					<input type="hidden" name="queryId" value="${queryInstance.id}" />
					<input type="hidden" name="frequency" value="${userFrequency}" />
					<input type="hidden" id="checkDate" name="checkDate" value="${today}" />
					<button type="submit" class="btn btn-outline-primary">Debug and Email me</button>
				</g:form>
			</div>

			<div class="col-md-10 mx-auto mt-2 text-center text-muted fst-italic">
				<b>Debug and Email me</b> offers a very limited debug test function. We encourage you to use
				<g:link controller="admin" action="query" class=" fst-normal">Alert Diagnostics &amp; Management</g:link>.
				it provides comprehensive diagnostic functions.
			</div>
		</div>


		<script type="text/javascript">
			var wipeQueryUrl = '${createLink(controller: 'query', action: 'wipe')}';
			var queryListUrl = '${createLink(controller: 'query', action: 'list')}';

			/**
			 * Delete a custom alert together with its notifications, results and property paths.
			 * QueryController#wipe renders {status: 0|1, message: '..'}
			 */
			function wipeQuery(id) {
				if (!confirm('This will permanently delete query ' + id + ' and all of its subscriptions. Continue?')) {
					return;
				}

				fetch(wipeQueryUrl + '?id=' + encodeURIComponent(id), {
					method: 'GET',
					headers: { 'Accept': 'application/json' }
				})
				.then(function (response) {
					if (!response.ok) {
						throw new Error('HTTP ' + response.status);
					}
					return response.json();
				})
				.then(function (data) {
					alert(data.message);
					if (data.status === 0) {
						// the query no longer exists - this page is gone, go back to the list
						window.location.href = queryListUrl;
					}
				})
				.catch(function (error) {
					alert('Failed to delete query ' + id + ': ' + error.message);
				});
			}
		</script>
	</body>
</html>
