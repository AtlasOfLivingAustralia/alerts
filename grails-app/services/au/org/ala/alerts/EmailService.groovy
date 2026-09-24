package au.org.ala.alerts

import grails.util.Environment
import grails.util.Holders

import java.util.regex.Pattern

class EmailService {
    def groovyPageRenderer
    def queryService
    def grailsApplication
    def messageSource
    def monitoringTeamService
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

        String emailSubject = query.name
        String hubName = getHubName(query.baseUrlForUI)
        if (hubName) {
            emailSubject = "[${hubName}] " + emailSubject
        }
        if (Environment.current == Environment.DEVELOPMENT || Environment.current == Environment.TEST) {
            emailSubject = "[${Environment.current}] " + emailSubject
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
                subject emailSubject
                bcc subsetOfAddresses
                headers([
                        'X-SES-CONFIGURATION-SET': 'alerts'
                ])
                html(emailBody)
            }
        } catch (Exception e) {
            log.error("Error sending email to addresses: " + subsetOfAddresses, e)
        }
    }

    def notifyMonitoringTeam(String teamName, Map messages) {
        if (grailsApplication.config.getProperty("mail.enabled", Boolean, false)) {
            String emailSubject = messages["subject"] ? messages["subject"] : "Error notification to ${teamName} team"
            String emailBody = groovyPageRenderer.render(view:  "/email/monitorTeamNotification",
                    plugin: "email-confirmation",
                    model: [messages: messages]
            )

            if (Environment.current == Environment.DEVELOPMENT || Environment.current == Environment.TEST) {
                emailSubject = "[${Environment.current}] " + emailSubject
            }

            def recipients = monitoringTeamService.getEmails(teamName)
            if (recipients) {
                log.info ("Sending Alerts complete notification to monitor team: ${teamName} at ${recipients.join(",")}")
                try {
                    sendMail {
                        from grailsApplication.config.mail.details.alertAddressTitle + "<" + grailsApplication.config.mail.details.sender + ">"
                        subject emailSubject
                        bcc recipients
                        headers([
                                'X-SES-CONFIGURATION-SET': 'biosecurity',
                                'X-SES-MESSAGE-TAGS': 'application=Biosecurity'
                        ])
                        html(emailBody)
                    }
                } catch (Exception e) {
                    log.error("Error in sending email to monitor team: " + recipients.join(","), e)
                }
            } else {
                log.warn("No recipients found for monitor team: ${teamName}. Error notification will not be sent.")
            }
        } else {
            log.info("Mail service disable. Error notification will not be sent to monitor team: ${recipients?.join(",")}.")
        }
    }

    String getHubName(String urlForUI) {
        if (!urlForUI) {
            return ""
        }

        //hubPattern is a key value pair map, label: key
        String hubPattern = grailsApplication.config.getProperty("hubs", String, "Atlas of Living Australia")
        if (hubPattern) {
            def hubMap = [:]
            hubPattern.split(",").each { entry ->
                // split on the FIRST ':' only, so hub values that are full urls
                // (e.g. "AVH:https://avh.ala.org.au") are not broken up by the scheme separator
                def parts = entry.split(":", 2)
                if (parts.length == 2 && parts[0].trim() && parts[1].trim()) {
                    hubMap[parts[0].trim()] = parts[1].trim()
                }
            }
            for (entry in hubMap) {
                if (matchesHub(urlForUI, entry.value as String)) {
                    return entry.key
                }
            }
        }
        return ""
    }

    /**
     * Matches a UI url against a configured hub value, ignoring the scheme.
     *
     * Both the url and the configured value may or may not carry "http://" / "https://"
     * (and an optional "www." prefix), so both sides are normalised and the configured
     * value is then matched as a case insensitive prefix of the url.
     *
     * e.g. "avh", "avh.ala.org.au", "http://avh.ala.org.au" and "https://www.avh.ala.org.au"
     * all match the url "https://avh.ala.org.au/occurrences/search".
     *
     * @param urlForUI the url to test
     * @param hubValue the configured hub value
     * @return true if the url belongs to the hub
     */
    static boolean matchesHub(String urlForUI, String hubValue) {
        String hub = stripScheme(hubValue)
        if (!hub) {
            return false
        }
        // both sides have already had the scheme/"www."/trailing slash removed, so the configured
        // value only has to match the start of the url. (?i) makes the match case insensitive and
        // Pattern.quote escapes regex metacharacters (e.g. the dots in a host name)
        def matcher = stripScheme(urlForUI) =~ /(?i)^${Pattern.quote(hub)}/
        return matcher.find()
    }

    /**
     * Removes the scheme, any "www." prefix and trailing slashes from a url, so urls can be
     * compared regardless of how they were configured.
     */
    private static String stripScheme(String url) {
        url?.trim()
                ?.replaceFirst(/(?i)^[a-z][a-z0-9+.\-]*:\/\//, '')
                ?.replaceFirst(/(?i)^www\./, '')
                ?.replaceFirst(/\/+$/, '')
    }
}
