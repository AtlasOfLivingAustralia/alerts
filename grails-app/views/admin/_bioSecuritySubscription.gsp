<%@ page import="java.text.SimpleDateFormat" %>
<g:set var="today" value="${new SimpleDateFormat('yyyy-MM-dd').format(new Date())}"/>
<%
    def qr = !query.queryResults.isEmpty() ? query.queryResults.last() : null
    def logs = qr?.logs?.split('\n') ?: []
%>

<g:set var="logs" value="${logs}"/>


<div id="subscription_${query.id}" class="row pt-4 pb-4" style="background-color: ${(i+startIdx) % 2 == 0 ? '#f0f0f0' : '#ffffff'};">
    <div class="col-md-4" id="${query.listId}">
        <g:if test ="${query.listId != null && !(query.listId instanceof String && query.listId.toLowerCase() == 'null')}">
            <%-- Inline edit title --%>
            <span id="queryTitle_${query.id}">
                <a href="${grailsApplication.config.lists.baseURL+'/speciesListItem/list/'+query.listId}" target="_blank" id="queryTitleText_${query.id}">${query.name}</a>
                <button class="btn btn-link btn-sm p-0 ms-1" title="Edit title" onclick="startEditTitle(${query.id})">
                    <i class="fa-solid fa-pencil"></i>
                </button>
                <g:if test ="${query.listId != null && !(query.listId instanceof String && query.listId.toLowerCase() == 'null')}">
                    <g:link controller="query" action="show" id="${query.id}">
                        <button class="btn btn-link p-0"><i class="fa-solid fa-circle-info p-0 ms-1" aria-hidden="true" title="Show the query"></i></button>
                    </g:link>
                </g:if>
                <g:else>
                    &nbsp; &nbsp;<span style="color: red;"><i class="fa-solid fa-triangle-exclamation" aria-hidden="true"></i></span>
                </g:else>
            </span>
            <span id="queryTitleEdit_${query.id}" style="display:none;">
                <textarea rows="2" id="queryTitleInput_${query.id}" class="form-control form-control-sm w-100"  style="height:auto">${query.name}</textarea>
                <br/>
                <button class="btn btn-primary btn-sm ms-1" onclick="saveTitle(${query.id})"><i class="fa-solid fa-check"></i></button>
                <button class="btn btn-outline-primary btn-sm ms-1" onclick="cancelEditTitle(${query.id})"><i class="fa-solid fa-xmark"></i></button>
            </span>

                <p></p>
                <g:if test="${query.lastChecked}">
                  Last checked on
                    <span name="showLastCheckDetails_${query.id}" style="cursor: pointer; text-decoration: underline;"  data-bs-toggle="popover" data-bs-placement="bottom" data-bs-trigger="click"
                          data-bs-html="true" data-bs-content="${logs.collect {'<li class="autowrap-popover">' + it + '</li>'  }.join()}" >
                        ${new java.text.SimpleDateFormat('dd MMM yyyy HH:mm').format(query.lastChecked)}
                    </span>
%{--                    <g:if test="${query.queryResults.find{it.frequency.name=='weekly'}?.id}">--}%
%{--                        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;--}%
%{--                        <g:link namespace="biosecurity" controller="csv" action="downloadLastResult" params="[id:  query.queryResults.find{it.frequency.name=='weekly'}?.id]" target="_blank">--}%
%{--                            <span><i class="fa fa-download"></i>CSV</span>--}%
%{--                        </g:link>--}%
%{--                    </g:if>--}%
                </g:if>
                <g:else>
                    <span hidden  name="showLastCheckDetails_${query.id}" style="cursor: pointer; text-decoration: underline;"  data-bs-toggle="popover" data-bs-placement="bottom" >
                    </span>
                    <small name="neverCheckedInfo">
                        This is the first time subscribing to this list. Please navigate to the 'Advanced Usage' section  on the right <i style="padding-left: 20px;" class="fa-solid fa-hand-point-right fa-lg" aria-hidden="true"></i> to set the initial check date.
                    Otherwise, the check date will default to 7 days before the scheduled task's execution date.
                    </small>
                </g:else>
        </g:if>
        <g:else>
            <span id="queryTitle_${query.id}">
                <span id="queryTitleText_${query.id}">${query.name}</span>
                <button class="btn btn-link btn-sm p-0 ms-1" title="Edit title" onclick="startEditTitle(${query.id})">
                    <i class="fa-solid fa-pencil fa-xs"></i>
                </button>
            </span>
            <span id="queryTitleEdit_${query.id}" style="display:none;">
                <textarea rows="2" id="queryTitleInput_${query.id}" class="form-control form-control-sm w-100">${query.name}</textarea>
                <button class="btn btn-success btn-sm ms-1" onclick="saveTitle(${query.id})"><i class="fa-solid fa-check"></i></button>
                <button class="btn btn-secondary btn-sm ms-1" onclick="cancelEditTitle(${query.id})"><i class="fa-solid fa-xmark"></i></button>
            </span>
            <p></p>
            <span style="color: red;">Warning: This query is not associated with a valid list.</span>
        </g:else>
    </div>
    <div class="col-md-5">
        <g:set var="subscribers" value="${query.collect{ q -> q.notifications.collect{ notification -> ['id': notification.user?.id,'userId': notification.user?.userId, 'email' : notification.user.email, 'enabled':notification.enabled] }}.flatten() as List}" />
        <g:render template="bioSecuritySubscribers" model="[subscribers: subscribers, queryid: query.id]"/>
        <div class="pt-4 pb-4">
            <g:form name="create-security-alert"  method="post">
                  <div class="form-group">
                      <div>
                      <label>Add subscribers</label>
                      <input type="hidden" name="queryid"  value="${query.id}"/>
                      <input class="form-control"  name="useremails" placeholder="You can input multiple user emails by separating them with ';'" />
                      <br>
                      </div>
                      <button  type="button" class="btn btn-primary " onclick="addSubscribers(this)">Add</button>
                    </div>
            </g:form>
        </div>

    </div>
    <div class="col-md-3 form-group">
        <form  method="post" name="previewAndEmail" action="${request.contextPath}/admin/previewBiosecurityAlert?queryid=${query.id}">
                <label >Check alerts since</label>
                <input type="date" name="date" value="${today}" class="form-control" /><br/>
                <button class="btn btn-primary" name="previewSubscription" type="button" onclick="submitPreview(this)" >Preview</button>
                <button class="btn btn-primary" name="triggerSubscription" type="button" onclick="triggerSubscriptionSince(this, ${query.id})">Notify</button>
                <a href="#" class="ms-2" onclick="toggleNotifyHelp(${query.id}); return false;"><i class="fas fa-question-circle"></i> Help</a>
                <div id="notifyHelp_${query.id}" class="mt-2" style="display:none;">
                    <small class="form-text text-muted d-block mt-1">
                        The '<span class="text-primary fw-bold">Preview</span>' button is primarily for administrators to verify that a query runs correctly.It does <span class="text-primary fw-bold">NOT</span> update the last execution date, send emails, or regenerate a CSV.
                    </small>
                    <hr/>
                    <small class="form-text text-muted d-block">
                        The '<span class="text-primary fw-bold">Notify</span>' button should only be used if the server unexpectedly goes down during a scheduled run or other unexpected failures, requiring the task to be triggered manually.<br><span class="text-primary fw-bold">It will send emails and generate a corresponding CSV file</span>. Otherwise, this button should not be used.
                    </small>
                </div>
        </form>
    </div>
</div>
<script>
    function toggleNotifyHelp(queryId) {
        var help = document.getElementById('notifyHelp_' + queryId);
        if (!help) {
            return;
        }
        help.style.display = help.style.display === 'none' ? 'block' : 'none';
    }

    function startEditTitle(queryId) {
        var currentName = document.getElementById('queryTitleText_' + queryId).textContent.trim();
        // Sanitise: strip HTML tags and control characters before editing
        var sanitised = currentName
            .replace(/<[^>]*>/g, '')           // strip any HTML tags
            .replace(/[\x00-\x1F\x7F]/g, '')   // strip control characters
            .trim();
        document.getElementById('queryTitleInput_' + queryId).value = sanitised;
        document.getElementById('queryTitle_' + queryId).style.display = 'none';
        document.getElementById('queryTitleEdit_' + queryId).style.display = 'block';
        document.getElementById('queryTitleInput_' + queryId).focus();
    }

    function cancelEditTitle(queryId) {
        document.getElementById('queryTitleEdit_' + queryId).style.display = 'none';
        document.getElementById('queryTitle_' + queryId).style.display = 'inline';
    }

    function saveTitle(queryId) {
        var input = document.getElementById('queryTitleInput_' + queryId);
        // Sanitise submitted value: strip HTML tags and control characters
        var newName = input.value
            .replace(/<[^>]*>/g, '')
            .replace(/[\x00-\x1F\x7F]/g, '')
            .trim();
        var currentName = document.getElementById('queryTitleText_' + queryId).textContent.trim();

        if (!newName || newName === currentName) {
            cancelEditTitle(queryId);
            return;
        }

        $.ajax({
            url: '${request.contextPath}/query/updateTitle/' + queryId,
            type: 'POST',
            data: { name: newName },
            success: function(response) {
                if (response.success) {
                    document.getElementById('queryTitleText_' + queryId).textContent = newName;
                    var link = document.querySelector('#queryTitle_' + queryId + ' a');
                    if (link) link.textContent = newName;
                    cancelEditTitle(queryId);
                } else {
                    alert('Failed to update title: ' + (response.message || 'Unknown error'));
                    cancelEditTitle(queryId);
                }
            },
            error: function(xhr) {
                alert('Failed to update title: ' + xhr.responseText);
                cancelEditTitle(queryId);
            }
        });
    }
</script>
