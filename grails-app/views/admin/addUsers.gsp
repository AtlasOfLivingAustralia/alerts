<%@ page import="au.org.ala.alerts.Notification" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
  <meta name="layout" content="${grailsApplication.config.ala.layout}" />
  <g:set var="entityName" value="${message(code: 'notification.label', default: 'Notification')}" />
  <title>My email alerts | Atlas of Living Australia</title>
</head>
<body>
<div id="content">
  <header id="page-header">
    <div class="inner">
      <nav id="breadcrumb">
        <ol>
          <li><a href="http://www.ala.org.au">Home</a></li>
          <li><a href="http://www.ala.org.au/my-profile/">My Profile</a></li>
          <li class="last">My email alerts</li>
        </ol>
      </nav>
      <h1>My email alerts</h1>
    </div><!--inner-->
  </header>
  <div class="inner">
    <div id="section" class="col-wide">

      <h2>Add Users</h2>
      <div>
        <g:form action="saveUsers" controller="admin">
          <g:textArea name="usersToAdd" rows="30" cols="80"></g:textArea>
          <input type="submit" value="Add Users"/>
        </g:form>
      </div>
    </div>
  </div>
</body>
</html>
