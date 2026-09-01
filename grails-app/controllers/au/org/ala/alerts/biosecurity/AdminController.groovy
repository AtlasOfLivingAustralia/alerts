package au.org.ala.alerts.biosecurity

import au.org.ala.alerts.Frequency
import au.org.ala.alerts.Query
import au.org.ala.alerts.QueryResult
import au.org.ala.alerts.User
import au.org.ala.web.AlaSecured
import grails.converters.JSON
import grails.util.Holders
import org.apache.commons.lang3.time.DateUtils
import org.springframework.http.HttpStatus

import java.text.SimpleDateFormat

class AdminController {
    static namespace = "biosecurity"

    /** Upper bound on the page size a caller may request from list(). */
    private static final int MAX_PAGE_SIZE = 500

    def queryService
    def userService
    def notificationService
    def biosecurityService
    def diffService
    def authService
    def messageSource
    def utilService
    def siteLocale = new Locale.Builder().setLanguageTag(Holders.config.siteDefaultLanguage as String).build()
    def subscriptionsPerPage = grailsApplication.config.getProperty('biosecurity.subscriptionsPerPage', Integer, 100)

    /**
     * Renders the biosecurity admin page, which shows the current job status and a list of subscriptions.
     * Subscriptions are paginated and rendered via Alpine Vue.js (a JavaScript framework)
     * @return
     */
    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true, redirectController = 'notification', redirectAction = 'myAlerts', message = "You don't have permission to view that page.")
    def index() {
        render(view: "/biosecurity/index")
    }

    /**
     * Paginated JSON list of biosecurity subscriptions. Renders JSON only - no view.
     *
     * @param offset zero-based index of the first record to return. Default 0.
     * @param max    maximum number of records to return. Defaults to the configured
     *               biosecurity.subscriptionsPerPage and is capped at MAX_PAGE_SIZE.
     * @return JSON of the form
     *         {"offset":0,"max":100,"total":253,"count":100,"items":[{"id":..,"name":..},..]}
     */
    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true)
    def list() {
        // params.int() returns null for absent or non-numeric values, so bad input falls back to the default
        int offset = params.int('offset') ?: 0
        int max = params.int('max') ?: subscriptionsPerPage

        // Clamp so a caller cannot request a negative page or pull the whole table in one go
        offset = Math.max(offset, 0)
        max = Math.min(Math.max(max, 1), MAX_PAGE_SIZE)

        int total = queryService.countBiosecurityQuery()
        // Skip the query entirely when the caller has paged past the end
        List<Query> queries = offset >= total ? [] : queryService.getBiosecurityQuery(offset, max)
        def alerts = queries.collect { queryToAlertMap(it) }
        render([offset: offset, max: max, total: total, count: alerts.size(), alerts: alerts] as JSON)
    }



    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true)
    def search() {
        List<Query> queries =  queryService.searchBiosecuritySubscriptions(params.q)
        def alerts = queries.collect { queryToAlertMap(it) }
        render alerts as JSON
    }

    def get(int id) {
        def query = Query.get(id)
        if (query) {
            render queryToAlertMap(query) as JSON
        } else {
            render(status: HttpStatus.NOT_FOUND.value(), text: "Query not found")
        }
    }

    /**
     * Converts a Query object to a map suitable for JSON rendering, including its subscribers and logs.
     * @param query
     * @return
     */
    private queryToAlertMap(Query query) {
        def activeSubscribers = queryService.getSubscribers(query.id).collect { User user ->
            [id: user.id, email: user.email, isActive: true]
        }
        def inactiveSubscribers = queryService.getInactiveSubscribers(query.id).collect { User user ->
            [id: user.id, email: user.email, isActive: false]
        }
        String log = query.getLogs("weekly")?.join("\n") ?: "No logs available"
        [
                id         : query.id,
                name       : query.name,
                listId     : query.listId,
                lastChecked: utilService.formatUtc(query.lastChecked),
                subscribers: activeSubscribers + inactiveSubscribers,
                log        : log
        ]
    }

    /**
     * partial view to display the list of subscribers for a given query. This includes both active and inactive subscribers.
     *
     * @param queryId
     * @return a partial view rendering the list of subscribers
     */
    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true)
    def getSubscribers() {
        def subscribers = queryService.getSubscribers(Long.valueOf(params.queryId))
        def inactiveSubscribers = queryService.getInactiveSubscribers(Long.valueOf(params.queryId))

        // Combine both lists into a single list of maps with id, email, and enabled status
        def combinedSubscribers = []
        subscribers.each { user ->
            combinedSubscribers.add([id: user.id, email: user.email, isActive: true])
        }
        inactiveSubscribers.each { user ->
            combinedSubscribers.add([id: user.id, email: user.email, isActive: false])
        }
        render([subscribers: combinedSubscribers] as JSON)
    }

    /**
     * API call
     *
     * Subscribe users/emails to a biosecurity query
     * @return
     */
    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true)
    def addSubscribers() {
        def result = [:]
        if ((!params.listId || params.listId.allWhitespace) && !params.queryId) {
            result = [status: 1, message: messageSource.getMessage("biosecurity.view.error.emptyspeciesid", null, "Species list uid can't be empty.", siteLocale)]
        } else if (!params.userEmails || params.userEmails.allWhitespace) {
            result = [status: 1, message: messageSource.getMessage("biosecurity.view.error.emptyemails", null, "User emails can't be empty.", siteLocale)]
        } else {
            def delimiters = /[\s\|;,]/
            String[] emails = ((String)params.userEmails).split(delimiters).findAll { it?.trim() }
            Map usermap = emails?.collectEntries{[it.trim(), userService.getUserByEmailOrCreate(it.trim())]}
            def invalidEmails = []
            usermap.each {entry ->
                if (entry.value == null) {
                    invalidEmails.add(entry.key)
                } else {
                    if (params.queryId) {
                        queryService.createQueryForUserIfNotExists(Query.get(params.queryId), entry.value as User, true,true)
                    } else {
                        queryService.subscribeBioSecurity(entry.value as User, params.listId.trim())
                    }
                }
            }
            if (invalidEmails) {
                result =[success: false, message: messageSource.getMessage("biosecurity.view.error.invalidemails", [invalidEmails.join(", ")] as Object[], "Users with emails: {0} are not found in the system.", siteLocale)]
            } else {
                result = [success: true]
            }
        }
        render(result as JSON)
    }

    /**
     * Unsubscribe user from a query by an Admin
     *
     * @param userId  the sequence id of the user. If not provided, it will use the email to find the user
     * @param useremail the email of the user to unsubscribe
     * @param queryid the ID of the biosecurity query
     *
     * @return
     */
    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true)
    def unsubscribe() {
        def result = [:]
        if (!params.userEmail || params.userEmail.allWhitespace) {
            result = [status: 1, message: messageSource.getMessage("unsubscribeusers.controller.error.emptyemail", null, "User email can't be empty.", siteLocale)]
        } else if (!params.queryId || params.queryId.allWhitespace) {
            result = [status: 1, message: messageSource.getMessage("unsubscribeusers.controller.error.emptyqueryid", null, "Query Id can't be empty.", siteLocale)]
        } else {
            try {
                User user
                if (params.userId) {
                    def userId = params.userId as Long
                    user = userService.getUserBySequenceId(userId);
                } else {
                    //todo - identify why duplicate users are occasionally created
                    user = userService.getUserByEmail(params.userEmail.trim())
                }

                if (user) {
                    notificationService.deleteAlertForUser(user, Long.valueOf(params.queryId))
                    result = [success : true]
                } else {
                    result = [success : false, message: messageSource.getMessage('unsubscribeusers.controller.error.emailnotfound', [params.useremail] as Object[], "User with email: {0} are not found in the system.", siteLocale)]
                }
            } catch (Exception e) {
                log.error("Error getting user : ${params.userId}", e)
                result = [success : false, message: "Error getting user : ${params.userId}"]
            }
        }
        render(result as JSON)
    }

    /**
     * Deletes a biosecurity query along with all of its subscriptions.
     *
     * Responds with JSON when the caller wants it - a '.json' URL suffix, a JSON Accept header, or
     * an XHR (AJAX) request - so the page can update in place. Plain browser navigation still gets
     * the redirect back to the biosecurity admin page.
     */
    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true)
    def delete(long id) {
        def result = [:]
        if (id) {
            // wipe() returns [status: 0 (success) | 1 (error), message: '...']
            def resp = queryService.wipe(id)
            boolean succeeded = resp?.status == 0
            result = [success: succeeded, message: resp?.message]
        } else {
            result=[success: false, message: "Missing or invalid query ID"]
        }
        render(result as JSON)
    }


    /**
     * run the given alerts since last check.
     * checks for new records since the last check, and sends alert emails to subscribers
     * @param id the ID of the biosecurity query to trigger
     * @return JSON indicating success or failure, and a message
     */
    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true)
    def trigger(int id) {
        def query = Query.get(id)
        if (query) {
            Date lastChecked = queryService.getLastCheckedDate(query)
            if (lastChecked == null) {
                lastChecked = DateUtils.addDays(new Date(), -7 )
            }

            def result = biosecurityService.triggerBiosecuritySubscription(query, lastChecked)
            //legacy code for backward compatibility, we used to return status 1 for success, now we return success: true/false
            result.success = (result.status == 0)
            result.remove('status')
            render(result as JSON)
        } else {
            render([success: false, message: "Query not found"] as JSON)
        }
    }
    /**
     *
     * It searches the records of given query back from the given date
     * And it also set the last checked date to the given date
     *
     * For example, if we set date = 2023-05-01, it will return the records from 2023-05-01 to now, and set the lastCheck date to 2023-05-01
     *
     * @param id
     * @param since  The date is from the JS calendar, it only has CURRENT Date part, no time part
     * @return
     */
    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true)
    def triggerAlertSince (int id) {
        String localDateString = params.since
        def query = Query.get(id)
        if (!query) {
            render([success: false, message: "Query not found"] as JSON)
            return
        }

        Date since
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd")
            sdf.lenient = false
            since = sdf.parse(localDateString)
        } catch (Exception ignored) {
            render([success: false, message: "A valid 'since' date (yyyy-MM-dd) is required"] as JSON)
            return
        }

        def result = biosecurityService.triggerBiosecuritySubscription(query, since)
        //legacy code for backward compatibility, we used to return status 1 for success, now we return success: true/false
        result.success = (result.status == 0)
        result.remove('status')

        render(result as JSON)
    }

    /**
     * This function is used to subscribe a user to a species list or a query (an existing subscription of a list)
     * @return
     */
    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true)
    def create() {
        if ((!params.listId || params.listId.allWhitespace) && !params.queryId) {
            flash.message = messageSource.getMessage("biosecurity.view.error.emptyspeciesid", null, "Species list uid can't be empty.", siteLocale)
            render([success: false, message: flash.message] as JSON)
        } else if (!params.emails || params.emails.allWhitespace) {
            flash.message = messageSource.getMessage("biosecurity.view.error.emptyemails", null, "User emails can't be empty.", siteLocale)
            render([success: false, message: flash.message] as JSON)
        } else {
            //If params contains listId, it is for subscribing to a species list
            if (params.listId) {
                boolean queryExists = queryService.speciesListExists(params.listId.trim())
                if (!queryExists) {
                    flash.message = messageSource.getMessage("biosecurity.view.error.invalidlisid", [params.listId.trim()] as Object[], "List with id: {0} is not found in the system.", siteLocale)
                    render([success: false, message: flash.message] as JSON)
                    return
                }
            }

            String[] emails = ((String)params.emails).split(/[;,\s]+/)
            Map usermap = emails?.collectEntries{[it.trim(), userService.getUserByEmailOrCreate(it.trim())]}
            def invalidEmails = []
            Query updatedQuery = null
            usermap.each {entry ->
                if (entry.value == null) {
                    invalidEmails.add(entry.key)
                } else {
                    if (params.queryId) {
                        updatedQuery = queryService.addUserToQuery(Query.get(params.queryId), entry.value as User, true)
                    } else {
                        updatedQuery = queryService.subscribeBioSecurity(entry.value as User, params.listId.trim())
                    }
                }
            }
            if (invalidEmails) {
                String message = messageSource.getMessage("biosecurity.view.error.invalidemails", [invalidEmails.join(", ")] as Object[], "Users with emails: {0} are not found or invalid in the system.", siteLocale)
                log.warn(message)
            }
            if (updatedQuery) {
                def alert = queryToAlertMap(updatedQuery)
                render([success: true, message: flash.message, alert: alert, invalidEmails: invalidEmails] as JSON)
            } else {
                String message = messageSource.getMessage("biosecurity.view.error.subscriptionfailed", null, "Subscription failed.", siteLocale)
                log.error(message)
                render([success: false, message: message] as JSON)
            }
        }
    }

    /**
     * It is a preview page for BioSecurity alert
     * DO NOT update database in this function
     * @return
     */
    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true)
    def preview(Long id) {
        log.info("Building preview page for BioSecurity alert")
        def date = params.date //only from preview
        def query = null
        Query.withTransaction {
            query = Query.get(id)
        }
        if (!query) {
            log.error("Query: ${id} not exists")
            render(text: "Query: ${id} not exists", contentType: "text/plain", encoding: "UTF-8")
            return
        }

        Date since
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd")
            sdf.lenient = false
            since = sdf.parse(date)
        } catch (Exception ignored) {
            log.error("Invalid 'date' parameter for preview: ${date}")
            render(text: "A valid 'date' (yyyy-MM-dd) is required, got: ${date}", contentType: "text/plain", encoding: "UTF-8")
            return
        }

        Date now = new Date()
        try {
            def processedJson = biosecurityService.processQueryBiosecurity(query, since, now)

            def frequency = 'weekly'
            QueryResult qr = notificationService.getQueryResult(query, Frequency.findByName(frequency))
            qr.lastResult = qr.compress(processedJson)
            //this logic only applies on preview page
            qr.previousCheck = qr.lastChecked
            qr.lastChecked = since
            query.lastChecked = since
            def records = diffService.diff(qr)
            biosecurityService.fetchExtraOccurrenceInfo(records)

            String urlPrefix = "${grailsApplication.config.getProperty("grails.serverURL")}${grailsApplication.config.getProperty('security.cas.contextPath', '')}"
            def localeSubject = messageSource.getMessage("emailservice.update.subject", [query.name] as Object[], siteLocale)

            //Get unsubscribe token
            def unsubscribeOneUrl

            def alaUser = authService.userDetails()
            def user = User.findByEmail(alaUser?.email)
            def unsubscribeToken = notificationService.getUnsubscribeToken(user, query)
            if (user && unsubscribeToken) {
                unsubscribeOneUrl = urlPrefix + "/unsubscribe?token=${unsubscribeToken}"
            }
            int maxRecords = grailsApplication.config.getProperty("biosecurity.query.maxRecords", Integer, 500)
            render(view: query.emailTemplate,
//                plugin: "email-confirmation",
                    model: [title           : localeSubject,
                            message         : query.updateMessage,
                            query           : query,
                            moreInfo        : qr.queryUrlUIUsed,
                            listcode        : queryService.isMyAnnotation(query) ? "biocache.view.myannotation.list" : "biocache.view.list",
                            stopNotification: urlPrefix + '/notification/myAlerts',
                            records         : records.take(maxRecords),
                            frequency       : messageSource.getMessage('frequency.' + frequency, null, siteLocale),
                            totalRecords    : records.size(),
                            unsubscribeAll  : urlPrefix + "/unsubscribe?token=test",
                            unsubscribeOne  : unsubscribeOneUrl
                    ])
        } catch (Exception e) {
            log.error("Error in previewing Biosecurity alert: ${e.message}")
            render(text: e.message, contentType: "text/plain", encoding: "UTF-8")
        }
    }

}

