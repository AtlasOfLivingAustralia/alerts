<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="au.org.ala.alerts.Notification" %>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <meta name="breadcrumb" content="BioSecurity alerts"/>
    <meta name="breadcrumbParent" content="${request.contextPath}/admin, Admin"/>

    <title>Admin - Manage BioSecurity alerts</title>
    <asset:stylesheet href="alerts.css"/>
    <asset:javascript src="bootstrap-3-typeahead-4.0.1.min.js"/>
    <script>
        var subscriptionsPerLoad = ${subscriptionsPerPage?:10};
        var nextSubscription = 0;

        function submitPreview(target) {
            let button = target
            let btnName = button['name']
            let form = button.closest('form')

            var formData = new FormData(form); // Get form data
            var localDate = new Date(formData.get("date"));
            var utcDate = localDate.toISOString();
            formData.set("date", utcDate)

            if (btnName === 'previewSubscription') {
                form.target = '_blank';
                form.submit();
            }
        }

        function formatToLocaleDate(currentDateTime) {
            var year = currentDateTime.getFullYear();
            var month = currentDateTime.toLocaleString('default', { month: 'short' }); // Get month abbreviation (e.g., Jan, Feb, etc.)
            var day = currentDateTime.getDate();
            var hours = currentDateTime.getHours();
            var minutes = currentDateTime.getMinutes();

            // Pad single-digit day and hours with leading zero if necessary
            day = day < 10 ? '0' + day : day;
            hours = hours < 10 ? '0' + hours : hours;
            minutes = minutes < 10 ? '0' + minutes : minutes;

            return day + ' ' + month + ' ' + year + ' ' + hours + ':' + minutes;
        }

        function formatUtcToLocal(utcString) {
            if (!utcString) return null;

            const dt = new Date(utcString); // ISO UTC → Date
            return dt.toLocaleString(undefined, {
                weekday: 'short',
                year: 'numeric',
                month: 'short',
                day: '2-digit',
                hour: '2-digit',
                minute: '2-digit'
            });
        }

        function triggerSubscriptionSince(target, queryId) {
            var yes = confirm("It will also update the last check date to the current time. Are you sure you want to proceed?");

            if (yes) {
                let form = target.closest('form');
                var formData = new FormData(form); // Get form data
                var localDate = new Date(formData.get("date"));

                //Convert it to UTC
                let utcDateSince = localDate.toISOString();
                let localDateTo = new Date();
                let url = "${request.contextPath}/ws/triggerBiosecurityAlertSince?id=" + queryId + "&since=" + utcDateSince
                $.ajax({
                    url: url,
                    type: 'GET',
                    success: function (data) {
                        console.log(data)
                        let popup = $('span[name=showLastCheckDetails_'+queryId+']')
                        if(data.status === 0 ) {
                            if (popup) {
                                let logs = data.logs
                                popup.removeAttr('hidden');
                                popup.attr('data-content', "<li class='autowrap-popover'>" + logs.map(item => item).join("</li><li class='autowrap-popover'>") + "</li>");
                                popup.html( formatToLocaleDate(localDateTo) + "<i class='fa fa-check' aria-hidden='true' style='color: red;padding-left: 15px;'></i>")
                                initializePopoverAgain();
                                //Hide possible info
                                popup.siblings('[name=neverCheckedInfo]').hide();
                            }
                            alert("The subscription has been successfully completed. Click on the last checked date for details.")
                        } else {
                            if (popup) {
                                let logs = data.logs
                                popup.removeAttr('hidden');
                                popup.attr('data-content', "<li class='autowrap-popover'>" + logs.map(item => item).join("</li><li class='autowrap-popover'>") + "</li>");
                                popup.html( formatToLocaleDate(localDateTo) + "<i class='fa fa-check' aria-hidden='true' style='color: red;padding-left: 15px;'></i>")
                                initializePopoverAgain();
                                //Hide possible info
                                popup.siblings('[name=neverCheckedInfo]').hide();
                            }
                            popup.text(localDate +" - Failed")
                            alert("The subscription failed. Click on the last checked date for details.")
                        }
                    },
                    error: function (xhr, status, error) {
                        // Handle errors
                        console.error(xhr.responseText);
                    }
                });
            }
        }

        /**
         * todo: refactor the way of passing params
         */
        function loadMore() {
            let url
            // If the function is called with no arguments, the action is to load more records
            if (arguments.length === 0) {
                // Make an AJAX request to fetch more records
                nextSubscription += subscriptionsPerLoad;
                url = "${createLink(controller: 'admin', action: 'getMoreBioSecurityQuery')}" + "?startIdx=" + nextSubscription;
            } else {
                // Fetch queries AKA subscriptions from the first record
                nextSubscription = 0;
                url = "${createLink(controller: 'admin', action: 'getMoreBioSecurityQuery')}" + "?startIdx=0" ;
            }

            $.ajax({
                url: url,
                type: "GET",
                success: function(response) {
                    // Append new records to the container
                    $("div#biosecurityDetails").append(response);
                    initializePopoverAgain();

                    // Hide the button if there are no more records to fetch
                    $.ajax({
                        url: "${createLink(controller: 'admin', action: 'countBioSecurityQuery')}" ,
                        type: "GET",
                        success: function(response) {
                            if (nextSubscription+subscriptionsPerLoad >= response.count) {
                                $("button.more-button").hide();
                            } else {
                                $("button.more-button").show();
                            }
                        },
                        error: function(xhr, status, error) {
                            console.error(error);
                        }
                    });

                },
                error: function(xhr, status, error) {
                    console.error(error);
                }
            })
        }



        function updateTotalNumberOfSubscriptions() {
            $.ajax({
                url: "${createLink(controller: 'admin', action: 'countBioSecurityQuery')}" ,
                type: "GET",
                success: function(response) {
                    $("span#numOfSubscriptions").text(response.count);
                },
                error: function(xhr, status, error) {
                    console.error(error);
                }
            });
        }

        function deleteSubscription(queryId) {
            let url = "${request.contextPath}/admin/deleteQuery?queryid=" + queryId;
            $.ajax({
                url: url,
                type: 'GET',
                success: function (data) {
                    $("div#subscription_" + queryId).remove();
                    updateTotalNumberOfSubscriptions();
                },
                error: function (xhr, status, error) {
                    // Handle errors
                    console.error(xhr.responseText);
                    alert("Delete failed");
                }
            });
        }

        function unsubscribe( queryId, userId, email) {
            if (!confirm("Are you sure you want to remove this email?")) {
                return; // Exit the function if the user cancels
            }

            let url  ="${request.contextPath}/ws/unsubscribeBiosecurity?queryid="+queryId+"&userid=" + userId + "&useremail="+email
            $.ajax({
                url: url,
                type: 'GET',
                xhrFields: {
                    withCredentials: true
                },
                success: function(data) {
                    if (data.status === 0) {
                        getSubscribers(queryId);
                    } else {
                        alert("Unsubscribe failed. " + data.message);
                    }
                },
                error: function(xhr, status, error) {
                    // Handle errors
                    console.error(xhr.responseText);
                    alert("Unsubscribe failed");
                }
            });
        }

        //get subscribers
        function getSubscribers(queryId) {
            let url = "${request.contextPath}/ws/getBiosecuritySubscribers?queryId=" + queryId;
            $.ajax({
                url: url,
                type: 'GET',
                success: function(data) {
                   $("div#subscribers_"+queryId).replaceWith(data);
                },
                error: function(xhr, status, error) {
                    // Handle errors
                    console.error(xhr.responseText);
                    alert("Get subscribers failed");
                }
            });
        }

        function triggerSubscriptions() {
            const userInput = prompt(`This action will trigger all alerts. Type ‘yes’ if you understand and wish to proceed. `);
            if (userInput && userInput.toLowerCase() === 'yes') {
                let url = "${request.contextPath}/ws/triggerBiosecurityAlerts"
                $.ajax({
                    url: url,
                    type: 'GET',
                    success: function (data) {
                        data.forEach(function (item) {
                            let status = item[0];
                            let message = item[1];

                            if (status === 0) {
                                console.info(message);
                            } else {
                                console.error(message);
                            }
                        });
                    },
                    error: function (xhr, status, error) {
                        // Handle errors
                        console.error(xhr.responseText);
                    }
                });

                alert("Subscriptions have been triggered. Monitor the console logs for progress updates.")
            }
        }

        // Initialize popovers again after any records are loaded by Ajax
        function initializePopoverAgain() {
            $('[data-toggle="popover"]').popover({
                html: true,
                container: 'body'
            });
        }

        function addSubscribers(button) {
            let form = button.closest('form')
            let action = "${request.contextPath}/ws/addSubscribers";

            //Using Ajax to keep the current page
            var formData = new FormData(form); // Get form data
            var queryId = formData.get("queryid")
            var xhr = new XMLHttpRequest(); // Create new XHR object
            xhr.open("POST", action, true); // Open POST request
            xhr.setRequestHeader("Accept", "application/json");
            xhr.withCredentials = true;
            xhr.onreadystatechange = function() {
                if (xhr.readyState === XMLHttpRequest.DONE) {
                    if (xhr.status === 200) {
                        var result = JSON.parse(xhr.responseText);
                        if (result['status'] === 0) {
                            getSubscribers(queryId)
                            form["useremails"].value = "";
                        } else {
                            alert("Error: " + result['message']);
                        }
                    } else {
                        // Error handling if needed
                        console.error("Error in subscribing:", xhr.status);
                        alert("Failed to subscribe. Please check the logs for more details.")
                    }
                }
            };
            xhr.send(formData);
        }


        $(document).ready(function(){
            $('input#searchSubscriptions').typeahead({
                minLength: 3,
                displayText: function(item) {
                    return item.name;
                },
                source: function(query, process) {
                    // Fetch options from another URL based on the input value
                    $.ajax({
                        url: '${request.contextPath}/ws/searchBiosecuritySubscriptions?q=' + query, // Replace with your URL
                        dataType: 'json',
                        success: function(data) {
                            process(data);
                        },
                        error: function(xhr, status, error) {
                            if (xhr.status == 401) {
                                alert.error('Authentication expired. Please login again.');
                            } else {
                                console.error('Failed to find queries. Please refresh pages and try again.');
                            }
                            console.error('Failed to fetch options:', error);
                        }
                    });
                },
                afterSelect: function(item) {
                    if(item.id) {
                        loadSubscription(item.id);
                    }
                }
            });

            /**
             * Used by autocomplete to load subscription details
             * @param id
             */
            function loadSubscription(id) {
                let url = "${createLink(controller: 'admin', action: 'getBioSecurityQuery')}" + "?id=" + id;
                $.ajax({
                    url: url,
                    type: "GET",
                    success: function(response) {
                        $("div#biosecurityDetails").html(response);
                        //hide 'load more' button
                        $("button.more-button").hide();
                        initializePopoverAgain();
                    },
                    error: function(xhr, status, error) {
                        console.error(error);
                    }
                })
            }

            $('#resetSubscriptionSearch').click(function() {
                $('#searchSubscriptions').val('');
                $("div#biosecurityDetails").html("");
                loadMore(0);
            })

            //Init popup on page load
            $('[data-toggle="popover"]').popover({
                html: true,
                container: 'body'
            })

            $("#scheduleBtn").click(function () {
                const pauseDate  = $("form[name='pauseResumeForm']").find("input[name='pauseDate']").val();
                const resumeDate = $("form[name='pauseResumeForm']").find("input[name='resumeDate']").val();

                const pauseUtcDate = new Date(pauseDate+"T00:00:00").toISOString();
                const resumeUtcDate = new Date(resumeDate+"T00:00:00").toISOString();
                $.ajax({
                    url: "${createLink(controller: 'biosecurityAdmin', action: 'pauseResumeAlerts')}",
                    type: "POST",
                    dataType: "json",
                    data: {
                        pauseDate: pauseUtcDate,
                        resumeDate: resumeUtcDate
                    },
                    success: function (response) {
                        if (!response.error) {
                            const pauseLocal  = formatUtcToLocal(response.pause);
                            const resumeLocal = formatUtcToLocal(response.resume);

                            if (pauseLocal && resumeLocal) {
                                // Both exist → cleaner phrasing
                                msg =
                                    "<b>Alerts will be paused on " + pauseLocal +
                                    " and resume on " + resumeLocal + "</b>";
                            }
                            else if (pauseLocal) {
                                msg = "<b>Alerts will be paused on " + pauseLocal + "</b>";
                            }
                            else if (resumeLocal) {
                                msg = "<b>Alerts will resume on " + resumeLocal + "</b>";
                            }
                            $("[name='pauseWindowInfo']").html("<div class='font-warning'>"+msg+"</div>")
                        } else {
                            $("[name='pauseWindowInfo']").html("")
                        }
                    },
                    error: function (xhr) {
                        $("[name='pauseWindowInfo']").html("")
                    }
                });
            });

            function loadPauseWindowInfo() {
                $.ajax({
                    url: "${createLink(controller: 'biosecurityAdmin', action: 'getAlertsPauseWindow')}",
                    type: "GET",
                    dataType: "json",
                    success: function (response) {
                        let msg = "";

                        const pauseLocal  = formatUtcToLocal(response.pause);
                        const resumeLocal = formatUtcToLocal(response.resume);
                        if (pauseLocal && resumeLocal) {
                            msg =
                                "<b>Alerts will be paused on " + pauseLocal +
                                " and resume on " + resumeLocal + "</b>";
                        }
                        else if (pauseLocal) {
                            msg = "<b>Alerts will be paused on " + pauseLocal + "</b>";
                        }
                        else if (resumeLocal) {
                            msg = "<b>Alerts will resume on " + resumeLocal + "</b>";
                        }
                        $("[name='pauseWindowInfo']").html("<div class='font-warning'>"+msg+"</div>")
                    },
                    error: function () {
                        $("[name='pauseWindowInfo']").html("");
                    }
                })
            }

            loadPauseWindowInfo();

            document.getElementById("showScheduleBtn").addEventListener("click", function(e) {
                e.preventDefault();
                let div = document.querySelector('div[name="rescheduleBiosecurity"]')
                div.style.display = "block";
                const btn = this;
                btn.style.display = "none";
            });

        })

        document.addEventListener('DOMContentLoaded', () => {
            const tz = Intl.DateTimeFormat().resolvedOptions().timeZone;
            // Set the hidden field to the browser's timezone
            document.getElementById('localTimeZone').value = tz;

            document.querySelectorAll('.ISODateTime').forEach(el => {
                const dt = new Date(el.dataset.time); // JS Date interprets ISO as UTC
                el.textContent = dt.toLocaleString(undefined, {
                    timeZone: tz,
                    weekday: 'short',
                    year: 'numeric',
                    month: 'short',
                    day: '2-digit',
                    hour: '2-digit',
                    minute: '2-digit'
                });
            });
        });

    </script>

</head>

<body>
<div id="content">
    <header id="page-header">
        <div class="inner row text-center">
            <h1><g:message code="biosecurity.view.header" default="Manage Biosecurity Alerts"/></h1>
        </div>
        <g:if test="${flash.message}">
            <div id="errorAlert" class="alert alert-danger alert-dismissible alert-dismissable" role="alert">
                <button type="button" class="close" onclick="$(this).parent().hide()" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4>${flash.message}</h4>
            </div>
        </g:if>
    </header>

    <div id="page-body" class="col-sm-12">
        <g:set var="today" value="${new java.text.SimpleDateFormat('yyyy-MM-dd').format(new Date())}"/>
        <div  class="row"  style="text-align: right">
            <div name="scheduleStatusInfo"  class="col-sm-10">
                <g:if test="${!jobStatus}">
                    <span style="color: red; font-weight: bold;">
                        No job is scheduled
                    </span>
                </g:if>

                <g:elseif test="${jobStatus.state == 'NORMAL'}">
                    <span style="color: green; font-weight: bold;">
                        Next run will be on <alerts:ISODateTime date="${jobStatus.nextFireTime}" />
                    </span>
                </g:elseif>

                <g:else>
                    <span style="color: red; font-weight: bold;">
                        Warning: Alerts are ${jobStatus.state}
                    </span>
                </g:else>
            </div>
            <div  class="col-sm-2">
                <button type="button" id="showScheduleBtn" class="btn btn-primary-outline">Schedule Manager</button>
            </div>
        </div>
        <p></p>

        <div class="well" name="rescheduleBiosecurity" style="display:none;">
            <div class="text-center"><h3>Alerts schedule manager</h3></div>
            <div class="row mt-20" >
                <div class="col-sm-12"><h4>Pause or resume now</h4></div>
                <div class="col-sm-12">Pause or resume alerts scheduling immediately. &nbsp;
                    <g:link controller="biosecurityAdmin" action="pauseAlerts" class="btn btn-primary" >
                        Pause now
                    </g:link> &nbsp;
                    <g:link controller="biosecurityAdmin" action="resumeAlerts" class="btn btn-primary-outline" >
                        Resume now
                    </g:link>
                </div>
            </div>
            <div class="mt-20"></div>
            <g:form name="pauseResumeForm" controller="admin" action="pauseResumeBioSecurityAlerts" method="post">
                <div><h4>Schedule a pause</h4></div>
                <div class="row" >
                    <div class="col-sm-12" >Set a date range to pause alerts. Dates start at midnight AEDT/AEST. You can save one scheduled pause at a time.</div>
                    <div class="col-sm-12 mt-20">Pause from <input type="date" name="pauseDate" value="${today}" />
                        Resume on  <input type="date" name="resumeDate" value="${today}" />
                        &nbsp;&nbsp;
                        <button type="button" id="scheduleBtn" class="btn btn-primary">Save schedule</button>
                        &nbsp;
                        <g:link controller="biosecurityAdmin" action="cancelScheduledPauseResumeJob" class="btn btn-primary-outline" >
                            Cancel scheduled pause
                        </g:link>
                    </div>
                    <div class="col-sm-12 mt-20" name="pauseWindowInfo" ></div>
                </div>
            </g:form>

            <div class="row mt-30">
                <div class="col-sm-12">
                    <h4>Schedule weekly biosecurity job</h4>
                    <p>You can change the weekday and time this job runs. This updates the weekly schedule only.</p>
                </div>

                <g:form controller="biosecurityAdmin" action="updateWeeklySchedule" method="POST">
                    <input type="hidden" name="localTimeZone" id="localTimeZone" />
                <div class="col-sm-2 mt-20">
                    <label for="weekday">Run on</label>
                    <select id="weekday" name="weekday" class="form-control">
                        <option value="MONDAY">Monday</option>
                        <option value="TUESDAY">Tuesday</option>
                        <option value="WEDNESDAY">Wednesday</option>
                        <option value="THURSDAY">Thursday</option>
                        <option value="FRIDAY">Friday</option>
                        <option value="SATURDAY">Saturday</option>
                        <option value="SUNDAY">Sunday</option>
                    </select>
                </div>

                <!-- Time selector -->
                <div class="col-sm-2 mt-20">
                    <label for="time">At time</label>
                    <input type="time" id="time" name="time" class="form-control" value="11:00">
                </div>

                <!-- Save button -->
                <div class="col-sm-3 mt-20">
                    <label>&nbsp;</label>
                    <button type="submit" id="saveWeeklyScheduleBtn" class="btn btn-primary form-control">
                        Update Weekly Schedule
                    </button>
                </div>
            </g:form>
            </div>


        </div>

        <div class="jumbotron jumbotron-fluid">
            <div class="container">
                <p class="lead">Quick entry for adding subscribers</p>
                <g:form name="create-security-alert" action="subscribeBioSecurity" method="post" class="form-horizontal">
                    <div class="row" >
                        <div class="col-sm-3">
                            <label class="control-label"><g:message code="biosecurity.view.body.label.specieslistid" default="Species list uid"/></label>
                            <input type="text" name="listid" class="form-control" placeholder='Species list ID, AKA drid'/>
                        </div>

                        <div class="col-sm-7">
                            <label for="useremails" class="control-label"><g:message code="biosecurity.view.body.label.useremails" default="User emails"/></label>
                            <input type="text" id="useremails" name="useremails" class="form-control" placeholder="<g:message code="biosecurity.view.body.label.useremailsallowmultiple" default="You can input multiple user emails by separating them with ';'"/>"/>
                        </div>

                        <div class="col-sm-2" style="text-align: right; ">
                            <label for="quick-submit"  style="visibility: hidden;">control</label>
                            <button type="submit" id="quick-submit" form="create-security-alert" class="btn btn-primary"><g:message code="biosecurity.view.body.button.subscribe" default="Subscribe"/></button>
                        </div>

                    </div>
                </g:form>

                <p></p>
                <div class="row" style="text-align: right">
                    <div class="col-sm-10" >
                        Search for new records of all subscriptions and notify to subscribers
                    </div>
                    <div class="col-sm-2" >
                        <button class="btn btn-primary-outline" onclick="triggerSubscriptions()">Check & Notify </button>
                    </div>
                </div>
                <p></p>
                <div class="row" style="text-align: right">
                    <div class="col-sm-10" >Download CSV list of all occurrences from all biosecurity alerts sent (scheduled and manual)</div>
                    <div class="col-sm-2" >
                        <a class="btn btn-primary-outline" href="${createLink( namespace: 'biosecurity',
                                controller: 'csv', action: 'list')}" target="_blank">CSV Reporting</a>
                    </div>
                </div>
                <p></p>

%{--                <div>
                <g:if test="${queries}">
                    <form target="_blank" action="${request.contextPath}/admin/csvAllBiosecurity" method="post">
                        <div class="row" style="text-align: right">
                            <div class="col-sm-10" >
                                Download CSV list of all occurrences from all alerts since: <input type="date" class="form" name="date" value="${today}"/>
                            </div>
                            <div class="col-sm-2">
                                <button type="submit" class="btn  btn-info">Download CSV</button>
                            </div>
                        </div>
                    </form>
                </g:if>
                </div>--}%

            </div>
        </div>

        <g:if test="${queries}">
            <div>
                <div style="display: flex; justify-content: space-between">
                    <div ><p class="lead">There are <span id="numOfSubscriptions">${total}</span> active alert(s)</p></div>
                    <div class="col-md-6 row" >
                            <div class="col-md-8">
                                <input type="text" class="form-control" placeholder="Search by query name" id="searchSubscriptions" />
                            </div>
                            <div class="col-auto">
                                <button type="button" id="resetSubscriptionSearch" class="btn btn-primary-outline">Reset</button>
                            </div>
                    </div>
                </div>
                <div id="biosecurityDetails" class="bioscecrurity-padding" >
                    <div class="row">
                        <div class="col-md-4"><b><g:message code="biosecurity.view.body.table.header.queryname" default="Subscription"/></b></div>
                        <div class="col-md-5"><b>Subscribers</b></div>
                        <div class="col-md-3"><b>Advanced Usage</b></div>
                    </div>
                    <g:render template="bioSecuritySubscriptions" model="[queries: queries, startIdx: 0 ]"/>
                </div>
                <g:if test="${ total > subscriptionsPerPage}">
                    <div>
                        <button  class="more-button" onclick="loadMore()">Show More</button>
                    </div>
                </g:if>
            </div>
        </g:if>
    </div>
</div>
</body>
</html>
