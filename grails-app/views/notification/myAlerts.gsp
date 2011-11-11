
<%@ page import="ala.postie.Notification" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'notification.label', default: 'Notification')}" />
        <title>My email alerts | Atlas of Living Australia</title>
    </head>
    <body>
        <div class="body">
            <h1>My email alerts</h1>
            <g:if test="${flash.message}">
            <!--<div class="message">${flash.message}</div>-->
            </g:if>

            <p>
              Enable an alert to have emails sent to your email address (<cl:loggedInUsername/>)
            </p>


            <div class="list">
                <table style="width:100%">
                    <!--
                    <thead>
                        <tr>
                            <th><g:message code="notification.query.label" default="Alert" /></th>
                            <g:sortableColumn property="description" title="${message(code: 'notification.description.label', default: 'Description')}" />
                        </tr>
                    </thead>
                    -->
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
                            <td>
                              <h3>${query.name}</h3>
                              ${query.description}
                            </td>
                            <td>
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
          });
        </script>
    </body>
</html>
