<%@ page import="au.org.ala.alerts.Notification" %>
<html>
<head>
    <title>Notification service | ${grailsApplication.config.skin.orgNameLong}</title>
    <meta name="layout" content="${grailsApplication.config.skin.layout}" />
    <meta name="breadcrumb" content="Bulk email" />
    <meta name="breadcrumbParent" content="${request.contextPath}/admin,Alerts admin" />
</head>
<body>

  <div class="inner">
    <div id="section" class="col-wide">
      <h2>Create email for registered users</h2>
      <div>
        <g:form action="sendBulkEmailForRegisteredUsers" controller="admin">
          <label for="emailSubject">Subject title</label>
          <br/>
          <g:textArea class="input-xxlarge"  name="emailSubject" rows="1" cols="135"></g:textArea>
          <br/>
          <label for="htmlEmailToSend">HTML Body</label>
          <br/>
          <g:textArea class="input-xxlarge" name="htmlEmailToSend" rows="30" cols="60"></g:textArea>
          <br/>
          <input type="submit" value="Send" class="btn"/>
        </g:form>
      </div>
    </div>
  </div>
</body>
</html>
