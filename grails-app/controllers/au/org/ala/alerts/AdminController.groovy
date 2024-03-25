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
import com.nimbusds.oauth2.sdk.util.date.SimpleDate
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.util.Holders

import java.text.SimpleDateFormat
import groovyx.net.http.HTTPBuilder
import groovy.json.JsonSlurper

import java.util.regex.Pattern

@AlaSecured(value = 'ROLE_ADMIN', redirectController = 'notification', redirectAction = 'myAlerts', message = "You don't have permission to view that page.")
class AdminController {

    def authService
    def notificationService
    def diffService
    def emailService
    def queryService
    def userService
    def messageSource
    def siteLocale = new Locale.Builder().setLanguageTag(Holders.config.siteDefaultLanguage as String).build()

    def subscriptionsPerPage = grailsApplication.config.biosecurity?.subscriptionsPerPage? grailsApplication.config.biosecurity.subscriptionsPerPage.toInteger() : 10
    def index() {}

    @Transactional
    def findUser() {
        List users = []
        if (params.term) {
            users = userService.findUsers(params.term)
        }
        render view: "/admin/userAlerts", model: [users: users]
    }

    @Transactional
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
                    from grailsApplication.config.postie.emailInfoAddressTitle + "<" + grailsApplication.config.postie.emailInfoSender + ">"
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
        Query.findAllByQueryPathForUI('/occurrences/search?q=*:*&fq=first_loaded_date:[___DATEPARAM___%20TO%20*]&sort=first_loaded_date&dir=desc').each {
            it.queryPathForUI = it.queryPath.substring(3)
            toUpdate << it
        }
        toUpdate.each { it.save(flush: true) }
        toUpdate.clear()


        Query.findAllByQueryPathForUI('/occurrences/search?q=*:*&fq=user_assertions:*&fq=last_assertion_date:[___DATEPARAM___%20TO%20*]&sort=last_assertion_date&dir=desc').each {
            it.queryPathForUI = it.queryPath.substring(3)
            toUpdate << it
        }
        toUpdate.each { it.save(flush: true) }
        toUpdate.clear()


        Query.findAllByQueryPathForUI('/occurrences/search?q=*:*&fq=last_assertion_date:[___DATEPARAM___%20TO%20*]&sort=last_assertion_date&dir=desc').each {
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
                        from grailsApplication.config.postie.emailInfoAddressTitle + "<" + grailsApplication.config.postie.emailInfoSender + ">"
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
            notificationService.checkQueryForFrequency(params.frequency)
        }
        response.setContentType("text/plain")
        response.setStatus(200)

        null
    }

    @Transactional
    def debugAlertsForUser() {
        User user = User.findByUserId(params.userId)
        if (user) {
            log.debug "User id: " + user.email + ", frequency: " + user.frequency
            //trigger this users alerts
            response.setContentType("text/plain")
            notificationService.debugQueriesForUser(user, response.getWriter())
        } else {
            log.error "user with id " + params.userId + " not found."
            response.sendError(404)
        }
    }

    @Transactional
    def debugAllAlerts() {
        response.setContentType("text/plain")
        notificationService.checkAllQueries(response.getWriter())
    }

    @Transactional
    def debugAlertEmail() {
        def frequency = params.frequency ?: 'weekly'
        def qcr = notificationService.checkQueryById(params.id, params.frequency ?: 'weekly')
        def model = emailService.generateEmailModel(qcr.query, frequency, qcr.queryResult)
        render(view: qcr.query.emailTemplate, model: model)
    }

    @Transactional
    def debugAlert() {
        [alerts: [
                hourly : notificationService.checkQueryById(params.id, params.frequency ?: 'hourly'),
                daily  : notificationService.checkQueryById(params.id, params.frequency ?: 'daily'),
                weekly : notificationService.checkQueryById(params.id, params.frequency ?: 'weekly'),
                monthly: notificationService.checkQueryById(params.id, params.frequency ?: 'monthly')
        ]
        ]
    }

    @Transactional
    def deleteOrphanAlerts() {
        int noDeleted = queryService.deleteOrphanedQueries()
        render(view: 'index', model: [message: """Removed ${noDeleted} queries from system"""])
    }

    @Transactional
    def showUsersAlerts() {
        User user = User.findByUserId(params.userId)
        if (user) {
            def userConfig = userService.getUserAlertsConfig(user)
            userConfig.put('adminUser', authService.userDetails())

            render(view: "../notification/myAlerts", model: userConfig)
        } else {
            log.info "user with id " + params.userId + " not found."
            response.sendError(404)
        }

        null
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

    @Transactional
    def unsubscribeUser(String id) {}

    /**
     * Used to check if server can send emails externally.
     * Sends to email address of logged-in user
     *
     */
    @Transactional
    def sendTestEmail() {
        def msg
        User user = userService.getUser()
        if (user) {
            def query = Query.get(14)
            def frequency = Frequency.get(1)
            def queryResult = QueryResult.findByQuery(query) ?: new QueryResult(query: query, frequency: frequency)
            QueryResult qr = notificationService.getQueryResult(query, frequency)
            emailService.sendGroupEmail(query, [user.email], queryResult, [], frequency, 0, "", "", [:], [:])
            msg = "Email was sent to ${user.email} - check tomcat logs for ERROR message with value \"Error sending email to addresses:\""
        } else {
            msg = "User was not found or not logged in"
        }

        log.debug "#sendTestEmail - msg = ${msg}"
        flash.message = msg
        redirect(action: "index")
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

    def biosecurity() {
        int total = queryService.countBiosecurityQuery()
        List queries = queryService.getBiosecurityQuery(0, subscriptionsPerPage)
        List subscribers = queries.collect {queryService.getSubscribers(it.id)}
        render view: "/admin/biosecurity", model: [total: total, queries: queries, subscribers: subscribers, subscriptionsPerPage: subscriptionsPerPage]
    }

    /**
     * For Ajax call to get more biosecurity queries (subscription)
     * @param offset
     * @param limit
     * @return
     */
    def getMoreBioSecurityQuery(int startIdx) {
        List queries = queryService.getBiosecurityQuery(startIdx, subscriptionsPerPage)
        List subscribers = queries.collect {queryService.getSubscribers(it.id)}
        render view: "/admin/_bioSecuritySubscriptions", model: [queries: queries, subscribers: subscribers, startIdx: startIdx ]
    }

    def getBioSecurityQuery(int id) {
        def subscription = queryService.findBiosecurityQueryById(id)
        //For be compatible with the method rendering a list of queries AKA subscriptions, we need to convert the single query to a list
        render view: "/admin/_bioSecuritySubscriptions", model: [queries: [subscription.subscription], subscribers: [subscription.subscribers], startIdx: 0 ]
    }

    def countBioSecurityQuery() {
        int total = queryService.countBiosecurityQuery()
        render (contentType: 'application/json') {
            count total
        }
    }

    @Transactional
    def subscribeBioSecurity() {
        if ((!params.listid || params.listid.allWhitespace) && !params.queryid) {
            flash.message = messageSource.getMessage("biosecurity.view.error.emptyspeciesid", null, "Species list uid can't be empty.", siteLocale)
        } else if (!params.useremails || params.useremails.allWhitespace) {
            flash.message = messageSource.getMessage("biosecurity.view.error.emptyemails", null, "User emails can't be empty.", siteLocale)
        } else {
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

    // Not transactional
    def previewBiosecurityAlert() {
        def date = params.date //only from preview
        def query = Query.get(params.queryid)

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd")
        Date since =  sdf.parse(date)
        def processedJson = notificationService.processQueryBiosecurity(query, since)

        def frequency = 'weekly'
        QueryResult qr = notificationService.getQueryResult(query, Frequency.findByName(frequency))
        qr.lastResult = notificationService.gzipResult(processedJson)
        //this logic only applies on preview page
        qr.previousCheck = qr.lastChecked
        qr.lastChecked = since
        query.lastChecked = since


        //notificationService.refreshProperties(qr, processedJson)

        def records = emailService.retrieveRecordForQuery(qr.query, qr)
        def userAssertions = queryService.isBioSecurityQuery(qr.query) ? emailService.getBiosecurityAssertions(qr.query, records as List) : [:]
        def speciesListInfo = emailService.getSpeciesListInfo(qr.query)

        String urlPrefix = "${grailsApplication.config.security.cas.appServerName}${grailsApplication.config.getProperty('security.cas.contextPath', '')}"
        def localeSubject = messageSource.getMessage("emailservice.update.subject", [query.name] as Object[], siteLocale)

        //Get unsubscribe token
        def unsubscribeOneUrl

        def alaUser = authService.userDetails()
        def user = userService.getUserByEmail(alaUser?.email)
        def unsubscribeToken = notificationService.getUnsubscribeToken(user, query)
        if (user && unsubscribeToken) {
            unsubscribeOneUrl = grailsApplication.config.grails.serverURL + "/unsubscribe?token=${unsubscribeToken}"
        }


        render(view: query.emailTemplate,
//                plugin: "email-confirmation",
                model: [title: localeSubject,
                        message: query.updateMessage,
                        query: query,
                        moreInfo: qr.queryUrlUIUsed,
                        speciesListInfo: speciesListInfo,
                        userAssertions: userAssertions,
                        listcode: queryService.isMyAnnotation(query) ? "biocache.view.myannotation.list" : "biocache.view.list",
                        stopNotification: urlPrefix + '/notification/myAlerts',
                        records: records.take(10),
                        frequency: messageSource.getMessage('frequency.' + frequency, null, siteLocale),
                        totalRecords: records.size(),
                        unsubscribeAll: urlPrefix + "/unsubscribe?token=test",
                        unsubscribeOne: unsubscribeOneUrl
                ])
    }

    @Transactional
    def previewBlogAlerts() {
        String urlPrefix = "${grailsApplication.config.security.cas.appServerName}${grailsApplication.config.getProperty('security.cas.contextPath', '')}"
        Query query = Query.findByName(messageSource.getMessage("query.ala.blog.title", null,
                new Locale.Builder().setLanguageTag(Holders.config.siteDefaultLanguage as String).build()))

        def unsubscribeOneUrl = ""
        def records = []
        if (query) {
            QueryResult qs = QueryResult.findByQuery(query)
            if(qs) {
                def http = new HTTPBuilder(query.baseUrl)
                try {
                    http.get(path: query.queryPath) { resp, json ->
                        if (json) {
                            records = json
                        }
                    }
                } catch (Exception ex) {
                    // Handle any exceptions
                    log.error("An error fetching data from ${query.baseUrl}, Using records in databae. : ${ex.message}")
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

    // Not transactional
    def csvAllBiosecurity() {
        def date = params.date

        response.setContentType("application/octet-stream")
        response.setHeader("Content-Disposition", "attachment; filename=occurrence_alerts_${date}.csv")
        def outputStream = response.outputStream

        try {

            queryService.getALLBiosecurityQuery().each { query ->
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd")
                def processedJson = notificationService.processQueryBiosecurity(query, sdf.parse(date))

                def frequency = 'weekly'
                QueryResult qr = notificationService.getQueryResult(query, Frequency.findByName(frequency))
                qr.lastResult = notificationService.gzipResult(processedJson)
                //notificationService.refreshProperties(qr, processedJson)

                def records = emailService.retrieveRecordForQuery(qr.query, qr)
                def userAssertions = queryService.isBioSecurityQuery(qr.query) ? emailService.getBiosecurityAssertions(qr.query, records as List) : [:]
                def speciesListInfo = emailService.getSpeciesListInfo(qr.query)

                String urlPrefix = "${grailsApplication.config.security.cas.appServerName}${grailsApplication.config.getProperty('security.cas.contextPath', '')}"
                def localeSubject = messageSource.getMessage("emailservice.update.subject", [query.name] as Object[], siteLocale)
                log.debug( "${records.size()}  records were found in ${query.name}" )
                if (records) {
                    records.each { record ->
                        outputStream << date
                        outputStream << ','
                        outputStream << record.uuid
                        outputStream << ','
                        outputStream << speciesListInfo.name
                        outputStream << '\n'
                    }
                }
            }
        }finally {
            outputStream.close()
        }
    }

    @Transactional
    def unsubscribeAllUsers() {
        queryService.unsubscribeAllUsers(Long.valueOf(params.queryid))
        redirect(controller: "admin", action: "biosecurity")
    }

    @Transactional
    def deleteQuery() {
        queryService.deleteQuery(Long.valueOf(params.queryid))
        redirect(controller: "admin", action: "biosecurity")
    }

    @Transactional
    def unsubscribeAlert() {
        if (!params.useremail || params.useremail.allWhitespace) {
            flash.message = messageSource.getMessage("unsubscribeusers.controller.error.emptyemail", null, "User email can't be empty.", siteLocale)
        } else if (!params.queryid || params.queryid.allWhitespace) {
            flash.message = messageSource.getMessage("unsubscribeusers.controller.error.emptyqueryid", null, "Query Id can't be empty.", siteLocale)
        } else {
            User user = userService.getUserByEmail(params.useremail);
            if (user) {
                notificationService.deleteAlertForUser(user, Long.valueOf(params.queryid))
            } else {
                flash.message = messageSource.getMessage('unsubscribeusers.controller.error.emailnotfound', [params.useremail] as Object[], "User with email: {0} are not found in the system.", siteLocale)
            }
        }
        redirect(controller: "admin", action: "biosecurity")
    }





}
