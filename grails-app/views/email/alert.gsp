<%@ page contentType="text/html"%>
<html>
  <head><title>Email alert from ${grailsApplication.config.skin.orgNameLong}</title></head>
  <body>
    <h2>${title}</h2>
    <p><g:message code="${message}" default="${message}" args="${[totalRecords]}"/></p>
    <p>To view details, <a href="${moreInfo}">click here</a></p>
    <p>To manage your alerts, <a href="${stopNotification}">click here</a></p>
  </body>
</html>