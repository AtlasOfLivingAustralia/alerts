<!doctype html>
<html>
    <head>
        <title><g:message code="not.found.title" /></title>
        <meta name="layout" content="${grailsApplication.config.skin.layout}" />
    </head>

    <body class="bg-light">
        <div class="container text-center py-5">
            <div class="row justify-content-center">
                <div class="col-md-8 col-lg-6">
                    <h1 class="display-1 text-disabled mb-3">404</h1>

                    <h2 class="h4 mb-4">
                        <g:message code="not.found.description" default="Oops! We could not find the page you are looking for." />
                    </h2>

                    <p class="lead mb-3">
                        <g:message code="not.found.path"/>
                        ${request.forwardURI}
                    </p>

                    <!-- Navigation buttons -->
                    <div class="d-flex justify-content-center gap-2 mb-4">
                        <g:link controller="notification" action="myAlerts" class="btn btn-primary">
                            <g:message code="error.page.home" default="Go Home"/>
                        </g:link>

                        <a href="javascript:history.back()" class="btn btn-outline-secondary">
                            <g:message code="error.page.back" default="Go Back"/>
                        </a>
                    </div>

                    <!-- Support contact / extra info -->
                    <div class="text-muted small">
                        <g:message code="not.found.email.body"
                                   args="[grailsApplication.config.skin.orgSupportEmail,
                                          grailsApplication.config.skin.orgNameShort,
                                          request.scheme,
                                          request.serverName,
                                          request.forwardURI,
                                          request.getHeader('referer')?:'(no referer found)',
                                          grailsApplication.config.skin.orgSupportEmail]" />
                    </div>

                </div>
            </div>
        </div>
    </body>
</html>
