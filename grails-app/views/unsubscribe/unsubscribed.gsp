<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="au.org.ala.alerts.Notification" %>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <meta name="breadcrumb" content="Unsubscription successful"/>
    <meta name="breadcrumbParent" content="${request.contextPath?:'/'},My alerts" />

    <title><g:message code="unsubscribed.title" /></title>
</head>

<body>
    <div id="content">
        <header id="page-header">
            <div class="inner row-fluid">
                <hgroup>
                    <h1><g:message code="unsubscribed.title" /></h1>
                </hgroup>
            </div>
        </header>

        <div id="page-body" role="main">
            <g:message code="unsubscribed.successfully.unsubscribed" />
            <br><br>
            <g:link uri="/"><g:message code="unsubscribed.view.your.active.email.alerts" /></g:link>
        </div>
    </div>

</body>
</html>