package au.org.ala.alerts

import grails.util.Environment
import grails.util.Holders

class EmailService {
    def groovyPageRenderer
    def diffService
    def queryService
    def grailsApplication
    def messageSource
    def siteLocale = new Locale.Builder().setLanguageTag(Holders.config.siteDefaultLanguage as String).build()


    /**
     * Todo check if it is only for testing purpose
     * @param notification
     * @return
     */
    @Deprecated
    def sendNotificationEmail(Notification notification) {

        log.debug("Using email template: " + notification.query.emailTemplate)

        def queryResult = QueryResult.findByQueryAndFrequency(notification.query, notification.user.frequency)

        def emailModel = generateEmailModel(notification, queryResult)
        def user = notification.user
        def localeSubject = messageSource.getMessage("emailservice.update.subject", [notification.query.name] as Object[], siteLocale)
        String emailBody = groovyPageRenderer.render(template: notification.query.emailTemplate, plugin: "email-confirmation", model: emailModel)

        if (grailsApplication.config.getProperty("mail.enabled", Boolean, false) && !user.locked) {
            log.info "Sending email to ${user.email} for ${notification.query.name}"
            sendMail {
                from grailsApplication.config.mail.details.alertAddressTitle + "<" + grailsApplication.config.mail.details.sender + ">"
                subject localeSubject
                bcc user.email
                html emailBody
            }
        } else if (grailsApplication.config.getProperty("mail.enabled", Boolean, false) && user.locked) {
            log.warn "Email not sent to locked user: ${user.email}"
        } else {
            log.info("Email would have been sent to: " + user.email)
            log.info("message:" + messageSource.getMessage(notification.query.updateMessage, null, siteLocale))
            log.debug("moreInfo:" + queryResult.queryUrlUIUsed)
            log.debug("stopNotification:" + grailsApplication.config.security.cas.appServerName + grailsApplication.config.security.cas.contextPath + '/notification/myAlerts')
            log.debug("records:" + emailModel.records)
            log.debug("frequency:" + notification.user.frequency)
            log.debug("totalRecords:" + emailModel.totalRecords)
        }
    }

    /**
     * Generate the email model.
     *
     * @param notification
     * @param queryResult
     * @return
     */
    def Map generateEmailModel(Notification notification, QueryResult queryResult) {
        generateEmailModel(notification.query, notification.user.frequency, queryResult)
    }

    /**
     * Generate the email model.
     *
     * @param notification
     * @param queryResult
     * @return
     */
    @Deprecated
    def Map generateEmailModel(Query query, String frequency, QueryResult queryResult) {
        def records = diffService.getNewRecords(queryResult)
        def totalRecords = queryResult.totalRecords
        [
            title           : query.name,
            message         : query.updateMessage,
            moreInfo        : queryResult.queryUrlUIUsed,
            listcode        : queryService.isMyAnnotation(query) ? "biocache.view.myannotation.list" : "biocache.view.list",
            query           : query,
            stopNotification: grailsApplication.config.security.cas.appServerName + grailsApplication.config.security.cas.contextPath + '/notification/myAlerts',
            frequency       : frequency,
            records         : records,
            totalRecords    : totalRecords >= 0 ? totalRecords : records.size()
        ]
    }

    /**
     * Key method to send emails to a group of recipients.
     *
     * @param queryResult
     * @param frequency
     * @param recipients
     */
    def sendGroupNotification(QueryResult queryResult, Frequency frequency, List<Map> recipients) {
        Query query = queryResult.query

        log.debug("Using email template: " + query.emailTemplate)
        if (queryResult.succeeded) {
            int totalRecords = queryResult.totalRecords
            def records = queryResult.newRecords
            int maxRecords = grailsApplication.config.getProperty("biosecurity.query.maxRecords", Integer, 500)

            if (totalRecords > 0 || Environment.current == Environment.DEVELOPMENT) {
                if (grailsApplication.config.getProperty("mail.enabled", Boolean, false)) {
                    def emails = recipients.collect { it.email }
                    log.info "Sending emails for ${query.name} to ${emails.size() <= 2 ? emails.join('; ') : emails.take(2).join('; ') + ' and ' + emails.size() + ' other users.'}"
                    recipients.each { recipient ->
                        if (!recipient.locked) {
                            sendGroupEmail(query, [recipient.email], queryResult, records.take(maxRecords), frequency, totalRecords, recipient.userUnsubToken as String, recipient.notificationUnsubToken as String)
                        } else {
                            log.warn "Email not sent to locked user: ${recipient}"
                        }
                    }
                } else {
                    log.info("Email would have been sent to: ${recipients*.email.join(',')} for ${query.name}.")
                    log.debug("message:" + query.updateMessage)
                    log.debug("moreInfo:" + queryResult.queryUrlUIUsed)
                    log.debug("stopNotification:" + grailsApplication.config.security.cas.appServerName + grailsApplication.config.security.cas.contextPath + '/notification/myAlerts')
                    log.debug("records:" + records)
                    log.debug("frequency:" + frequency)
                    log.debug("totalRecords:" + (totalRecords >= 0 ? totalRecords : records.size()))
                }
                def status = ["status": 0, "message": "Emails were dispatched to the Mail service."]
            } else {
                log.info("No email sent for [${queryResult.frequency.name}] - [${query.id}]. ${query.name}, as there were no changes.")
                def status = ["status": 0, "message": "No email sent for [${query.id}]. ${query.name} ."]
            }
        } else {
            String error = "No email sent for [${queryResult?.frequency?.name}] - [${query.id}]) ${query?.name}, as the query failed."
            log.error(error)
            def status = ["status": 1, "message": "${error}", "logs": queryResult.logs]
        }
    }


    void sendGroupEmail(Query query, subsetOfAddresses, QueryResult queryResult, records, Frequency frequency, int totalRecords, String userUnsubToken, String notificationUnsubToken) {
        String urlPrefix = "${grailsApplication.config.security.cas.appServerName}${grailsApplication.config.getProperty('security.cas.contextPath', '')}"
        def localeSubject = messageSource.getMessage("emailservice.update.subject", [query.name] as Object[], siteLocale)
        // pass the last check date to template

        // lastChecked is used into template :  records since lastChecked date
        // That is why we need to assign the previousCheck of queryResult to query.lastChecked
        // todo : separate the since and to with lastChecked and previousCheck in the QueryResult or query
        query.lastChecked = queryResult.previousCheck

        String title = query.name
        if (Environment.current == Environment.DEVELOPMENT || Environment.current == Environment.TEST) {
            title = "[${Environment.current}] " + query.name
        }

        String emailBody = groovyPageRenderer.render(view:  query.emailTemplate,
                plugin: "email-confirmation",
                model: [title: localeSubject,
                       message: query.updateMessage,
                       query: query,
                       moreInfo: queryResult.queryUrlUIUsed,
                       listcode: queryService.isMyAnnotation(query) ? "biocache.view.myannotation.list" : "biocache.view.list",
                       stopNotification: urlPrefix + '/notification/myAlerts',
                       records: records,
                       frequency: messageSource.getMessage('frequency.' + frequency, null, siteLocale),
                       totalRecords: (totalRecords >= 0 ? totalRecords : records.size()),
                       unsubscribeAll: urlPrefix + "/unsubscribe?token=" + userUnsubToken,
                       unsubscribeOne: urlPrefix + "/unsubscribe?token=" + notificationUnsubToken
               ]
        )

        try {
            sendMail {
                from grailsApplication.config.mail.details.alertAddressTitle + "<" + grailsApplication.config.mail.details.sender + ">"
                subject title
                bcc subsetOfAddresses
                html(emailBody)
            }
        } catch (Exception e) {
            log.error("Error sending email to addresses: " + subsetOfAddresses, e)
        }
    }
}
