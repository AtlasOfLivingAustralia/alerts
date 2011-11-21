<%@ page import="org.codehaus.groovy.grails.commons.ConfigurationHolder; javax.security.auth.login.Configuration" contentType="text/javascript;charset=UTF-8" %>
<g:set var="server" value="${ConfigurationHolder.config.security.cas.serverName + ConfigurationHolder.config.security.cas.contextPath}"/>
<g:if test="${alertExists}">alertsCallback({"alertExists" : true,"link" : "${server + '/notification/myAlerts'}", "name" : "${taxonName}"});</g:if>
<g:else>alertsCallback({"link" : "${server + '/webservice/createTaxonAlert?redirect='+redirect+'&guid=' + guid +'&taxonName='+ taxonName}", "name" : "${taxonName}"});</g:else>
