<html>
<head>
  <title>Notification service | Atlas of Living Australia</title>
  <meta name="layout" content="main" />
</head>
<body>
<h1>Admin functions - Alert service</h1>
<div>
  <ul>
    <li><g:link controller="notification" action="myAlerts">View my alerts</g:link></li>
    <li><g:link controller="admin" action="debugAllAlerts">Debug all alerts</g:link></li>
    <li><g:link controller="query" action="list">View list of alert types</g:link></li>
    <li class="controller"><g:link controller="quartz">View scheduling</g:link></li>
    <li class="controller"><g:link controller="admin" action="createBulkEmailForRegisteredUsers">
        Ad hoc bulk email to registered users</g:link></li>
      <li class="controller"><g:link controller="admin" action="notificationReport">
          Notification report</g:link></li>
  </ul>
</div>
</body>
</html>
