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

        function resetResultsInDB(id) {
            const userInput = prompt(`This action will reset the previous and current results in the database. Type ‘yes’ if you understand and wish to proceed. `);
            if (userInput && userInput.toLowerCase() === 'yes') {
                const numericId = parseInt(id, 10);
                fetch(`/admin/resetQueryResult?id=`+numericId, {
                    method: 'GET',
                    headers: {
                        'Content-Type': 'application/json'
                    }
                })
                .then(response => response.json())
                .then(data => {
                    if (data.status === 1) {
                        alert("Failed:" + data.message);
                    } else {
                        alert("Reset successfully.");
                    }
                })
                .catch(error => {
                    alert('Error occurred while resetting record: ' + error);
                });
            }
        }
    </script>

</head>
<body>
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
                                    <div><p class="bg-info"><b>Query URL:</b> ${query.baseUrl+query.queryPath} </p>
                                        <p><i class="fa fa-info-circle" aria-hidden="true" title="The URL MAY be used to build a UI link for this query."></i> <i>UI ref. ${query.baseUrlForUI}</i></p>
                                    </div>
                                    <i class="fa fa-cog" aria-hidden="true"></i> <i><b>JSON record path:</b>${query.recordJsonPath} <b>JSON ID path:</b>${query.idJsonPath}</i>
                                    %{-- <div>--}%
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
                                                        <g:if test="${queryResult.hasChanged}">
                                                            <span class="badge badge-info">Changed</span>
                                                        </g:if>
                                                        <g:else>
                                                            <span class="badge badge-dark">No changes</span>
                                                        </g:else>
                                                    </div>
                                                    <div>
                                                        <b>${queryResult.frequency?.name?.toUpperCase()}</b>
                                                        query result ID: <g:link controller="queryResult" action="getDetails" params="[id: queryResult.id]" target="_blank"> <span class="badge badge-primary">${queryResult.id}</span></g:link>

                                                        <g:if test="${queryResult?.lastChecked}">
                                                             Last checked: ${queryResult?.lastChecked}&nbsp;&nbsp;
                                                        </g:if>
                                                        <g:link controller="ws" action="getQueryLogs" params="[id: query.id, frequency: queryResult.frequency?.name]" target="_blank">Log</g:link>
                                                                                                               &nbsp;&nbsp;
                                                        <g:if test="${queryResult?.queryUrlUsed}">
                                                            <a href="${queryResult?.queryUrlUsed}" target="_blank" title="URL for search">
                                                                Query URL
                                                            </a>
                                                        </g:if>
                                                        <g:if test="${grailsApplication.config.grails.env != 'production'}">
                                                        <br/>
                                                        <b><i class="fa fa-warning" style="color:#c44d34"></i> Resetting the previous and current results of this record to help testers identify new records.</b><button class="btn btn-primary" onclick="resetResultsInDB(${queryResult.id})">Reset</button>
                                                        </g:if>
                                                        <br/>
                                                        Subscribers:${query.countSubscribers(queryResult.frequency?.name)}
                                                    </div>
                                                    <div>
                                                        <g:each var="pv" in="${queryResult.propertyValues}">
                                                            <span class="badge badge-light">${pv.propertyPath.id}</span> ${pv.propertyPath}<br>
                                                            <span class="badge badge-light">${pv.id}</span> Current Value: ${pv.currentValue}; Previous Value: ${pv.previousValue} <br>
                                                        </g:each>
                                                    </div>
                                                    <div style="text-align: right;">
                                                        <hr>
                                                         <div style="padding: 5px;">
                                                            <label>Evaluate the new record discovery algorithm using <i class="fa fa-info-circle" aria-hidden="true" style="color: #c44d34;"
                                                                                                                        title="It won't query our data services"></i>
                                                                <span style="color:#c44d34;">the last check results</span> in Alerts.</label><g:link class="btn btn-info"  controller="notification" action="evaluateChangeDetectionAlgorithm" params="[queryId: query.id, queryResultId: queryResult.id, emailMe:true]" target="_blank">
                                                                Evaluate & email me
                                                            </g:link>
                                                         </div>
                                                        <g:if test="${queryType != 'biosecurity'}">
                                                            <div style="padding: 5px;">
                                                            <label>Query the latest records from the data services
                                                                <i class="fa fa-info-circle" aria-hidden="true" style="color: #c44d34;"
                                                                   title="The query may use the last checked date. The query may end on that date, span a period around that date, or not used at all. e.g, if the last checked date of a Monthly Image alerts is 1 Jan 2025, this function will query the images which were uploaded from 1 Dec 2024 to current time"></i>
                                                                , compare them with the current records in Alerts, and email me the findings.</label>
                                                                <g:link class="btn btn-info"  controller="admin" action="emailMeLastCheck" params="[queryId: query.id, frequency: queryResult.frequency?.name]" target="_blank">
                                                                Query & Email me.
                                                            </g:link>
                                                            </div>
%{--                                                            <div style="padding: 5px;">--}%
%{--                                                                <label>Query the latest records, compare them with the current records in Alerts, and display the changes.</label> <g:link class="btn btn-info"  controller="admin" action="dryRunQuery" params="[queryId: query.id, frequency: queryResult.frequency?.name]" target="_blank">--}%
%{--                                                                    Dry run (no DB update, no emails)--}%
%{--                                                                </g:link>--}%
%{--                                                            </div>--}%
                                                            <div style="margin-top: 20px; margin-bottom: 20px;">
                                                                <g:form class="form-inline" controller="admin" action="emailAlertsOnCheckDate" method="POST" target="_blank">
                                                                    <%@ page import="java.time.LocalDate" %>
                                                                    <%
                                                                        String today = LocalDate.now().toString();  // Format: YYYY-MM-DD
                                                                    %>
                                                                    <input type="hidden" name="queryId" value="${query.id}" />
                                                                    <input type="hidden" name="frequency" value="${queryResult.frequency?.name}" />
                                                                    <label for="checkDate">Run the
                                                                        <g:if test="${queryResult?.queryUrlUsed}">
                                                                            <a href="${queryResult?.queryUrlUsed}" target="_blank" title="URL for search"><i class="fa fa-link" aria-hidden="true"></i> query</a>
                                                                         </g:if>
                                                                        <g:else>
                                                                            query
                                                                        </g:else>
                                                                         against the given date <i class="fa fa-info-circle" aria-hidden="true" style="color: #c44d34;"
                                                                                                  title="It may be set as starting from that date, ending on that date, spanning a period around that date, or not used at all."></i>
                                                                        , and email new records. The date range associated with the given date is determined by the query.

                                                                     </label>
                                                                     <input type="date" id="checkDate" name="checkDate" value="${today}" class="form-control" />
                                                                    <button type="submit" class="btn btn-info mb-2">Email me</button>
                                                                </g:form>
                                                            </div>
                                                            <div>
                                                                <label>Perform the check and update the database with no emails for users..</label>
                                                                <g:link class="btn btn-primary"  controller="admin" action="runQueryWithLastCheckDate" params="[queryId: query.id, frequency: queryResult.frequency?.name]" target="_blank">
                                                                    Update
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