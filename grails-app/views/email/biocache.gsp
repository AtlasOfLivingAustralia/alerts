<%@ page contentType="text/html"%>
<html>
  <head><title>Email alert from ${query.resourceName}</title></head>
  <body>
    <h2>${title}</h2>
    <p>
      <g:message code="${message}" default="${message}" args="${[totalRecords]}"/>
    </p>
    <p>To view a list of all the records that have added/changed, <a href="${moreInfo}">click here</a></p>
    <p>To disable this alert or to manage your alerts, <a href="${stopNotification}">click here</a>.
      Your current settings are to receive alerts ${frequency}.
    </p>
    <style type="text/css">
    	body { font-family:Arial; }
    	table { border-collapse: collapse; border: 1px solid #CCC; padding:2px; }
		td, th { border: 1px solid #CCC; padding:4px; }
        img { max-width:140px; max-height:160px; }
        .detail { font-size: 11px;}
        .imageCol { padding:0; margin:0; }
        .linkCell { }
    </style>
    <g:if test="${records}">
    <h3>Occurrences record update</h3>
    <table style="border-collapse: collapse; border: 1px solid #CCC; padding:2px;">
    	<thead>
			<th>Record details</th>
			<th class="imageCol">Image</th>
			<th>Link</th>
    	</thead>
    <g:each in="${records}" var="oc">
      <tbody>
      <tr>
        <td class="detail">
            ${oc.vernacularName}<br/>
            <g:if test="${oc.taxonRankID > 5000}"><i></g:if>
              ${oc.scientificName}
            <g:if test="${oc.taxonRankID > 5000}"></i></g:if>
            <br/>
            ${oc.family}<br/>
            ${oc.stateProvince}<br/>
        </td>
        <td class="imageCol">
          <g:if test="${oc.image != null}">
            <a href="${query.baseUrlForUI}/occurrences/${oc.uuid}">
            <biocacheImage:imageTag imageUrl="${oc.smallImageUrl}"/>
            </a>
          </g:if>
          <g:elseif test="${oc.image}">
            <a href="${query.baseUrlForUI}/occurrences/${oc.uuid}">
             <img src="${oc.image}" alt="image for record"/>
            </a>
          </g:elseif>
          <g:else>
            No image
          </g:else>
          </td>
         <td class="linkCell" nowrap="nowrap">
           <a href="${query.baseUrlForUI}/occurrences/${oc.uuid}">View</a>
         </td>
      </tr>
      </tbody>
    </g:each>
    </table>
    </g:if>
  
    <p>
      iPhone/iPad users: To view the images in this email, you may need to enable "Load Remote Images" on your iOS
      device. This is done via "Settings" then "Mail" on iOS5.
    </p>
  
  </body>
</html>