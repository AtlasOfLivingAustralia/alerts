<!DOCTYPE html>
<html lang="en">
<head>
    <title>Biosecurity Alerts Reporting</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <meta name="breadcrumb" content="CSV"/>
    <meta name="breadcrumbParent" content="${request.contextPath}/biosecurity/csv,BioSecurity"/>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</head>
<body>
<div class="container py-4">
    <h2 class="mb-4">Downloads history</h2>

    <div class="vstack gap-4">
        <g:each in="${downloads}" var="download">

            <div class="row align-items-center border rounded p-2 g-2">
                <g:set var="isExpired" value="${download.expiresAt < new Date()}" />
                <!-- Created -->
                <div class="col-12 col-md-3">
                    <div class="text-muted small">Created</div>
                    <div>
                        <g:formatDate date="${download.createdAt}" format="yyyy-MM-dd HH:mm:ss"/>
                    </div>
                </div>
                <!-- Expires + Status -->
                <div class="col-12 col-md-3">
                    <div class="text-muted small">Expires</div>
                    <div>
                        <g:formatDate date="${download.expiresAt}" format="yyyy-MM-dd HH:mm:ss"/>
                        <span class="badge bg-${isExpired ? 'danger' : 'success'} ms-2">
                            ${isExpired ? 'Expired' : 'Active'}
                        </span>
                    </div>
                </div>
                <!-- Download Button -->
                <div class="col-12 col-md-4 text-md-end">
                <g:if test="${!isExpired}">
                    <a href="${createLink(controller:'csv', action:'downloadWithToken', params:[token:download.token])}"
                       class="btn btn-primary btn-sm ">
                        Download
                    </a>
                </g:if>
                </div>
            </div>

        </g:each>
    </div>
</div>
</body>
</html>