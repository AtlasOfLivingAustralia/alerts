<%@ page contentType="text/html"%>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title><g:message code="alert.title" args="[query.resourceName]" /></title>
</head>
<style>
  a {
    color: #003A70;
    text-decoration: none;
  }
</style>
<body style="background-color: #f4f4f4;margin: 0;padding: 0;font-family: 'Roboto', sans-serif;font-size: 16px;line-height: 1.5;">
<table border="0" cellpadding="0" cellspacing="0" width="100%" style="background-color: #f4f4f4;border-spacing: 0;border-collapse: collapse;">
  <tr>
    <td align="center" style="padding: 20px;font-family: 'Roboto', sans-serif;font-size: 16px;line-height: 1.5;">
      <table border="0" cellpadding="0" cellspacing="0" width="650" style="background-color: #ffffff;font-family: 'Roboto', sans-serif;font-size: 16px;line-height: 1.5;border-spacing: 0;border-collapse: collapse;">
        <!-- Logo -->
        <tr>
          <td style="text-align: center; padding: 20px; background-color: #fff;">
            <a href="https://www.ala.org.au" target="_blank" >
              <img src="${grailsApplication.config.grails.serverURL + '/assets/email/logo-dark.png'}" height="60" alt="Logo" style="display: block; margin: auto;border: 0;line-height: 100%;">
            </a>
          </td>
        </tr>
        <!-- Header -->
        <tr>
          <td  align="center"  background="${grailsApplication.config.grails.serverURL}/assets/email/banner-ocean.png" width="620" height="120" style="color:white;background-color:#003A70;padding: 20px 10px 20px 10px;text-align: center;font-family: 'Roboto', sans-serif;font-size: 16px;line-height: 1.5;background-position: top center;background-size: cover;background-repeat: no-repeat">
            <h1 style="font-size: 24px; color: #fff;">ALA Alerts - ${query.name}</h1>
            <p style="font-size: 16px; color: #fff;"><strong>${new SimpleDateFormat("dd MMM yyyy").format(new Date())}</strong></p>
          </td>
        </tr>
        <tr>
          <td style="background-color: #E8E8E8;color: #000;padding: 40px 30px 40px 30px;text-align: center;font-family: 'Roboto', sans-serif;font-size: 22px;line-height: 1.5;">
            <div> ${totalRecords} ${totalRecords == 1 ? 'spatial layer ' : 'spatial layers'} ${totalRecords == 1 ? 'has' : 'have'} been updated
            </div>
          </td>
        </tr>
      <!-- Records Section -->
        <g:each status="i" in="${records}" var="oc">
          <tr>
            <td style="padding: 20px;background-color: white;font-family: 'Roboto', sans-serif;font-size: 14px;line-height: 1.5;">
              <g:set var="oclink" value="${oc.source_link}"></g:set>
              <table style="width: 100%">
                <tr style="vertical-align: top;">
                  <td style="width: 70%">
                    <a href="${oclink}" style="color: #003A70;text-decoration: none;font-family: 'Roboto', sans-serif;font-size: 16px;line-height: 1.5;">
                      <strong>${i+1}. <strong>${oc.displayname}</strong></strong>
                    </a>
                    <p style="padding-left: 15px;">
                      <g:if test="${oc.keywords}">
                        Keywords: ${oc.keywords}<br/>
                      </g:if>
                      <g:if test="${oc.source}">
                        Source: ${oc.source}<br/>
                      </g:if>
                      <g:if test="${oc.minlatitude != null && oc.minlongitude != null && oc.maxlatitude != null && oc.maxlongitude != null}">
                        Extents: Latitude ${oc.minlatitude} to ${oc.maxlatitude}, Longitude ${oc.minlongitude} to ${oc.maxlongitude}<br/>
                      </g:if>
                      <g:if test="${oc.licence_notes}">
                        Licence: <a href="${oc.licence_link}" style="color: #003A70;text-decoration: none;font-family: 'Roboto', sans-serif;font-size: 16px;line-height: 1.5;">${oc.licence_notes}</a><br/>
                      </g:if>
                      <br/>
                      <g:if test="${oc.description}">
                        Description: ${oc.description}<br/>
                      </g:if>
                    </p>

                  </td>
                  <td style="width: 33%; text-align: left; padding-left: 30px;" >
                    <g:if test="${oc.dt_added}">
                      Date added: ${new SimpleDateFormat("dd MMM yyyy").format(oc.dt_added)}<br/>
                    </g:if>
                    <g:if test="${oc.type}">
                      Layer type: ${oc.type}<br/>
                    </g:if>
                    <g:if test="${oc.classification1}">
                      Classification: ${oc.classification1}<br/>
                    </g:if>
                  </td>
                </tr>
              </table>
            </td>
          </tr>
        </g:each>
        <g:render template="/email/footer"/>
        <g:render template="/email/unsubscribe" model="[unsubscribeOne: unsubscribeOne, queryName: query.name]"/>
      </table>
    </td>
  </tr>
</table>
</body>
</html>
