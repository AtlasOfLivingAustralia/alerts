<%@ page contentType="text/html"%>
<html>
  <head><title>Email alert from Atlas of Living Australia</title></head>
  <body>
    <h2>${title}</h2>
    <p>${message}</p>
    <p>To view a list of all the records that have changed, <a href="${moreInfo}">click here</a></p>
    <p>To disable this alert or to manage your alerts, <a href="${stopNotification}">click here</a>.
      Your current settings are to receive alerts ${frequency}.
    </p>
    <style type="text/css">
    	body { font-family:Arial; }
    	table { border-collapse: collapse; border: 1px solid #CCC; padding:2px; }
		td, th { border: 1px solid #CCC; padding:4px; }
        img { max-width:150px; max-height:150px; }
    </style>
    <g:if test="${records}">
    <h3>Occurrences record update</h3>
    <table style="border-collapse: collapse; border: 1px solid #CCC; padding:2px;">
    	<thead>
			<th>Scientific name</th>
			<th>Common name</th>
			<th>Dataset</th>
			<th>State</th>
			<th>Basis of record</th>
			<th>Family</th>
			<th>Image</th>
			<th>&nbsp;</th>
    	</thead>
    <g:each in="${records}" var="oc">
      <tbody>
      <tr>
        <td>${oc.scientificName}</td>
        <td>${oc.vernacularName}</td>
        <td>${oc.dataResourceName}</td>
        <td>${oc.stateProvince}</td>
        <td>${oc.basisOfRecord}</td>
        <td>${oc.family}</td>
        <td>
          <g:if test="${oc.image != null && oc.image.startsWith('/data/biocache-media')}">
            <img src="${oc.image.replaceAll('/data/biocache-media/', 'http://biocache.ala.org.au/biocache-media/')}" alt="image for record"/>
          </g:if>
          <g:elseif test="${oc.image}">
             <img src="${oc.image}" alt="image for record"/>
          </g:elseif>
          </td>
         <td>
           <a href="http://biocache.ala.org.au/occurrences/${oc.uuid}">click to view</a>
         </td>
      </tr>
      </tbody>
    </g:each>
    </table>
    </g:if>
  </body>
</html>