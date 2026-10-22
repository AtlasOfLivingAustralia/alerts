<%@ page contentType="text/html"%>
<html>
  <head><title><g:message code="alert.title" args="[grailsApplication.config.skin.orgNameLong]" /></title></head>
  <body>
    <h2>${title}</h2>
    <p><g:message code="${message}" default="${message}"/></p>
    <g:if test="${moreInfo?.queryUrlUIUsed}">
      <p><a href="${moreInfo.queryUrlUIUsed}"><g:message code="alert.details" /></a></p>
    </g:if>
    <p><a href="${stopNotification}"><g:message code="alert.manage" /></a></p>
  </body>
</html>