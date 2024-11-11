<%@ page contentType="text/html"%>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><g:message code="alert.title" args="[query.resourceName]" /></title>
    <style>
    /* Gallery styling */
    .imgCon {
        display: inline-block;
        /* margin-right: 8px; */
        text-align: center;
        line-height: 1.3em;
        background-color: #DDD;
        color: #DDD;
        font-size: 12px;
        /*text-shadow: 2px 2px 6px rgba(255, 255, 255, 1);*/
        /* padding: 5px; */
        /* margin-bottom: 8px; */
        margin: 2px 4px 2px 0;
        position: relative;
    }

    .info-button {
        border: 1pt solid #B53929; /* Flamingo color */
        text-decoration: none;
        font-size: 14px;
        padding: 10px 15px 10px 15px;
    }
    </style>
</head>
<style>
</style>
<body style="background-color: #f4f4f4;margin: 0;padding: 0;font-family: 'Arial', sans-serif;font-size: 16px;line-height: 1.5;">
<table border="0" cellpadding="0" cellspacing="0" width="100%" style="background-color: #f4f4f4;border-spacing: 0;border-collapse: collapse;">
    <tr>
        <td align="center" style="padding: 20px;font-family: 'Arial', sans-serif;font-size: 16px;line-height: 1.5;">
            <table border="0" cellpadding="0" cellspacing="0" width="650" style="background-color: #ffffff;font-family: 'Arial', sans-serif;font-size: 16px;line-height: 1.5;border-spacing: 0;border-collapse: collapse;">
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
                    <td  align="center" bgcolor="#B53929" background="${grailsApplication.config.grails.serverURL}/assets/email/biosecurity-alert-header.png" width="620" height="120" style="color:white;background-color:#B53929;padding: 20px 10px 20px 10px;text-align: center;font-family: 'Arial', sans-serif;font-size: 16px;line-height: 1.5;background-image:url(${grailsApplication.config.grails.serverURL}/assets/email/biosecurity-alert-header.png);background-position: top center;background-size: cover;background-repeat: no-repeat">
                        <h1 style="font-size: 24px; color: #fff;">ALA Alerts - ${query.name}</h1>
                        <p style="font-size: 16px; color: #fff;"><strong>${new SimpleDateFormat("dd MMMM yyyy").format(new Date())}</strong></p>
                    </td>
                </tr>
                <tr>
                    <td style="background-color: #E8E8E8;color: #000;padding: 40px 30px 40px 30px;text-align: center;font-family: 'Arial', sans-serif;font-size: 22px;line-height: 1.5;">
                        <g:set var="totalRecords" value="${records.values().sum { it.size() }}" />
                        <div>
                        ${totalRecords} ${totalRecords == 1 ? ' record with image ' : ' records with image'} ${totalRecords == 1 ? 'has' : 'have'} been added
                        </div>
                        <br/>
                        <div>
                            <a class="btn info-button" href="${moreInfo}" style="color: #B53929;">View all records with new images</a>
                        </div>
                    </td>
                </tr>
            <!-- Records Section -->
                <g:each status="i" in="${records.keySet()}" var="dataResourceId">
                    <tr>
                        <td style="padding: 20px;background-color: white;font-family: 'Arial', sans-serif;font-size: 14px;line-height: 1.5;">
                            <g:set var="occurrences" value="${records[dataResourceId]}" />
                            <g:set var="dataResourceName" value="${occurrences[0]?.dataResourceName?:dataResourceId}" />
                            <g:set var="dataResourcePublicUrl" value="${occurrences[0]?.dataResourceInfo?.alaPublicUrl}" />

                            <g:set var="lastUpdated" value="${occurrences[0]?.dataResourceInfo?.lastUpdated}" />
                            <table style="width: 100%">
                                <tr>
                                    <td colspan="4">
                                        <h3>Dataset: <a href="${dataResourcePublicUrl}">${dataResourceName}</a></h3>
                                        <g:if test="${occurrences[0]?.dataResourceInfo?.lastUpdated}">
                                            <%
                                                try {
                                                    def parsedDate = Date.parse("yyyy-MM-dd'T'HH:mm:ss'Z'", occurrences[0]?.dataResourceInfo?.lastUpdated)
                                                    out << "<p>Last updated: " + parsedDate.format('dd/MM/yyyy') +"</p>"
                                                } catch (Exception e) {
                                                    out <<"<p>Last updated: " + occurrences[0]?.dataResourceInfo?.lastUpdated +"</p>"
                                                }
                                            %>
                                        </g:if>
                                        <hr/>
                                    </td>
                                </tr>
                                <g:each in="${occurrences}" var="oc" status="j">
                                    <g:if test="${j % 4 == 0}">
                                        <tr>
                                    </g:if>
                                    <td style="width: 25%; vertical-align: top;">
                                        <a href="${query.baseUrlForUI}/occurrences/${oc.uuid}">
                                            <img src="${oc.smallImageUrl}" alt="${message(code:"biocache.images.alt.image", args:[oc.scientificName])}" style="vertical-align: top; width:150px; height:150px;border-radius: 6px;line-height: 100%;"/></a>
                                        <br/>
                                        <i>${oc.scientificName} <br>
                                            ${new SimpleDateFormat("dd MMMM yyyy").format(oc.eventDate)}
                                        </i>
                                    </td>
                                    <g:if test="${(j + 1) % 4 == 0 || j + 1 == occurrences.size()}">
                                        </tr>
                                    </g:if>
                                </g:each>
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

