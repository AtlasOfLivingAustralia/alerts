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
                      <g:if test="${!isMyOwnAlerts}">
                          <button type="button" class="btn btn-outline-primary" onclick="previewUserDeletion()">
                              <i class="fas fa-trash" aria-hidden="true"></i> Delete this user
                          </button>
                      </g:if>
                      <a href="${createLink(controller: 'admin', action: 'findUser')}" class="btn btn-outline-primary">Find users</a>
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

      %{-- Confirmation dialog for deleting this user --}%
      <div class="modal fade" id="deleteUserModal" tabindex="-1" aria-labelledby="deleteUserModalLabel" aria-hidden="true">
          <div class="modal-dialog modal-dialog-scrollable">
              <div class="modal-content">
                  <div class="modal-header">
                      <h5 class="modal-title" id="deleteUserModalLabel">Delete ${user.email}?</h5>
                      <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                  </div>
                  <div class="modal-body">
                      <p>This cannot be undone. The following will be permanently removed:</p>
                      <div id="deleteUserSummary"></div>
                      <div class="small text-muted mt-3">
                          Alerts shared with other users are not deleted - only this user's subscription to them.
                      </div>
                  </div>
                  <div class="modal-footer">
                      <button type="button" class="btn btn-outline-primary" data-bs-dismiss="modal">Cancel</button>
                      <button type="button" class="btn btn-primary" id="confirmDeleteUserBtn" onclick="deleteUser()">
                          <i class="fas fa-trash" aria-hidden="true"></i> Yes, delete this user
                      </button>
                  </div>
              </div>
          </div>
      </div>

      <script type="text/javascript">
          var previewUserDeletionUrl = '${createLink(controller: 'admin', action: 'previewUserDeletion')}';
          var deleteUserUrl = '${createLink(controller: 'admin', action: 'deleteUser')}';
          var findUserUrl = '${createLink(controller: 'admin', action: 'findUser')}';
          var targetUserId = '${user.userId}';

          /**
           * Ask the server what would be deleted, list it, then let the admin confirm.
           */
          function previewUserDeletion() {
              fetch(previewUserDeletionUrl + '?userId=' + encodeURIComponent(targetUserId), {
                  headers: { 'Accept': 'application/json' }
              })
              .then(function (response) {
                  if (!response.ok) { throw new Error('HTTP ' + response.status); }
                  return response.json();
              })
              .then(function (data) {
                  if (data.status !== 0) {
                      alert(data.message);
                      return;
                  }

                  var html = '<ul class="mb-0">';
                  html += '<li>The user account <b>' + data.email + '</b></li>';

                  if (data.queries && data.queries.length > 0) {
                      html += '<li>' + data.queries.length + ' custom alert(s) that exist only for this user:<ul>';
                      data.queries.forEach(function (query) {
                          html += '<li>[' + query.id + '] ' + query.name + '</li>';
                      });
                      html += '</ul></li>';
                  } else {
                      html += '<li>No alerts are exclusive to this user, so none will be deleted</li>';
                  }
                  html += '</ul>';

                  document.getElementById('deleteUserSummary').innerHTML = html;
                  new bootstrap.Modal(document.getElementById('deleteUserModal')).show();
              })
              .catch(function (error) {
                  alert('Could not check what would be deleted: ' + error.message);
              });
          }

          function deleteUser() {
              var button = document.getElementById('confirmDeleteUserBtn');
              button.disabled = true;

              fetch(deleteUserUrl, {
                  method: 'POST',
                  headers: {
                      'Accept': 'application/json',
                      'Content-Type': 'application/x-www-form-urlencoded'
                  },
                  body: 'userId=' + encodeURIComponent(targetUserId)
              })
              .then(function (response) {
                  if (!response.ok) { throw new Error('HTTP ' + response.status); }
                  return response.json();
              })
              .then(function (data) {
                  alert(data.message);
                  if (data.status === 0) {
                      // this user's page no longer exists - go back to the user search
                      window.location.href = findUserUrl;
                  } else {
                      button.disabled = false;
                  }
              })
              .catch(function (error) {
                  alert('Failed to delete the user: ' + error.message);
                  button.disabled = false;
              });
          }
      </script>
    </body>
</html>


