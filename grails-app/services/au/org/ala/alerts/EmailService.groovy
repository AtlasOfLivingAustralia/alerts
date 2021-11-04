package au.org.ala.alerts

import grails.util.Holders
import org.grails.web.json.JSONObject

class EmailService {

    static transactional = true

    def diffService
    def queryService
    def grailsApplication
    def webService
    def messageSource
    def siteLocale = new Locale.Builder().setLanguageTag(Holders.config.siteDefaultLanguage as String).build()

    /**
     * Returns a list of records
     * @param query
     * @param queryResult
     * @return
     */
    def retrieveRecordForQuery(query, queryResult) {
        //if there's a fire on property, then don't do a diff
        if (queryService.hasAFireProperty(query) && query.recordJsonPath) {
            diffService.getNewRecords(queryResult)
        } else if (query.recordJsonPath) {
            diffService.getNewRecordsFromDiff(queryResult)
        } else {
            []
        }
    }

    def sendNotificationEmail(Notification notification) {

        log.debug("Using email template: " + notification.query.emailTemplate)

        def queryResult = QueryResult.findByQueryAndFrequency(notification.query, notification.user.frequency)

        def emailModel = generateEmailModel(notification, queryResult)
        def user = notification.user
        def localeSubject = messageSource.getMessage("emailservice.update.subject", [notification.query.name] as Object[], siteLocale)
        if (grailsApplication.config.postie.enableEmail && !user.locked) {
            log.info "Sending email to ${user.email} for ${notification.query.name}"
            sendMail {
                from grailsApplication.config.postie.emailAlertAddressTitle + "<" + grailsApplication.config.postie.emailSender + ">"
                subject localeSubject
                bcc user.email
                body(view: notification.query.emailTemplate,
                        plugin: "email-confirmation",
                        model: emailModel
                )
            }
        } else if (grailsApplication.config.postie.enableEmail && user.locked) {
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
    def Map generateEmailModel(Query query, String frequency, QueryResult queryResult) {
        def records = retrieveRecordForQuery(query, queryResult)
        def totalRecords = queryService.fireWhenNotZeroProperty(queryResult)
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

    def sendGroupNotification(Query query, Frequency frequency, List<Map> recipients) {

        log.debug("Using email template: " + query.emailTemplate)
        QueryResult queryResult = QueryResult.findByQueryAndFrequency(query, frequency)

        def records = retrieveRecordForQuery(query, queryResult)
        def speciesListInfo = getSpeciesListInfo(query)

        Integer totalRecords = queryService.fireWhenNotZeroProperty(queryResult)
        if (grailsApplication.config.getProperty("postie.enableEmail", Boolean, false)) {
            log.info "Sending group email for ${query.name} to ${recipients.collect { it.email }}"
            recipients.each { recipient ->
                if (!recipient.locked) {
                    sendGroupEmail(query, [recipient.email], queryResult, records, frequency, totalRecords, recipient.userUnsubToken as String, recipient.notificationUnsubToken as String, speciesListInfo)
                } else {
                    log.warn "Email not sent to locked user: ${recipient}"
                }
            }
        } else {
            log.info("Email would have been sent to: ${recipients*.email.join(',')} for ${query.name}")
            log.debug("message:" + query.updateMessage)
            log.debug("moreInfo:" + queryResult.queryUrlUIUsed)
            log.debug("stopNotification:" + grailsApplication.config.security.cas.appServerName + grailsApplication.config.security.cas.contextPath + '/notification/myAlerts')
            log.debug("records:" + records)
            log.debug("frequency:" + frequency)
            log.debug("totalRecords:" + (totalRecords >= 0 ? totalRecords : records.size()))
        }
    }

    private void sendGroupEmail(Query query, subsetOfAddresses, QueryResult queryResult, records, Frequency frequency, int totalRecords, String userUnsubToken, String notificationUnsubToken, Map speciesListInfo) {
        String urlPrefix = "${grailsApplication.config.security.cas.appServerName}${grailsApplication.config.getProperty('security.cas.contextPath', '')}"
        def localeSubject = messageSource.getMessage("emailservice.update.subject", [query.name] as Object[], siteLocale)
        try {
            sendMail {
                from grailsApplication.config.postie.emailAlertAddressTitle + "<" + grailsApplication.config.postie.emailSender + ">"
                subject query.name
                bcc subsetOfAddresses
                body(view: query.emailTemplate,
                        plugin: "email-confirmation",
                        model: [title           : localeSubject,
                                message         : query.updateMessage,
                                query           : query,
                                moreInfo        : queryResult.queryUrlUIUsed,
                                speciesListInfo : speciesListInfo,
                                listcode        : queryService.isMyAnnotation(query) ? "biocache.view.myannotation.list" : "biocache.view.list",
                                stopNotification: urlPrefix + '/notification/myAlerts',
                                records         : records,
                                frequency       : messageSource.getMessage('frequency.' + frequency, null, siteLocale),
                                totalRecords    : (totalRecords >= 0 ? totalRecords : records.size()),
                                unsubscribeAll  : urlPrefix + "/unsubscribe?token=" + userUnsubToken,
                                unsubscribeOne  : urlPrefix + "/unsubscribe?token=" + notificationUnsubToken
                        ])
            }
        } catch (Exception e) {
            log.error("Error sending email to addresses: " + subsetOfAddresses, e)
        }
    }

    /**
     * Get the species list info
     *
     * @param query
     * @return a Map contains list name and list URL
     */
    private Map getSpeciesListInfo(Query query) {
        // if it's biosecurity query, we try to get list details
        if (query.emailTemplate == queryService.createBioSecurityQuery('').emailTemplate) {
            String[] parts = query.name.split(" ")
            if (parts.length >= 1) {
                String listid = parts[parts.length - 1]
                String speciesListServer = grailsApplication.config.getProperty("specieslist.server", String, "https://lists.ala.org.au")
                String listURL = speciesListServer + '/ws/speciesList/' + listid
                try {
                    JSONObject rslt = webService.getJsonElements(listURL)
                    if (rslt && rslt.listName) {
                        return [name: rslt.listName, url: speciesListServer + '/speciesListItem/list/' + listid]
                    }
                } catch (Exception ex) {
                    log.error("Failed to get species list detail from " + listURL, ex)
                }
            }
        }

        [:]
    }
}
