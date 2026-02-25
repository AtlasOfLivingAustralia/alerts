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
        <asset:stylesheet src="alerts.css"/>
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css" rel="stylesheet">
        <script>
            document.addEventListener('DOMContentLoaded', function () {
                var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
                var tooltipList = tooltipTriggerList.map(function (el) {
                    return new bootstrap.Tooltip(el);
                });
            });
        </script>
    </head>
    <body>
      <div class="container ms-2">
          <header id="page-header">
              <div class="row align-items-center">
                  <div class="col-6">
                      <h2>
                          <g:message code="my.alerts.h1" args="[userPrefix]" />
                          <g:if test="${user.locked}">
                              <i class="fas fa-lock" data-bs-toggle="tooltip" data-placement="bottom" title="${g.message(code:'my.alerts.user.isLocked.title')}"></i>
                          </g:if>
                      </h2>
                  </div>
                  <div class="col-6 text-end">
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
              <div class="alert alert-info" role="alert">${flash.message}</div>
          </g:if>
          <g:if test="${flash.errorMessage}">
              <div class="alert alert-danger">${flash.errorMessage}</div>
          </g:if>
          <div id="page-body" role="main" class="mt-3">
                <g:set var="userId">${user.userId}</g:set>
                <div  class="mt-2">
                      <i class="fas fa-envelope"></i> Notifications will be sent to <b>${user.email}</b>
                </div>
                <div class="mt-3">
                    <i class="fas fa-clock"></i> Notification frequency
                    <g:select name="userFrequency" from="${frequencies}" id="userFrequency" value="${user?.frequency?.name}" optionKey="name"
                            optionValue="${ { name->g.message(code: 'frequency.' + name) } }"/> &nbsp;applies to all active notifications
                </div>
          </div>
          <!-- Main Content starts -->
          <div class="row mt-4">
              <div class="col-12">
                      <div class="nav nav-tabs" id="alertTabs" role="tablist">
                          <button class="nav-link active " id="standard-alerts-tab"  data-bs-toggle="tab" data-bs-target="#standard-alerts" role="tab" aria-controls="standard-alerts" >Standard Alerts</button>
                          <button class="nav-link" id="custom-alerts-tab" data-bs-toggle="tab" data-bs-target="#custom-alerts"  role="tab" aria-controls="custom-alerts">Custom Alerts</button>
                      </div>

                      <!-- Tabs Content -->
                      <div class="tab-content" id="alertTabsContent">

                          <!-- Standard Alerts Tab -->
                          <div class="tab-pane fade active show" id="standard-alerts" role="tabpanel" aria-labelledby="standard-alerts-tab">
                           <div class="row">
                            <div class="col-12 col-lg-7">
                                <div class="pt-1">
                                Enable alerts to have notifications sent to your email address
                                </div>
                                <div class="list-group mt-2">
                                    <g:each in="${enabledQueries}" status="i" var="query">
                                        <div class="list-group-item border-top-0  border-start-0 border-end-0 d-flex justify-content-between align-items-center px-0 py-2">
                                            <div class="flex-grow-1 me-2" >
                                                <h5>${query.name}</h5>
                                                <p class="mb-0">${query.description}</p>
                                            </div>
                                            <div class="pe-1">
                                                <div class="form-check form-switch">
                                                    <input class="form-check-input" type="checkbox" role="switch" id="${query.id}" checked style="transform: scale(1.4);"/>
                                                </div>
                                            </div>
                                        </div>
                                    </g:each>

                                    <g:each in="${disabledQueries}" status="i" var="query">
                                        <div class="list-group-item border-top-0  border-start-0 border-end-0 d-flex justify-content-between align-items-center px-0 py-2">
                                            <div class="flex-grow-1 me-2">
                                                <h5>${query.name}</h5>
                                                <p class="mb-0">${query.description}</p>
                                            </div>
                                            <div class="pe-1">
                                                <div class="form-check form-switch">
                                                    <input class="form-check-input" type="checkbox" role="switch" id="${query.id}" style="transform: scale(1.4);"/>
                                                </div>
                                            </div>
                                        </div>
                                    </g:each>

                                    <g:if test="${myannotation != null}">
                                        <g:set var="myannotationChecked" value="${myannotation.size() != 0}" />
                                        <div class="list-group-item border-top-0  border-start-0 border-end-0 d-flex justify-content-between align-items-center px-0 py-2">
                                            <div class="flex-grow-1 me-2">
                                                <h5>My Annotations</h5>
                                                <p class="mb-0">Notify me when records I have flagged are updated.</p>
                                            </div>
                                            <div class="pe-1">
                                                <div class="form-check form-switch" data-on="danger">
                                                    <input class="form-check-input" type="checkbox" role="switch" data-type='myannotation' ${myannotationChecked ? 'checked' : ''} style="transform: scale(1.4);"/>
                                                </div>
                                            </div>
                                        </div>
                                    </g:if>
                                </div>
                             </div>
                               <div class="mt-2">
                                   <g:if test="${isMyAlerts}">
                                       <g:link controller="unsubscribe"
                                               action="index"
                                               class="btn btn-outline-primary">Disable all alerts
                                       </g:link>
                                   </g:if>
                               </div>
                           </div>
                          </div>

                          <!-- Custom Alerts Tab -->
                          <div class="tab-pane fade" id="custom-alerts" role="tabpanel" aria-labelledby="custom-alerts-tab">
                            <div class="row">
                                  <div class="col-12 col-lg-7">
                                      <g:if test="${customQueries?.size() > 0}">
                                          <div class="list-group">
                                              <g:each in="${customQueries}" status="i" var="query">
                                                  <div id="custom-${query.id}" class="list-group-item border-top-0  border-start-0 border-end-0 d-flex justify-content-between align-items-center px-0 py-2">
                                                      <div class="flex-grow-1 me-2">
                                                          <h5>${query.name}</h5>
                                                          <p class="mb-0">${query.description}</p>
                                                      </div>
                                                      <div class="pe-1">
                                                          <button type="button" class="btn btn-outline-primary deleteButton" id="${query.id}">
                                                              Delete
                                                          </button>
                                                      </div>
                                                  </div>
                                              </g:each>
                                          </div>
                                      </g:if>
                                      <g:else>
                                            <div class="card card-body mt-1">
                                                <p>You have no custom alerts.</p>
                                                <p>Custom alerts allow you to create specific notifications based on your unique interests and needs.</p>
                                                <p>To create a custom alert, start by performing a search in the ALA. Once you have your search results, look for the <a class="btn btn-outline-disable" disabled="true"> <i class="fa-regular fa-bell"></i> Alerts</a> button to set up an alert based on that search.</p>
                                            </div>
                                      </g:else>
                                  </div>

                                  <div class="col-12 col-lg-5">
                                        <div class="card card-body mt-1">
                                          <p>You can set up specific alerts in various sections of the ALA, including</p>

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

                                          <p>
                                              Look for the <a class="btn btn-outline-disable" disabled="true"> <i class="fa-regular fa-bell"></i> Alerts</a> button.
                                          </p>
                                      </div>
                                  </div>
                            </div>
                          </div>
                      </div>
                </div>
          </div>
         <!-- end main content -->
      </div>
      <asset:javascript src="alerts.js"/>
      <script type="text/javascript">
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

              $(".form-switch input[type='checkbox']").on("change", function() {
                  var state = $(this).is(":checked"); // true = on, false = off

                  // Optionally update a label text next to the switch
                  // var label = $("label[for='" + this.id + "']");
                  // if (label.length) {
                  //     label.text(state ? "Enabled" : "Disabled");
                  // }

                  // Build URL
                  var url = '';
                  if ($(this).data("type") === "myannotation") {
                      url = (state ? subscribeMyAnnotationUrl : unsubscribeMyAnnotationUrl) + "?userId=${userId}";
                  } else {
                      url = (state ? addMyAlertUrl : deleteMyAlertUrl) + this.id + "?userId=${userId}";
                  }

                  // AJAX request
                  $.get(url)
                      .done(function(resp) {
                          console.log("Success", resp);
                      })
                      .fail(function(err) {
                          console.error("Error", err);
                      });
              });
          });

          // Show the correct tab based on the URL hash
          $(window).on('load', function () {
              var hash = window.location.hash;
              if (hash) {
                var alertsTab = $('a.nav-link[href="' + hash + '"]');
                if (alertsTab.length) {
                  alertsTab.tab('show');
                }
              }
            });
      </script>
    </body>
</html>