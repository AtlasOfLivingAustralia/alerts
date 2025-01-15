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
    .info-button {
        border: 1pt solid #C44D34;
        text-decoration: none;
        font-size: 14px;
        padding: 10px 15px 10px 15px;
        color: black;
        border-radius: 5px;
    }

     a {
         color: #003A70;
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
                    <td height="120" style="color:white;background-color:#003A70;padding: 20px 10px 20px 10px;text-align: center;font-family: 'Roboto', sans-serif;font-size: 16px;line-height: 1.5;background-image:url(${grailsApplication.config.grails.serverURL}/assets/email/banner-ocean.png);background-position: top center;background-size: cover;background-repeat: no-repeat">
                        <h1 style="font-size: 24px; color: #fff;">ALA Alerts - ${query.name}</h1>
                        <p style="font-size: 16px; color: #fff;"><strong>${new SimpleDateFormat("dd MMM yyyy").format(new Date())}</strong></p>
                    </td>
                </tr>
                <tr>
                    <td style="background-color: #E8E8E8;color: #000;padding: 40px 30px 40px 30px;text-align: center;font-family: 'Roboto', sans-serif;font-size: 22px;line-height: 1.5;">
                        <div> ${totalRecords} ${totalRecords == 1 ? 'dataset' : 'datasets'} ${totalRecords == 1 ? 'has' : 'have'} been updated
                        </div>
                        <div style="padding-top: 20px;">
                            <a class="btn info-button" href="${moreInfo}"><g:message code="datasets.view.details.of.the.added" /></a>
                        </div>
                    </td>
                </tr>
            <!-- Records Section -->
                <g:each status="i" in="${records}" var="oc">
                    <tr>
                        <td style="padding: 20px;background-color: white;font-family: 'Roboto', sans-serif;font-size: 14px;line-height: 1.5;">
                            <g:set var="oclink" value="${grailsApplication.config.collectory.baseURL}/public/show/${oc.uid}"></g:set>
                            <table style="width: 100%">
                                <tr style="vertical-align: top;">
                                    <td style="width: 70%">
                                        <table>
                                            <tr>
                                                <td style="white-space: nowrap; vertical-align: top; text-align: right; padding-right: 10px;">
                                                    <strong>${i+1}. </strong>
                                                </td>
                                                <td style="vertical-align: top;">
                                                    <a href="${oclink}" style="color: #003A70;font-family: 'Roboto', sans-serif;font-size: 16px;line-height: 1.5;">
                                                        <strong><em>${oc.label}</em></strong>
                                                    </a>
                                                    <p>
                                                        <g:if test="${oc.count}">
                                                            Total records: ${oc.count}<br/>
                                                        </g:if>

                                                        <g:if test="${oc.details?.licenseType}">
                                                            Licence: ${oc.details?.licenseType}<br/>
                                                        </g:if>
                                                        <g:if test="${oc.details?.resourceType}">
                                                            Content type: ${oc.details?.resourceType}<br/>
                                                        </g:if>
                                                        <br/>
                                                        <g:if test="${oc.details?.pubShortDescription}">
                                                            ${StringUtils.abbreviate(oc.details?.pubShortDescription, 200)}<br/>
                                                        </g:if>
                                                        <g:else>
                                                            <g:if test="${oc.details?.pubDescription}">
                                                                ${StringUtils.abbreviate(oc.details?.pubDescription, 200)}<br/>
                                                            </g:if>
                                                        </g:else>
                                                    </p>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                    <td style="width: 33%; text-align: right;" >

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
