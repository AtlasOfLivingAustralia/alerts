<%@ page contentType="text/html"%>
<html>
  <head><title><g:message code="alert.title" args="[grailsApplication.config.skin.orgNameLong]" /></title></head>
  <body>
    <h3><g:message code="alert.title" args="[grailsApplication.config.skin.orgNameLong]"/></h3>
    <h2>${title}</h2>
    <p><g:message code="${message}" default="${message}" args="${[records.size()]}"/></p>

    <p><a href="${moreInfo}"><g:message code="datasets.view.details.of.the.added" /></a></p>

    <style type="text/css">
    	body { font-family:Arial; }
    	table { border-collapse: collapse; border: 1px solid #CCC; padding:2px; }
		td, th { border: 1px solid #CCC; padding:4px; }
        img { max-width:150px; max-height:150px; }
    </style>
    <g:if test="${records}">

    <h3><g:message code="datasets.update" /></h3>

    <table style="border-collapse: collapse; border: 1px solid #CCC; padding:2px;">
    	<thead>
			<th><g:message code="datasets.name" /></th>
			<th><g:message code="datasets.link" /></th>
    	</thead>
    <g:each in="${records}" var="dataset">
      <tbody>
      <tr>
        <td>${dataset.name}</td>
         <td>
           <a href="${grailsApplication.config.collectory.baseURL}/public/show/${dataset.uid}"><g:message code="datasets.view.details.of" args="[dataset.name]" /></a>
         </td>
      </tr>
      </tbody>
    </g:each>
    </table>
    </g:if>

    <g:render template="/email/unsubscribe"/>

  </body>
</html>