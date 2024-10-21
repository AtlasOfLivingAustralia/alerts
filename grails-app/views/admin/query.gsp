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
            });
        });
    </script>

</head>
<body>
    <div>
        <div class="panel panel-default">
            <div class="panel-heading">Dry run tests. No DB updates, No emails sent </div>
            <div class="panel-body">
                <a class="btn btn-info" href = "/admin/dryRunAllQueriesForFrequency?frequency=daily" target="_blank">Dry run daily tasks</a>
            </div>
        </div>
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
                            <li>
                                <g:link controller="query" action="wipe" params="[id: query.id]" target="_blank"><i class="fa fa-trash" aria-hidden="true"></i></g:link>
                                <span class="badge badge-light">${query.id}</span>
                                <a href="javascript:void(0);" class="toggle-more-query-details" data-target="#more-${query.id}"  title="Query ID:${query.id}">
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
                                       <g:if test="${query.queryResults?.size() > 0}">
                                            <ul>
                                                <g:each var="queryResult" in="${query.queryResults.sort { it.frequency?.name }}">
                                                    <div>
                                                        [<g:link controller="queryResult" action="getDetails" params="[id: queryResult.id]" target="_blank">${queryResult.id}</g:link>] <b title="Query Result ID:${queryResult.id}"> ${queryResult.frequency?.name} subscribers: </b> <span class="badge badge-primary">${query.countSubscribers(queryResult.frequency?.name)}</span>
                                                        - Last checked: <g:link controller="ws" action="getQueryLogs" params="[id: query.id, frequency: queryResult.frequency?.name]" target="_blank"> <i class="fa fa-info-circle" aria-hidden="true"></i>${queryResult?.lastChecked}</g:link>
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
                                                        <g:link class="btn btn-info"  controller="notification" action="evaluateChangeDetectionAlgorithm" params="[queryId: query.id, queryResultId: queryResult.id]" target="_blank">
                                                            Evaluate the latest check in DB
                                                        </g:link>
                                                        <g:if test="${queryType != 'biosecurity'}">
                                                            <g:link class="btn btn-info"  controller="admin" action="emailMeLastCheck" params="[queryId: query.id, frequency: queryResult.frequency?.name]" target="_blank">
                                                                Email me the latest check result (No DB update)
                                                            </g:link>
                                                            <g:link class="btn btn-info"  controller="admin" action="dryRunQuery" params="[queryId: query.id, frequency: queryResult.frequency?.name]" target="_blank">
                                                                Dry run (no DB update, no emails)
                                                            </g:link>
                                                            <br>
                                                            <div style="margin-top: 20px; margin-bottom: 20px;">
                                                                <g:form class="form-inline" controller="admin" action="emailAlertsOnCheckDate" method="POST" target="_blank">
                                                                    <%@ page import="java.time.LocalDate" %>
                                                                    <%
                                                                        String today = LocalDate.now().toString();  // Format: YYYY-MM-DD
                                                                    %>
                                                                    <input type="hidden" name="queryId" value="${query.id}" />
                                                                    <input type="hidden" name="frequency" value="${queryResult.frequency?.name}" />
                                                                    <label for="checkDate">Email me the results checked on the given check date (no DB update) </label>
                                                                     <input type="date" id="checkDate" name="checkDate"value="${today}" class="form-control" />

                                                                    <button type="submit" class="btn btn-primary mb-2">Run</button>
                                                                </g:form>
                                                            </div>
                                                            <div>
                                                            <g:link class="btn btn-primary"  controller="admin" action="runQueryWithLastCheckDate" params="[queryId: query.id, frequency: queryResult.frequency?.name]" target="_blank">
                                                                Run the last check (DB update, no emails)
                                                            </g:link>
                                                            </div>
                                                        </g:if>
                                                        <g:else>
                                                            <g:link class="btn btn-primary" controller="admin" action="downloadLastBiosecurityResult" params="[id:  queryResult.id]" target="_blank">
                                                                Download CSV from the latest check result
                                                            </g:link>
                                                        </g:else>
                                                    </div>
                                                    <hr>
                                                </g:each>
                                            </ul>

                                        </g:if>
                                        <g:else>
                                            <g:if test="${queryType != 'biosecurity'}">
                                                <g:link class="btn btn-info"  controller="admin" action="initFirstCheckAndEmailMe" params="[queryId: query.id, frequency: 'weekly']" target="_blank">
                                                    Init the first query and mail me the latest check result (DB updates)
                                                </g:link>
                                            </g:if>
                                        </g:else>
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