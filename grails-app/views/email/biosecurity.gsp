<%@ page contentType="text/html"%>
<html>
  <head>
    <title><g:message code="alert.title" args="[query.resourceName]" /></title>
    <style type="text/css">
    body { font-family:Arial; }
    table.container { width: 640px; border-collapse: collapse;}
    table.container td { padding:5px; }
    table.content { border-collapse: collapse; border: 1px solid #CCC; padding:2px; }
    table.content td { border: 1px solid #CCC; padding:4px; }
    table.content img { max-width:140px; max-height:160px; }
    td.imageCol { padding:0; margin:0; }
    </style>
  </head>
  <body>
  <table class="container">
    <tr><td><div style="background-color:black; padding:10px">
      <a href="http://www.ala.org.au/" title="visit the ALA website"><img
              src="https://www.ala.org.au/app/uploads/2019/01/logo-300x45.png" alt="ALA logo"/></a>
    </div>
    </td></tr>
    <tr><td><h3><g:message code="alert.title" args="[grailsApplication.config.skin.orgNameLong]"/></h3></td></tr>
    <tr><td><p><g:message code="${message}" default="${message}" args="${[totalRecords, moreInfo, query.name]}"/></p>
    </td></tr>
    <g:if test="${records}">
      <tr><td>
        <table class="content">
          <tbody>
          <g:each in="${records}" var="oc">
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
                <g:else>
                  <td>${oc.year}</td>
                </g:else>
              </g:elseif>
              <g:else>
                <td></td>
              </g:else>
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
              <g:else>
                <td></td>
              </g:else>
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
                <td class="imageCol">
                  <a href="${query.baseUrlForUI}/occurrences/${oc.uuid}">
                    <img src="${oc.imageUrl}" alt="${message(code: "biocache.alt.image.for.record")}"/>
                  </a>
                </td>
              </tr>
            </g:if>

            <tr><td colspan="2"><br/></td></tr>
          </g:each>
          </tbody>
        </table>
      </td></tr>
    </g:if>
    <tr><td><h4>Please check with the relevant team before forwarding this email outside of the department.</h4></td></tr>
    <tr><td>This email has been generated as part of ALA's national biosecurity alert system. To find out more about this program click <a
            href="http://www.google.com">here</a></td></tr>
    <tr><td><g:render template="/email/unsubscribe"><p><a href="http://www.google.com">manage your alerts</a></p></g:render></td></tr>
    <tr><td>
        <h4>The ALA is made possible by contributions from its partners, is supported by <a href="https://www.education.gov.au/national-collaborative-research-infrastructure-strategy-ncris">NCRIS</a>, is hosted by <a href="https://csiro.au/">CSIRO</a>, and is the Australian node of <a href="https://www.gbif.org/en/">GBIF</a>.</h4>
        <p>
          <a href="https://www.education.gov.au/national-collaborative-research-infrastructure-strategy-ncris"><img src="https://www.ala.org.au/app/uploads/2019/06/NCRIS_150px-150x109.jpg" alt="NCRIS logo" width="150" height="109" /></a>
          <a href="https://csiro.au/"><img src="https://www.ala.org.au/app/uploads/2019/07/CSIRO_Solid_RGB-150x150.png" alt="CSIRO logo" width="109" height="109" /></a>
          <a href="https://www.gbif.org/en/"><img src="https://www.ala.org.au/app/uploads/2019/06/GBIF_109px.png" alt="GBIF logo" width="207" height="109" /></a>
        </p>
    </td></tr>
    <tr><td>
      <h4>Acknowledgement of Traditional Owners and Country</h4>
      <p>The Atlas of Living Australia acknowledges Australia’s Traditional Owners and pays respect to the past and present Elders of the nation’s Aboriginal and Torres Strait Islander communities. We honour and celebrate the spiritual, cultural and customary connections of Traditional Owners to country and the biodiversity that forms part of that country.</p>
    </td></tr>
  </table>
  </body>
</html>
