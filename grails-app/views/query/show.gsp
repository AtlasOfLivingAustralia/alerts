
<%@ page import="au.org.ala.alerts.Query" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'query.label', default: 'Query')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
	</head>
	<body>
		<a href="#show-query" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
				<li><g:link class="list" action="index"><g:message code="default.list.label" args="[entityName]" /></g:link></li>
				<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
			</ul>
		</div>
		<div id="show-query" class="content scaffold-show" role="main">
			<h1><g:message code="default.show.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<ol class="property-list query">
			
				<g:if test="${queryInstance?.description}">
				<li class="fieldcontain">
					<span id="description-label" class="property-label"><g:message code="query.description.label" default="Description" /></span>
					
						<span class="property-value" aria-labelledby="description-label"><g:fieldValue bean="${queryInstance}" field="description"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${queryInstance?.dateFormat}">
				<li class="fieldcontain">
					<span id="dateFormat-label" class="property-label"><g:message code="query.dateFormat.label" default="Date Format" /></span>
					
						<span class="property-value" aria-labelledby="dateFormat-label"><g:fieldValue bean="${queryInstance}" field="dateFormat"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${queryInstance?.idJsonPath}">
				<li class="fieldcontain">
					<span id="idJsonPath-label" class="property-label"><g:message code="query.idJsonPath.label" default="Id Json Path" /></span>
					
						<span class="property-value" aria-labelledby="idJsonPath-label"><g:fieldValue bean="${queryInstance}" field="idJsonPath"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${queryInstance?.recordJsonPath}">
				<li class="fieldcontain">
					<span id="recordJsonPath-label" class="property-label"><g:message code="query.recordJsonPath.label" default="Record Json Path" /></span>
					
						<span class="property-value" aria-labelledby="recordJsonPath-label"><g:fieldValue bean="${queryInstance}" field="recordJsonPath"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${queryInstance?.updateMessage}">
				<li class="fieldcontain">
					<span id="updateMessage-label" class="property-label"><g:message code="query.updateMessage.label" default="Update Message" /></span>
					
						<span class="property-value" aria-labelledby="updateMessage-label"><g:fieldValue bean="${queryInstance}" field="updateMessage"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${queryInstance?.queryPathForUI}">
				<li class="fieldcontain">
					<span id="queryPathForUI-label" class="property-label"><g:message code="query.queryPathForUI.label" default="Query Path For UI" /></span>
					
						<span class="property-value" aria-labelledby="queryPathForUI-label"><g:fieldValue bean="${queryInstance}" field="queryPathForUI"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${queryInstance?.queryPath}">
				<li class="fieldcontain">
					<span id="queryPath-label" class="property-label"><g:message code="query.queryPath.label" default="Query Path" /></span>
					
						<span class="property-value" aria-labelledby="queryPath-label"><g:fieldValue bean="${queryInstance}" field="queryPath"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${queryInstance?.baseUrl}">
				<li class="fieldcontain">
					<span id="baseUrl-label" class="property-label"><g:message code="query.baseUrl.label" default="Base Url" /></span>
					
						<span class="property-value" aria-labelledby="baseUrl-label"><g:fieldValue bean="${queryInstance}" field="baseUrl"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${queryInstance?.baseUrlForUI}">
				<li class="fieldcontain">
					<span id="baseUrlForUI-label" class="property-label"><g:message code="query.baseUrlForUI.label" default="Base Url For UI" /></span>
					
						<span class="property-value" aria-labelledby="baseUrlForUI-label"><g:fieldValue bean="${queryInstance}" field="baseUrlForUI"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${queryInstance?.custom}">
				<li class="fieldcontain">
					<span id="custom-label" class="property-label"><g:message code="query.custom.label" default="Custom" /></span>
					
						<span class="property-value" aria-labelledby="custom-label"><g:formatBoolean boolean="${queryInstance?.custom}" /></span>
					
				</li>
				</g:if>
			
				<g:if test="${queryInstance?.emailTemplate}">
				<li class="fieldcontain">
					<span id="emailTemplate-label" class="property-label"><g:message code="query.emailTemplate.label" default="Email Template" /></span>
					
						<span class="property-value" aria-labelledby="emailTemplate-label"><g:fieldValue bean="${queryInstance}" field="emailTemplate"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${queryInstance?.name}">
				<li class="fieldcontain">
					<span id="name-label" class="property-label"><g:message code="query.name.label" default="Name" /></span>
					
						<span class="property-value" aria-labelledby="name-label"><g:fieldValue bean="${queryInstance}" field="name"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${queryInstance?.propertyPaths}">
				<li class="fieldcontain">
					<span id="propertyPaths-label" class="property-label"><g:message code="query.propertyPaths.label" default="Property Paths" /></span>
					
						<g:each in="${queryInstance.propertyPaths}" var="p">
						<span class="property-value" aria-labelledby="propertyPaths-label"><g:link controller="propertyPath" action="show" id="${p.id}">${p?.encodeAsHTML()}</g:link></span>
						</g:each>
					
				</li>
				</g:if>
			
				<g:if test="${queryInstance?.queryResults}">
				<li class="fieldcontain">
					<span id="queryResults-label" class="property-label"><g:message code="query.queryResults.label" default="Query Results" /></span>
					
						<g:each in="${queryInstance.queryResults}" var="q">
						<span class="property-value" aria-labelledby="queryResults-label"><g:link controller="queryResult" action="show" id="${q.id}">${q?.encodeAsHTML()}</g:link></span>
						</g:each>
					
				</li>
				</g:if>
			
				<g:if test="${queryInstance?.resourceName}">
				<li class="fieldcontain">
					<span id="resourceName-label" class="property-label"><g:message code="query.resourceName.label" default="Resource Name" /></span>
					
						<span class="property-value" aria-labelledby="resourceName-label"><g:fieldValue bean="${queryInstance}" field="resourceName"/></span>
					
				</li>
				</g:if>
			
			</ol>
			<g:form url="[resource:queryInstance, action:'delete']" method="DELETE">
				<fieldset class="buttons">
					<g:link class="edit" action="edit" resource="${queryInstance}"><g:message code="default.button.edit.label" default="Edit" /></g:link>
					<g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
				</fieldset>
			</g:form>
		</div>
	</body>
</html>
