<%@ page contentType="text/html"%>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${title}</title>
</head>
<body style="background-color: #f4f4f4;margin: 0;padding: 0;font-family: 'Roboto', sans-serif;font-size: 16px;line-height: 1.5;">
<table role="presentation" style="width: 100%;border: 0;background-color: #f4f4f4;font-family: 'Roboto', sans-serif;font-size: 16px;line-height: 1.5;border-spacing: 0;border-collapse: collapse;">
    <tr>
        <td align="center" style="padding: 20px;font-family: 'Roboto', sans-serif;font-size: 16px;line-height: 1.5;">
            <table role="presentation" width="650" style="width: 650px;max-width: 100%;border: 0;background-color: #ffffff;font-family: 'Roboto', sans-serif;font-size: 16px;line-height: 1.5;border-spacing: 0;border-collapse: collapse;">
                <!-- Logo -->
                <tr>
                    <td style="text-align: center; padding: 20px; background-color: #fff;font-family: 'Roboto', sans-serif;font-size: 16px;line-height: 1.5;">
                        <a href="https://www.ala.org.au" target="_blank" style="font-family: 'Roboto', sans-serif;font-size: 16px;line-height: 1.5;">
                            <img src="${grailsApplication.config.grails.serverURL + '/assets/email/logo-dark.png'}" height="60" alt="Logo" style="display: block;border: 0;line-height: 100%;">
                        </a>
                    </td>
                </tr>
                <!-- Header -->
                <tr>
                    <td background="${grailsApplication.config.grails.serverURL}/assets/email/biosecurity-alert-header.png"
                        style="width:620px ; height: 120px; text-align: center; color:white;background-color:#B53929;padding: 20px 10px 20px 10px;font-family: 'Roboto', sans-serif;font-size: 16px;line-height: 1.5;background-image:url(${grailsApplication.config.grails.serverURL}/assets/email/biosecurity-alert-header.png);background-position: top center;background-size: cover;background-repeat: no-repeat">
                        <h1 style="font-size: 24px; color: #fff;">Biosecurity Alerts</h1>
                        <p style="font-size: 16px; color: #fff;"><strong>${new SimpleDateFormat("dd MMM yyyy").format(new Date())}</strong></p>
                        <p style="font-size: 16px; color: #fff;">Alerts service for new ALA records listing potential invasive species</p>
                    </td>
                </tr>
                <tr>
                    <td style="text-align:center; background-color: #E8E8E8;color: #000;padding: 20px 30px 20px 30px;font-family: 'Roboto', sans-serif;font-size: 22px;line-height: 1.5;">
                        <div> ${totalRecords} new ${totalRecords == 1 ? 'record' : 'records'} for
                        </div>
                        <div>
                            <g:set var="listURL" value="${grailsApplication.config.getProperty("lists.baseURL") + '/speciesListItem/list/' + query.listId}" />
                            <g:set var="listName" value="${query.name.replaceAll('(?i)BioSecurity alert for', '').replaceAll('\"', '').trim()}" />
                            <strong>${StringUtils.abbreviate(listName, 40)}, ${query.listId}</strong>
                        </div>
                        <g:if test="${moreInfo?.lastChecked}">
                            <div><i>since ${new SimpleDateFormat("dd MMM yyyy").format(moreInfo.lastChecked)}</i></div>
                        </g:if>
                    </td>
                </tr>
                <g:if test="${moreInfo.countByDataProvider}">
                    <tr style="background-color: #E8E8E8; font-size: 12px; color: #635b5b; font-family: 'Roboto', sans-serif;line-height: 1.5;">
                        <td style="padding-left: 20px;">
                            <g:each in="${moreInfo.countByDataProvider}" var="entry" status="j">
                                <g:if test="${j > 0}">/</g:if>
                                <a href="#provider-${entry.key.replaceAll('[^a-zA-Z0-9]+', '-').toLowerCase()}"
                                   style="color: #635b5b; text-decoration: underline;">${entry.key} (${entry.value})</a>
                            </g:each>
                        </td></tr>
                </g:if>
                   <!-- Records Section -->
                <%-- Tracks the providers already anchored, so only the first record of each provider gets the anchor id --%>
                <g:set var="anchoredProviders" value="${[] as Set}" />
                <g:each status="i" in="${records}" var="oc">
                <g:set var="link" value="${query.baseUrlForUI}/occurrences/${oc.uuid}"></g:set>
                <g:set var="ocProvider" value="${oc.dataProviderName ?: oc.dataProvider ?: oc.dataResourceName ?: 'Unknown'}" />
                <g:set var="isFirstRecordOfProvider" value="${anchoredProviders.add(ocProvider)}" />

                <g:if test="${isFirstRecordOfProvider}">
                    <tr id="${'provider-' + ocProvider.replaceAll('[^a-zA-Z0-9]+', '-').toLowerCase()}" >
                        <td style="text-align: center;padding-top:20px;font-size: 12px; color: #635b5b; font-family: 'Roboto', sans-serif;line-height: 1.5;">--- ${ocProvider} ---</td>
                    </tr>
                </g:if>
                <tr>
                    <td style="padding: 20px;background-color: white;font-family: 'Roboto', sans-serif;font-size: 16px;line-height: 1.5;">
                        <table role="presentation" style="border: 0;width: 100%;font-family: 'Roboto', sans-serif;font-size: 16px;line-height: 1.5;border-spacing: 0;border-collapse: collapse;">
                            <tr>
                                <td width="46%" valign="top" style="width: 46%;vertical-align: top;font-family: 'Roboto', sans-serif;font-size: 16px;line-height: 1.5;">
                                    <a href="${query.baseUrlForUI}/occurrences/${oc.uuid}" style="color: #C44D34;text-decoration: none;font-family: 'Roboto', sans-serif;font-size: 16px;line-height: 1.5;">
                                        <strong>${i+1}. <em>${oc.scientificName ?: 'N/A'}</em></strong>
                                    </a>
                                    <p style="font-size: 13px; color: #212121;padding-left: 15px;">
                                        <g:if test="${oc.scientificName && oc.raw_scientificName && oc.scientificName != oc.raw_scientificName}">
                                            Supplied as:<em>${oc.raw_scientificName}</em><br>
                                        </g:if>
                                        <g:if test="${oc.vernacularName}">
                                            Common name: ${oc.vernacularName}<br>
                                        </g:if>
                                        <g:if test="${oc.lga}">
                                            <strong>${oc.lga}</strong><br>
                                        </g:if>
                                        <g:if test="${oc.locality && oc.stateProvince}">
                                            <strong>${oc.locality}; ${oc.stateProvince}</strong><br>
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
                                            Time & date: ${new SimpleDateFormat('dd-MM-yyyy HH:mm').format(oc.eventDate)} <br>
                                        </g:if>
                                        <g:if test="${oc.dataResourceName}">
                                            Source: ${oc.dataResourceName} <br>
                                        </g:if>
                                    </p>
                                </td>
                                <td width="28%" valign="top" style="width: 28%;vertical-align: top;font-family: 'Roboto', sans-serif;font-size: 16px;line-height: 1.5;">
                                    <a href="https://www.google.com/maps/place/${oc.latLong}" target="_blank" style="font-family: 'Roboto', sans-serif;font-size: 16px;line-height: 1.5;">
                                        <img src="https://maps.googleapis.com/maps/api/staticmap?center=${oc.latLong}&markers=|${oc.latLong}&zoom=12&size=150x150&maptype=roadmap&key=${grailsApplication.config.getProperty('google.apikey')}" alt="Map Image" style="width: 150px;height: 150px;display: block;border: 0;line-height: 100%;border-radius: 6px;">
                                    </a>
                                </td>
                                <td width="26%" valign="top" style="width: 26%;vertical-align: top;font-family: 'Roboto', sans-serif;font-size: 16px;line-height: 1.5;">
                                    <g:set var="imageUrl" value="${oc.imageUrls?.get(0) ?: oc.thumbnailUrl ?: oc.smallImageUrl}" />
                                    <g:if test="${imageUrl}">
                                        <a href="${query.baseUrlForUI}/occurrences/${oc.uuid}">
                                            <img src="${imageUrl}${imageUrl.contains('?') ? '&thumbnailType=square_white' : ''}" alt="Species Image" height="150" width="150" style="vertical-align: top;max-width: 150px;width: 150px;height: 150px;border-radius: 6px;line-height: 100%;" />
                                        </a>
                                    </g:if>
                                    <g:else>
                                        <a href="${query.baseUrlForUI}/occurrences/${oc.uuid}">
                                            <img src="${grailsApplication.config.grails.serverURL}/assets/email/no-image-available.png" alt="Species Image" height="150" width="150" style="vertical-align: top;max-width: 150px;width: 150px;height: 150px;border-radius: 6px;line-height: 100%;" />
                                        </a>
                                    </g:else>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
                </g:each>
                <tr>
                    <td style="padding: 35px 70px 35px 70px;background-color: #C44D34;color: #fff;font-family: 'Roboto', sans-serif;font-size: 14px;line-height: 1.43;text-align: center;">
                        <p>If you notice a record has been misidentified, we encourage you to use your expertise to improve the quality of Australia's Biosecurity data.</p>
                        <p>Please either annotate the record in the provider platform itself or notify us at <a href="mailto:biosecurity@ala.org.au" style="color: #f2f2f2; font-weight: 700;">biosecurity@ala.org.au</a> for assistance.</p>
                    </td>
                </tr>
                <tr>
                    <td style="padding: 35px 70px 35px 70px;background-color: #000000;color: #fff;font-family: 'Roboto', sans-serif;font-size: 14px;line-height: 1.43;;text-align: center;">
                        <p>The Atlas of Living Australia acknowledges Australia's Traditional Owners and pays respect to the past and present Elders of the nation's Aboriginal and Torres Strait Islander communities.</p>
                        <p>We honour and celebrate the spiritual, cultural and customary connections of Traditional Owners to Country and the biodiversity that forms part of that Country.</p>
                    </td>
                </tr>
                <tr>
                    <td style="padding: 35px 70px 35px 70px;background-color: #ffffff;color: #000;font-family: 'Roboto', sans-serif;font-size: 14px;line-height: 1.43;text-align: center;">
                        <p>
                            <a href="https://www.education.gov.au/national-collaborative-research-infrastructure-strategy-ncris"><img loading="lazy"  src="${grailsApplication.config.grails.serverURL}/assets/email/NCRIS_150px-150x109.jpg" alt="NCRIS logo" width="125" height="90"></a>&nbsp;
                            &nbsp;<a href="https://csiro.au/"><img loading="lazy" src="${grailsApplication.config.grails.serverURL}/assets/email/CSIRO_Solid_RGB-150x150.png" alt="CSIRO logo" width="90" height="90"></a>&nbsp;
                            &nbsp;<a href="https://www.gbif.org/en/"><img loading="lazy"  src="${grailsApplication.config.grails.serverURL}/assets/email/GBIF_109px.png" alt="GBIF logo" width="171" height="90"></a>
                        </p>
                        <div style="margin-top: 25px;">
                            <div>
                                <p>Our mailing address is: </p>
                                Atlas of Living Australia <br> GPO Box 1700<br> Canberra, ACT 2601<br>Australia
                            </div>
                            <br/>
                            You are receiving this email because you opted in to ALA alerts.
                            <br/>
                            Don't want to receive these emails? You can <a href="${unsubscribeOne}" style="color: #C44D34;">unsubscribe</a>.
                        </div>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
</table>
</body>
</html>
