<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="ala.postie.Notification" %>
<!doctype html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="userPrefix" value="${adminUser ? user.email : 'My' }"/>
        <title>${userPrefix} email alerts | Atlas of Living Australia</title>
        <r:require modules="bootstrapSwitch"/>
    </head>
    <body>
      <a href="#page-body" class="skip"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
      <div id="content">
          <header id="page-header">
            <div class="inner row-fluid">
              <nav id="breadcrumb" class="span12">
                <ol class="breadcrumb">
                  <li><a href="http://www.ala.org.au">Home</a> <span class="icon icon-arrow-right"></span></li>
                  <li><a href="http://www.ala.org.au/my-profile/">My Profile</a> <span class="icon icon-arrow-right"></span></li>
                  <li class="active">${userPrefix} email alerts</li>
                </ol>
              </nav>
              <hgroup>
                <h1>${userPrefix} email alerts</h1>
              </hgroup>
            </div>
          </header>
          <div id="page-body" role="main">
                <g:set var="userId">${user.userId}</g:set>
                <p>Enable an alert to have emails sent to your email address (${user.email})</p>
                <h3>
                  Send me alerts:
                  <g:select name="userFrequency" from="${frequencies}" id="userFrequency" value="${user?.frequency?.name}" optionKey="name" />
                </h3>
            <div class="row-fluid">
                <div class="span12">
                    <table>
                        <tbody>
                        <g:each in="${enabledQueries}" status="i" var="query">
                            <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                                <td>
                                  <h3>${query.name}</h3>
                                  ${query.description}
                                </td>
                                <td>
                                    <div class="switch switch-large" data-on="danger">
                                        <input  id="${query.id}"  name="field2"  type="checkbox" checked />
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
                                    <div class="switch switch-large" data-on="danger" >
                                        <input  id="${query.id}"  name="field2"  type="checkbox" checked />
                                    </div>
                                </td>
                            </tr>
                        </g:each>
                        </tbody>
                    </table>
                    <g:if test="${customQueries}">
                    <h2>My custom alerts</h2>
                    <table>
                        <tbody>
                        <g:each in="${customQueries}" status="i" var="query">
                            <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                                <td class="queryDescription">
                                  <h3>${query.name}</h3>
                                  ${query.description}
                                </td>
                                <td class="queryActions">
                                <p class="field switch">
                                    %{--<span class='button red deleteButton' id='${query.id}'>--}%
                                      %{--<g:link controller="notification" action="deleteMyAlertWR" id="${query.id}" params="${[userId:userId]}">Delete</g:link>--}%
                                    %{--</span>--}%
                                    <span class='btn-ala' id='${query.id}'>Delete</span>
                                </p>
                                </td>
                            </tr>
                        </g:each>
                        </tbody>
                    </table>
                    </g:if>
                 </div>
            </div>
          </div>
      </div>
      <script type="text/javascript">

          var addMyAlertUrl = 'addMyAlert/';
          var deleteMyAlertUrl = 'deleteMyAlert/';
          var deleteMyAlertWRUrl ='deleteMyAlertWR/'

          $(document).ready( function(){
              $(".cb-enable").click(function(){
                  var parent = $(this).parents('.switch');
                  $('.cb-disable',parent).removeClass('selected');
                  $(this).addClass('selected');
                  $('.checkbox',parent).attr('checked', true);
                  //send DB update
                  //alert($('.checkbox',parent).attr('id'));
                  $.get(addMyAlertUrl + $('.checkbox',parent).attr('id'));
              });
              $(".cb-disable").click(function(){
                  var parent = $(this).parents('.switch');
                  $('.cb-enable',parent).removeClass('selected');
                  $(this).addClass('selected');
                  $('.checkbox',parent).attr('checked', false);
                  //send DB update
                  //send DB update
                  //alert($('.checkbox',parent).attr('id'));
                  $.get(deleteMyAlertUrl + $('.checkbox',parent).attr('id'));
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
                //$.get('deleteMyAlert/'+ $(this).attr('id'));
                document.location.href = deleteMyAlertWRUrl + $(this).attr('id') + '?userId=${userId}'
              });
          });
        </script>
    </body>
</html>