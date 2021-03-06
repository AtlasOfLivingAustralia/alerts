<%@ page contentType="text/html"%>
<html>
  <head><title><g:message code="alert.title" args="[query.resourceName]" /></title></head>
  <body>
    <h3><g:message code="alert.title" args="[grailsApplication.config.skin.orgNameLong]"/></h3>
    <h2>${title}</h2>
    <p>
      <g:message code="${message}" default="${message}" args="${[totalRecords]}"/>
    </p>
    <p><a href="${moreInfo}"><g:message code="${listcode ? listcode : "biocache.view.list"}"/></a></p>
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
    <table style="border-collapse: collapse; border: 1px solid #CCC; padding:2px;">
    	<thead>
			<th><g:message code="biocache.record.details" /></th>
			<th class="imageCol"><g:message code="biocache.record.image" /></th>
			<th><g:message code="biocache.record.link" /></th>
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
            <img src="${oc.smallImageUrl}" alt="image for record"/>
            </a>
          </g:if>
          <g:elseif test="${oc.image}">
            <a href="${query.baseUrlForUI}/occurrences/${oc.uuid}">
             <img src="${oc.image}" alt="${message(code:"biocache.alt.image.for.record")}"/>
            </a>
          </g:elseif>
          <g:else>
            <g:message code="biocache.no.image" />
          </g:else>
          </td>
         <td class="linkCell" nowrap="nowrap">
         <g:if test="${oc.scientificName != null}">
           <a href="${query.baseUrlForUI}/occurrences/${oc.uuid}"><g:message code="biocache.view.this.record" args="[oc.scientificName]" /></a>
         </g:if>
         <g:else>
           <a href="${query.baseUrlForUI}/occurrences/${oc.uuid}"><g:message code="biocache.record.details" /></a>
         </g:else>
         </td>
      </tr>
      </tbody>
    </g:each>
    </table>
    </g:if>
    <g:render template="/email/unsubscribe"/>
  </body>
</html>
