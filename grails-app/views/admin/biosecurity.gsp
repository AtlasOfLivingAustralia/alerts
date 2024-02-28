<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="au.org.ala.alerts.Notification" %>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <meta name="breadcrumb" content="BioSecurity alerts"/>
    <meta name="breadcrumbParent" content="${request.contextPath}/admin,Alerts admin"/>

    <title>Admin - Manage BioSecurity alerts</title>
    <asset:stylesheet href="alerts.css"/>
    <script>
        var subscriptionsPerLoad = ${subscriptionsPerPage?:10};
        var nextSubscription = 0;

        function submitPreview(action, button, newPage) {
            let form = button.closest('form')
            form.action = action;
            if (newPage) {
                form.target = '_blank';
                form.submit();
            } else {
                //Using Ajax to keep the current page
                var formData = new FormData(form); // Get form data
                var xhr = new XMLHttpRequest(); // Create new XHR object
                xhr.open("POST", action, true); // Open POST request
                xhr.onreadystatechange = function() {
                    if (xhr.readyState === XMLHttpRequest.DONE) {
                        if (xhr.status === 200) {
                            // Request successful, do something if needed
                            console.log("Form submitted successfully.");
                        } else {
                            // Error handling if needed
                            console.error("Error submitting form:", xhr.status);
                        }
                    }
                };
                xhr.send(formData);
                alert ("Email is sent.")
            }

        }

        function loadMore() {
            // Make an AJAX request to fetch more records
            nextSubscription += subscriptionsPerLoad;
            let url = "${createLink(controller: 'admin', action: 'getMoreBioSecurityQuery')}" + "?startIdx=" + nextSubscription;
            $.ajax({
                url: url,
                type: "GET",
                success: function(response) {
                    // Append new records to the container
                    $("div#biosecurityDetails").append(response);

                    // Hide the button if there are no more records to fetch
                    $.ajax({
                        url: "${createLink(controller: 'admin', action: 'countBioSecurityQuery')}" ,
                        type: "GET",
                        success: function(response) {

                            if (nextSubscription+subscriptionsPerLoad >= response.count) {
                                $("button.more-button").hide();
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

        function addSubscribers(button) {
            let form = button.closest('form')
            let action = "${request.contextPath}/admin/addSubscribers";

            //Using Ajax to keep the current page
            var formData = new FormData(form); // Get form data
            var queryId = formData.get("queryid")
            var xhr = new XMLHttpRequest(); // Create new XHR object
            xhr.open("POST", action, true); // Open POST request
            xhr.setRequestHeader("Accept", "application/json");
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
                    }
                }
            };
            xhr.send(formData);
        }

        function deleteSubscription(queryId) {
            let url = "${request.contextPath}/admin/deleteQuery?queryid=" + queryId;
            $.ajax({
                url: url,
                type: 'GET',
                success: function (data) {
                    $("div[name='subscription_" + queryId + "']").remove();
                },
                error: function (xhr, status, error) {
                    // Handle errors
                    console.error(xhr.responseText);
                    alert("Delete failed");
                }
            });
        }

        function unsubscribe( queryId, email) {
            let url  ="${request.contextPath}/admin/unsubscribe?queryid="+queryId+"&useremail="+email
            $.ajax({
                url: url,
                type: 'GET',
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
            let url = "${request.contextPath}/admin/getSubscribers?queryId=" + queryId;
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

    </script>
</head>

<body>
<div id="content">
    <header id="page-header">
        <div class="inner row">
            <div class="col-sm-6 col-xs-12">
                <h1><g:message code="biosecurity.view.header" default="Manage Biosecurity Alerts"/></h1>
            </div>
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
        <g:form name="create-security-alert" action="subscribeBioSecurity" method="post" class="form-horizontal">
            <div class="row">
                <div class="col-lg-6 col-sm-6 col-xs-12">
                    <div class="form-group">
                        <label for="listid" class="control-label"><g:message code="biosecurity.view.body.label.specieslistid" default="Species list uid"/></label>
                        <input type="text" id="listid" name="listid" class="form-control"/>
                    </div>

                    <div class="form-group">
                        <label for="useremails" class="control-label"><g:message code="biosecurity.view.body.label.useremails" default="User emails"/></label>
                        <input type="text" id="useremails" name="useremails" class="form-control" placeholder="<g:message code="biosecurity.view.body.label.useremailsallowmultiple" default="You can input multiple user emails by separating them with ';'"/>"/>
                    </div>
                </div>
            </div>

            <div class="form-group">
                <button type="submit" form="create-security-alert" class="btn btn-primary"><g:message code="biosecurity.view.body.button.subscribe" default="Subscribe alert"/></button>
            </div>
        </g:form>
        <g:if test="${queries}">
            <div>
                <hr>
                Get CSV list for all occurrences in all alerts
                <form target="_blank" action="${request.contextPath}/admin/csvAllBiosecurity" method="post">
                    Date: <input type="date" name="date" value="${date}"/>
                    <button type="submit" class="btn">CSV</button>
                </form>
                <hr>
            </div>
            <div>

                <div class="text-center"><h3>There are ${total} subscription(s)</h3></div>
                <div id="biosecurityDetails" class="bioscecrurity-padding" >
                    <div class="row">
                        <div class="col-md-4"><b><g:message code="biosecurity.view.body.table.header.queryname" default="Subscription"/></b></div>
                        <div class="col-md-5"><b>Subscribers</b></div>
                        <div class="col-md-3"><b>Action</b></div>
                    </div>
                    <g:render template="bioSecuritySubscriptions" model="[queries: queries,  subscribers: subscribers, startIdx: 0 ]"/>
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
