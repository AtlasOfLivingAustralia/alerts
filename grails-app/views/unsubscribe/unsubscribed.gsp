<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="au.org.ala.alerts.Notification" %>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>

    <title>Unsubscribe</title>
    <asset:stylesheet href="alerts.css"/>
</head>

<body>
<div id="content">
    <header id="page-header">
        <div class="inner row-fluid">
            <hgroup>
                <h1>Unsubscribe</h1>
            </hgroup>
        </div>
    </header>

    <div id="page-body" role="main">
        Thank you. You have successfully unsubscribed.
        <br><br>
        <g:link uri="/">View your active email alerts</g:link>
    </div>
</div>
<asset:javascript src="alerts.js"/>
</body>
</html>