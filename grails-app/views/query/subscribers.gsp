<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="au.org.ala.alerts.Notification" %>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <meta name="breadcrumb" content="Subscribed users"/>
    <meta name="breadcrumbParent" content="${request.contextPath}/admin/biosecurity, BioSecurity alerts"/>

    <title>Admin - Subscribed users</title>
    <asset:stylesheet href="alerts.css"/>
</head>

<body>
<div id="content">
    <header id="page-header">
        <div class="inner row">
            <div class="col-sm-6 col-xs-12">
                <h2><g:message code="" default="Users subscribed to alert dr2627"/></h2>
            </div>
        </div>
        <g:if test="${flash.message}">
            <div id="errorAlert" class="alert alert-danger alert-dismissible alert-dismissable" role="alert">
                <button type="button" class="close" onclick="$(this).parent().hide()" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4>${flash.message}</h4>
            </div>
        </g:if>
    </header>

    <div id="page-body" class="col-sm-12">
        <table class="table table-striped">
            <thead>
            <tr>
                <th><g:message code="unsubscribeusers.view.table.header.useremail" default="User email"/></th>
                <th></th>
            </tr>
            </thead>
            <tbody>
            <g:each status="i" in="${users}" var="user">
                <tr>
                    <td>${user.email}</td>
                    <td><a href="${request.contextPath}/query/unsubscribeAlert?queryid=${queryid}&useremail=${user.email}">Unsubscribe user</a></td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>