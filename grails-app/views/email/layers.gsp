<%@ page contentType="text/html"%>
<html>
  <head><title>Email alert from Atlas of Living Australia</title></head>
  <body>
    <h2>${title}</h2>
    <p>${message}</p>
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
			<th>Source</th>
    	</thead>
    <g:each in="${records}" var="layer">
      <tbody>
      <tr>
        <td>${layer.name}</td>
        <td>${layer.description}</td>
        <td>${layer.layer}</td>
         <td>
           <a href="http://spatial.ala.org.au/layers">click to view</a>
         </td>
      </tr>
      </tbody>
    </g:each>
    </table>
    </g:if>
  </body>
</html>