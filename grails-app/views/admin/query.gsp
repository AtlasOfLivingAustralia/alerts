<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="au.org.ala.alerts.Query" %>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <meta name="breadcrumb" content="Manage all alerts"/>
    <meta name="breadcrumbParent" content="${request.contextPath}/admin,Alerts admin"/>

    <title>Admin - Manage alerts</title>
    <asset:stylesheet href="alerts.css"/>
    <asset:javascript src="bootstrap-3-typeahead-4.0.1.min.js"/>

    <script>
        $(document).ready(function () {
            $('.toggle-more-query-details').click(function () {
                var target = $(this).data('target');
                $(target).collapse('toggle');
                //var isExpanded = $(target).hasClass('show');
                //$(this).text(isExpanded ? $(this).text().replace('Show Less', 'Show More') : $(this).text().replace('Show More', 'Show Less'));
            });
        });
    </script>

</head>
<body>
    <div>
        <a class="btn btn-primary" href = "/admin/runTask?frequency=daily" target="_blank">Run daily task</a>
    </div>
    <div>
        <ul class="nav nav-tabs" id="myTab" role="tablist">
            <g:each var="queryType" in="${queries.keySet()}" status="i">
                <li class="nav-item">
                    <a class="nav-link ${i == 0 ? 'active' : ''}" id="tab-${queryType}-tab" data-toggle="tab" href="#tab-${queryType}-content" role="tab" aria-controls="tab-${queryType}" >${queryType}</a>
                </li>
            </g:each>
        </ul>
        <div class="tab-content" id="myTabContent">
            <g:each var="queryType" in="${queries.keySet()}" status="i">
                <div class="tab-pane fade ${i == 0 ? 'active in' : ''}" id="tab-${queryType}-content" role="tabpanel" aria-labelledby="tab-${queryType}-content">
                    <ul>
                        <g:each var="query" in="${queries[queryType]}">
                            <li><a href="javascript:void(0);" class="toggle-more-query-details" data-target="#more-${query.id}"  title="Query ID:${query.id}">

                                   <g:if test="${query.name == 'My Annotations'}">
                                       <%
                                               def users = query.notifications.collect { it.user?.email }.join(', ')
                                       %>
                                       ${users?:"No users"}
                                   </g:if>
                                   <g:else>
                                       ${query.name}
                                   </g:else>
                                 </a>
                            </li>
                            <div class="collapse" id="more-${query.id}">
                                <div class="card card-body">

                                    <div><p class="bg-info"> ${query.baseUrl+query.queryPath}</p></div>
                                    <i class="fa fa-cog" aria-hidden="true"></i> <i><b>JSON ID path:</b>${query.idJsonPath}  &nbsp; <b>JSON record path:</b>${query.recordJsonPath}</i>
    %{--                                <div>--}%
    %{--                                    <g:if test="${query.notifications}">--}%
    %{--                                            <g:each var="notification" in="${query.notifications}">--}%
    %{--                                                <li>${notification.user?.email}</li> --}%
    %{--                                            </g:each>--}%
    %{--                                    </g:if>--}%
    %{--                                <div>--}%
                                    <div>
                                       <g:if test="${query.queryResults}">

                                            <ul>
                                                <g:each var="queryResult" in="${query.queryResults}">
                                                    <div>
                                                         <b title="Query Result ID:${queryResult.id}">${queryResult.frequency?.name} subscribers: </b> <span class="badge badge-primary">${query.countSubscribers(queryResult.frequency?.name)}</span>
                                                        - Last checked:  <a href = "/ws/getQueryLogs?id=${query.id}&frequency=${queryResult.frequency?.name}" target="_blank"><i class="fa fa-info-circle" aria-hidden="true"></i> ${queryResult?.lastChecked}  </a>
                                                    </div>
                                                    <div>
                                                        <g:if test="${queryResult.hasChanged}">
                                                            <span class="badge badge-info">Changed</span>
                                                        </g:if>
                                                        <g:else>
                                                            <span class="badge badge-dark">No changes</span>
                                                        </g:else>
                                                    </div>
                                                    <div>
                                                        <g:each var="pv" in="${queryResult.propertyValues}">
                                                            ${pv.propertyPath}<br>
                                                            Current Value: ${pv.currentValue}  &nbsp;
                                                            Previous Value: ${pv.previousValue} <br>

                                                        </g:each>
                                                    </div>
                                                    <div>
                                                        <a class="btn btn-primary" href = "/notification/evaluateChangeDetectionAlgorithm?queryId=${query.id}&queryResultId=${queryResult.id}" target="_blank">Evaluate the latest result</a>
                                                        <g:if test="${queryType != 'biosecurity'}">
                                                            <a class="btn btn-primary" href = "/admin/runQueryWithLastCheckDate?queryId=${query.id}&frequency=${queryResult.frequency?.name}" target="_blank">Run the last check (no emails)</a>
                                                        </g:if>
                                                    </div>
                                                    <hr>
                                                </g:each>
                                            </ul>

                                        </g:if>
                                    </div>

                                </div>
                            </div>
                        </g:each>
                    </ul>
                </div>
            </g:each>
        </div>
    </div>

</body>