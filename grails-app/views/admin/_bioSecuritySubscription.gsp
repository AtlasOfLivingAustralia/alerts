<g:set var="today" value="${new java.text.SimpleDateFormat('yyyy-MM-dd').format(new Date())}"/>

<div name="subscription_${query.id}" class="row bioscecrurity-padding" style="background-color: ${(i+startIdx) % 2 == 0 ? '#f0f0f0' : '#ffffff'};">
    <div class="col-md-4 indented-text">${i+startIdx+1}. <g:link controller="query" action="show" id="${query.id}">${query.name}</g:link></div>
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
    <div class="col-md-3">
        <form class="form-group" action="${request.contextPath}/admin/testBiosecurity?queryid=${query.id}" method="post" name="previewAndEmail">
                <label >Preview of alerts since</label>
                <input type="date" name="date" value="${today}"  class="form-control"/><br/>
                <button class="btn btn-primary" type="button" onclick="submitPreview('${request.contextPath}/admin/previewBiosecurityAlert?queryid=${query.id}', this, true)" >Preview</button>
                <button class="btn btn-primary" type="button" onclick="submitPreview('${request.contextPath}/admin/sendExampleEmail?queryid=${query.id}', this, false)">Email to yourself</button>
        </form>
    </div>
</div>