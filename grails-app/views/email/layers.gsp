<%@ page contentType="text/html"%>
<html>
  <head><title><g:message code="alert.title" args="[grailsApplication.config.skin.orgNameLong]" /></title></head>
  <body>
    <h2>${title}</h2>
    <p><g:message code="${message}" default="${message}"/></p>
    <p><a href="${moreInfo}"><g:message code="layers.view.details" /></a></p>
    <p><a href="${stopNotification}"><g:message code="layers.disable.this" /></a></p>
    <style type="text/css">
    	body { font-family:Arial; }
    	table { border-collapse: collapse; border: 1px solid #CCC; padding:2px; }
		td, th { border: 1px solid #CCC; padding:4px; }
        img { max-width:150px; max-height:150px; }
    </style>
    <g:if test="${records}">
    <h3><g:message code="layers.update" /></h3>
    <table style="border-collapse: collapse; border: 1px solid #CCC; padding:2px;">
    	<thead>
			<th><g:message code="layers.name" /></th>
			<th><g:message code="layers.description" /></th>
            <th>&nbsp;</th>
    	</thead>
    <g:each in="${records}" var="layer">
      <tbody>
      <tr>
        <td>
          <strong>${layer.name}</strong><br/>
          ${layer.description}
        </td>
        <td>
           <a href="${grailsApplication.config.spatial.baseURL}/layers/more/${layer.name}"><g:message code="layers.view.details.of" args="[layer.name]" />
           </a>
        </td>
      </tr>
      </tbody>
    </g:each>
    </table>
    </g:if>

    <g:render template="/email/unsubscribe"/>

  </body>
</html>