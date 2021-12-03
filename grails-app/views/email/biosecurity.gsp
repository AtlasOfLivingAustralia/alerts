<%@ page contentType="text/html"%>
<html>
  <head>
    <title><g:message code="alert.title" args="[query.resourceName]" /></title>
    <style type="text/css">
    body { font-family:Arial,serif; font-size: 14px; color: #212121; }
    table.container { width: 640px; border-collapse: collapse;}
    table tr.top-row { border-top: 1px solid #CCC; padding-top: 10px; }
    table tr:last-child { border-bottom: 1px solid #CCC; }
    table.container td { padding: 5px; }
    table.container h1 { margin: 10px 0px; font-size: 18px;}
    table.content { border-collapse: collapse; padding:2px; }
    table.content td { border: 0px solid #CCC; padding:4px; }
    table.content td:first-child { color: #424242; }
    table.content td.normalseparator { border-left-style:hidden; border-right-style:hidden; }
    table.content td.lastseparator { border-left-style:hidden; border-right-style:hidden; border-bottom-style: hidden; }
    table.content td:first-child { text-align: left; }
    table.content img { max-width:200px; max-height:200px; }
    td.imageCol { padding:0; margin:0; }
    .box { display: flex; }
    </style>
  </head>
  <body>
  <table class="container">
    <tr>
      <td>
        <div>
          <a href="https://www.ala.org.au/" title="Visit the ALA website"><asset:image
              src="biosecurity-banner-v3.png" alt="ALA logo" absolute="true" width="1000" height="167"/></a>
        </div>
      </td>
    </tr>
    <tr><td><h1><g:message code="alert.title" args="[grailsApplication.config.skin.orgNameLong]"/></h1></td></tr>
    <tr><td>
      <p><g:message code="${message}" default="${message}" args="${[moreInfo, totalRecords + message(code:'email.records.summary'), speciesListInfo.url, speciesListInfo.name]}"/></p>
      <g:if test="${records && records.size() >= 20}">
        <p><g:message code="email.displaying.max.msg" default="Showing first 20 records"/> - <a href="${moreInfo}">view all ${totalRecords} records</a></p>
      </g:if>
    </td></tr>
    <g:if test="${records}">
      <tr><td>
        <table class="content">
          <tbody>
          <g:each status="i" in="${records}" var="oc">
            <g:set var="link" value="${query.baseUrlForUI}/occurrences/${oc.uuid}"/>
            <tr class="top-row"><td colspan="2">&nbsp;<code style="display: none;">${(oc as grails.converters.JSON).toString(true)}</code></td></tr>
            <tr><td>Record ID</td><td><a href="${link}">${oc.uuid}</a></td></tr>
            <tr><td>Scientific name </td><td><em>${oc.scientificName ?:"N/A"}</em></td></tr>
            <g:if test="${oc.scientificName && oc.raw_scientificName && oc.scientificName != oc.raw_scientificName}">
              <tr><td>Scientific name (raw)</td><td><em>${oc.raw_scientificName}</em></td></tr>
            </g:if>
            <g:if test="${oc.vernacularName}">
              <tr><td><g:message code="email.biosecurity.label.vernacularname" default="Vernacular name"/></td><td>${oc.vernacularName}</td></tr>
            </g:if>
            <g:if test="${oc.speciesGroups}">
              <tr><td>Species groups</td><td>${raw(oc.speciesGroups?.join(" → "))}</td></tr>
            </g:if>
            <g:if test="${oc.dataResourceName}">
              <tr><td>Dataset</td><td>${oc.dataResourceName}</td></tr>
            </g:if>
            <tr>
              <td>Date</td>
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
                  <td><g:message code="email.biosecurity.label.year" default="Year"/>: ${oc.year}</td>
                </g:else>
              </g:elseif>
              <g:else>
                <td></td>
              </g:else>
            </tr>

            <g:if test="${userAssertions.containsKey(oc.uuid) && userAssertions.get(oc.uuid)}">
              <tr>
                <td><g:message code="email.biosecurity.userassertion" default="Biosecurity annotation"/></td>
                <td>
                  <g:each in="${userAssertions.get(oc.uuid)}" var="comment">
                    ${comment}
                  </g:each>
                </td>
              </tr>
            </g:if>

            <tr>
              <td>Locality</td>
              <g:if test="${oc.locality && oc.stateProvince}">
                <td>${oc.locality}; ${oc.stateProvince}</td>
              </g:if>
              <g:elseif test="${oc.locality}">
                <td>${oc.locality}</td>
              </g:elseif>
              <g:elseif test="${oc.stateProvince}">
                <td>${oc.stateProvince}</td>
              </g:elseif>
              <g:else>
                <td></td>
              </g:else>
            </tr>

            <g:if test="${oc.recordNumber}">
              <tr>
                <td><g:message code="email.biosecurity.label.originalRecord" default="Original source record"/></td>
                <g:if test="${oc.recordNumber.startsWith("http")}">
                  <td><a href="${oc.recordNumber}">${oc.recordNumber}</a></td>
                </g:if>
                <g:else>
                  <td>${oc.recordNumber}</td>
                </g:else>
              </tr>
            </g:if>

            <g:if test="${oc.thumbnailUrl || oc.smallImageUrl || oc.latLong}">
              <tr valign="top">
                <td class="imageCol">
                  Map and image
                </td>
                <td class="">
                  <g:if test="${oc.latLong}">
                    <img src="https://maps.googleapis.com/maps/api/staticmap?center=${oc.latLong}&markers=|${oc.latLong}&zoom=5&size=300x300&maptype=roadmap&key=${grailsApplication.config.getProperty('google.apikey')}" alt="location preview map"/>
                  </g:if>
                  <g:if test="${oc.thumbnailUrl || oc.smallImageUrl }">&nbsp;&nbsp;
                    <a href="${query.baseUrlForUI}/occurrences/${oc.uuid}">
                      <img src="${oc.thumbnailUrl ?: oc.smallImageUrl}" alt="${message(code: "biocache.alt.image.for.record")}"/>
                    </a>
                  </g:if>
                </td>
              </tr>
            </g:if>

            <tr><td colspan="2" class="${i == records.size() - 1 ? 'lastseparator' : 'normalseparator'}"><br/></td></tr>
          </g:each>
          </tbody>
        </table>
      </td></tr>
    </g:if>
    <tr><td><h4>Please check with the relevant team before forwarding this email outside of the department.</h4></td></tr>
    <tr><td>This email has been generated as part of ALA's national biosecurity alert system. To find out more about this program, read <a
            href="${grailsApplication.config.getProperty('biosecurity.moreinfo.link', String, 'missing')}">ALA helps to stop pests in their tracks</a> or email
            <a href="mailto:support@ala.org.au">support@ala.org.au</a>.</td></tr>
    <tr><td><g:render template="/email/unsubscribe"/></td></tr>
    <tr><td>
      <h4>The ALA is made possible by contributions from its partners, is supported by <a href="https://www.education.gov.au/national-collaborative-research-infrastructure-strategy-ncris">NCRIS</a>, is hosted by <a href="https://csiro.au/">CSIRO</a>, and is the Australian node of <a href="https://www.gbif.org/en/">GBIF</a>.</h4>
      <div class="box">
        <div style="margin-left: 5px"><a href="https://www.education.gov.au/national-collaborative-research-infrastructure-strategy-ncris"><img src="https://www.ala.org.au/app/uploads/2019/06/NCRIS_150px-150x109.jpg" alt="NCRIS logo" width="150" height="109" /></a></div>
        <div style="margin-left: 78px; margin-right: 64px;"><a href="https://csiro.au/"><img src="https://www.ala.org.au/app/uploads/2019/07/CSIRO_Solid_RGB-150x150.png" alt="CSIRO logo" width="109" height="109" /></a></div>
        <div style="margin-right: 5px"><a href="https://www.gbif.org/en/"><img src="https://www.ala.org.au/app/uploads/2019/06/GBIF_109px.png" alt="GBIF logo" width="207" height="109" /></a></div>
      </div>
    </td></tr>
    <tr><td>
      <h4>Acknowledgement of Traditional Owners and Country</h4>
      <p>The Atlas of Living Australia acknowledges Australia’s Traditional Owners and pays respect to the past and present Elders of the nation’s Aboriginal and Torres Strait Islander communities. We honour and celebrate the spiritual, cultural and customary connections of Traditional Owners to country and the biodiversity that forms part of that country.</p>
    </td></tr>
  </table>
  </body>
</html>
