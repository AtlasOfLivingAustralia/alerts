<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="au.org.ala.alerts.Query" %>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <meta name="breadcrumb" content="Manage all alerts"/>
    <meta name="breadcrumbParent" content="${request.contextPath}/admin,Alerts admin"/>

    <title>Debug and manage alerts</title>
    <asset:stylesheet src="alerts.css"/>

    <script>
        $(document).ready(function () {
            $('[data-bs-toggle="tooltip"]').each(function () {
                new bootstrap.Tooltip(this);
            });

            $('.toggle-more-query-details').click(function () {
                var target = document.querySelector($(this).data('target'));
                if (target) {
                    new bootstrap.Collapse(target).toggle();
                }
            });

            $('.toggle-advanced').click(function (e) {
                e.preventDefault();
                var target = document.querySelector($(this).data('target'));
                if (target) {
                    target.classList.toggle('d-none');
                }
            });
        });

        var wipeQueryUrl = '${createLink(controller: 'query', action: 'wipe')}';

        /**
         * Delete a custom alert (query + its notifications / results) and report the outcome.
         * The 'wipe' action renders {status: 0|1, message: '..'}
         */
        function wipeQuery(id) {
            if (!confirm('This will permanently delete query ' + id + ' and all of its subscriptions. Continue?')) {
                return;
            }

            fetch(wipeQueryUrl + '?id=' + encodeURIComponent(id), {
                method: 'GET',
                headers: { 'Accept': 'application/json' }
            })
            .then(function (response) {
                if (!response.ok) {
                    throw new Error('HTTP ' + response.status);
                }
                return response.json();
            })
            .then(function (data) {
                alert(data.message);
                if (data.status === 0) {
                    // drop the list item and its details panel from the page
                    var item = document.getElementById('query-' + id);
                    var details = document.getElementById('more-' + id);
                    if (item) item.remove();
                    if (details) details.remove();
                }
            })
            .catch(function (error) {
                alert('Failed to delete query ' + id + ': ' + error.message);
            });
        }

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
        <div class="nav nav-tabs" id="myTab" role="tablist">
            <g:each var="queryType" in="${queries.keySet()}" status="i">
                <button class="nav-link  ${i == 0 ? 'active' : ''}"  id="tab-${queryType}-tab" data-bs-toggle="tab" data-bs-target="#tab-${queryType}-content"  role="tab" aria-controls="tab-${queryType}" >${queryType}</button>
            </g:each>
        </div>
        <div class="tab-content" id="myTabContent">
            <g:each var="queryType" in="${queries.keySet()}" status="i">
                <div class="tab-pane fade ${i == 0 ? 'show active' : ''}" id="tab-${queryType}-content" role="tabpanel" aria-labelledby="tab-${queryType}-content">
                    <ul>
                        <g:each var="query" in="${queries[queryType]}">
                            <li id="query-${query.id}">
                                <g:if test="${query.custom}">
                                    <a href="javascript:void(0);" onclick="wipeQuery(${query.id})"
                                       data-bs-toggle="tooltip" data-bs-placement="top"
                                       title="Delete this custom alert and all of its subscriptions"><i class="fas fa-trash" aria-hidden="true"></i></a>
                                </g:if>
                                <g:link controller="query" action="show" params="[id: query.id]" target="_blank" data-bs-toggle="tooltip" data-bs-placement="top" title="View query details"> <span class="badge badge-outline-primary"><i class="fa fa-info-circle" aria-hidden="true"></i> ${query.id}</span></g:link>

                                <a href="javascript:void(0);" class="toggle-more-query-details" data-target="#more-${query.id}"  data-bs-toggle="tooltip" data-bs-placement="top" title="Click to show more functions">
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
                                <div class="card card-body mt-2">

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
                                                        <b><i class="fa fa-calendar-check-o" aria-hidden="true"></i> ${queryResult.frequency?.name?.toUpperCase()}</b>
                                                        <g:if test="${queryResult.hasChanged}">
                                                            <span class="badge bg-info">Changed</span>
                                                        </g:if>
                                                        <g:else>
                                                            <span class="badge bg-dark">No changes</span>
                                                        </g:else>
                                                        <g:link controller="queryResult" action="getDetails" params="[id: queryResult.id]" target="_blank" data-bs-toggle="tooltip" data-bs-placement="top" title="Show the latest query result - QS ID: ${queryResult.id}"> <span class="badge badge-outline-primary"><i class="fa fa-database" aria-hidden="true"></i></span></g:link>
                                                        <label data-bs-toggle="tooltip" data-bs-placement="top" title="${query.getSubscriberEmails(queryResult.frequency?.name)}"><span class="badge bg-info"> <i class="fa fa-user"></i> ${query.countSubscribers(queryResult.frequency?.name)}</span></label>
                                                        <g:link controller="ws" action="getQueryLogs" params="[id: query.id, frequency: queryResult.frequency?.name]" target="_blank"  data-bs-toggle="tooltip" data-bs-placement="top" title="Display the log "><i class="fa fa-history" aria-hidden="true"></i></g:link>

                                                        <g:if test="${queryResult?.lastChecked}">
                                                            Last checked: ${queryResult?.lastChecked}
                                                        </g:if>&nbsp;&nbsp;
                                                    </div>
                                                    <div>


                                                        <g:form controller="admin" action="emailAlertsOnCheckDate" method="POST" target="_blank">
                                                            <%@ page import="java.time.LocalDate" %>
                                                            <%
                                                                String today = LocalDate.now().toString();  // Format: YYYY-MM-DD
                                                            %>
                                                            <input type="hidden" name="queryId" value="${query.id}" />
                                                            <input type="hidden" name="frequency" value="${queryResult.frequency?.name}" />
                                                            <div class="row my-3 align-items-start">
                                                                <!-- Column 1: Text Description -->
                                                                <div class="col-md-7">
                                                                    <label class="form-label mb-2">Run the
                                                                        <g:if test="${queryResult?.queryUrlUsed}">
                                                                            <a href="${queryResult?.queryUrlUsed}" target="_blank" data-bs-toggle="tooltip" data-bs-placement="top"  title="${queryResult?.queryUrlUsed}"><i class="fa fa-link" aria-hidden="true"></i> query</a>
                                                                        </g:if>
                                                                        <g:else>
                                                                            query
                                                                        </g:else>
                                                                        against the given date and email new records.</label>
                                                                    <div class="small text-muted fst-italic">The date range associated with the given date is determined by the query. It may be set as starting from that date, ending on that date, spanning a period around that date, or not used at all.</div>
                                                                </div>
                                                                <!-- Column 2: Date Input -->
                                                                <div class="col-md-2">
                                                                    <input type="date" id="checkDate" name="checkDate" value="${today}" class="form-control" />
                                                                </div>
                                                                <!-- Column 3: Button and Checkbox -->
                                                                <div class="col-md-3">
                                                                    <button type="submit" class="btn btn-outline-primary w-100 mb-2">Email me</button>
                                                                    <label class="form-check-label">
                                                                        <input type="checkbox" name="sendToSubscribers" class="form-check-input me-1" />
                                                                        send a copy to subscribers
                                                                    </label>
                                                                </div>
                                                            </div>
                                                        </g:form>
                                                    </div>
                                                    <div>
%{--                                                    <g:each var="pv" in="${queryResult.propertyValues}">--}%
%{--                                                        <span class="badge badge-light">${pv.id}</span> Current Value: ${pv.currentValue}; Previous Value: ${pv.previousValue} <br>--}%
%{--                                                    </g:each>--}%
                                                    </div>
                                                    <a href="javascript:void(0);" class="toggle-advanced" data-target="#advanced-${queryResult.id}"><i class="fa-solid fa-bug"></i> Advanced tools for developers</a>
                                                    <div id="advanced-${queryResult.id}" class="text-end d-none" >
                                                        <hr>
                                                         <div class="p-1">
                                                            <label>Evaluate the new record discovery algorithm using
                                                                <span title="It won't query our data services">
                                                                <i class="fa fa-info-circle" aria-hidden="true" style="color: #c44d34;"></i> the last check results</span> in Alerts.</label><g:link class="btn btn-outline-primary"  controller="notification" action="evaluateChangeDetectionAlgorithm" params="[queryId: query.id, queryResultId: queryResult.id, emailMe:true]" target="_blank">
                                                                Evaluate & Email me
                                                            </g:link>
                                                         </div>
                                                        <g:if test="${queryType != 'biosecurity'}">
                                                            <div class="p-1">
                                                            <label>Query the latest records from the data services
                                                                <i class="fa fa-question-circle-o" aria-hidden="true" style="color: #c44d34;"
                                                                   title="The query may use the last checked date. The query may end on that date, span a period around that date, or not used at all. e.g, if the last checked date of a Monthly Image alerts is 1 Jan 2025, this function will query the images which were uploaded from 1 Dec 2024 to current time"></i>
                                                                , compare them with the current records in Alerts, and email me the findings.</label>
                                                                <g:link class="btn btn-outline-primary"  controller="admin" action="emailMeLastCheck" params="[queryId: query.id, frequency: queryResult.frequency?.name]" target="_blank">
                                                                Query & Email me.
                                                            </g:link>
                                                            </div>
%{--                                                            <div style="padding: 5px;">--}%
%{--                                                                <label>Query the latest records, compare them with the current records in Alerts, and display the changes.</label> <g:link class="btn btn-info"  controller="admin" action="dryRunQuery" params="[queryId: query.id, frequency: queryResult.frequency?.name]" target="_blank">--}%
%{--                                                                    Dry run (no DB update, no emails)--}%
%{--                                                                </g:link>--}%
%{--                                                            </div>--}%

                                                            <div>
                                                                <label>Perform the check and update the database with no emails for users..</label>
                                                                <g:link class="btn btn-primary"  controller="admin" action="runQueryWithLastCheckDate" params="[queryId: query.id, frequency: queryResult.frequency?.name]" target="_blank">
                                                                    Update
                                                                </g:link>
                                                            </div>
                                                            <g:if test="${grailsApplication.config.grails.env != 'production'}">
                                                                <div class="mt-1">
                                                                    <b><i class="fa fa-warning" style="color:#c44d34"></i> Note: Reset the query result is only available in TEST and DEV environments.</b><button class="btn btn-primary" onclick="resetResultsInDB(${queryResult.id})">Reset</button>
                                                                </div>
                                                            </g:if>
                                                        </g:if>
                                                        <g:else>
                                                            <g:link class="btn btn-primary" namespace="biosecurity" controller="csv" action="downloadLastResult" params="[id:  queryResult.id]" target="_blank">
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