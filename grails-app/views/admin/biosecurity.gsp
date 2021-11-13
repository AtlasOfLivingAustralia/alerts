<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="au.org.ala.alerts.Notification" %>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <meta name="breadcrumb" content="BioSecurity alerts"/>
    <meta name="breadcrumbParent" content="${request.contextPath}/admin,Alerts admin"/>

    <title>Admin - Manage BioSecurity alerts</title>
    <asset:stylesheet href="alerts.css"/>
</head>

<body>
<div id="content">
    <header id="page-header">
        <div class="inner row">
            <div class="col-sm-6 col-xs-12">
                <h1><g:message code="biosecurity.view.header" default="Manage BioSecurity alerts"/></h1>
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
        <g:form name="create-security-alert" action="subscribeBioSecurity" method="post" class="form-horizontal">
            <div class="row">
                <div class="col-lg-6 col-sm-6 col-xs-12">
                    <div class="form-group">
                        <label for="listid" class="control-label"><g:message code="biosecurity.view.body.label.specieslistid" default="Species list uid"/></label>
                        <input type="text" id="listid" name="listid" class="form-control"/>
                    </div>

                    <div class="form-group">
                        <label for="useremails" class="control-label"><g:message code="biosecurity.view.body.label.useremails" default="User emails"/></label>
                        <input type="text" id="useremails" name="useremails" class="form-control" placeholder="<g:message code="biosecurity.view.body.label.useremailsallowmultiple" default="You can input multiple user emails by separating them with ';'"/>"/>
                    </div>
                </div>
            </div>

            <div class="form-group">
                <button type="submit" form="create-security-alert" class="btn btn-primary"><g:message code="biosecurity.view.body.button.subscribe" default="Subscribe alert"/></button>
            </div>
        </g:form>
        <g:if test="${queries}">
            <div>
                <table class="table table-striped">
                    <thead>
                    <tr>
                        <th class="biosecurityTableColumn"><g:message code="biosecurity.view.body.table.header.queryname" default="Query name"/></th>
                        <th class="biosecurityTableColumn"><g:message code="biosecurity.view.body.table.header.subnumber" default="Number of subscribers"/></th>
                        <th class="biosecurityTableColumn"></th>
                        <th class="biosecurityTableColumn"></th>
                        <th class="biosecurityTableColumn"></th>
                    </tr>
                    </thead>
                    <tbody>
                    <g:each status="i" in="${queries}" var="query">
                        <tr>
                            <td><g:link controller="query" action="show" id="${query.id}">${query.name}</g:link></td>
                            <td>${subscribers[i].size()}</td>
                            <td>
                            <g:if test="${subscribers[i].size() != 0}"><a href="${request.contextPath}/query/subscribers?queryid=${query.id}">View all subscribers</a></td></g:if>
                            <td><a href="${request.contextPath}/admin/deleteQuery?queryid=${query.id}"><g:message code="biosecurity.view.body.table.deletequery" default="delete the query"/></a>
                            </td>
                            <td><a href="${request.contextPath}/admin/unsubscribeAllUsers?queryid=${query.id}"><g:message code="biosecurity.view.body.table.unsubscribeall" default="unsubscribe all users"/></a>
                            </td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
        </g:if>

        <asset:image src="biosecurity-banner_v2.png" alt="ALA logo" absolute="true" />
    </div>
</div>
</body>
</html>