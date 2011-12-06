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
			<th>Record details</th>
			<th>Image</th>
			<th>&nbsp;</th>
    	</thead>
    <g:each in="${records}" var="oc">
      <tbody>
      <tr>
        <td>
            <g:if test="${oc.taxonRankID > 5000}"><i></g:if>
              ${oc.scientificName}
            <g:if test="${oc.taxonRankID > 5000}"></i></g:if>
            <br/>
            ${oc.vernacularName}<br/>
            ${oc.family}<br/>
            ${oc.basisOfRecord}<br/>
            ${oc.stateProvince}<br/>
        </td>
        <td>
          <g:if test="${oc.image != null && oc.image.startsWith('/data/biocache-media')}">
            <a href="http://biocache.ala.org.au/occurrences/${oc.uuid}">
            <img src="${oc.image.replaceAll('/data/biocache-media/', 'http://biocache.ala.org.au/biocache-media/')}" alt="image for record"/>
            </a>
          </g:if>
          <g:elseif test="${oc.image}">
            <a href="http://biocache.ala.org.au/occurrences/${oc.uuid}">
             <img src="${oc.image}" alt="image for record"/>
            </a>
          </g:elseif>
          <g:else>
            No image
          </g:else>
          </td>
         <td>
           <a href="http://biocache.ala.org.au/occurrences/${oc.uuid}">View</a>
         </td>
      </tr>
      </tbody>
    </g:each>
    </table>
    </g:if>
  </body>
</html>