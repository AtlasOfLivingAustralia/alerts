<%@ page expressionCodec="none" %>
%{--
    Shared alerts management panel.

    Rendered by:
      - /notification/myAlerts       (a user managing their own alerts)
      - /admin/userAlerts            (an admin managing another user's alerts)

    Model:
      user                    - the User whose alerts are shown/managed (the "target" user)
      isMyOwnAlerts           - true when the target user is the logged-in user
      enabledStandardQueries  - List<Query>
      disabledStandardQueries - List<Query>
      enabledCustomQueries    - List<Query>
      disabledCustomQueries   - List<Query>
      frequencies             - List<Frequency>

    Every action posted from this panel carries the target user's id, so the controller
    always knows which user to act on (see NotificationController#getUser).
--}%
<g:set var="targetUserId" value="${user?.userId}"/>
<g:set var="biosecurityAlerts" value="${((enabledCustomQueries ?: []) + (disabledCustomQueries ?: [])).findAll { it?.biosecurity }}"/>

<div id="page-body" role="main" class="mt-3">
    <div class="mt-2">
        <i class="fas fa-envelope"></i>
        <g:if test="${isMyOwnAlerts}"><g:message code="my.alerts.notifications.sent.own"/></g:if>
        <g:else><g:message code="my.alerts.notifications.sent.other"/></g:else>
        <b>${user.email}</b>
    </div>

    <div class="mt-3">
        <i class="fas fa-clock"></i> <g:message code="my.alerts.notification.frequency"/>
        <g:select name="userFrequency" from="${frequencies}" id="userFrequency" value="${user?.frequency?.name}"
                  optionKey="name"
                  optionValue="${ { name -> g.message(code: 'frequency.' + name) } }"/> &nbsp;<g:message code="my.alerts.frequency.applies.to.all"/>
    </div>
</div>

<!-- Main Content starts -->
<div class="row mt-4">
    <div class="col-12">
        <div class="nav nav-tabs" id="alertTabs" role="tablist">
            <button class="nav-link active" id="standard-alerts-tab" data-bs-toggle="tab" data-bs-target="#standard-alerts" role="tab" aria-controls="standard-alerts"><g:message code="my.alerts.tab.standard"/></button>
            <button class="nav-link" id="custom-alerts-tab" data-bs-toggle="tab" data-bs-target="#custom-alerts" role="tab" aria-controls="custom-alerts"><g:message code="my.alerts.tab.custom"/></button>
            <g:if test="${biosecurityAlerts.size() > 0}">
                <button class="nav-link" id="biosecurity-alerts-tab" data-bs-toggle="tab" data-bs-target="#biosecurity-alerts" role="tab" aria-controls="biosecurity-alerts"><g:message code="my.alerts.tab.biosecurity"/></button>
            </g:if>
        </div>

        <!-- Tabs Content -->
        <div class="tab-content" id="alertTabsContent">

            <!-- Standard Alerts Tab -->
            <div class="tab-pane fade active show" id="standard-alerts" role="tabpanel" aria-labelledby="standard-alerts-tab">
                <div class="row">
                    <div class="col-12 col-lg-7">
                        <div class="pt-1">
                            <i><g:if test="${isMyOwnAlerts}"><g:message code="my.alerts.standard.enable.own"/></g:if><g:else><g:message code="my.alerts.standard.enable.other"/></g:else></i>
                        </div>

                        <div class="list-group mt-2">
                            <g:each in="${enabledStandardQueries}" status="i" var="query">
                                <div class="list-group-item border-top-0 border-start-0 border-end-0 d-flex justify-content-between align-items-center px-0 py-2">
                                    <div class="flex-grow-1 me-2">
                                        <h5>${query.name}</h5>
                                        <p class="mb-0">${query.description}</p>
                                    </div>
                                    <div class="pe-1">
                                        <div class="form-check form-switch">
                                            <input class="form-check-input" type="checkbox" role="switch" id="${query.id}" name="${query.name.replace(" ", "_")}" data-type="${query.emailTemplate == '/email/myAnnotations' ? 'myannotation' : ''}" checked style="transform: scale(1.4);"/>
                                        </div>
                                    </div>
                                </div>
                            </g:each>

                            <g:each in="${disabledStandardQueries}" status="i" var="query">
                                <div class="list-group-item border-top-0 border-start-0 border-end-0 d-flex justify-content-between align-items-center px-0 py-2">
                                    <div class="flex-grow-1 me-2">
                                        <h5>${query.name}</h5>
                                        <p class="mb-0">${query.description}</p>
                                    </div>
                                    <div class="pe-1">
                                        <div class="form-check form-switch">
                                            <input class="form-check-input" type="checkbox" role="switch" id="${query.id}" name="${query.name.replace(" ", "_")}" data-type="${query.emailTemplate == '/email/myAnnotations' ? 'myannotation' : ''}" style="transform: scale(1.4);"/>
                                        </div>
                                    </div>
                                </div>
                            </g:each>
                        </div>

                        <div class="mt-2">
                            <g:if test="${isMyOwnAlerts}">
                                <g:link controller="unsubscribe" action="index" class="btn btn-outline-primary"><g:message code="my.alerts.disable.all"/></g:link>
                            </g:if>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Custom Alerts Tab -->
            <div class="tab-pane fade" id="custom-alerts" role="tabpanel" aria-labelledby="custom-alerts-tab">
                <div class="row">
                    <div class="col-12 col-lg-7">
                        <div class="pt-1">
                            <i><g:if test="${isMyOwnAlerts}"><g:message code="my.alerts.custom.enable.own"/></g:if><g:else><g:message code="my.alerts.custom.enable.other"/></g:else></i>
                        </div>

                        <g:set var="enabledNonBiosecurity" value="${(enabledCustomQueries ?: []).findAll { !it?.biosecurity }}"/>
                        <g:set var="disabledNonBiosecurity" value="${(disabledCustomQueries ?: []).findAll { !it?.biosecurity }}"/>

                        <g:if test="${enabledNonBiosecurity.size() > 0 || disabledNonBiosecurity.size() > 0}">
                            <div class="list-group">
                                <g:each in="${enabledNonBiosecurity}" status="i" var="query">
                                    <div id="custom-${query.id}" class="list-group-item border-top-0 border-start-0 border-end-0 d-flex justify-content-between align-items-center px-0 py-2">
                                        <div class="flex-grow-1 me-2">
                                            <h5>${query.name}</h5>
                                            <p class="mb-0">${query.description}</p>
                                        </div>
                                        <div class="form-check form-switch">
                                            <input class="form-check-input" type="checkbox" role="switch" id="${query.id}" checked style="transform: scale(1.4);"/>
                                        </div>
                                        <div class="ps-1">
                                            <i class="fa fa-trash deleteButton text-primary cursor-pointer" aria-hidden="true" id="${query.id}"></i>
                                        </div>
                                    </div>
                                </g:each>
                                <g:each in="${disabledNonBiosecurity}" status="i" var="query">
                                    <div id="custom-${query.id}" class="list-group-item border-top-0 border-start-0 border-end-0 d-flex justify-content-between align-items-center px-0 py-2">
                                        <div class="flex-grow-1 me-2">
                                            <h5>${query.name}</h5>
                                            <p class="mb-0">${query.description}</p>
                                        </div>
                                        <div class="form-check form-switch">
                                            <input class="form-check-input" type="checkbox" role="switch" id="${query.id}" style="transform: scale(1.4);"/>
                                        </div>
                                        <div class="ps-1">
                                            <i class="fa fa-trash deleteButton text-primary cursor-pointer" aria-hidden="true" id="${query.id}"></i>
                                        </div>
                                    </div>
                                </g:each>
                            </div>
                        </g:if>
                        <g:else>
                            <div class="card card-body mt-1">
                                <g:if test="${isMyOwnAlerts}">
                                    <p><g:message code="my.alerts.custom.none.own"/></p>
                                </g:if>
                                <g:else>
                                    <p><g:message code="my.alerts.custom.none.other"/></p>
                                </g:else>
                                <p><g:message code="my.alerts.custom.description"/></p>
                            </div>
                        </g:else>
                    </div>

                    <div class="col-12 col-lg-5">
                        <div class="card card-body mt-1">
                            <p><g:message code="my.alerts.custom.where" args="[grailsApplication.config.skin.orgNameShort]"/></p>
                            <ul>
                                <li><g:message code="my.alerts.data.resource.desc" args="[grailsApplication.config.collection.searchURL, grailsApplication.config.collection.searchTitle]"/></li>
                                <li><g:message code="my.alerts.species.desc" args="[grailsApplication.config.speciesPages.searchURL, grailsApplication.config.speciesPages.searchTitle]"/></li>
                                <li><g:message code="my.alerts.region.desc" args="[grailsApplication.config.regions.searchURL, grailsApplication.config.regions.searchTitle]"/></li>
                                <li><g:message code="my.alerts.new.record.desc" args="[grailsApplication.config.occurrence.searchURL, grailsApplication.config.occurrence.searchTitle]"/></li>
                            </ul>
                            <p><g:message code="my.alerts.look.for.alerts.btn"/></p>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Biosecurity Alerts Tab -->
            <g:if test="${biosecurityAlerts.size() > 0}">
                <div class="tab-pane fade" id="biosecurity-alerts" role="tabpanel" aria-labelledby="biosecurity-alerts-tab">
                    <div class="row">
                        <div class="col-12 col-lg-7">
                            <div class="pt-1">
                                <i><g:if test="${isMyOwnAlerts}"><g:message code="my.alerts.biosecurity.enable.own"/></g:if><g:else><g:message code="my.alerts.biosecurity.enable.other"/></g:else></i>
                            </div>
                            <g:set var="enabledBiosecurityAlerts" value="${(enabledCustomQueries ?: []).findAll { it?.biosecurity }}"/>
                            <g:set var="disabledBiosecurityAlerts" value="${(disabledCustomQueries ?: []).findAll { it?.biosecurity }}"/>
                            <div class="list-group mt-2">
                                <g:each in="${enabledBiosecurityAlerts}" status="i" var="query">
                                    <div id="custom-${query.id}" class="list-group-item border-top-0 border-start-0 border-end-0 d-flex justify-content-between align-items-center px-0 py-2">
                                        <div class="flex-grow-1 me-2">
                                            <h5>${query.name}</h5>
                                            <p class="mb-0">${query.description}</p>
                                        </div>
                                        <div class="form-check form-switch">
                                            <input class="form-check-input" type="checkbox" role="switch" id="${query.id}" checked style="transform: scale(1.4);"/>
                                        </div>
                                    </div>
                                </g:each>
                                <g:each in="${disabledBiosecurityAlerts}" status="i" var="query">
                                    <div id="custom-${query.id}" class="list-group-item border-top-0 border-start-0 border-end-0 d-flex justify-content-between align-items-center px-0 py-2">
                                        <div class="flex-grow-1 me-2">
                                            <h5>${query.name}</h5>
                                            <p class="mb-0">${query.description}</p>
                                        </div>
                                        <div class="form-check form-switch">
                                            <input class="form-check-input" type="checkbox" role="switch" id="${query.id}" style="transform: scale(1.4);"/>
                                        </div>
                                    </div>
                                </g:each>
                            </div>
                        </div>
                    </div>
                </div>
            </g:if>
        </div>
    </div>
</div>
<!-- end main content -->

<asset:javascript src="alerts.js"/>
<script type="text/javascript">
    // Absolute links: this panel is also rendered from /admin/user/$userId, so relative
    // URLs would resolve against the wrong path.
    var addMyAlertUrl = '${createLink(controller: 'notification', action: 'addMyAlert')}/';
    var deleteMyAlertUrl = '${createLink(controller: 'notification', action: 'deleteMyAlert')}/';
    var deleteMyAlertWRUrl = '${createLink(controller: 'notification', action: 'deleteMyAlertWR')}/';

    var enableMyAlertUrl = '${createLink(controller: 'notification', action: 'enableAlert')}/';
    var disableMyAlertUrl = '${createLink(controller: 'notification', action: 'disableAlert')}/';

    var subscribeMyAnnotationUrl = '${createLink(controller: 'notification', action: 'subscribeMyAnnotation')}';
    var unsubscribeMyAnnotationUrl = '${createLink(controller: 'notification', action: 'unsubscribeMyAnnotation')}';

    var changeFrequencyUrl = '${createLink(controller: 'notification', action: 'changeFrequency')}';

    // The user being managed - may differ from the logged-in user when an admin is managing someone else
    var targetUserId = '${targetUserId}';

    $(document).ready(function () {

        $("#userFrequency").change(function () {
            $.get(changeFrequencyUrl + '?userId=' + encodeURIComponent(targetUserId) + '&frequency=' + $('#userFrequency').val())
                .fail(function () {
                    alert('<g:message code="my.alerts.problem.retry" />');
                });
        });

        $('.deleteButton').click(function () {
            var id = $(this).attr('id');
            $.get(deleteMyAlertWRUrl + id + '?userId=' + encodeURIComponent(targetUserId));
            $('#custom-' + id).hide('slow', function () {
                $('#custom-' + id).remove();
            });
        });

        $(".form-switch input[type='checkbox']").on("change", function () {
            var state = $(this).is(":checked"); // true = on, false = off
            var url;

            if ($(this).data("type") === "myannotation") {
                url = (state ? subscribeMyAnnotationUrl : unsubscribeMyAnnotationUrl) + "?userId=" + encodeURIComponent(targetUserId);
            } else {
                url = (state ? enableMyAlertUrl : disableMyAlertUrl) + this.id + "?userId=" + encodeURIComponent(targetUserId);
            }

            $.get(url)
                .done(function (resp) {
                    console.log("Success", resp);
                })
                .fail(function (err) {
                    alert('<g:message code="my.alerts.operation.failed" encodeAs="JavaScript"/>');
                    console.error("Error", err);
                });
        });
    });

    // Show the correct tab based on the URL hash
    $(window).on('load', function () {
        var hash = window.location.hash;
        if (hash) {
            var tabEl = document.querySelector('button.nav-link[data-bs-target="' + hash + '"]');
            if (tabEl) {
                new bootstrap.Tab(tabEl).show();
            }
        }
    });
</script>

