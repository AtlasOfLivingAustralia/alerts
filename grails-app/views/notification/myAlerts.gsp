<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="au.org.ala.alerts.Notification" %>
<!doctype html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="${grailsApplication.config.skin.layout}" />
        <meta name="breadcrumb" content="My alerts" />
        <meta name="breadcrumbParent" content="${grailsApplication.config.security.cas.casServerName}/userdetails/myprofile, My profile" />
        <g:set var="userPrefix" value="${adminUser ? user.email : 'My' }"/>
        <title>${userPrefix} email alerts | ${grailsApplication.config.skin.orgNameLong}</title>
        <r:require modules="bootstrapSwitch,alerts"/>
    </head>
    <body>
      <div id="content">
          <header id="page-header">
            <div class="inner row-fluid">
              <hgroup>
                <h1>${userPrefix} email alerts</h1>
              </hgroup>
            </div>
          </header>
          <div id="page-body" role="main">
                <g:set var="userId">${user.userId}</g:set>
                <h3>
                  Send me alerts:
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
                    <h2 id="customQueriesHdr">My custom alerts</h2>
                    <table>
                        <tbody id="customQueries">
                        <g:each in="${customQueries}" status="i" var="query">
                            <tr class="${(i % 2) == 0 ? 'odd' : 'even'}" id='custom-${query.id}'>
                                <td class="queryDescription">
                                  <h3>${query.name}</h3>
                                  ${query.description}
                                </td>
                                <td class="queryActions">
                                    <a href="javascript:void(0);" class='btn btn-danger deleteButton' id='${query.id}'>Delete</a>
                                </td>
                            </tr>
                        </g:each>
                        </tbody>
                    </table>
                    </g:if>
                 </div>
                <div class="col-md-6">
                    <div class="well">
                        <p>Enable an alert to have emails sent to your email address <b>${user.email}</b></p>
                        <p>
                            Specific alerts can be created in a number of places in the Atlas.<br/>
                            These include:
                            <ul>
                                <li>
                                    Data resource pages e.g. <a href="${grailsApplication.config.collection.searchURL}">${grailsApplication.config.collection.searchTitle}</a>
                                    for alerts on new records or annotations.
                                </li>
                                <li>
                                    Species pages e.g. <a href="${grailsApplication.config.speciesPages.searchURL}">${grailsApplication.config.speciesPages.searchTitle}</a>
                                    for alerts on new records or annotations.
                                </li>
                                <li>
                                    Region pages e.g. <a href="${grailsApplication.config.regions.searchURL}">${grailsApplication.config.regions.searchTitle}</a>
                                    for alerts on new records or annotations.
                                </li>
                                <li>
                                    Any <a href="${grailsApplication.config.occurrence.searchURL}">${grailsApplication.config.occurrence.searchTitle}</a>
                                    for alerts on new records or annotations.
                                </li>
                            </ul>
                        </p>
                        <p>
                            Look for the <a class="btn btn-default" href="javascript:void(0);" disabled="true"><i class="icon icon-bell"></i> Alerts</a> button.
                        </p>
                    </div>
                </div>
            </div>
          </div>
      </div>
      <script type="text/javascript">

          var addMyAlertUrl = 'addMyAlert/';
          var deleteMyAlertUrl = 'deleteMyAlert/';
          var deleteMyAlertWRUrl ='deleteMyAlertWR/';

          $(document).ready( function(){

              $(".query-cb").change(function(e){
                  if($(this).is(':checked')){
                     $.get(addMyAlertUrl + $(this).attr('id') + '?userId=${userId}');
                  } else {
                     $.get(deleteMyAlertUrl + $(this).attr('id')+ '?userId=${userId}');
                  }
              });
              $("#userFrequency").change(function(){
                  $.get('changeFrequency?frequency='+$('#userFrequency').val())
                      .success(function() {
                          //alert("Your alerts have been changed to : " + $('#userFrequency').val());
                      })
                      .error(function() {
                          alert("There was a problem updating your alert frequency. Please try again later.");
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
          });
        </script>
    </body>
</html>