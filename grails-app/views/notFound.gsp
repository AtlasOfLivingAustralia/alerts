<!doctype html>
<html>
    <head>
        <title>Page Not Found</title>
        <meta name="layout" content="main">
        <g:if env="development"><asset:stylesheet src="errors.css"/></g:if>
    </head>
    <body>
        <ul class="errors">
            <li>Error: Page Not Found (404)</li>
            <li>Path: ${request.forwardURI}</li>
        </ul>
        <div>If you think this is an error, please send an email to <a href="mailto:support@ala.org.au?subject=ALA Alerts%20error&body=Requested URL: ${request.scheme}://${request.serverName}${request.forwardURI}%0AForwarded from: ${request.getHeader("referer")?:'(no referer found)'}%0APlease describe the steps you took to trigger this error:">support@ala.org.au</a> describing the issue</div>

    </body>
</html>
