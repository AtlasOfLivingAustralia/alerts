<%@ page import="ala.postie.Notification" %>
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

                <g:set var="userId"><cl:loggedInUsername/></g:set>
                <p>
                  Enable an alert to have emails sent to your email address (<cl:loggedInUsername/>)
                </p>

                <h3>
                  Send me alerts:
                  <g:select name="userFrequency" from="${frequencies}" id="userFrequency" value="${user?.frequency?.name}" optionKey="name" />
                </h3>

                <div class="list">
                    <table>
                        <tbody>
                        <g:each in="${enabledQueries}" status="i" var="query">
                            <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                                <td>
                                  <h3>${query.name}</h3>
                                  ${query.description}
                                </td>
                                <td>
                                <p class="field switch">
                                    <label class="cb-enable selected"><span>Enabled</span></label>
                                    <label class="cb-disable"><span>Disabled</span></label>
                                    <input type="checkbox" id="${query.id}" class="checkbox" name="field2" />
                                </p>
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
                                <p class="field switch">
                                    <label class="cb-enable"><span>Enabled</span></label>
                                    <label class="cb-disable selected"><span>Disabled</span></label>
                                    <input type="checkbox" id="${query.id}" class="checkbox" name="field2" />
                                </p>
                                </td>
                            </tr>
                        </g:each>
                        </tbody>
                    </table>
                    <g:if test="${customQueries}">
                    <h2>My custom alerts</h2>
                    <table >
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
                                    <span class='button red deleteButton' id='${query.id}'>Delete</span>
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
          $(document).ready( function(){
              $(".cb-enable").click(function(){
                  var parent = $(this).parents('.switch');
                  $('.cb-disable',parent).removeClass('selected');
                  $(this).addClass('selected');
                  $('.checkbox',parent).attr('checked', true);
                  //send DB update
                  //alert($('.checkbox',parent).attr('id'));
                  $.get('addMyAlert/'+$('.checkbox',parent).attr('id'));
              });
              $(".cb-disable").click(function(){
                  var parent = $(this).parents('.switch');
                  $('.cb-enable',parent).removeClass('selected');
                  $(this).addClass('selected');
                  $('.checkbox',parent).attr('checked', false);
                  //send DB update
                  //send DB update
                  //alert($('.checkbox',parent).attr('id'));
                  $.get('deleteMyAlert/'+$('.checkbox',parent).attr('id'));
              });

              $("#userFrequency").change(function(){
                  $.get('changeFrequency?frequency='+$('#userFrequency').val());
              });

              $('.deleteButton').click(function(data){
                //$.get('deleteMyAlert/'+ $(this).attr('id'));
                document.location.href = 'deleteMyAlertWR/'+$(this).attr('id') + '?userId=${userId}'
              });
          });
        </script>
    </body>
</html>
