<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="au.org.ala.alerts.Notification" %>
<!doctype html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="${grailsApplication.config.skin.layout}" />
        <meta name="breadcrumb" content="${message(code:"my.alerts.breadcrumbs")}" />
        <meta name="breadcrumbParent" content="${grailsApplication.config.security.cas.casServerName}/userdetails/myprofile, ${message(code:"my.alerts.breadcrumb.parent")}" />
        <g:set var="userPrefix" value="${adminUser ? user.email : message(code:"my.alerts.my") }"/>
        <title><g:message code="my.alerts.title" args="[userPrefix]" /> | ${grailsApplication.config.skin.orgNameLong}</title>
        <asset:stylesheet href="alerts.css"/>
    </head>
    <body>
      <div id="content">
          <header id="page-header">
            <div class="inner row-fluid">
              <h1><g:message code="my.alerts.h1" args="[userPrefix]" /></h1>
            </div>
          </header>
          <g:if test="${flash.message}">
              <div class="alert alert-info">${flash.message}</div>
          </g:if>
          <g:if test="${flash.errorMessage}">
              <div class="alert alert-danger">${flash.errorMessage}</div>
          </g:if>
          <div id="page-body" role="main">
                <g:set var="userId">${user.userId}</g:set>
                <h3>
                  <g:message code="my.alerts.send.me.alerts" />
                  <g:select name="userFrequency" from="${frequencies}" id="userFrequency" value="${user?.frequency?.name}" optionKey="name" />
                </h3>
            <div class="row">
                <div class="col-md-6">
                    <table>
                        <tbody>
                        <g:each in="${enabledQueries}" status="i" var="query">
                            <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                                <td class="queryDescription">
                                  <h3>${query.name}</h3>
                                  ${query.description}
                                </td>
                                <td class="queryActions">
                                    <div class="switch" data-on="danger">
                                        <input id="${query.id}" class="query-cb" name="field2"  type="checkbox" checked />
                                    </div>
                                </td>
                            </tr>
                        </g:each>
                        <g:each in="${disabledQueries}" status="i" var="query">
                            <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                                <td class="queryDescription">
                                  <h3>${query.name}</h3>
                                  ${query.description}
                                </td>
                                <td class="queryActions">
                                    <div class="switch" data-on="danger" >
                                        <input  id="${query.id}" class="query-cb" name="field2"  type="checkbox" />
                                    </div>
                                </td>
                            </tr>
                        </g:each>
                        </tbody>
                    </table>
                    <g:if test="${customQueries}">
                    <hr>
                    <h2 id="customQueriesHdr"><g:message code="my.alerts.my.custom.alerts" /></h2>
                    <table>
                        <tbody id="customQueries">
                        <g:each in="${customQueries}" status="i" var="query">
                            <tr class="${(i % 2) == 0 ? 'odd' : 'even'}" id='custom-${query.id}'>
                                <td class="queryDescription">
                                  <h3>${query.name}</h3>
                                  ${query.description}
                                </td>
                                <td class="queryActions">
                                    <a href="javascript:void(0);" class='btn btn-ala deleteButton' id='${query.id}'><g:message code="my.alerts.delete.label" /></a>
                                </td>
                            </tr>
                        </g:each>
                        </tbody>
                    </table>
                    </g:if>
                 </div>
                <div class="col-md-6">
                    <div class="well">
                        <p><g:message code="my.alerts.enable.to.email" args="[user.email]" /></p>
                        <p>
                            <g:message code="my.alerts.sample.list.intro" args="[grailsApplication.config.skin.orgNameShort]" />
                            <ul>
                                <li>
                                    <g:message code="my.alerts.data.resource.desc" args="[grailsApplication.config.collection.searchURL, grailsApplication.config.collection.searchTitle]" />
                                </li>
                                <li>
                                    <g:message code="my.alerts.species.desc" args="[grailsApplication.config.speciesPages.searchURL, grailsApplication.config.speciesPages.searchTitle]" />
                                </li>
                                <li>
                                    <g:message code="my.alerts.region.desc" args="[grailsApplication.config.regions.searchURL, grailsApplication.config.regions.searchTitle]" />
                                </li>
                                <li>
                                    <g:message code="my.alerts.new.record.desc" args="[grailsApplication.config.occurrence.searchURL, grailsApplication.config.occurrence.searchTitle]" />
                                </li>
                            </ul>
                        </p>
                        <p>
                            <g:message code="my.alerts.look.for.btn" />
                        </p>
                    </div>
                </div>
            </div>
          </div>
      </div>
      <asset:javascript src="alerts.js"/>
      <asset:script type="text/javascript">

          var addMyAlertUrl = 'addMyAlert/';
          var deleteMyAlertUrl = 'deleteMyAlert/';
          var deleteMyAlertWRUrl ='deleteMyAlertWR/';

          $(document).ready( function(){

              $("#userFrequency").change(function(){
                  $.get('changeFrequency?frequency='+$('#userFrequency').val())
                      .success(function() {
                          //alert("Your alerts have been changed to : " + $('#userFrequency').val());
                      })
                      .error(function() {
                          alert(<g:message code="my.alerts.problem.retry" />);
                      });
              });

              $('.deleteButton').click(function(data){
                var id = $(this).attr('id');
                $.get(deleteMyAlertWRUrl + id + '?userId=${userId}');
                $('#custom-' + id).hide('slow', function(){
                    $('#custom-' + id).remove();
                    if($('#customQueries').children().size() == 0){
                      $('#customQueriesHdr').hide('slow');
                    }
                });
              });

              $(".switch input").bootstrapSwitch(
                  {   onColor:'ala',
                      onSwitchChange: function (event, state) {
                          event.preventDefault();
                          $(this).attr('checked', state); // probably not needed

                          if (state){
                             $.get(addMyAlertUrl + $(this).attr('id') + '?userId=${userId}');
                          } else {
                             $.get(deleteMyAlertUrl + $(this).attr('id')+ '?userId=${userId}');
                          }
                          return true;
                      }
                  }
              );
          });
        </asset:script>
    </body>
</html>