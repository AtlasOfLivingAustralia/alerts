<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page expressionCodec="none" %>
%{--
    A user managing their OWN alerts. Rendered by NotificationController#myAlerts.
    The alert lists/actions live in /notification/_alertsPanel.gsp, shared with /admin/manageUserAlerts.gsp
--}%
<!doctype html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="${grailsApplication.config.skin.layout}" />

        <meta name="breadcrumb" content="${message(code:"my.alerts.breadcrumbs")}" />
        <meta name="breadcrumbParent" content="${grailsApplication.config.userdetails.web.url}/myprofile, ${message(code:"my.alerts.breadcrumb.parent")}" />
        <g:set var="userPrefix" value="${message(code:'my.alerts.my')}"/>
        <title><g:message code="my.alerts.title" args="[userPrefix]" /> | ${grailsApplication.config.skin.orgNameLong}</title>
        <asset:stylesheet src="alerts.css"/>
        <script>
            document.addEventListener('DOMContentLoaded', function () {
                var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
                tooltipTriggerList.map(function (el) {
                    return new bootstrap.Tooltip(el);
                });
            });
        </script>
    </head>
    <body>
      <div class="container ms-2">
          <header id="page-header">
              <div class="row align-items-center">
                  <div class="col-6">
                      <h2>
                          <g:message code="my.alerts.h1" args="[userPrefix]" />
                          <g:if test="${user.locked}">
                              <i class="fas fa-lock" data-bs-toggle="tooltip" data-bs-placement="bottom" title="${g.message(code:'my.alerts.user.isLocked.title')}"></i>
                          </g:if>
                      </h2>
                  </div>
                  <div class="col-6 text-end">
                      <% if (request.isUserInRole("ROLE_ADMIN")) { %>
                      <a href="${createLink(controller: 'admin', action: 'index')}" class="btn btn-primary">Admin</a>
                      <% } %>

                      <% if (request.isUserInRole("ROLE_BIOSECURITY_ADMIN")) { %>
                      <a href="${createLink(namespace: 'biosecurity', controller: 'admin', action: 'index')}" class="btn btn-primary">Biosecurity Admin</a>
                      <% } %>
                  </div>
              </div>
          </header>

          <g:if test="${flash.message}">
              <div class="alert alert-info" role="alert">${flash.message}</div>
          </g:if>
          <g:if test="${flash.errorMessage}">
              <div class="alert alert-danger">${flash.errorMessage}</div>
          </g:if>

          <g:render template="/notification/alertsPanel"
                    model="[user                   : user,
                            isMyOwnAlerts          : true,
                            enabledStandardQueries : enabledStandardQueries,
                            disabledStandardQueries: disabledStandardQueries,
                            enabledCustomQueries   : enabledCustomQueries,
                            disabledCustomQueries  : disabledCustomQueries,
                            frequencies            : frequencies]"/>
      </div>
    </body>
</html>

