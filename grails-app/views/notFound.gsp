<!doctype html>
<html>
    <head>
        <title><g:message code="not.found.title" /></title>
        <meta name="layout" content="main">
        <g:if env="development"><asset:stylesheet src="errors.css"/></g:if>
    </head>
    <body>
        <ul class="errors">
            <li><g:message code="not.found.page.title" /></li>
            <li><g:message code="not.found.path" />${request.forwardURI}</li>
        </ul>
        <div><g:message code="not.found.email.body" args="[grailsApplication.config.skin.orgSupportEmail,
                                                           grailsApplication.config.skin.orgNameShort, request.scheme,
                                                           request.serverName, request.forwardURI,
                                                           request.getHeader('referer')?:'(no referer found)',
                                                           grailsApplication.config.skin.orgSupportEmail]" /></div>

    </body>
</html>
