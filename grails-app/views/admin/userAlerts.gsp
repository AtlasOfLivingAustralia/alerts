<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="au.org.ala.alerts.Notification" %>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <meta name="breadcrumb" content="Find / Create users" />
    <meta name="breadcrumbParent" content="${request.contextPath}/admin,Alerts admin" />

    <title>Admin - user alerts</title>
    <asset:stylesheet href="alerts.css"/>
    <asset:javascript src="typeahead-1.3.1.min.js"/>
</head>

<body>
<div id="content">
    <header id="page-header">
        <div class="inner row">
            <div class="col-lg-6 col-sm-6">
                <h1>Find or Create users</h1>
            </div>
        </div>
    </header>

    <div id="page-body" role="main">
        <g:if test="${flash.message}">
            <div class="alert alert-info" role="alert">${flash.message}</div>
        </g:if>
        <g:if test="${flash.errorMessage}">
            <div class="alert alert-danger" role="alert">${flash.errorMessage}</div>
        </g:if>

        <g:form controller="admin" action="findUser" method="post">
            <div class="row">
                <div class="col-lg-6 col-sm-6">
                    <label for="term" class="form-label">Email contains:</label>
                    <div class="input-group">
                        <g:textField name="term" id="term" value="${params.term}" class="form-control"
                                     placeholder="Search for..." autocomplete="off"/>
                        <g:actionSubmit value="Find" class="btn btn-primary" action="findUser"/>
                    </div><!-- /input-group -->
                    <div class="form-text">Start typing at least 3 characters, then pick a user to manage their alerts.</div>
                </div><!-- /.col-lg-6 -->
            </div><!-- /.row -->
        </g:form>

        %{-- submitted by addUser() below, when the searched email is not in the alerts database yet --}%
        <g:form name="addUserForm" controller="admin" action="addUser" method="post">
            <input type="hidden" name="term" id="addUserTerm"/>
        </g:form>
    </div>
</div>
<asset:javascript src="alerts.js"/>
<script type="text/javascript">

    /**
     * Add the email currently typed in the search box to the alerts database.
     * Called from the 'no matching users' suggestion. AdminController#addUser looks the account up
     * in userdetails/CAS, creates it, and redirects to that user's alerts page.
     */
    function addUser() {
        // typeahead('val') returns what the user actually typed
        var term = ($('#term').typeahead('val') || $('#term').val() || '').trim();

        if (!term) {
            alert('Please enter an email address first.');
            return;
        }

        if (term.indexOf('@') === -1) {
            alert('Please enter the full email address of the user you want to add.');
            return;
        }

        if (!confirm('Add ' + term + ' to Alerts?')) {
            return;
        }

        $('#addUserTerm').val(term);
        // selected by name: an 'id' attribute on g:form would end up in the action URL
        $('form[name="addUserForm"]').submit();
    }

    $(document).ready(function () {

        // Email autocomplete on the user search field.
        // Selecting a suggestion goes straight to that user's alerts page.
        $('input#term').typeahead(
            {
                hint: true,
                highlight: true,
                minLength: 3
            },
            {
                name: 'users',
                display: 'email',
                limit: 10,
                source: function (query, syncResults, asyncResults) {
                    $.ajax({
                        url: '${request.contextPath}/ws/searchUsers',
                        data: { q: query },
                        dataType: 'json',
                        success: function (data) {
                            asyncResults(data);
                        },
                        error: function (xhr, status, error) {
                            if (xhr.status === 401 || xhr.status === 403) {
                                console.error('Not authorised to search users - your session may have expired.');
                            } else {
                                console.error('Failed to search users:', error);
                            }
                            asyncResults([]);
                        }
                    });
                },
                templates: {
                    notFound: '<div class="tt-suggestion text-muted">' +
                              'No matching users. Do you want to add this user? ' +
                              '<button type="button" class="btn btn-sm btn-outline-primary" onclick="addUser()">Yes</button>' +
                              '</div>'
                }
            }).bind('typeahead:select', function (ev, user) {
                if (user && user.userId) {
                    window.location.href = '${request.contextPath}/admin/user/' + encodeURIComponent(user.userId);
                }
            });
    });
</script>
</body>
</html>