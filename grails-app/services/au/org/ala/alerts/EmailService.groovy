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
     * Key method to send emails to a group of recipients.
     *
     * @param queryResult
     * @param frequency
     * @param recipients
     */
    def sendGroupNotification(QueryResult queryResult, Frequency frequency, List<Map> recipients, Map moreInfo = [:]) {
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
                            sendGroupEmail(query, [recipient.email], queryResult, records.take(maxRecords), frequency, totalRecords, recipient.userUnsubToken as String, recipient.notificationUnsubToken as String, moreInfo)
                        } else {
                            log.warn "Email not sent to locked user: ${recipient}"
                        }
                    }
                } else {
                    log.info("Email would have been sent to: ${recipients*.email.join(',')} for ${query.name}.")
                    log.debug("message:" + query.updateMessage)
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


    void sendGroupEmail(Query query, subsetOfAddresses, QueryResult queryResult, records, Frequency frequency, int totalRecords, String userUnsubToken, String notificationUnsubToken, Map moreInfo) {
        String urlPrefix = "${grailsApplication.config.security.cas.appServerName}${grailsApplication.config.getProperty('security.cas.contextPath', '')}"
        def localeSubject = messageSource.getMessage("emailservice.update.subject", [query.name] as Object[], siteLocale)

        moreInfo = moreInfo ?: [:]
        moreInfo.queryUrlUIUsed = queryResult.queryUrlUIUsed
        moreInfo.lastChecked = queryResult.previousCheck

        String title = query.name
        if (Environment.current == Environment.DEVELOPMENT || Environment.current == Environment.TEST) {
            title = "[${Environment.current}] " + query.name
        }

        String emailBody = groovyPageRenderer.render(view:  query.emailTemplate,
                plugin: "email-confirmation",
                model: [title: localeSubject,
                       message: query.updateMessage,
                       query: query,
                       moreInfo: moreInfo,
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
