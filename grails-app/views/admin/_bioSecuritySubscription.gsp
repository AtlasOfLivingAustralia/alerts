<g:set var="today" value="${new java.text.SimpleDateFormat('yyyy-MM-dd').format(new Date())}"/>

<div name="subscription_${query.id}" class="row bioscecrurity-padding" style="background-color: ${(i+startIdx) % 2 == 0 ? '#f0f0f0' : '#ffffff'};">
    <div class="col-md-4 indented-text">
        <g:if test ="${query.listId != null && !(query.listId instanceof String && query.listId.toLowerCase() == 'null')}">            &nbsp;&nbsp;
            <g:link controller="query" action="show" id="${query.id}">
                <span><i class="fa fa-info-circle" aria-hidden="true" title="Show the query"></i></span>
            </g:link>
        </g:if>
        <g:else>
            &nbsp; &nbsp;<span style="color: red;"><i class="fa fa-exclamation-triangle" aria-hidden="true"></i></span>
        </g:else>

        <g:if test ="${query.listId != null && !(query.listId instanceof String && query.listId.toLowerCase() == 'null')}">
            <a href="${grailsApplication.config.lists.baseURL+'/speciesListItem/list/'+query.listId}" target="_blank">${query.name}</a> &nbsp; &nbsp;
                <p></p>
                <g:if test="${query.lastChecked}">
                  Last checked on
                    <span name="showLastCheckDetails_${query.id}" style="cursor: pointer; text-decoration: underline;"  data-toggle="popover" data-placement="bottom" data-content="${logs.collect {'<li>' + it + '</li>'  }.join()}" >
                        ${new java.text.SimpleDateFormat('dd MMM yyyy HH:mm').format(query.lastChecked)}
                    </span>
                </g:if>
                <g:else>
                    <span hidden  name="showLastCheckDetails_${query.id}" style="cursor: pointer; text-decoration: underline;"  data-toggle="popover" data-placement="bottom" >
                    </span>
                    <small name="neverCheckedInfo">
                        This is the first time subscribing to this list. Please navigate to the 'Advanced Usage' section  on the right <i style="padding-left: 20px;" class="fa fa-hand-o-right fa-lg" aria-hidden="true"></i> to set the initial check date.
                    Otherwise, the check date will default to 7 days before the scheduled task's execution date.
                    </small>
                </g:else>
        </g:if>
        <g:else>
             ${query.name}
            <p></p>
            <span style="color: red;">Warning: This query is not associated with a valid list.</span>
        </g:else>
    </div>
    <div class="col-md-5">
        <g:set var="subscribers" value="${query.collect{ q -> q.notifications.collect{ notification -> ['userId': notification.user?.id, 'email' : notification.user.email] }}.flatten() as List}" />
        <g:render template="bioSecuritySubscribers" model="[subscribers: subscribers, queryid: query.id]"/>
        <div class="bioscecrurity-padding">
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
                <br>
                <small class="form-text text-info">"Notify" will send alerts to the subscribers and update the last check date to today</small><br>

        </form>
    </div>
</div>