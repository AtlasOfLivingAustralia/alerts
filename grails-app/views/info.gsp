<!doctype html>
<html>
    <head>
        <title>Info</title>
        <meta name="layout" content="${grailsApplication.config.skin.layout}" />
        <meta name="breadcrumb" content="Info"/>
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
    </head>

    <body class="bg-light">
        <div class="container text-center py-5">
            <div class="row justify-content-center">
                <div class="col-md-8 col-lg-6">
                    <g:set var="isSuccess" value="${status == 0}" />
                    <p class="lead mb-3">
                        <i class="fas ${isSuccess ? 'fal fa-info-circle' : 'fa-exclamation-triangle'} me-2"></i>
                        ${message ?: 'No additional information provided.'}
                    </p>

                    <div class="d-flex justify-content-center gap-2 mb-4">
                        <g:if test="${redirectUrl}">
                            <a href="${redirectUrl}"  class="btn btn-primary"><i class="fas fa-home me-2"></i> Go Home</a>
                        </g:if>
                        <g:else>
                            <a href="javascript:history.back()" class="btn btn-outline-secondary">
                                <g:message code="error.page.back" default="Go Back"/>
                            </a>
                        </g:else>
                    </div>
                </div>
            </div>
        </div>
    </body>
</html>