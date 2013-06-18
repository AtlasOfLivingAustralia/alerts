<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="ala.postie.Notification" %>
<!doctype html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="userPrefix" value="${adminUser ? user.email : 'My' }"/>
        <title>${userPrefix} email alerts | Atlas of Living Australia</title>
        <r:require modules="bootstrapSwitch,alerts"/>
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
                                    <a href="javascript:void(0);" class='btn-ala btn deleteButton' id='${query.id}'>Delete</a>
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

              $(".query-cb").change(function(e){
                  if($(this).is(':checked')){
                     $.get(addMyAlertUrl + $(this).attr('id'));
                  } else {
                      $.get(deleteMyAlertUrl + $(this).attr('id'));
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