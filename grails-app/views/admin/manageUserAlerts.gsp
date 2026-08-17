<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page expressionCodec="none" %>
%{--
    An ADMIN managing another user's alerts. Rendered by AdminController#showUsersAlerts (/admin/user/$userId).
    The alert lists/actions live in /notification/_alertsPanel.gsp, shared with /notification/myAlerts.gsp
--}%
<!doctype html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="${grailsApplication.config.skin.layout}" />
        <meta name="breadcrumb" content="${user.email}" />
        <meta name="breadcrumbParent" content="${request.contextPath}/admin,Alerts admin" />
        <title><g:message code="my.alerts.title" args="[user.email]" /> | ${grailsApplication.config.skin.orgNameLong}</title>
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
                  <div class="col-8">
                      <h2>
                          <g:message code="my.alerts.h1" args="[user.email]" />
                          <g:if test="${user.locked}">
                              <i class="fas fa-lock" data-bs-toggle="tooltip" data-bs-placement="bottom" title="${g.message(code:'my.alerts.user.isLocked.title')}"></i>
                          </g:if>
                      </h2>
                      <small class="text-muted">User id: ${user.userId}</small>
                  </div>
                  <div class="col-4 text-end">
                      <a href="${createLink(controller: 'admin', action: 'findUser')}" class="btn btn-outline-primary">Find users</a>
                      <a href="${createLink(controller: 'admin', action: 'index')}" class="btn btn-primary">Admin</a>
                  </div>
              </div>
          </header>

          <g:if test="${!isMyOwnAlerts}">
              <div class="alert alert-warning mt-3" role="alert">
                  <i class="fas fa-user-shield"></i>
                  You are managing alerts on behalf of <b>${user.email}</b>. Changes are applied to that user immediately.
              </div>
          </g:if>

          <g:if test="${flash.message}">
              <div class="alert alert-info" role="alert">${flash.message}</div>
          </g:if>
          <g:if test="${flash.errorMessage}">
              <div class="alert alert-danger">${flash.errorMessage}</div>
          </g:if>

          <g:render template="/notification/alertsPanel"
                    model="[user                   : user,
                            isMyOwnAlerts          : isMyOwnAlerts,
                            enabledStandardQueries : enabledStandardQueries,
                            disabledStandardQueries: disabledStandardQueries,
                            enabledCustomQueries   : enabledCustomQueries,
                            disabledCustomQueries  : disabledCustomQueries,
                            frequencies            : frequencies]"/>
      </div>
    </body>
</html>


