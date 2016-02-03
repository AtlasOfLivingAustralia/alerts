<%@ page import="au.org.ala.alerts.Notification" %>
<html>
<head>
    <title>Notification service | ${grailsApplication.config.skin.orgNameLong}</title>
    <meta name="layout" content="${grailsApplication.config.skin.layout}" />
</head>
<body>

<header id="page-header">
    <div class="inner row-fluid">
        <nav id="breadcrumb" class="span12">
            <ol class="breadcrumb">
                <li><g:link controller="admin" class="home">Admin</g:link> <span class="icon icon-arrow-right"> </span></li>
                <li class="last">Send bulk email</li>
            </ol>
        </nav>
        <hgroup>
            <h1>${userPrefix} email alerts</h1>
        </hgroup>
    </div>
</header>

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
