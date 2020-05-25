<%@ page import="grails.converters.JSON" %>
<html>
  <head>
	  <title>Grails Runtime Exception</title>
	  <meta name="layout" content="main">
	  <style type="text/css">
	  		.message {
	  			border: 1px solid black;
	  			padding: 5px;
	  			background-color:#E9E9E9;
	  		}
	  		.stack {
	  			border: 1px solid black;
	  			padding: 5px;
	  			overflow:auto;
	  			height: 300px;
	  		}
	  		.snippet {
	  			padding: 5px;
	  			background-color:white;
	  			border:1px solid black;
	  			margin:3px;
	  			font-family:courier;
	  		}
	  </style>
  </head>

  <body>
    <h1><g:message code="error.title" /></h1>
    <div><g:message code="error.email" args="[grailsApplication.config.skin.orgSupportEmail,
											  grailsApplication.config.skin.orgNameShort,
											  request.scheme, request.serverName, request.forwardURI,
											  grailsApplication.config.skin.orgSupportEmail]" /></div>

  	<div class="message">
		<strong><g:message code="error.status.code" args="[request.'javax.servlet.error.status_code']" />:</strong> ${request.'javax.servlet.error.message'.encodeAsHTML()}<br/>
		<strong>Servlet:</strong> ${request.'javax.servlet.error.servlet_name'}<br/>
		<strong>URI:</strong> ${request.'javax.servlet.error.request_uri'}<br/>
		<g:if test="${exception}">
	  		<strong><g:message code="error.exception.message" /></strong> ${exception.message?.encodeAsHTML()} <br />
	  		<strong><g:message code="error.exception.caused.by" /></strong> ${exception.cause?.message?.encodeAsHTML()} <br />
	  		<strong><g:message code="error.exception.class" /></strong> ${exception.className} <br />
	  		<strong><g:message code="error.exception.at.line" /></strong> [${exception.lineNumber}] <br />
	  		<strong><g:message code="error.exception.code.snippet" /></strong><br />
	  		<div class="snippet">
	  			<g:each var="cs" in="${exception.codeSnippet}">
	  				${cs?.encodeAsHTML()}<br />
	  			</g:each>
	  		</div>
		</g:if>
  	</div>
	<g:if test="${exception}">
	    <h2><g:message code="error.exception.stack.trace" /></h2>
	    <div class="stack">
	      <pre><g:each in="${exception.stackTraceLines}">${it.encodeAsHTML()}<br/></g:each></pre>
	    </div>
	</g:if>
  </body>
</html>