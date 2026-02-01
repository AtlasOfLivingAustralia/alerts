<%@ page import="au.org.ala.alerts.Query" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="${grailsApplication.config.skin.layout}" />
		<g:set var="entityName" value="${message(code: 'query.label', default: 'Query')}" />
		<meta name="breadcrumbParent" content="${request.contextPath}/query,Query List"/>
		<meta name="breadcrumb" content="Edit - ${queryInstance.name}"/>

		<title><g:message code="default.edit.label" args="[entityName]" /></title>
	</head>
	<body>

	<div id="edit-query" class="container py-4" role="main">

		<h1 class="mb-4">
			<g:message code="default.edit.label" args="[entityName]" />
		</h1>

	<!-- Flash message -->
		<g:if test="${flash.message}">
			<div class="alert alert-info alert-dismissible fade show" role="alert">
				${flash.message}
				<button type="button" class="btn-close" data-bs-dismiss="alert"></button>
			</div>
		</g:if>

	<!-- Validation errors -->
		<g:hasErrors bean="${queryInstance}">
			<div class="alert alert-danger" role="alert">
				<ul class="mb-0">
					<g:eachError bean="${queryInstance}" var="error">
						<li
							<g:if test="${error in org.springframework.validation.FieldError}">
								data-field-id="${error.field}"
							</g:if>
						>
							<g:message error="${error}" />
						</li>
					</g:eachError>
				</ul>
			</div>
		</g:hasErrors>

	<!-- Form -->
		<g:form
				url="[resource: queryInstance, action: 'update']"
				method="PUT"
				class="card"
		>
			<div class="card-body">

				<g:hiddenField name="version" value="${queryInstance?.version}" />

				<div class="mb-3">
					<g:render template="form"/>
				</div>

				<div class="d-flex justify-content-end gap-2">
					<g:actionSubmit
							class="btn btn-primary"
							action="update"
							value="${message(code: 'default.button.update.label', default: 'Update')}"
					/>
				</div>

			</div>
		</g:form>

	</div>
	</body>
</html>
