/*
 * Copyright (C) 2017 Atlas of Living Australia
 * All Rights Reserved.
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 */

package au.org.ala.alerts

import au.org.ala.web.AlaSecured
import au.org.ala.ws.service.WebService
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.plugin.cache.CacheEvict
import grails.util.Environment
import grails.util.Holders

import java.text.SimpleDateFormat
import groovy.json.JsonSlurper
import java.nio.file.Files

@AlaSecured(value = 'ROLE_ADMIN', redirectController = 'notification', redirectAction = 'myAlerts', message = "You don't have permission to view that page.")
class AdminController {

    def authService
    def notificationService
    def biosecurityService
    BiosecurityJobService biosecurityJobService
    def queryResultService
    def diffService
    def emailService
    def queryService
    def userService
    def messageSource
    WebService webService
    def siteLocale = new Locale.Builder().setLanguageTag(Holders.config.siteDefaultLanguage as String).build()

    def subscriptionsPerPage = grailsApplication.config.getProperty('biosecurity.subscriptionsPerPage', Integer, 100)
    static allowedMethods = [deleteUser: 'POST']

    def index() {}


    def findUser() {
        List users = []
        if (params.term) {
            users = userService.findUsers(params.term)
        }
        render view: "/admin/userAlerts", model: [users: users]
    }


    def updateUserEmails() {
        def updated = userService.updateUserEmails()
        flash.message = "Updated ${updated} email addresses in system"
        redirect(action: 'index')
    }

    def createBulkEmail = {}

    def createBulkEmailForRegisteredUsers = {}

    def sendBulkEmailForRegisteredUsers = {
        // def users = [User.findByEmail("david.martin@csiro.au")]
        User.findAll().each { user ->
            log.info "Sending email to: " + user.email
            try {
                sendMail {
                    to user.email.toString()
                    from grailsApplication.config.mail.details.infoAddressTitle + "<" + grailsApplication.config.mail.details.infoSender + ">"
                    subject params.emailSubject
                    body(view: "/email/htmlEmail",
                            plugin: "email-confirmation",
                            model: [htmlBody: params.htmlEmailToSend]
                    )
                }
            } catch (Exception e) {
                log.error("Problem sending email to ${user.email} - ${e.message}")
            }
        }
    }

    @Transactional
    def fixupBiocacheQueries() {
        def toUpdate = []
        Query.findAllByQueryPathForUI('/occurrences/search?q=*:*&fq=first_loaded_date:' + '[___DATEPARAM___  TO *]'.encodeAsURL() + '&sort=first_loaded_date&dir=desc').each {
            it.queryPathForUI = it.queryPath.substring(3)
            toUpdate << it
        }
        toUpdate.each { it.save(flush: true) }
        toUpdate.clear()


        Query.findAllByQueryPathForUI('/occurrences/search?q=*:*&fq=user_assertions:*&fq=last_assertion_date:' + '[___DATEPARAM___  TO *]'.encodeAsURL() + '&sort=last_assertion_date&dir=desc').each {
            it.queryPathForUI = it.queryPath.substring(3)
            toUpdate << it
        }
        toUpdate.each { it.save(flush: true) }
        toUpdate.clear()


        Query.findAllByQueryPathForUI('/occurrences/search?q=*:*&fq=last_assertion_date:' + '[___DATEPARAM___  TO *]'.encodeAsURL() + '&sort=last_assertion_date&dir=desc').each {
            it.queryPathForUI = it.queryPath.substring(3)
            toUpdate << it
        }
        toUpdate.each { it.save(flush: true) }
        toUpdate.clear()
    }

    def sendBulkEmail = {
        params.emailsToUse.trim().split("\n").each { email ->
            if (email) {
                try {
                    log.info "Sending email to: " + email
                    sendMail {
                        to email.toString()
                        from grailsApplication.config.mail.details.infoAddressTitle + "<" + grailsApplication.config.mail.details.infoSender + ">"
                        subject params.emailSubject
                        body(view: "/email/htmlEmail",
                                plugin: "email-confirmation",
                                model: [htmlBody: params.htmlEmailToSend]
                        )
                    }
                } catch (Exception e) {
                    log.error("Problem sending email to ${email} -- ${e.message}")
                }
            }
        }
    }

    def notificationReport = {
        [queryInstanceList: Query.list()]
    }

    def runChecksNow = {
        log.info("Run checks....")
        if (params.frequency) {
            log.info('Manual start of ' + params.frequency + ' checks')
            notificationService.execQueryForFrequency(params.frequency)
        }
        response.setContentType("text/plain")
        response.setStatus(200)

        null
    }


    def deleteOrphanAlerts() {
        def result = queryService.deleteOrphanedQueries()
        render(view: 'index', model: [message: "Removed ${result['OrphanQuery']} queries, and ${result['OrphanNotification']} orphaned notifications."])
    }

    /**
     * Show/manage another user's alerts.
     * Renders /admin/manageUserAlerts which reuses the shared /notification/_alertsPanel template.
     */
    def showUsersAlerts() {
        User user = User.findByUserId(params.userId)
        if (user) {
            def userConfig = notificationService.getAlerts(user)
            userConfig.put('adminUser', authService.userDetails())
            userConfig.put('isMyOwnAlerts', authService.userDetails()?.userId == user.userId)

            render(view: "/admin/manageUserAlerts", model: userConfig)
        } else {
            log.info "user with id " + params.userId + " not found."
            response.sendError(404)
        }

        null
    }

    /**
     * What would be removed if this user were deleted? Used to populate the confirmation dialog on
     * the 'manage user alerts' page - it does NOT change anything.
     *
     * @param userId ALA user id
     * @return [status: 0|1, email: .., notifications: n, queries: [[id: .., name: ..], ..]]
     */
    def previewUserDeletion() {
        User user = User.findByUserId(params.userId)
        if (!user) {
            render([status: 1, message: "User not found: ${params.userId}"] as JSON)
            return
        }

        render([status       : 0,
                email        : user.email,
                notifications: Notification.countByUserAndEnabled(user,true),
                queries      : userService.findQueriesRelatedToDeletedUser(user)] as JSON)
    }

    /**
     * Delete a user, their subscriptions and any queries that exist only for them.
     *
     * @param userId ALA user id
     * @return [status: 0|1, message: '..']
     */

    def deleteUser() {
        User user = User.findByUserId(params.userId)
        if (!user) {
            render([status: 1, message: "User not found: ${params.userId}"] as JSON)
            return
        }

        if (authService.userDetails()?.userId == user.userId) {
            render([status: 1, message: "You cannot delete your own account here."] as JSON)
            return
        }

        render(userService.delete(user) as JSON)
    }

    /**
     * Add a user to the alerts database so their alerts can be managed.
     *
     * Users are only created in Alerts the first time they log in, so an admin may need to add
     * someone who has never visited the site. The account must already exist in userdetails/CAS -
     * UserService#getUserByEmailOrCreate looks it up there.
     *
     * @param term email address (or ALA user id) of the user to add
     */
    def addUser() {
        String term = params.term?.trim()
        if (!term) {
            flash.errorMessage = "Please enter an email address first."
            redirect(action: 'findUser')
            return
        }

        User user = userService.getUserByEmailOrCreate(term)
        if (!user) {
            flash.errorMessage = "No account found for '${term}'. The user must be registered with ${grailsApplication.config.skin.orgNameLong ?: 'the ALA'} before their alerts can be managed."
            redirect(action: 'findUser', params: [term: term])
            return
        }

        flash.message = "${user.email} is now available in Alerts."
        redirect(action: 'showUsersAlerts', params: [userId: user.userId])
    }

    /**
     * Appears to be deprecated for #updateUserEmails
     *
     * @return
     */
    @Transactional
    def refreshUserDetails() {
        try {
            // this is to update User table with the current ID value
            User.all.each { User user ->
                def foundUser = authService.getUserForEmailAddress(user.email)
                if (user.id != foundUser.id) {
                    user.id = foundUser.id
                    user.save(true)
                }
            }
        } catch (Exception e) {
            log.error("Cache refresh error" + e.message, e)
        }
    }


    /**
     * Utility method to fix broken unsubscribe links in email, where the unsubscribe link
     * has '?token=NULL'.
     *
     * @return
     */
    @Transactional
    def repairNotificationsWithoutUnsubscribeToken() {
        List notifications = Notification.findAllByUnsubscribeTokenIsNull()
        def count = 0

        notifications.each { Notification notification ->
            notification.unsubscribeToken = UUID.randomUUID().toString()
            notification.save(flush: true)
            count++
        }

        flash.message = "Updated ${count} notification entries with new unsubscribeToken value (was NULL)."
        redirect(action: 'index')
    }

    /**
     * Utility method to fix broken unsubscribe links in email, where the "unsubscribe all" link
     * has '?token=NULL'.
     *
     * @return
     */
    @Transactional
    def repairUsersWithoutUnsubscribeToken() {
        List users = User.findAllByUnsubscribeTokenIsNull()
        def count = 0

        users.each { User user ->
            user.unsubscribeToken = UUID.randomUUID().toString()
            user.save(flush: true)
            count++
        }

        flash.message = "Updated ${count} user entries with new unsubscribeToken value (was NULL)."
        redirect(action: 'index')
    }

    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true)
    def biosecurity() {
        int total = queryService.countBiosecurityQuery()
        List<Query> queries = queryService.getBiosecurityQuery(0, subscriptionsPerPage)
        Map jobStatus = biosecurityJobService.getJobInfo()
        render view: "/admin/biosecurity", model: [total: total, queries: queries, subscriptionsPerPage: subscriptionsPerPage, jobStatus: jobStatus]
    }

    /**
     * For Ajax call to render more biosecurity queries (subscription)
     * @param offset
     * @param limit
     * @return
     */
    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true)
    def getMoreBioSecurityQuery(int startIdx) {
        List queries = queryService.getBiosecurityQuery(startIdx, subscriptionsPerPage)
        render view: "/admin/_bioSecuritySubscriptions", model: [queries: queries, startIdx: startIdx ]
    }

    /**
     * For Ajax call to render one biosecurity queries (subscription)
     * @param id
     * @return
     */
    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true)
    def getBioSecurityQuery(int id) {
        def query = queryService.findBiosecurityQueryById(id)
       // def queryLog = queryService.getQueryLogs(query, "weekly")
        //For be compatible with the method rendering a list of queries AKA subscriptions, we need to convert the single query to a list
        render view: "/admin/_bioSecuritySubscriptions", model: [queries: [query], startIdx: 0 ]
    }

    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true)
    def countBioSecurityQuery() {
        int total = queryService.countBiosecurityQuery()
        render (contentType: 'application/json') {
            count total
        }
    }

    /**
     * This function is used to subscribe a user to a species list or a query (an existing subscription of a list)
     * @return
     */
    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true)
    def subscribeBioSecurity() {
        if ((!params.listid || params.listid.allWhitespace) && !params.queryid) {
            flash.message = messageSource.getMessage("biosecurity.view.error.emptyspeciesid", null, "Species list uid can't be empty.", siteLocale)
        } else if (!params.useremails || params.useremails.allWhitespace) {
            flash.message = messageSource.getMessage("biosecurity.view.error.emptyemails", null, "User emails can't be empty.", siteLocale)
        } else {
            //If params contains listid, it is for subscribing to a species list
            if (params.listid) {
                boolean queryExists = queryService.speciesListExists(params.listid.trim())
                if (!queryExists) {
                    flash.message = messageSource.getMessage("biosecurity.view.error.invalidListId", [params.listid.trim()] as Object[], "List with id: {0} is not found in the system.", siteLocale)
                    redirect(controller: "admin", action: "biosecurity")
                    return
                }
            }

            String[] emails = ((String)params.useremails).split(';')
            Map usermap = emails?.collectEntries{[it.trim(), userService.getUserByEmailOrCreate(it.trim())]}
            def invalidEmails = []
            usermap.each {entry ->
                if (entry.value == null) {
                    invalidEmails.add(entry.key)
                } else {
                    if (params.queryid) {
                        queryService.createQueryForUserIfNotExists(Query.get(params.queryid), entry.value as User, true)
                    } else {
                        queryService.subscribeBioSecurity(entry.value as User, params.listid.trim())
                    }
                }
            }
            if (invalidEmails) {
                flash.message = messageSource.getMessage("biosecurity.view.error.invalidemails", [invalidEmails.join(", ")] as Object[], "Users with emails: {0} are not found in the system.", siteLocale)
            }
        }
        redirect(controller: "admin", action: "biosecurity")
    }

    /**
     * It is a preview page for BioSecurity alert
     * DO NOT update database in this function
     * @return
     */
    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true)
    def previewBiosecurityAlert() {
        log.info("Building preview page for BioSecurity alert")
        def date = params.date //only from preview
        def query = null
        Query.withTransaction {
            query = Query.get(params.queryid)
        }
        if (!query) {
            log.error("Query: ${params.queryid} not exists")
            render(text: "Query: ${params.queryid} not exists", contentType: "text/plain", encoding: "UTF-8")
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd")
        Date since =  sdf.parse(date)
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

    /**
     * It is a preview page for Blog alert
     * DO NOT update database in this function
     * @return
     */
    def previewBlogAlerts() {
        String urlPrefix = "${grailsApplication.config.security.cas.appServerName}${grailsApplication.config.getProperty('security.cas.contextPath', '')}"
        Query query = Query.findByName(messageSource.getMessage("query.ala.blog.title", null,
                new Locale.Builder().setLanguageTag(Holders.config.siteDefaultLanguage as String).build()))

        def unsubscribeOneUrl = ""
        def records = []
        if (query) {
            QueryResult qs = QueryResult.findByQuery(query)
            if(qs) {
                try {
                    def url = new URL("${query.baseUrl}${query.queryPath}")
                    def connection = url.openConnection()
                    connection.setRequestProperty("Accept", "application/json")
                    def jsonSlurper = new JsonSlurper()
                    records = jsonSlurper.parse(connection.inputStream)
                } catch (Exception ex) {
                    // Handle any exceptions
                    log.error("An error fetching data from ${query.baseUrl}, Using records in database. : ${ex.message}")
                    def lastResult = diffService.decompressZipped(qs?.lastResult)
                    def jsonSlurper = new JsonSlurper()
                    records = jsonSlurper.parseText(lastResult)
                }
            }

            //Get unsubscribe token
            def alaUser = authService.userDetails()
            def user = userService.getUserByEmail(alaUser?.email)
            def unsubscribeToken = notificationService.getUnsubscribeToken(user, query)
            if (user && unsubscribeToken) {
                unsubscribeOneUrl = grailsApplication.config.grails.serverURL + "/unsubscribe?token=${unsubscribeToken}"
            }
        }

        render(view: query.emailTemplate,
//                plugin: "email-confirmation",
                model: [
                        query: query,
                        stopNotification: urlPrefix + '/notification/myAlerts',
                        records: records.take(5),
                        totalRecords: records.size(),
                        unsubscribeOne: unsubscribeOneUrl,
                ])
    }


    /**
     * @return
     */
    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true)
    def unsubscribeAllUsers() {
        queryService.unsubscribeAllUsers(Long.valueOf(params.queryid))
        redirect(controller: "admin", action: "biosecurity")
    }

    /**
      * @return
     */

    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true)
    def deleteQuery() {
        queryService.deleteQuery(Long.valueOf(params.queryid))
        redirect(controller: "admin", action: "biosecurity")
    }

//    /**
//     * todo: check if it is still used
//     * @return
//     */
//    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true)
//    def unsubscribeAlert() {
//        if (!params.useremail || params.useremail.allWhitespace) {
//            flash.message = messageSource.getMessage("unsubscribeusers.controller.error.emptyemail", null, "User email can't be empty.", siteLocale)
//        } else if (!params.queryid || params.queryid.allWhitespace) {
//            flash.message = messageSource.getMessage("unsubscribeusers.controller.error.emptyqueryid", null, "Query Id can't be empty.", siteLocale)
//        } else {
//            User user = userService.getUserByEmail(params.useremail);
//            if (user) {
//                notificationService.deleteAlertForUser(user, Long.valueOf(params.queryid))
//            } else {
//                flash.message = messageSource.getMessage('unsubscribeusers.controller.error.emailnotfound', [params.useremail] as Object[], "User with email: {0} are not found in the system.", siteLocale)
//            }
//        }
//        redirect(controller: "admin", action: "biosecurity")
//    }

    /**
     * Page for debugging and testing all queries
     * @return
     */
    def query(){
        def queries = queryService.summarize()
        render view: "/admin/query", model: [queries: queries]
    }

    /**
     * Database UPDATED, No email sent
     * Rerun the last check of a query for a given frequency without sending any notifications
     * @param queryId
     * @param frequency
     * @return
     */
    def runQueryWithLastCheckDate(){
        def id = params.queryId
        def frequency = params.frequency
        if (id && frequency) {
            Query query = Query.get(id)
            Frequency fre = Frequency.findByName(frequency)
            if (query && fre) {
                QueryResult qs = notificationService.executeQuery(query, fre, true)
                //def records = diffService.getRecordChanges(qs)
                if (qs.succeeded) {
                    def records = qs.newRecords
                    def results = ["hasChanged": qs.hasChanged, "records": records, "details": qs.brief()]
                    render results as JSON
                } else {
                    render([status: 1, message: qs.logs] as JSON)
                }
            } else {
                render([status: 1, message: "Cannot find query: ${id}"] as JSON)
            }
        } else {
            render([status: 1, message: "Missing queryId or frequency"] as JSON)
        }
    }

    /**
     * NO Database update, Email sent to current user
     * Run the last check and email the result to current user
     * NOT designed for Biosecurity queries
     * @return
     */
    def emailMeLastCheck(){
        def id = params.queryId
        def frequency = params.frequency
        if (id && frequency) {
            Query query = Query.get(id)
            if(!query.isBiosecurity()) {
                Frequency fre = Frequency.findByName(frequency)
                if (query && fre) {
                    QueryResult qs = notificationService.executeQuery(query, fre, true, true)
                    if (qs.succeeded) {
                        def records = qs.newRecords
                        User currentUser = userService.getUser()
                        def recipient =
                                [email: currentUser.email, userUnsubToken: currentUser.unsubscribeToken, notificationUnsubToken: '']
                        emailService.sendGroupNotification(qs, fre, [recipient])
                        def results = ["hasChanged": qs.hasChanged, "totalRecords": qs.totalRecords, "records": records, "recipient": currentUser.email, details: qs.brief()]
                        render results as JSON
                    } else {
                        def results = ["status": qs.succeeded, "error": qs.logs]
                        render results as JSON
                    }
                } else {
                     render([status: 1, message: "This function does not work with Biosecurity query: ${id}"] as JSON)
                }
            } else {
                render([status: 1, message: "Cannot find query: ${id}"] as JSON)
            }
        } else {
            render([status: 1, message: "Missing queryId or frequency"] as JSON)
        }
    }

    /**
     * NO Database update, Email sent to current user
     * Query on the check date and email the result to current user
     * @return
     */
    def emailAlertsOnCheckDate(){
        def id = params.queryId
        def frequency = params.frequency
        def checkDate = params.checkDate
        boolean sendToSubscribers = params.sendToSubscribers ?: false
        if (id && frequency && checkDate) {
            Query query = Query.get(id)
            Frequency fre = Frequency.findByName(frequency)

            Date date = new SimpleDateFormat("yyyy-MM-dd").parse(checkDate)
            if (query && fre) {
                QueryResult qs = notificationService.executeQuery(query, fre, false, true, date)
                if (qs.succeeded) {
                    def records = qs.newRecords
                    User currentUser = userService.getUser()
                    def recipients =
                            [[email: currentUser.email, userUnsubToken: currentUser.unsubscribeToken, notificationUnsubToken: '']]
                    if (sendToSubscribers) {
                        def subscribers = queryService.getSubscribers(id.toLong())
                        def others = subscribers.collect { subscriber ->
                            [email:subscriber.email, userUnsubToken: subscriber.unsubscribeToken, notificationUnsubToken: '']
                        }
                        recipients.addAll(others)
                    }
                    emailService.sendGroupNotification(qs, fre, recipients)
                    def results = ["hasChanged": qs.hasChanged, "records": records, "recipient": recipients*.email, details: qs.brief()]
                    render results as JSON
                } else {
                    def results = ["status": qs.succeeded, "error": qs.logs]
                    render results as JSON
                }
            } else {
                render([status: 1, message: "Cannot find query: ${id}"] as JSON)
            }
        } else {
            render([status: 1, message: "Missing queryId or frequency or check date"] as JSON)
        }
    }

    /**
     * Database updates, Email sent to current user
     *
     * Test only. Test if a QueryResult [Weekly - hard coded]can be initiated and sent to the current user
     *
     * @return
     */
    def initFirstCheckAndEmailMe(){
        def id = params.queryId
        def frequency = params.frequency
        if (id && frequency) {
            Query query = Query.get(id)
            Frequency fre = Frequency.findByName(frequency)
            if (query && fre) {
                QueryResult qs = notificationService.executeQuery(query, fre, false, false)
                if (qs.succeeded) {
                    def records = qs.newRecords
                    User currentUser = userService.getUser()
                    def recipient =
                            [email: currentUser.email, userUnsubToken: currentUser.unsubscribeToken, notificationUnsubToken: '']
                    emailService.sendGroupNotification(qs, fre, [recipient])
                    def results = ["hasChanged": qs.hasChanged, "records": records, "recipient": currentUser.email]
                    render results as JSON
                } else  {
                    render ([status: 1, message: qs.logs] as JSON)
                }

            } else {
                render([status: 1, message: "Cannot find query: ${id}"] as JSON)
            }
        } else {
            render([status: 1, message: "Missing queryId or frequency"] as JSON)
        }
    }

    /**
     * NO Database update, No emails sent
     *
     * Rerun a query for a given frequency without updating database and sending any notifications
     * NOTE: Biosecurity excluded
     *
     * @param queryId
     * @param frequency
     * @return
     */
    def dryRunQuery(){
        def id = params.queryId
        def frequency = params.frequency
        if (id && frequency) {
            Query query = Query.get(id)
            Frequency fre = Frequency.findByName(frequency)
            if (query && fre) {
                QueryResult queryResult = notificationService.executeQuery(query, fre, false, true)
                if (queryResult.succeeded) {
                    def records = queryResult.newRecords
                    def results = ["status": queryResult.succeeded, "hasChanged": queryResult.hasChanged, "logs": queryResult.getLog(), "records": records, "details": queryResult.brief()]
                    render results as JSON
                } else {
                    render([status: 1, message: queryResult.logs] as JSON)
                }
            } else {
                render([status: 1, message: "Cannot find query: ${id}"] as JSON)
            }
        } else {
            render([status: 1, message: "Missing queryId or frequency"] as JSON)
        }
    }

    /**
     * Simulating Quartz jobs
     *
     * Only send emails in Development environment
     * And NO database updates
     */
    def triggerQueriesByFrequency(String frequency, Boolean testMode) {
        def allowedFrequencies = ['hourly', 'weekly', 'daily', 'monthly']
        def jobFrequency = (frequency in allowedFrequencies) ? frequency : 'daily'
        // Set the test mode.
        // It is used to determine if a copy of email should be sent to the current user
        grailsApplication.config.testMode = testMode ?: false

        log.info("****** Simulating ${jobFrequency} jobs, Test mode: ${grailsApplication.config.testMode}, NO Database updates   ****** " + new Date())
        def logs = notificationService.execQueryForFrequency(jobFrequency, Environment.current == Environment.DEVELOPMENT, true)
        log.info("****** End ${jobFrequency} job simulation ****** " + new Date())
        render(logs.sort { [it.succeeded ? 1 : 0, it.hasChanged ? 0 : 1] } as JSON)
    }


    /**
     * Reset the previous / current results stored in QueryResult
     * @return
     */
    @AlaSecured(value = ['ROLE_ADMIN'], anyRole = true)
    def resetQueryResult() {
        try{
            def id = params.id.toInteger()
            if(id){
                queryResultService.reset(id)
                render([status: 0, message: "Query result has been reset"] as JSON)
            } else {
                render([status: 1, message: "Missing ID"] as JSON)
            }
        } catch (Exception e) {
            render([status: 1, message: "Error in resetting query result: ${e.message}"] as JSON)
        }
    }

    def sendTestEmail() {
        try {
            User currentUser = userService.getUser()
            if (currentUser) {
                String title = "Test email"
                String emailBody = "<p>This is a test email sent to ${currentUser.email} from the ALA Notification System.</p>"
                sendMail {
                    from grailsApplication.config.mail.details.alertAddressTitle + "<" + grailsApplication.config.mail.details.sender + ">"
                    subject title
                    bcc currentUser.email
                    html(emailBody)
                }
            } else {
                log.warn("No user found to send email to.")
            }

            render([status: 0, message: "Test email has been sent to ${currentUser.email}"] as JSON)
        } catch (Exception e) {
            log.error("Error in sending test email: ${e.message}", e)
            render([status: 1, message: "Error in sending test email: ${e.message}"] as JSON)
        }
    }
}
