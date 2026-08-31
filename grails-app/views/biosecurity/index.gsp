<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="au.org.ala.alerts.Notification" %>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <meta name="breadcrumb" content="BioSecurity alerts"/>
    <meta name="breadcrumbParent" content="${request.contextPath}/notification/myAlerts, My Alerts"/>
    <asset:javascript src="alpinejs.3.16.3.min.js" defer="defer"/>
    <asset:stylesheet href="alerts.css"/>
    <style>
        .pagination {
            list-style: none !important;
            padding-left: 0 !important;
        }
        .pagination li {
            display: inline-block !important;
        }
        [x-cloak] {
            display: none !important;
        }
    </style>
    <title>Manage BioSecurity alerts</title>
</head>
<body>
<div class="container">
    <g:render template="/biosecurity/schedule" model="[jobStatus: jobStatus ]"/>
</div>
<div class="container mt-4">
    <div class="alerts-panel" x-data="AlertList">
        <!-- Error message -->
        <div x-show.important="failed && !loading" class="alert alert-danger" x-cloak>
            Failed to load alerts: <span x-text="failedReason"></span>.
            <button class="btn btn-sm btn-outline-danger ms-2" @click="loadAlerts()">Try again</button>
        </div>
        <!-- Loading -->
        <div x-show.important="loading && alerts.length === 0" class="text-muted">Loading alerts...</div>
        <!-- No alerts -->
        <div x-show.important="!loading && alerts.length === 0 && !failed" class="alert alert-info " x-cloak>
            You don't have any alerts.
        </div>

        <div class="card card-body mt-20">
            <div class="container-fluid">
                <h4>Quick entry for adding subscribers</h4>
                    <div class="row align-items-center mb-2" >
                        <div class="col-sm-3">
                            <label class="form-label"> <g:message code="biosecurity.view.body.label.specieslistid" default="Species list uid"/></label>
                            <input type="text" name="listid" class="form-control" x-model="newQuery.listId" placeholder='Species list ID, AKA drid'/>
                        </div>

                        <div class="col-sm-7">
                            <label for="useremails" class="form-label"><g:message code="biosecurity.view.body.label.useremails" default="User emails"/></label>
                            <input type="text"  name="useremails" class="form-control" x-model="newQuery.emails" placeholder="<g:message code="biosecurity.view.body.label.useremailsallowmultiple" default="You can input multiple user emails by separating them with ';'"/>"/>
                        </div>

                        <div class="col-sm-2 mt-10 text-end" >
                            <label class="form-label invisible" >control</label>
                            <button type="submit" id="quick-submit" class="btn btn-primary" @click="newSubscription()" :disabled="newQuery.isProcessing"><g:message code="biosecurity.view.body.button.subscribe" default="Subscribe"/></button>
                        </div>

                    </div>
            </div>
        </div>

        <!-- Alert list -->
        <div class="row ">
            <!-- Pagination -->
            <div x-show.important="totalPages > 1 && !isSearching" class="mt-4 d-flex col-md-8 align-items-center mb-3" x-cloak>
                <nav>
                    <ul class="pagination mb-0">
                        <li class="page-item" :class="{ disabled: currentPage === 0 }">
                            <a class="page-link" href="#" @click.prevent="prevPage()">Previous</a>
                        </li>
                        <template x-for="page in totalPages" :key="page">
                            <li class="page-item" :class="{ active: currentPage === page - 1 }">
                                <a class="page-link" href="#" @click.prevent="goToPage(page - 1)" x-text="page"></a>
                            </li>
                        </template>
                        <li class="page-item" :class="{ disabled: currentPage === totalPages - 1 }">
                            <a class="page-link" href="#" @click.prevent="nextPage()">Next</a>
                        </li>
                    </ul>
                </nav>
                <div class="text-muted small ms-3">
                    Showing <span x-text="alerts.length > 0 ? currentPage * pageSize + 1 : 0"></span> - <span x-text="Math.min((currentPage + 1) * pageSize, total)"></span> of <span x-text="total"></span>
                </div>
            </div>        <!--Pagination ends -->
            <div class="mt-4 mb-3 d-flex col-md-4 align-items-right justify-content-end gap-2" >
                <input type="text" class="form-control" placeholder="Type 3+ characters to search by name or list ID" x-model="searchKeyword"  @input="search()" />
                <button type="button" class="btn btn-outline-primary" :disabled="!isSearching" @click="resetSearch()" >Reset</button>
            </div>
        </div>

        <template x-for="query in alerts" :key="query.id">
            <div class="card mb-3">
                <div class="card-body border" :class="activeId === query.id ? 'border-primary' : 'border-light'" >
                    <div class="row">
                        <div class="col-md-4">
                            <div>
                                    <span x-show.important="editingId !== query.id" >
                                    <a :href="'${createLink(controller: 'query', action: 'show')}/' + query.id"  target="_blank" class="btn btn-link text-wrap text-start p-0" x-text="query.name"></a>
                                    <span class="badge-outline-secondary"><a :href="'${grailsApplication.config.lists.baseURL}' + '/speciesListItem/list/' + query.listId" target="_blank" x-text="query.listId"></a></span>
                                    <button class="btn btn-link btn-sm p-0 ms-1" title="Edit title" @click="editTitle(query.id)">
                                        <i class="fa-solid fa-pencil"></i>
                                    </button>
                                </span>
                                <div x-show.important="editingId === query.id" class="gap-2" x-cloak>
                                    <textarea class="form-control form-control-sm" rows="3" x-model="editingTitle" placeholder="Enter new title"></textarea>
                                    <br>
                                    <button class="btn btn-sm btn-primary" @click="updateTitle(query.id)">Save</button>
                                    <button class="btn btn-sm btn-outline-primary" @click="cancelEditTitle()">Cancel</button>
                                </div>
                                <div x-show.important="query.lastChecked" class="mt-1" >
                                    <span class="text-muted">Last checked:</span>
                                    <span class="link-primary" @click="query.showLog = !query.showLog"  style="cursor: pointer;" x-text="formatDate(query.lastChecked)"></span>
                                    <div class="text-muted mt-1 fst-italic">Missed the last check? Would you like to <a href="#" @click.prevent="run(query.id)">check now</a>?</div>
                                </div>
                                <div x-show.important="!query.lastChecked" class="text-muted mt-1">
                                    This is the first time subscribing to this list. Please navigate to the right section to set the initial check date and click the <button class="btn btn-outline-primary" type="button">Notify</button> button.
                                Otherwise, the check date will default to 7 days before the scheduled task's execution date.
                                </div>
                                <div x-show.important="query.showLog" class="text-muted mt-1 " style="white-space: pre-line;" x-text="query.log" x-cloak></div>
                            </div>
                        </div>
                        <div class="col-md-5">
                            <template x-for="subscriber in query.subscribers" :key="subscriber.id">
                                <span class="badge border rounded  text-primary me-1" :class="subscriber.isActive ? 'border-primary' : 'badge-outline-secondary bg-light'">
                                    <span :class="subscriber.isActive ? '' : 'text-decoration-line-through text-muted'" x-text="subscriber.email"></span>
                                    <i @click="unsubscribe(query.id, subscriber.id, subscriber.email)" class="fa fa-trash clickable"></i>
                                </span>
                            </template>
                            <button x-show.important="query.subscribers.length === 0" class="btn btn-primary" @click="deleteSubscription(query.id)">Delete this subscription</button>

                            <div class="mt-2">
                                <input class="form-control"  x-model="query.newSubscribers"
                                       placeholder="You can input multiple user emails by separating them with ';'"/>
                                <button type="button" class="btn btn-primary mt-2"  :disabled="!query.newSubscribers || query.newSubscribers.trim().length === 0"
                                        @click="addSubscribers(query.id, query.newSubscribers)">Add</button>
                            </div>
                        </div>
                        <div class="col-md-3">
                            <label >Check alerts since</label>
                            <input type="date" name="date" x-model="previewDate" class="form-control" /><br/>
                            <button class="btn btn-primary" name="previewSubscription" type="button" @click="submitPreview(query.id)" >Preview</button>
                            <button class="btn btn-primary" name="triggerAlertSince" type="button" @click="triggerAlertSince(query.id)">Notify</button>
                            <a href="#" class="ms-2" @click.prevent="query.showHelp = !query.showHelp"><i class="fas fa-question-circle"></i> Help</a>
                            <div x-show.important="query.showHelp" x-cloak>
                                <small class="form-text text-muted d-block mt-1">
                                    The '<span class="text-primary fw-bold">Preview</span>' button is primarily for administrators to verify that a query runs correctly.It does <span class="text-primary fw-bold">NOT</span> update the last execution date, send emails, or regenerate a CSV.
                                </small>
                                <hr/>
                                <small class="form-text text-muted d-block">
                                    The '<span class="text-primary fw-bold">Notify</span>' button should only be used if the server unexpectedly goes down during a scheduled run or other unexpected failures, requiring the task to be triggered manually.<br><span class="text-primary fw-bold">It will send emails and generate a corresponding CSV file</span>. Otherwise, this button should not be used.
                                </small>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </template>
        <!-- Pagination -->
        <div x-show.important="totalPages > 1 && !isSearching" class="mt-4 d-flex col-md-12 align-items-center" x-cloak>
            <nav>
                <ul class="pagination mb-0">
                    <li class="page-item" :class="{ disabled: currentPage === 0 }">
                        <a class="page-link" href="#" @click.prevent="prevPage()">Previous</a>
                    </li>
                    <template x-for="page in totalPages" :key="page">
                        <li class="page-item" :class="{ active: currentPage === page - 1 }">
                            <a class="page-link" href="#" @click.prevent="goToPage(page - 1)" x-text="page"></a>
                        </li>
                    </template>
                    <li class="page-item" :class="{ disabled: currentPage === totalPages - 1 }">
                        <a class="page-link" href="#" @click.prevent="nextPage()">Next</a>
                    </li>
                </ul>
            </nav>
            <div class="text-muted small ms-3">
                Showing <span x-text="alerts.length > 0 ? currentPage * pageSize + 1 : 0"></span> - <span x-text="Math.min((currentPage + 1) * pageSize, total)"></span> of <span x-text="total"></span>
            </div>
        </div>
    </div>
</div>
<script>
    // Resolved once, server-side, by GSP. GSP evaluates dollar-brace expressions everywhere in
    // this file, including inside <script> and even JS comments, so build URLs by concatenation.
    const CONTEXT_PATH = '${request.contextPath}';
    const PAGE_SIZE = ${grailsApplication.config.getProperty('biosecurity.subscriptionsPerPage', Integer, 10)};

    document.addEventListener('alpine:init', () => {
        Alpine.data('AlertList', () => ({
            alerts: [],
            total: 0,
            loading: true,
            failed: false,
            failedReason: '',
            currentPage: 0,
            pageSize: PAGE_SIZE,
            editingId: null,
            editingTitle: '',
            //record the active alert which is being edited
            activeId: null,
            //searching function
            searchKeyword: '',
            isSearching: false, // true if valid results are returned, mainly used for hide/show pagination
            //add a new subscription
            newQuery: {
                isProcessing: false,
                listId: '',
                emails: ''
            },
            //others
            previewDate: new Date().toISOString().split('T')[0],

            init() {
                this.loadAlerts();
            },

            async loadAlerts(page = 0) {
                this.currentPage = page;
                this.loading = true;
                this.failed = false;
                try {
                    const offset = page * this.pageSize;
                    const response = await fetch(CONTEXT_PATH + '/biosecurity/list?offset=' + offset + '&max=' + this.pageSize);
                    if (!response.ok) {
                        throw new Error( response.status + ' ' + response.statusText);
                    }
                    var result = await response.json();
                    // Seed newSubscriber on every alert so the "add subscriber" input is bound to a
                    // defined value. Without it x-model reads undefined and the box can show "undefined".
                    this.alerts = (result.alerts || []).map(alert => ({ ...alert, newSubscribers: '', showLog: false, showHelp: false }));
                    this.total = result.total;
                } catch (e) {
                    console.error(e);
                    this.failed = true;
                    this.failedReason = e.message;
                } finally {
                    this.loading = false;
                }
            },
            get totalPages() {
                return Math.ceil(this.total / this.pageSize);
            },
            goToPage(page) {
                if (page >= 0 && page < this.totalPages) {
                    this.loadAlerts(page);
                }
            },
            nextPage() {
                if (this.currentPage < this.totalPages - 1) {
                    this.loadAlerts(this.currentPage + 1);
                }
            },
            prevPage() {
                if (this.currentPage > 0) {
                    this.loadAlerts(this.currentPage - 1);
                }
            },
            // =========================
            // Refresh
            // =========================
            async refreshAlerts() {
                await this.loadAlerts();
            },

            async newSubscription() {
                if (!this.newQuery.listId || !this.newQuery.emails) {
                    window.alert('List ID and emails cannot be empty');
                    return;
                }
                try {
                    this.newQuery.isProcessing = true;
                    const response = await fetch(CONTEXT_PATH + '/biosecurity/subscription', {
                        method: 'POST',
                        body: new URLSearchParams({ listId: this.newQuery.listId, emails: this.newQuery.emails })
                    });
                    if (!response.ok) {
                        throw new Error('Failed to add new subscription');
                    } else {
                        var result = await response.json();
                        if (result.success && result.alert) {
                            let theAlert = {...result.alert, newSubscribers:"", showHelp:false, showLog:false,active:true};
                            const index = this.alerts.findIndex(a => a.id === result.alert.id);
                            if (index !== -1) {
                                this.alerts.splice(index, 1);
                            }
                            this.alerts.unshift(theAlert);
                            this.newQuery = { isProcessing: true, listId: '', emails: '' };
                            this.activeId = result.alert.id;
                            if(result.invalidEmails.length > 0 ) {
                                window.alert("Users were added to this alert, but the following invalid email addresses were ignored: " + result.invalidEmails.join(", "));
                            }
                        } else {
                            throw new Error(result.message || 'Failed to add new subscription');
                        }
                    }
                } catch(e) {
                    console.error(e);
                    window.alert('Failed! ' + e.message);
                } finally {
                    this.newQuery.isProcessing = false;
                }
            },
            // =========================
            // Enable / Disable
            // =========================
            async unsubscribe(queryId, userId, userEmail) {
                try {
                    const response = await fetch(CONTEXT_PATH + '/biosecurity/unsubscribe?queryId='
                        + queryId + '&userId=' + userId + '&userEmail=' + encodeURIComponent(userEmail), {
                        method: 'POST'
                    });
                    if (!response.ok) {
                        throw new Error('Failed to remove subscriber from this alert');
                    } else {
                        var result = await response.json();
                        if (result.success) {
                            this.activeId = queryId;
                            // Remove the subscriber from the local alert's subscriber list
                            const alert = this.alerts.find(a => a.id === queryId);
                            if (alert) {
                                alert.subscribers = alert.subscribers.filter(s => s.id !== userId);
                            }
                        } else {
                            throw new Error( result.message);
                        }
                    }
                } catch (e) {
                    console.error(e);
                    // window.alert, because the 'alert' parameter shadows the global here
                    window.alert('Failed! ' + e.message);
                }
            },

            async addSubscribers(queryId, userEmails) {
                try {
                    const response = await fetch(CONTEXT_PATH + '/biosecurity/subscribe?queryId='
                        + queryId + '&userEmails=' + encodeURIComponent(userEmails.trim()), {
                        method: 'POST'
                    });
                    if (!response.ok) {
                        throw new Error('Failed to add subscriber to this alert');
                    } else {
                        let result = await response.json();
                        if (result.success ) {
                            // Add the subscriber to the local alert's subscriber list
                            this.activeId = queryId;
                            const alert = this.alerts.find(a => a.id === queryId);
                            var subscribersResponse = await fetch(CONTEXT_PATH + '/biosecurity/subscribers.json?queryId=' + queryId);
                            var subscribers = await subscribersResponse.json();
                            if (alert) {
                                alert.subscribers = subscribers.subscribers;
                                alert.newSubscribers = '';
                            }
                        } else {
                            throw new Error( result.message);
                        }
                    }
                } catch (e) {
                    console.error(e);
                    // window.alert, because the 'alert' parameter shadows the global here
                    window.alert('Failed! ' + e.message);
                }
            },

            // =========================
            // Delete
            // =========================
            async  deleteSubscription(queryId) {
                const alert = this.alerts.find(a => a.id === queryId);
                if (!alert) {
                    return;
                }
                if (!confirm('Delete "' + alert.name + '"?')) {
                    return;
                }
                try {
                    const response = await fetch(CONTEXT_PATH + '/biosecurity/subscription/' + alert.id +'.json', {
                        method: 'DELETE'
                    });
                    if (!response.ok) {
                        throw new Error('Failed to delete alert');
                    } else {
                        let result = await response.json();
                        if (result.success) {
                            this.alerts = this.alerts.filter(item => item.id !== alert.id);
                        } else {
                            throw new Error(result.message || 'Failed to delete alert');
                        }
                    }
                } catch (e) {
                    console.error(e);
                    // window.alert, because the 'alert' parameter shadows the global here
                    window.alert('Failed to delete alert.');
                }
            },

            // =========================
            // Edit Title
            // =========================
            editTitle(queryId) {
                const alert = this.alerts.find(a => a.id === queryId);
                if (alert) {
                    this.editingId = queryId;
                    this.editingTitle = alert.name;
                }
            },
            cancelEditTitle() {
                this.editingId = null;
                this.editingTitle = '';
            },

            async updateTitle(queryId) {
                if (!this.editingTitle || this.editingTitle.trim() === '') {
                    window.alert('Title cannot be empty');
                    return;
                }
                try {
                    const response = await fetch(CONTEXT_PATH + '/query/updateTitle?id=' + queryId
                        + '&name=' + encodeURIComponent(this.editingTitle.trim()), {
                        method: 'POST'
                    });
                    if (!response.ok) {
                        throw new Error('Failed to update title');
                    }
                    const result = await response.json();
                    if (result.success) {
                        this.activeId = queryId;
                        const alert = this.alerts.find(a => a.id === queryId);
                        if (alert) {
                            alert.name = this.editingTitle;
                        }
                        this.editingId = null;
                        this.editingTitle = '';
                    } else {
                        throw new Error(result.message || 'Failed to update title');
                    }
                } catch (e) {
                    console.error(e);
                    window.alert('Failed! ' + e.message);
                }
            },

            async search() {
                if (this.searchKeyword && this.searchKeyword.length >= 3) {
                    const response = await fetch(CONTEXT_PATH + '/biosecurity/search?q=' + encodeURIComponent(this.searchKeyword));
                    if (response.ok) {
                        const result = await response.json();
                        if (result.length > 0) {
                            this.alerts = result.map(alert => ({ ...alert, showLog: false, showHelp: false,newSubscribers:'' }));
                            this.isSearching = true;
                        }
                    } else {
                       console.error('Failed to search keywords' + response.status + ' ' + response.statusText);
                    }
                } else if ( this.isSearching ) {
                    //It means it was searching before, but now the keyword is less than 3 characters, so reset the search
                    this.resetSearch();
                    this.isSearching = false;
                }
            },

            async resetSearch() {
                this.searchKeyword = '';
                this.isSearching = false;
                this.refreshAlerts();
            },

            async submitPreview(queryId) {
                  this.activeId = queryId;
                  var localDate = new Date(this.previewDate);
                  var utcDate = localDate.toISOString();
                  window.open(CONTEXT_PATH + '/biosecurity/preview?id=' + queryId + '&date=' + utcDate, '_blank')
            },

            async run(queryId) {
                let yes = confirm("This will run the alert immediately and send emails to all subscribers. Are you sure you want to proceed?");
                if (yes) {
                    this.activeId = queryId;
                    let url = CONTEXT_PATH + "/biosecurity/trigger/" + queryId;
                    const response = await fetch(url);
                    if (response.ok) {
                        const result = await response.json();
                        if (result.success) {
                            let query = this.alerts.find(a => a.id === queryId);
                            const resp = await fetch(CONTEXT_PATH + "/biosecurity/subscription/" + queryId+".json");
                            const respJson = await resp.json();
                            query = respJson.alert;
                            window.alert("The alerts has been executed successfully.");
                        } else {
                            window.alert("Failed to trigger alert: " + result.message);
                        }
                    } else {
                        window.alert("Failed to trigger alert: " + response.status + ' ' + response.statusText);
                    }
                }
            },

            async triggerAlertSince(queryId) {
                var localDate = new Date(this.previewDate);
                var utcDate = localDate.toISOString();
                var yes = confirm("It will also update the last check date to the current time. Are you sure you want to proceed?");

                if (yes) {
                    this.activeId = queryId;
                    let url = CONTEXT_PATH + "/biosecurity/triggerAlertSince?id=" + queryId + "&since=" + utcDate;
                    const response = await fetch(url, {
                        method: 'POST'
                    });
                    if (response.ok) {
                        const result = await response.json();
                        if (result.success) {
                            window.alert("Notification triggered successfully.");
                            let query = this.alerts.find(a => a.id === queryId);
                            query.lastChecked = utcDate;
                            query.logs = result.logs.join("\n");
                        } else {
                            window.alert("Failed to trigger notification: " + result.message);
                        }
                    } else {
                        window.alert("Failed to trigger notification: " + response.status + ' ' + response.statusText);
                    }
                }
            },

            // Formatting
            formatText(text) {
                return text.replace(/\n/g, '<br>');
            },
            formatDate(date) {
                if (!date) {
                    return '';
                }
                return new Date(date).toLocaleString();
            }
        }));
    });

</script>
</body>
</html>