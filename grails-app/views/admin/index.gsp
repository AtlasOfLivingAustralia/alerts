<html>
<head>
  <title>Notification service | Atlas of Living Australia</title>
  <meta name="layout" content="main" />
</head>
<body>
<h1>Welcome to the Notification service</h1>
<div>
  <ul>
    <li><g:link controller="notification" action="myAlerts">View my alerts</g:link></li>
  </ul>
</div>

<h2>Admin functions</h2>
<div>
  <ul>
    <li><g:link controller="query" action="list">View list of alert types</g:link></li>
    <g:each var="c" in="${grailsApplication.controllerClasses.sort { it.fullName } }">
      <li class="controller"><g:link controller="${c.logicalPropertyName}">${c.fullName}</g:link></li>
    </g:each>
  </ul>
</div>
</body>
</html>
