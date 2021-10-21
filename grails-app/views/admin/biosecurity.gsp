<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="au.org.ala.alerts.Notification" %>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <meta name="breadcrumb" content="Find users" />
    <meta name="breadcrumbParent" content="${request.contextPath}/admin,Alerts admin" />

    <title>Admin - creat BioSecurity alert</title>
    <asset:stylesheet href="alerts.css"/>
</head>

<body>
<div id="content">
    <header id="page-header">
        <div class="inner row">
            <div class="col-lg-6 col-sm-6 col-xs-12">
                <h1>Create BioSecurity alert</h1>
            </div>
        </div>
    </header>

    <div id="page-body" role="main">
        <g:form name="create-security-alert" controller="webservice" action="subscribeBioSecurityAlert" method="post" class="form-horizontal">
            <div class="row">
                <div class="col-lg-6 col-sm-6 col-xs-12">
                    <label for="listid" class="control-label">Species list uid:</label>
                    <div class="input-group">

    %{--                    <g:input-g--}%
                        <input type="text" id= "listid" name="listid"/>
                    </div>
                    <label for="useremail" class="control-label">Email contains:</label>
                    <div class="input-group">
                        <input type="text" id= "useremail" name="useremail"/>

%{--                        <g:textField name="term" value="${params.term}" class="form-control" placeholder="Search for..."/>--}%
                        %{--<input type="text" class="form-control" placeholder="Search for...">--}%
                    </div><!-- /input-group -->
                </div><!-- /.col-lg-6 -->
            </div><!-- /.row -->

            <div>
            <span class="input-group-btn">
%{--                <g:actionSubmit value="Find" class="btn btn-primary" action="createBioSecurityAlert"/>--}%
                <button type="submit" form="create-security-alert" class="btn btn-primary">Save changes</button>
                %{--<button class="btn btn-default" type="button">Go!</button>--}%
            </span>
            </div>

        </g:form>
        <div class="row">
            <div class="col-md-12">
                <g:if test="${users}">
                    <table class="table table-striped">
                        <thead>
                        <th>User Id</th>
                        <th>Email</th>
                        <th></th>
                        </thead>
                        <tbody>
                        <g:each in="${users}" var="user">
                            <tr>
                                <td>${user.userId}</td>
                                <td>${user.email}</td>
                                <td><a href="${request.contextPath}/admin/user/${user.userId}">Manage alerts</a></td>
                            </tr>
                        </g:each>
                        </tbody>
                    </table>
                </g:if>
                <g:elseif test="${params.term}">
                    <div style="margin-top:15px;">No users found for &quot;${params.term}&quot;</div>
                </g:elseif>
            </div><!-- /.col-md-12 -->
        </div><!-- /.row -->

    </div>
</div>
<asset:javascript src="alerts.js"/>
</body>
</html>