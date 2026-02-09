<%@ page import="grails.converters.JSON" %>
<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<title><g:message code="error.title" /></title>
	<meta name="layout" content="${grailsApplication.config.skin.layout}" />
	<link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
	<style>
	body {
		background-color: #f8f9fa;
	}
	.error-container {
		display: flex;
		flex-direction: column;
		justify-content:  flex-start;
		align-items: center;
		text-align: center;
		padding: 2rem;
	}
	.error-icon {
		font-size: 6rem;
		color: #6c757d;
		margin-bottom: 1rem;
	}
	.error-title {
		font-size: 2.5rem;
		font-weight: 700;
		margin-bottom: 1rem;
	}
	.error-message {
		font-size: 1.25rem;
		color: #6c757d;
		margin-bottom: 2rem;
	}
	.error-details {
		font-size: 0.875rem;
		color: #495057;
		max-width: 700px;
		text-align: left;
		background-color: #e9ecef;
		padding: 1rem;
		border-radius: 0.25rem;
		overflow-x: auto;
		margin-bottom: 1rem;
	}
	</style>
</head>
<body>

<div class="error-container">
	<g:if test="${flash.message}">
		<div class="alert alert-warning w-100" role="alert">
			<strong><g:message code="error.message" default="Error Message"/>:</strong> ${flash.message}
		</div>
	</g:if>
	<div class="error-icon">
		<i class="fas fa-exclamation-triangle"></i>
	</div>

	<h1 class="error-title"><g:message code="error.title" default="Something went wrong" /></h1>

	<p class="error-message">
		<g:message code="error.email" args="[
				grailsApplication.config.skin.orgSupportEmail,
				grailsApplication.config.skin.orgNameShort,
				request.scheme,
				request.serverName,
				request.forwardURI,
				grailsApplication.config.skin.orgSupportEmail
		]" />
	</p>
	<g:if test="${error}">
		<div class="error-details">
			<g:if test="${error.status}">
				<p><strong>Error Code:</strong>${error.status}</p>
			</g:if>
			<g:if test="${error.message}">
				<p><strong>Message:</strong> ${error.message}</p>
			</g:if>
			<g:if test="${error.details}">
				<p><strong>Details:</strong> ${error.details}</p>
			</g:if>
		</div>
	</g:if>

	<g:if test="${exception}">
		<div class="error-details">
			<p><strong>Status:</strong> ${request.'javax.servlet.error.status_code'} - ${request.'javax.servlet.error.message'.encodeAsHTML()}</p>
			<p><strong>Servlet:</strong> ${request.'javax.servlet.error.servlet_name'}</p>
			<p><strong>URI:</strong> ${request.'javax.servlet.error.request_uri'}</p>
			<p><strong>Exception:</strong> ${exception.message?.encodeAsHTML()}</p>
			<p><strong>Caused by:</strong> ${exception.cause?.message?.encodeAsHTML()}</p>
			<p><strong>Class:</strong> ${exception.className} [Line ${exception.lineNumber}]</p>

			<strong>Code Snippet:</strong>
			<div class="snippet mt-2">
				<g:each var="cs" in="${exception.codeSnippet}">
					${cs?.encodeAsHTML()}<br/>
				</g:each>
			</div>

			<strong>Stack Trace:</strong>
			<pre>
				<g:each in="${exception.stackTraceLines}">${it.encodeAsHTML()}<br/></g:each>
			</pre>
		</div>
	</g:if>
	<g:if test="${!redirectUrl}">
		<g:set var="redirectUrl"
			   value="${request.scheme}://${request.serverName}${request.serverPort in [80,443] ? '' : ':'+request.serverPort}${request.contextPath}" />
	</g:if>
	<a href="${redirectUrl}"  class="btn btn-primary"><i class="fas fa-home me-2"></i> Go Home</a>
</div>

</body>
</html>