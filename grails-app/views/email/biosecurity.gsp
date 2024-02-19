<%@ page contentType="text/html"%>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:v="urn:schemas-microsoft-com:vml" xmlns:o="urn:schemas-microsoft-com:office:office">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <meta name="format-detection" content="telephone=no">
  <meta name="x-apple-disable-message-reformatting">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>${title}</title>
  <style>

  .ala-color {
    color: #C44D34 !important;
  }

  .default-font {
    vertical-align: top;
    font-family: 'Lato', Helvetica, Arial, sans-serif;
    font-size: 16px;
    font-weight: 500;
    line-height: 1.42;
    letter-spacing: -0.4px;
    text-align: center;
    color: #ffffff;
  }

  .large-white-font {
    font-weight: 700;
    font-size: 24px; /* Adjust the font size as needed */
    color: #ffffff; /* Adjust the color as needed */
  }

  .medium-weight-font {
    vertical-align: top;
    font-size: 22px;
    font-weight: 500;
    line-height: 1.42;
    letter-spacing: -0.4px;
    color: #000000;
    text-align: center;
  }

  .light-wight-font {
    vertical-align: top;
    line-height: 1.42;
    font-size: 16px;
    font-weight: 400;
    letter-spacing: -0.4px;
    color: #ffffff;
    text-align: center;
  }

  .species-summary-font {
    vertical-align: top;
    padding: 0;
    line-height: 1.56;
    font-size: 13px;
    font-weight: 300;
    color: #212121;
    text-align: left;
  }

  .text-left {
    text-align: left !important;
  }

  .padding {
    padding: 20px 10px 20px 10px;
  }

  .small-padding {
    padding: 15px 20px 15px 20px;
  }

  .large-padding {
    padding: 40px 30px 40px 30px;
  }

  body {
    background-color: #f4f4f4;
    width: 100% !important;
    margin: 0 !important;
    padding: 0;
    line-height: 1.5;
    -webkit-font-smoothing: antialiased;
    -webkit-text-size-adjust: 100%;
    -ms-text-size-adjust: 100%;
  }

  .main-shadow-div {
    mso-table-lspace: 0pt;
    mso-table-rspace: 0pt;
    box-shadow: 0 2px 4px 0 rgba(0, 0, 0, 0.1);
    width: 100%;
    max-width: 620px;
    margin: 20px auto 0px;
  }

  .center-white-div{
    display: flex;
    vertical-align: top;
    justify-content: center;
    align-items: center;
    background-color: #ffffff;
    padding: 20px 20px 10px 20px;
  }

  .background-image-div {
    vertical-align: top;
    background-image: url("${grailsApplication.config.grails.serverURL+'/assets/email/biosecurity-alert-header.png'}");
    background-position: top center;
    background-size: cover;
    background-repeat: no-repeat;
    background-color: #C44D34;
  }

  img {
    border: 0;
    line-height: 100%;
    outline: 0;
  }

  .species-thumbnail-img {
    vertical-align: top;
    border: 0;
    line-height: 100%;
    outline: 0;
    -ms-interpolation-mode: bicubic;
    display: block;
    color: #212121;
    border-radius: 6px;
    max-width: 130px;
    max-height: 118px;
    margin: 0 auto;
  }

  .species-thumbnail-div {
    vertical-align: top;
    max-width: 130px;
  }

  .species-div {
    padding: 20px 20px 20px 20px;
    background-color: white;
    display: flex;
    flex-direction: row;
    justify-content: center;
  }

  .info-div {
    padding: 20px 70px 14px 70px;
    background-color: #C44D34;
    font-size: 14px;
    line-height: 1.43;
    letter-spacing: -0.2px;
    color: #ffffff;
  }

  .normal-font-color {
    color: #000000;
    background-color: #ffffff;
  }

  .reversed-font-color {
    color: #ffffff;
    background-color: #000000;
  }

  .record-button {
    background-color: #C44D34;
    cursor: pointer;
    border: 0;
    border-radius: 10px;
    color: white;
    padding: 11px 19px;
    text-align: center;
    display: inline-block;
    font-size: 16px;
  }

  @media only screen and (min-width:812px) and (orientation: landscape){
    .main-shadow-div {
      width: 620px !important;
    }
  }

  @media only screen and (max-width: 375px) {
    .main-shadow-div {
      width: 100%; /* Set width to 100% to make it full-width on mobile phones */
    }
  }
  </style>
</head>
<body>
    <div class="main-shadow-div default-font">
      <div class="center-white-div">
        <a href="https://www.ala.org.au" style="text-decoration: none;">
          <img src="${grailsApplication.config.grails.serverURL + '/assets/email/logo-dark.png'}" height="60" alt="" >
        </a>
      </div>
      <div class="background-image-div padding ">
        <div class="default-font large-white-font">Biosecurity Alerts</div>
        <div class="default-font">
          <b>${new SimpleDateFormat("dd-MMMM-yyyy").format(new Date())}</b>
        </div>
        <div class="light-wight-font small-padding" >
          Alerts service for new ALA records listing potential invasive species
        </div>
      </div>
      <div class="large-padding medium-weight-font">
        <div> ${totalRecords} new ${totalRecords == 1 ? 'alert' : 'alerts'} for
        </div>
        <div title="${speciesListInfo.name}">
          <strong>${StringUtils.abbreviate(speciesListInfo.name, 40)}, <a href="${speciesListInfo.url}">${speciesListInfo.drId}</a></strong>
        </div>
      </div>

      <g:each status="i" in="${records}" var="oc">
        <g:set var="link" value="${query.baseUrlForUI}/occurrences/${oc.uuid}"/>
        <div class="species-div" >
          <div style="width: 50%; display: inline-flex; ">
            <table  style="width: 100%;" >
              <tbody>
              <tr>
                <td class="text-left" >
                  <a href="${link}" class="default-font ala-color text-left" style="text-decoration: none;">${i+1}. <em>${oc.scientificName ?:"N/A"}</em></a>
                </td>
              </tr>
              <tr>
                <td class="species-summary-font">
                  <g:if test="${oc.scientificName && oc.raw_scientificName && oc.scientificName != oc.raw_scientificName}">
                    Supplied as:<em>{oc.raw_scientificName}</em><br>
                  </g:if>
                  <g:if test="${oc.vernacularName}">
                    Common name: ${oc.vernacularName}<br>
                  </g:if>
                  <g:if test="${oc.locality && oc.stateProvince}">
                    <strong>${oc.locality}; ${oc.stateProvince}</strong>><br>
                  </g:if>
                  <g:elseif test="${oc.locality}">
                    <strong>${oc.locality}</strong><br>
                  </g:elseif>
                  <g:elseif test="${oc.stateProvince}">
                    <strong>${oc.stateProvince}</strong><br>
                  </g:elseif>
                  <g:if test="${oc.latLong}">
                    Coordinates: ${oc.latLong} <br>
                  </g:if>
                  <g:if test="${oc.eventDate}">
                    Time & date: ${new SimpleDateFormat('yyyy-MM-dd HH:mm').format(oc.eventDate)} <br>
                  </g:if>
                  <g:if test="${oc.dataResourceName}">
                    Source: ${oc.dataResourceName} <br>
                  </g:if>

                </td>
              </tr>
              </tbody>
            </table>
          </div>
          <div style="width: 50%; display: inline-flex; flex-direction: row; justify-content: space-between ">
            <div class="species-thumbnail-div">
              <g:if test="${oc.thumbnailUrl || oc.smallImageUrl }">
                <a href="${query.baseUrlForUI}/occurrences/${oc.uuid}">
                  <img class="species-thumbnail-img" src="${oc.thumbnailUrl ?: oc.smallImageUrl}"  alt="${message(code: "biocache.alt.image.for.record")}" />
                </a>
              </g:if>
            </div>
            <div class="species-thumbnail-div">
              <g:if test="${oc.latLong}">
                <img class="species-thumbnail-img" src="https://maps.googleapis.com/maps/api/staticmap?center=${oc.latLong}&markers=|${oc.latLong}&zoom=5&size=240x200&maptype=roadmap&key=${grailsApplication.config.getProperty('google.apikey')}" alt="location preview map" />
              </g:if>
            </div>
          </div>
        </div>
      </g:each>

      <div class="species-div" >
        <a href="${query.baseUrlForUI + query.queryPathForUI}">
          <button class="record-button" ><strong>View all records online</strong>
          </button>
        </a>
      </div>

      <div class="info-div">
        <p>If you notice a record has been misidentified, we encourage you to use your expertise to improve the quality of Australia’s biosecurity data.</p>
        <p>Please either annotate the record in the provider platform itself or notify us at <a href="mailto:biosecurity@ala.org.au" style="color: #f2f2f2; font-weight: 700;">biosecurity@ala.org.au</a> and we can make the suggested change.</p>
      </div>

      <div class="info-div reversed-font-color" >
        <p>The Atlas of Living Australia acknowledges Australia’s Traditional Owners and pays respect to the past and present Elders of the nation’s Aboriginal and Torres Strait Islander communities.</p>
        <p>
          We honour and celebrate the spiritual, cultural and customary connections of Traditional Owners to Country and the biodiversity that forms part of that Country.</p>
      </div>

      <div class="info-div normal-font-color">
        <div>
          <img src="${grailsApplication.config.grails.serverURL}/assets/email/ncris.png" alt="Affiliated orgs" usemap="#orgsMap"  height="80" >
          <map name="orgsMap">
            <area shape="rect" coords="0,0,100,100" href="https://www.education.gov.au/ncris" alt="NCRIS">
            <area shape="rect" coords="100,0,180,100" href="https://csiro.au" alt="CSIRO">
            <area shape="rect" coords="180,0,300,100" href="https://www.gbif.org/" alt="GBIF">
          </map>
          <p>You are receiving this email because you opted in to ALA biosecurity alerts.
            <br>
            Don't want to receive these emails? You can <a href="${unsubscribeOne}" style="color: #C44D34;">unsubscribe</a>.
          </p>
        </div>
      </div>
    </div>

  </body>
</html>