<%@ page import="javax.security.auth.login.Configuration" contentType="text/javascript;charset=UTF-8" %><g:set
        var="server" value="${grailsApplication.config.serverName + grailsApplication.config.contextPath}"
/>alertsCallback({"alertExists":${notification != null}, "link" : "${link}", "name" : "${displayName}" <g:if test="${deleteLink}">, "deleteLink": "${deleteLink}"</g:if>});
