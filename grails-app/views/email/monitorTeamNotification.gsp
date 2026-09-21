<%@ page contentType="text/html"%>
<html>
  <head><title><g:message code="alert.title" args="[grailsApplication.config.skin.orgNameLong]" /></title></head>
  <body>
    <h2>${messages?.subject}</h2>
    <g:if test="${messages?.logs}">
      <g:set var="logs" value="${messages.logs}" />
      <g:each in="${logs}" var="log">
          <p>
             <b><a href="${log.url}">${log.message}</a></b> <br/>
             <g:each in="${log.logs ?: []}" var="line">${line}<br/></g:each>
          </p>
          <hr/>
      </g:each>
    </g:if>
  </body>
</html>