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
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.util.Holders

import java.text.SimpleDateFormat
import groovyx.net.http.HTTPBuilder
import groovy.json.JsonSlurper
import java.nio.file.Files
import java.nio.file.Paths

@AlaSecured(value = 'ROLE_ADMIN', redirectController = 'notification', redirectAction = 'myAlerts', message = "You don't have permission to view that page.")
class AdminController {

    def authService
    def notificationService
    def biosecurityService
    def biosecurityCSVService
    def queryResultService
    def diffService
    def emailService
    def queryService
    def userService
    def messageSource
    def siteLocale = new Locale.Builder().setLanguageTag(Holders.config.siteDefaultLanguage as String).build()

    def subscriptionsPerPage = grailsApplication.config.getProperty('biosecurity.subscriptionsPerPage', Integer, 100)
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

    def debugAllAlerts() {
        response.setContentType("text/plain")
        notificationService.checkAllQueries(response.getWriter())
    }

    def debugAlertEmail() {
        def frequency = params.frequency ?: 'weekly'
        def qcr = notificationService.checkQueryById(params.id, params.frequency ?: 'weekly')
        def model = emailService.generateEmailModel(qcr.query, frequency, qcr.queryResult)
        render(view: qcr.query.emailTemplate, model: model)
    }


    def debugAlert() {
        [alerts: [
                hourly : notificationService.checkQueryById(params.id, params.frequency ?: 'hourly'),
                daily  : notificationService.checkQueryById(params.id, params.frequency ?: 'daily'),
                weekly : notificationService.checkQueryById(params.id, params.frequency ?: 'weekly'),
                monthly: notificationService.checkQueryById(params.id, params.frequency ?: 'monthly')
        ]
        ]
    }

    def deleteOrphanAlerts() {
        def result = queryService.deleteOrphanedQueries()
        render(view: 'index', model: [message: "Removed ${result['OrphanQuery']} queries, and ${result['OrphanNotification']} orphaned notifications."])
    }

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
        render view: "/admin/biosecurity", model: [total: total, queries: queries, subscriptionsPerPage: subscriptionsPerPage]
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
        def query = Query.get(params.queryid)

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd")
        Date since =  sdf.parse(date)
        Date now = new Date()

        def processedJson = biosecurityService.processQueryBiosecurity(query, since, now)

        def frequency = 'weekly'
        QueryResult qr = notificationService.getQueryResult(query, Frequency.findByName(frequency))
        qr.lastResult = notificationService.gzipResult(processedJson)
        //this logic only applies on preview page
        qr.previousCheck = qr.lastChecked
        qr.lastChecked = since
        query.lastChecked = since


        def records = notificationService.retrieveRecordForQuery(qr.query, qr)
        def userAssertions = queryService.isBioSecurityQuery(qr.query) ? emailService.getBiosecurityAssertions(qr.query, records as List) : [:]
        def speciesListInfo = emailService.getSpeciesListInfo(qr.query)

        String urlPrefix = "${grailsApplication.config.getProperty("grails.serverURL")}${grailsApplication.config.getProperty('security.cas.contextPath', '')}"
        def localeSubject = messageSource.getMessage("emailservice.update.subject", [query.name] as Object[], siteLocale)

        //Get unsubscribe token
        def unsubscribeOneUrl

        def alaUser = authService.userDetails()
        def user = userService.getUserByEmail(alaUser?.email)
        def unsubscribeToken = notificationService.getUnsubscribeToken(user, query)
        if (user && unsubscribeToken) {
            unsubscribeOneUrl = urlPrefix + "/unsubscribe?token=${unsubscribeToken}"
        }
        int maxRecords = grailsApplication.config.getProperty("biosecurity.query.maxRecords", Integer, 500)
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
                        records: records.take(maxRecords),
                        frequency: messageSource.getMessage('frequency.' + frequency, null, siteLocale),
                        totalRecords: records.size(),
                        unsubscribeAll: urlPrefix + "/unsubscribe?token=test",
                        unsubscribeOne: unsubscribeOneUrl
                ])
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

    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true)
    def csvAllBiosecurity() {
        def date = params.date
        String outputFile = "occurrence_alerts_${date}.csv"
        log.info("Generate CSV for Biosecurity queries staring from ${date}")
        def queries =  queryService.getALLBiosecurityQuery()

        //Get all CSV files for each Biosecurity query
        List<String> csvFiles  = []
        queries.each { query ->
            log.info("Generate CSV for Biosecurity query: ${query.name}")
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd")
            def processedJson = biosecurityService.processQueryBiosecurity(query, sdf.parse(date), new Date())

            def frequency = 'weekly'
            QueryResult qr = notificationService.getQueryResult(query, Frequency.findByName(frequency))
            qr.lastResult = notificationService.gzipResult(processedJson)
            File tempCSV = biosecurityCSVService.createTempCSV(qr)
            csvFiles.add(tempCSV.path)
        }

         //aggregate all CSV files into one
        log.info("Aggregate CSV files into one file")
        def tempFilePath = Files.createTempFile(outputFile, ".csv")
        def tempFile = tempFilePath.toFile()
        tempFile.withWriter { writer ->
            try {
                csvFiles.eachWithIndex {  csvFile, index ->
                    new File(csvFile).withReader('UTF-8') { reader ->
                        reader.eachLine { line, lineNumber ->
                            if (index == 0 || lineNumber > 1) { // Write header from the first file and skip headers from the rest
                                writer.writeLine(line)
                            }
                        }
                    }
                }

            }catch (Exception e) {
                log.error("Error in generating CSV file: ${e.message}")
            }
        }

        response.setContentType("application/octet-stream")
        response.setHeader("Content-Disposition", "attachment; filename=occurrence_alerts_${date}.csv")
        def outputStream = response.outputStream

        try {
            BufferedReader reader = Files.newBufferedReader(tempFilePath)
            String line
            while ((line = reader.readLine()) != null) {
                outputStream.println(line)
            }
            reader.close()
        } finally {
            outputStream.close()
        }
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

    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true)
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
                boolean hasChanged = notificationService.hasChanged(qs)
                def records = notificationService.collectUpdatedRecords(qs)
                def results = ["hasChanged": hasChanged, "records": records]
                render results as JSON
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
     * @return
     */
    def emailMeLastCheck(){
        def id = params.queryId
        def frequency = params.frequency
        if (id && frequency) {
            Query query = Query.get(id)
            Frequency fre = Frequency.findByName(frequency)
            if (query && fre) {
                QueryResult qs = notificationService.executeQuery(query, fre, true, true)
                boolean hasChanged = notificationService.hasChanged(qs)
                def records = notificationService.collectUpdatedRecords(qs)
                User currentUser = userService.getUser()
                def recipient =
                    [email: currentUser.email, userUnsubToken: currentUser.unsubscribeToken, notificationUnsubToken: '']
                emailService.sendGroupNotification(qs, fre, [recipient])
                def results = ["hasChanged": hasChanged, "records": records, "recipient": currentUser.email]
                render results as JSON
            } else {
                render([status: 1, message: "Cannot find query: ${id}"] as JSON)
            }
        } else {
            render([status: 1, message: "Missing queryId or frequency"] as JSON)
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
                boolean hasChanged = notificationService.hasChanged(qs)
                def records = notificationService.collectUpdatedRecords(qs)
                User currentUser = userService.getUser()
                def recipient =
                        [email: currentUser.email, userUnsubToken: currentUser.unsubscribeToken, notificationUnsubToken: '']
                emailService.sendGroupNotification(qs, fre, [recipient])
                def results = ["hasChanged": hasChanged, "records": records, "recipient": currentUser.email]
                render results as JSON
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
                def records = notificationService.collectUpdatedRecords(queryResult)
                def results = ["status": queryResult.succeed, "hasChanged": queryResult.hasChanged, "logs": queryResult.getLog(), "records": records, "details": queryResult.brief()]
                render results as JSON
            } else {
                render([status: 1, message: "Cannot find query: ${id}"] as JSON)
            }
        } else {
            render([status: 1, message: "Missing queryId or frequency"] as JSON)
        }
    }

    /**
     * No database updated, No email sent
     *
     * Run a task to execute the query for a specific frequency
     *
     */
    def dryRunAllQueriesForFrequency(){
        def freq = params.frequency
        Frequency frequency = Frequency.findByName(freq)
        def queries = Query.executeQuery(
                """select q from Query q
                  inner join q.notifications n
                  inner join n.user u
                  where u.frequency = :frequency
                  group by q""", [frequency: frequency])
        int total = queries.size()

        response.setContentType("text/plain")
        def writer= response.getWriter()

        queries.eachWithIndex { query, index ->
            QueryResult queryResult = notificationService.executeQuery(query, Frequency.findByName(frequency), false, true)
            def records = notificationService.collectUpdatedRecords(queryResult)
            def results = ["POS": "${index+1}/${total}", "status": queryResult.succeed, "hasChanged": queryResult.hasChanged, "logs": queryResult.getLog(), "brief": queryResult.brief()]

            writer.write("${results["POS"]}. ${query.id} - ${query.name} - ${frequency}\n")
            writer.write("Status: ${results["status"]}, Changed:${results["hasChanged"]} \n")
            writer.write("Logs: ${results["logs"]}\n")
            writer.write("Brief: ${results["brief"]}\n")
            writer.write(("-" * 80) + "\n")
            writer.flush()
            log.info("Query ${query.id} has been executed for frequency ${frequency}")
        }
    }

    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true, redirectController = 'notification', redirectAction = 'myAlerts', message = "You don't have permission to view that page.")
    def listBiosecurityAuditCSV() {
        def result = [:]
        try {
            result  = biosecurityCSVService.list()
        } catch (Exception e) {
            log.error("Error in listing Biosecurity CSV files: ${e.message}")
            result = [status: 1, message: "Error in listing Biosecurity CSV files: ${e.message}"]
        }
        render(view: 'biosecurityCSV', model: result)
    }

    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true,redirectController = 'notification', redirectAction = 'myAlerts', message = "You don't have permission to view that page.")
    def aggregateBiosecurityAuditCSV(String folderName) {
        if (!biosecurityCSVService.folderExists(folderName)) {
            render(status: 404, text: 'Data not found')
            return
        }

        // Get a list of all CSV files in the folder
        String mergedCSVFile = biosecurityCSVService.aggregateCSVFiles(folderName)
        if (folderName == "/" || folderName.isEmpty()) {
            folderName = "biosecurity_alerts"
        }
        def saveToFile = folderName +".csv"
        response.contentType = 'application/octet-stream'
        response.setHeader('Content-Disposition', "attachment; filename=\"${saveToFile}\"")
        response.outputStream << new File(mergedCSVFile).bytes
    }

    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true,redirectController = 'notification', redirectAction = 'myAlerts', message = "You don't have permission to view that page.")
    def downloadBiosecurityAuditCSV(String filename) {
        String contents = biosecurityCSVService.getFile(filename)
        if (!contents) {
            render(status: 404, text: "Data not found")
            return
        }
        response.contentType = 'text/csv'
        response.setHeader("Content-disposition", "attachment; filename=\"${filename.tokenize(File.separator).last()}\"")
        response.outputStream.withWriter("UTF-8") { writer ->
            writer << contents
        }
        response.outputStream.flush()
    }

    /**
     * params.id is the QueryResult id
     * @return
     */
    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true)
    def downloadLastBiosecurityResult() {
        // Gorm object QueryResult does not fetch Query object, so we need to fetch it manually
        QueryResult qs = queryResultService.get(params.id)
        if (qs) {
            File tempFile = biosecurityCSVService.createTempCSV(qs)
            if (!tempFile.exists() || tempFile.isDirectory()) {
                render(status: 404, text: "File not found")
                return
            }

            def saveToFile = biosecurityCSVService.sanitizeFileName(qs.query?.name + "-" + (qs.lastChecked?new SimpleDateFormat("yyyy-MM-dd").format(qs.lastChecked):"") + ".csv")
            response.contentType = 'application/octet-stream'
            response.setHeader('Content-Disposition', "attachment; filename=\"${saveToFile}\"")
            response.outputStream << tempFile.bytes
        } else {
            render(status: 200, text: "QueryResult not found")
        }
    }

    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true)
    def deleteBiosecurityAuditCSV(String filename) {
        Map message = biosecurityCSVService.deleteFile(filename)
        render(status: 200, contentType: 'application/json', text: message as JSON)
    }

    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true)
    def moveLocalFilesToS3() {
        Boolean dryRun = params.boolean('dryRun', true)
        Map message = biosecurityCSVService.moveLocalFilesToS3(dryRun)
        render(status: 200, contentType: 'application/json', text: message as JSON)
    }
}
