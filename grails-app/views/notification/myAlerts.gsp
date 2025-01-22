<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page expressionCodec="none" %>
<%@ page import="au.org.ala.alerts.Notification" %>
<!doctype html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="${grailsApplication.config.skin.layout}" />

        <meta name="breadcrumb" content="${message(code:"my.alerts.breadcrumbs")}" />
        <meta name="breadcrumbParent" content="${grailsApplication.config.userdetails.web.url}/myprofile, ${message(code:"my.alerts.breadcrumb.parent")}" />
        <g:set var="userPrefix" value="${adminUser ? user.email : message(code:"my.alerts.my") }"/>
        <title><g:message code="my.alerts.title" args="[userPrefix]" /> | ${grailsApplication.config.skin.orgNameLong}</title>
        <asset:stylesheet href="alerts.css"/>
        <script>
            $(function () {
                $('[data-toggle="tooltip"]').tooltip()
            })
        </script>

        <style>
            .query-cb {
                height: 40px; /* Define the height of the input */
                padding: 0 20px; /* Add padding for internal spacing */
                border: 1px solid #000; /* Add a border for visibility */
                border-radius: 20px; /* Set radius to half of the height */
                outline: none; /* Remove focus outline */
                box-sizing: border-box; /* Ensure padding doesn't affect size */
            }
        </style>
    </head>
    <body>
      <div id="content">
          <header id="page-header">
              <div class="inner row-fluid">
                  <div class="content">
                      <h2>
                          <g:message code="my.alerts.h1" args="[userPrefix]" />
                          <g:if test="${user.locked}">
                              <i class="fa fa-lock" data-toggle="tooltip" data-placement="bottom" title="${g.message(code:'my.alerts.user.isLocked.title')}"></i>
                          </g:if>
                      </h2>
                  </div>
                  <div>
                      <% if (request.isUserInRole("ROLE_ADMIN")) { %>
                      <a href="${createLink(controller: 'admin', action: 'index')}" class="btn btn-primary">Admin</a>
                      <% } %>

                      <% if (request.isUserInRole("ROLE_BIOSECURITY_ADMIN")) { %>
                      <a href="${createLink(controller: 'admin', action: 'biosecurity')}" class="btn btn-primary">Biosecurity Admin</a>
                      <% } %>
                  </div>
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
                <div>
                  <p>
                  Manage your ALA alerts, send to : ${user.email}
                  </p>
                  <p>
                  <b>Send me alerts:</b>
                  <g:select name="userFrequency" from="${frequencies}" id="userFrequency" value="${user?.frequency?.name}" optionKey="name"
                            optionValue="${ { name->g.message(code: 'frequency.' + name) } }"
                  /> &nbsp;
                  This setting applies to all your active ALA alerts, including standard and custom alerts.
                    </p>
                </div>
              <!-- Main Content starts -->
              <div class="row">
                      <ul class="nav nav-tabs" id="alertTabs" role="tablist">
                          <li class="nav-item active" role="presentation">
                              <a class="nav-link " id="standard-alerts-tab"  data-toggle="tab" href="#standard-alerts" role="tab" aria-controls="standard-alerts" >Standard Alerts</a>
                          </li>
                          <li class="nav-item" role="presentation">
                              <a class="nav-link" id="custom-alerts-tab" data-toggle="tab" href="#custom-alerts"  role="tab" aria-controls="custom-alerts">Custom Alerts</a>
                          </li>
                      </ul>

                      <!-- Tabs Content -->
                      <div class="tab-content" id="alertTabsContent">
                          <!-- Standard Alerts Tab -->
                          <div class="tab-pane fade active in" id="standard-alerts" role="tabpanel" aria-labelledby="standard-alerts-tab">
                            <div class="col-md-7">
                                <div style="padding-top: 20px;">
                                Enable alerts to have notifications sent to your email address.
                                </div>
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
                                                  <input id="${query.id}" class="query-cb" name="field2" type="checkbox" checked />
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
                                              <div class="switch" data-on="danger">
                                                  <input id="${query.id}" class="query-cb" name="field2" type="checkbox" />
                                              </div>
                                          </td>
                                      </tr>
                                  </g:each>

                                  <g:if test="${myannotation != null}">
                                      <g:set var="myannotationChecked" value="${myannotation.size() != 0}" />
                                      <tr>
                                          <td class="queryDescription">
                                              <h3>My Annotations</h3>
                                              Notify me when records I have flagged are updated.
                                          </td>
                                          <td class="queryActions">
                                              <div class="switch" data-on="danger">
                                                  <g:if test="${myannotationChecked}">
                                                      <input data-type='myannotation' class="query-cb" name="field2" type="checkbox" checked />
                                                  </g:if>
                                                  <g:else>
                                                      <input data-type='myannotation' class="query-cb" name="field2" type="checkbox" />
                                                  </g:else>
                                              </div>
                                          </td>
                                      </tr>
                                  </g:if>
                                  </tbody>
                              </table>
                            </div>
                          </div>

                          <!-- Custom Alerts Tab -->
                          <div class="tab-pane fade" id="custom-alerts" role="tabpanel" aria-labelledby="custom-alerts-tab">
                              <g:if test="${customQueries}">
                                  <div class="col-md-7">
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
                                  </div>
                              </g:if>
                                  <div class="col-md-5">
                                      <br/>
                                      <div class="well">
                                          <p>You can set up specific alerts in various sections of the ALA, including</p>
                                          <p>
                                          <ul>
                                              <li>
                                                  <g:message code="my.alerts.data.resource.desc" args="[grailsApplication.config.collectory.searchURL, grailsApplication.config.collection.searchTitle]" />
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
 <!-- end main content -->
          </div>
      </div>
      <asset:javascript src="alerts.js"/>
      <asset:script type="text/javascript">

          var addMyAlertUrl = 'addMyAlert/';
          var deleteMyAlertUrl = 'deleteMyAlert/';
          var deleteMyAlertWRUrl ='deleteMyAlertWR/';

          var subscribeMyAnnotationUrl = 'subscribeMyAnnotation/'
          var unsubscribeMyAnnotationUrl = 'unsubscribeMyAnnotation/'

          $(document).ready( function(){

              $("#userFrequency").change(function(){
                  $.get('changeFrequency?userId=${userId}&frequency='+$('#userFrequency').val())
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

                          var url = '';
                          if ($(this).attr('data-type') === 'myannotation') {
                              url = (state ? subscribeMyAnnotationUrl : unsubscribeMyAnnotationUrl) + '?userId=${userId}';
                          } else {
                              url = (state ? addMyAlertUrl : deleteMyAlertUrl) + $(this).attr('id') + '?userId=${userId}';
                          }

                          $.get(url);
                          return true;
                      }
                  }
              );
          });
        </asset:script>
    </body>
</html>