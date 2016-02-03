<%@ page contentType="text/html"%>
<html>
  <head><title>Email alert from ${grailsApplication.config.skin.orgNameLong}</title></head>
  <body>
    <h2>${title}</h2>
    <p><g:message code="${message}" default="${message}"/></p>
    <p>To view details of the layers that have been added/changed, <a href="${moreInfo}">click here</a></p>
    <p>To disable this alert or to manage your alerts, <a href="${stopNotification}">click here</a></p>
    <style type="text/css">
    	body { font-family:Arial; }
    	table { border-collapse: collapse; border: 1px solid #CCC; padding:2px; }
		td, th { border: 1px solid #CCC; padding:4px; }
        img { max-width:150px; max-height:150px; }
    </style>
    <g:if test="${records}">
    <h3>Spatial layers update</h3>
    <table style="border-collapse: collapse; border: 1px solid #CCC; padding:2px;">
    	<thead>
			<th>Name</th>
			<th>Description</th>
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
           <a href="http://spatial.ala.org.au/layers/more/${layer.name}">click to layer details</a>
        </td>
      </tr>
      </tbody>
    </g:each>
    </table>
    </g:if>
  </body>
</html>