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
    }

    .info-button {
        border: 1pt solid #C44D34;
        text-decoration: none;
        font-size: 14px;
        padding: 10px 15px 10px 15px;
        color: black;
        border-radius: 8px;
    }
</style>
<body style="background-color: #f4f4f4;margin: 0;padding: 0;font-family: 'Roboto', sans-serif;font-size: 16px;line-height: 1.5;">
<table style="width: 100%; background-color: #f4f4f4;border-spacing: 0;border-collapse: collapse;">
    <tr>
        <td align="center" style="padding: 20px;font-family: 'Roboto', sans-serif;font-size: 16px;line-height: 1.5;">
            <table style="width: 650px; background-color: #ffffff; font-family: 'Roboto', sans-serif;font-size: 16px;line-height: 1.5;border-spacing: 0;border-collapse: collapse;">
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
                    <td height="120" style="color:white;background-color:#003A70;padding: 20px 10px 20px 10px;text-align: center;font-family: 'Roboto', sans-serif;font-size: 16px;line-height: 1.5;background-image:url(${grailsApplication.config.grails.serverURL}/assets/email/banner-ocean.png);background-position: top center;background-size: cover;background-repeat: no-repeat">
                        <h1 style="font-size: 24px; color: #fff;">ALA Alerts - ${query.name}</h1>
                        <p style="font-size: 16px; color: #fff;"><strong>${new SimpleDateFormat("dd MMM yyyy").format(new Date())}</strong></p>
                    </td>
                </tr>
                <tr>
                    <td style="background-color: #E8E8E8;color: #000;padding: 40px 30px 40px 30px;text-align: center;font-family: 'Roboto', sans-serif;font-size: 22px;line-height: 1.5;">
                        <div> <g:formatNumber number="${totalRecords}" format="###,###" /> occurrence ${totalRecords == 1 ? 'record' : 'records'} ${totalRecords == 1 ? 'has' : 'have'} been added
                        </div>
                        <div>
                            <a class="btn info-button" href="${moreInfo}">View all new records</a>
                        </div>
                    </td>
                </tr>
            <!-- Records Section -->
                <g:each status="i" in="${records}" var="oc">
                    <tr>
                        <td style="padding: 20px;background-color: white;font-family: 'Roboto', sans-serif;font-size: 14px;line-height: 1.5;">
                        <g:set var="occurrencelink" value="${query.baseUrlForUI}/occurrences/${oc.uuid}"></g:set>
                        <g:set var="assertionlink" value="${query.baseUrl}/occurrences/${oc.uuid}/assertions"></g:set>
                        <table style="width: 100%">
                            <tr style="vertical-align: top;">
                                <td style="width: 37%">
                                    <g:if test="${oc.scientificName?: oc.vernacularName ?: oc.raw_raw_scientificName}">
                                        <strong>${i+1}. <em><a href="${occurrencelink}" style="color: #003A70;text-decoration: none;font-family: 'Roboto', sans-serif;font-size: 16px;line-height: 1.5;">${oc.scientificName?: oc.vernacularName ?: oc.raw_raw_scientificName}</a></em></strong>
                                    </g:if>
                                    <p style="padding-left: 15px;">
                                        <g:if test="${oc.vernacularName}">
                                            ${oc.vernacularName}<br/>
                                        </g:if>
                                        <g:if test="${oc.stateProvince}">
                                            ${oc.stateProvince}<br/>
                                        </g:if>
                                        <g:if test="${oc.family}">
                                            Family: ${oc.family}<br/>
                                        </g:if>
                                        <g:if test="${oc.dataResourceName}">
                                            Source: ${oc.dataResourceName}
                                        </g:if>
                                    </p>
                                </td>
                                <td class="annotation" nowrap="nowrap" style="width: 30%">
                                    <g:if test="${oc.eventDate}">
                                        Date created: ${new SimpleDateFormat("dd MMM yyyy").format(oc.eventDate)}<br/>
                                    </g:if>
                                </td>
                                <td style="width: 33%; text-align: center; vertical-align: middle;" >
                                    <g:if test="${oc.image != null}">
                                        <a href="${occurrencelink}">
                                            <img src="${oc.smallImageUrl}" alt="image for record" style="vertical-align: top;max-width: 150px; width: 150px; height: 150px;border-radius: 6px;line-height: 100%;"/>
                                        </a>
                                    </g:if>
                                    <g:elseif test="${oc.image}">
                                        <a href="${occurrencelink}">
                                            <img src="${oc.image}" alt="${message(code:"biocache.alt.image.for.record")}" style="vertical-align: top;max-width: 150px; width: 150px; height: 150px;border-radius: 6px;line-height: 100%;"/>
                                        </a>
                                    </g:elseif>
                                    <g:else>
                                        <img src="${grailsApplication.config.grails.serverURL}/assets/email/no-image-available.png" alt="No images" style="vertical-align: top;max-width: 150px; width: 150px;height: 150px;border-radius: 6px;line-height: 100%;" />
                                    </g:else>
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
