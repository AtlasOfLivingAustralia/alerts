<%@ page import="org.codehaus.groovy.grails.commons.ConfigurationHolder; javax.security.auth.login.Configuration" contentType="text/javascript;charset=UTF-8" %>
<g:set var="server" value="${ConfigurationHolder.config.security.cas.serverName + ConfigurationHolder.config.security.cas.contextPath}"/>
<g:if test="${alertExists}">alertsCallback({"link" : "${server + '/webservice/deleteTaxonAlert?guid=' + guid}", "name" : "Delete alert for ${taxonName}"});</g:if>
<g:else>alertsCallback({"link" : "${server + '/webservice/createTaxonAlert?guid=' + guid +'&taxonName='+ taxonName}", "name" : "Create alert for ${taxonName}"});</g:else>
