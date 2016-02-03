<%@ page contentType="text/html"%>
<html>
  <head><title>Email alert from ${grailsApplication.config.skin.orgNameLong}</title></head>
  <body>
    <h2>${title}</h2>
    <p><g:message code="${message}" default="${message}"/></p>
    <p>To view details of the datasets that have been added/changed, <a href="${moreInfo}">click here</a></p>
    <p>To disable this alert or to manage your alerts, <a href="${stopNotification}">click here</a></p>
    <style type="text/css">
    	body { font-family:Arial; }
    	table { border-collapse: collapse; border: 1px solid #CCC; padding:2px; }
		td, th { border: 1px solid #CCC; padding:4px; }
        img { max-width:150px; max-height:150px; }
    </style>
    <g:if test="${records}">
    <h3>Datasets update</h3>
    <table style="border-collapse: collapse; border: 1px solid #CCC; padding:2px;">
    	<thead>
			<th>Name</th>
			<th>Link</th>
    	</thead>
    <g:each in="${records}" var="dataset">
      <tbody>
      <tr>
        <td>${dataset.name}</td>
         <td>
           <a href="http://collections.ala.org.au/public/show/${dataset.uid}">click to details</a>
         </td>
      </tr>
      </tbody>
    </g:each>
    </table>
    </g:if>
  </body>
</html>