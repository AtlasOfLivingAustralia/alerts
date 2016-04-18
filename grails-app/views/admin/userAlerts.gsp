<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="au.org.ala.alerts.Notification" %>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>

    <title>Admin - user alerts</title>
    <r:require modules="bootstrapSwitch,alerts"/>
</head>

<body>
<div id="content">
    <header id="page-header">
        <div class="inner row-fluid">
            <nav id="breadcrumb" class="span12">
                <ol class="breadcrumb">
                    <li><a href="${request.contextPath}/admin">Admin</a> <span class="icon icon-arrow-right"></span></li>
                    <li class="active">Manage user alerts</li>
                </ol>
            </nav>
            <hgroup>
                <h1>Find users</h1>
            </hgroup>
        </div>
    </header>

    <div id="page-body" role="main">
        <g:form controller="admin" action="findUser" method="post" class="form-horizontal">

            <div class="control-group">
                <label for="term" class="control-label">Email contains:</label>

                <div class="controls">
                    <g:textField name="term"/>
                    <g:actionSubmit value="Find" class="btn btn-primary" action="findUser"/>
                </div>
            </div>
        </g:form>

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
    </div>
</div>
</body>
</html>