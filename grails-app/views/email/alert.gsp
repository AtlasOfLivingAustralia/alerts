<%@ page contentType="text/html"%>
<html>
  <head><title>Email alert from ${grailsApplication.config.skin.orgNameLong}</title></head>
  <body>
    <h2>${title}</h2>
    <p><g:message code="${message}" default="${message}"/></p>
    <p><a href="${moreInfo}">View details</a></p>
    <p><a href="${stopNotification}">Manage your alerts</a></p>
  </body>
</html>