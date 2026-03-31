<%@ page import="grails.converters.JSON" %>
<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<title>Unauthorised</title>
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
		<i class="bi bi-shield-lock"></i>
	</div>

	<h1 class="error-title">Authorisation required</h1>

	<p class="error-message">
		Not signed in? Click Sign In (top‑right)
	</p>


</div>

</body>
</html>