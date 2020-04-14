<html>
    <head>
        <title>Notification service | ${grailsApplication.config.skin.orgNameLong?: 'Atlas of Living Australia'}</title>
        <meta name="layout" content="${grailsApplication.config.skin.layout}" />
        <meta name="breadcrumb" content="Admin functions" />
        <meta name="breadcrumbParent" content="${request.contextPath?:'/'},Alerts" />
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
    
          <div>
            <ul>
              <li><g:link controller="admin" action="runChecksNow" params="${[frequency:'hourly']}">Run hourly checks now</g:link></li>
              <li><g:link controller="admin" action="runChecksNow" params="${[frequency:'daily']}">Run daily checks now</g:link></li>
              <li><g:link controller="admin" action="runChecksNow" params="${[frequency:'monthly']}">Run monthly checks now</g:link></li>
            </ul>
            
          </div>
    </body>
</html>
