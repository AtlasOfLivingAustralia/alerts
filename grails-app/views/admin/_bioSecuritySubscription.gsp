<g:set var="today" value="${new java.text.SimpleDateFormat('yyyy-MM-dd').format(new Date())}"/>

<div name="subscription_${query.id}" class="row bioscecrurity-padding" style="background-color: ${(i+startIdx) % 2 == 0 ? '#f0f0f0' : '#ffffff'};">
    <div class="col-md-4 indented-text">${i+startIdx+1}.
        <g:if test ="${query.listId != null && !(query.listId instanceof String && query.listId.toLowerCase() == 'null')}">
            <a href="${grailsApplication.config.lists.baseURL+'/speciesListItem/list/'+query.listId}" target="_blank">${query.name}</a> &nbsp; &nbsp;
            &nbsp; &nbsp;
                <g:link controller="query" action="show" id="${query.id}">
                    <span><i class="fa fa-info-circle" aria-hidden="true" title="Show the query"></i></span></g:link>
               <p></p>
            <div style="text-align: center;">
                <g:if test="${query.lastChecked}">
                    Last checked on ${new java.text.SimpleDateFormat('yyyy-MM-dd').format(query.lastChecked)} &nbsp;
                </g:if>
%{--                <button class="btn btn-info"  onclick="triggerSubscription(${query.id})">Check & Notify</button>--}%
            </div>
        </g:if>
        <g:else>
            ${query.name}nbsp;
            <p></p>
            <span style="color: red;"><i class="fa fa-exclamation-triangle" aria-hidden="true"></i> Warning: This query is not associated with a valid list.</span>
        </g:else>
    </div>
    <div class="col-md-5">
        <g:render template="bioSecuritySubscribers" model="[subscribers: subscribers, queryid: query.id]"/>

        <div class="bioscecrurity-padding">
            <g:form name="create-security-alert"  method="post">
                  <div class="form-group">
                      <div>
                      <label>Add subscribers</label>
                      <input type="hidden" name="queryid"  value="${query.id}"/>
                      <input class="form-control"  name="useremails" placeholder="You can input multiple user emails by separating them with ';'" />
                      <small class="form-text text-muted">You can input multiple user emails by separating them with ';'</small>
                      </div>
                      <button  type="button" class="btn btn-primary " onclick="addSubscribers(this)">Add</button>
                    </div>
            </g:form>
        </div>

    </div>
    <div class="col-md-3 form-group">
        <form  action="${request.contextPath}/admin/testBiosecurity?queryid=${query.id}" method="post" name="previewAndEmail">
                <label >Preview of alerts since</label>
                <input type="date" name="date" value="${today}" class="form-control" />
                <small class="form-text text-info">The records shown on this page may differ from those obtained through Biocache search</small><br>
                <button class="btn btn-primary" type="button" onclick="submitPreview('${request.contextPath}/admin/previewBiosecurityAlert?queryid=${query.id}', this, true)" >Preview</button>
                <br>
                <small class="form-text text-info">'Check&Notify' searches new records since the PREVIEW DATE, and then set the last checked date to the PREVIEW DATE</small><br>
                <button class="btn btn-primary" type="button" onclick="submitPreview('${request.contextPath}/ws/triggerBiosecurityAlertSince?id=${query.id}', this, false)">Check&Notify</button>
        </form>
    </div>
</div>