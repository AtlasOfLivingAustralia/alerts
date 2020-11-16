<html>
    <head>
        <title><g:message code="admin.notification.title" /> | ${grailsApplication.config.skin.orgNameLong?: 'Atlas of Living Australia'}</title>
        <meta name="layout" content="${grailsApplication.config.skin.layout}" />
        <meta name="breadcrumb" content="${message(code:"admin.functions")}" />
        <meta name="breadcrumbParent" content="${request.contextPath?:'/'},${message(code:"admin.alerts.breadcrumb")}" />
    </head>
    <body>
          <h1><g:message code="admin.welcome" /></h1>
          <div>
            <ul>
              <li><g:link controller="notification" action="myAlerts"><g:message code="admin.view.my.alerts" /></g:link></li>
            </ul>
          </div>

          <h2><g:message code="admin.functions" /></h2>
          <div>
            <ul>
              <li><g:link controller="query" action="list"><g:message code="admin.view.list" /></g:link></li>
              <g:each var="c" in="${grailsApplication.controllerClasses.sort { it.fullName } }">
                  <li class="controller"><g:link controller="${c.logicalPropertyName}">${c.fullName}</g:link></li>
              </g:each>
            </ul>
          </div>
    
          <div>
            <ul>
              <li><g:link controller="admin" action="runChecksNow" params="${[frequency: message(code: "frequency.hourly")]}"><g:message code="admin.run.hourly" /></g:link></li>
              <li><g:link controller="admin" action="runChecksNow" params="${[frequency: message(code: "frequency.daily")]}"><g:message code="admin.run.daily" /></g:link></li>
              <li><g:link controller="admin" action="runChecksNow" params="${[frequency: message(code: "frequency.monthly")]}"><g:message code="admin.run.monthly" /></g:link></li>
            </ul>
            
          </div>
    </body>
</html>
