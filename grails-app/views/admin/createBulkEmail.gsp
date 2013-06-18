<%@ page import="ala.postie.Notification" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
  <meta name="layout" content="${grailsApplication.config.ala.layout}" />
  <title>Send Bulk Email | Atlas of Living Australia</title>
</head>
<body>
<div id="content">
  <header id="page-header">
    <div class="inner">
      <nav id="breadcrumb">
        <ol>
          <li><a href="http://www.ala.org.au">Home</a></li>
          <li class="last">Send bulk email</li>
        </ol>
      </nav>
      <h1>My email alerts</h1>
    </div><!--inner-->
  </header>
  <div class="inner">
    <div id="section" class="col-wide">
      <h2>Create email</h2>
      <div>
        <g:form action="sendBulkEmail" controller="admin">
          <label for="emailsToUse">Emails</label>
          <br/>

          <g:textArea name="emailsToUse" rows="10" cols="40"></g:textArea>
          <br/>

          <label for="emailSubject">Subject title</label>
          <br/>
          <g:textArea name="emailSubject" rows="1" cols="135"></g:textArea>

          <label for="htmlEmailToSend">HTML Body</label>
          <br/>

          <g:textArea name="htmlEmailToSend" rows="30" cols="135"></g:textArea>
          <br/>

          <input type="submit" value="Send"/>
        </g:form>
      </div>
    </div>
  </div>
</body>
</html>
