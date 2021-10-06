<%@ page contentType="text/html"%>
<html>
  <head><title>aa<g:message code="alert.title" args="[query.resourceName]" /></title></head>
  <body>
    <h3><g:message code="alert.title" args="[grailsApplication.config.skin.orgNameLong]"/></h3>
    <p><g:message code="${message}" default="${message}" args="${[totalRecords, moreInfo, query.name]}"/></p>
    <style type="text/css">
    	body { font-family:Arial; }
    	table { border-collapse: collapse; border: 1px solid #CCC; padding:2px; }
		td, th { border: 1px solid #CCC; padding:4px; }
        img { max-width:140px; max-height:160px; }
        .detail { font-size: 11px;}
        .imageCol { padding:0; margin:0; }
        .linkCell { }

        /*tr.noBorder td {*/
        /*  border: 0;*/
        /*}*/
        /*.thickborder {*/
        /*    border-bottom: 5px solid black;*/
        /*    !*border-top: 5px solid black;*!*/
        /*}*/
    </style>
    <g:if test="${records}">
    <table style="border-collapse: collapse; border: 1px solid #CCC; padding:2px;">
      <tbody>
      <g:each in="${records}" var="oc">
        <tr><td>Species name</td><td>Stub species name</td></tr>
        <g:if test="${oc.vernacularName}">
          <tr><td>Vernacular name</td><td>${oc.vernacularName}</td></tr>
        </g:if>
        <g:elseif test="${oc.scientificName}">
          <tr><td>Scientific name</td><td>${oc.scientificName}</td></tr>
        </g:elseif>

        <tr>
          <td>Date of observation</td>
          <g:if test="${oc.eventDate}">
            <td>${g.formatDate(date: new Date(oc.eventDate), format: "yyyy-MM-dd")}</td>
          </g:if>
          <g:elseif test="${oc.year}">
            <g:if test="${oc.month && oc.day}">
              <td>${oc.year}-${oc.month}-${oc.day}</td>
            </g:if>
            <g:elseif test="${oc.month}">
              <td>${oc.year}-${oc.month}</td>
            </g:elseif>
          </g:elseif>
        </tr>
        <tr>
          <td>Locality of observation</td>
          <g:if test="${oc.locality}">
            <td>${oc.locality}</td>
          </g:if>
          <g:elseif test="${oc.country}">
            <g:if test="${oc.stateProvince}">
              <td>${oc.country}, ${oc.stateProvince}</td>
            </g:if>
            <g:else>
              <td>${oc.country}</td>
            </g:else>
          </g:elseif>
        </tr>
        <tr>
          <td>ALA record</td>
          <g:set var="link" value="${query.baseUrlForUI}/occurrences/${oc.uuid}"/>
          <td><a href="${link}">${link}</a></td>
        </tr>
        <g:if test="${oc.recordNumber}">
          <tr>
            <td>Original source record</td>
            <g:if test="${oc.recordNumber.startsWith("http://")}">
              <td><a href="${oc.recordNumber}">${oc.recordNumber}</a></td>
            </g:if>
            <g:else>
              <td>${oc.recordNumber}</td>
            </g:else>
          </tr>
        </g:if>
        <g:if test="${oc.imageUrl}">
          <tr>
            <td>Location map & image (if supplied)</td>
            <td>
              <a href="${query.baseUrlForUI}/occurrences/${oc.uuid}">
                <img src="${oc.imageUrl}" alt="${message(code: "biocache.alt.image.for.record")}" width="100"
                     height="100"/>
              </a>
            </td>
          </tr>
        </g:if>
        <tr><td colspan="2"><br/></td></tr>
      </g:each>
      </tbody>
    </table>

      <p>Please check with the relevant team before forwarding this email outside of the department.</p>

      <p>This email has been generated as part of ALA's national biosecurity alert system. To find out more about this program click <a
              href="http://www.google.com">here</a></p>
    </g:if>
  <g:render template="/email/unsubscribe"><p><a href="http://www.google.com">manage your alerts</a></p></g:render>
  </body>
</html>
