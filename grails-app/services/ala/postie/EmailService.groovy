package ala.postie

import java.text.SimpleDateFormat

class EmailService {

    static transactional = true

    def diffService

    def queryService

    def grailsApplication

    def serviceMethod() {}

    def sendNotificationEmail(Notification notification) {

        log.debug("Using email template: " + notification.query.emailTemplate)

        QueryResult queryResult = QueryResult.findByQueryAndFrequency(notification.query, notification.user.frequency)

        def records = null

        //if theres a fire on property, then dont do a diff
        if (queryService.hasAFireProperty(notification.query) && notification.query.recordJsonPath) {
            records = diffService.getNewRecords(queryResult)
        } else if (notification.query.recordJsonPath) {
            records = diffService.getNewRecordsFromDiff(queryResult)
        }

        Integer totalRecords = queryService.fireWhenNotZeroProperty(queryResult)
        if (grailsApplication.config.postie.enableEmail) {
            sendMail {
                from grailsApplication.config.postie.emailAlertAddressTitle + "<" + grailsApplication.config.postie.emailSender + ">"
                subject "Update - " + notification.query.name
                bcc notification.user.email
                body(view: notification.query.emailTemplate,
                    plugin: "email-confirmation",
                    model: [title: notification.query.name,
                        message: notification.query.updateMessage,
                        moreInfo: queryResult.queryUrlUIUsed,
                        query: notification.query,
                        stopNotification: grailsApplication.config.security.cas.appServerName + grailsApplication.config.security.cas.contextPath + '/notification/myAlerts',
                        records: records,
                        frequency: notification.user.frequency,
                        totalRecords: totalRecords
                    ]
                )
            }
        } else {
            log.debug("Email would have been sent to: " + notification.user.email)
            log.debug("message:" + notification.query.updateMessage)
            log.debug("moreInfo:" + queryResult.queryUrlUIUsed)
            log.debug("stopNotification:" + grailsApplication.config.security.cas.appServerName + grailsApplication.config.security.cas.contextPath + '/notification/myAlerts')
            log.debug("records:" + records)
            log.debug("frequency:" + notification.user.frequency)
            log.debug("totalRecords:" + totalRecords)
        }
    }

    def sendGroupNotification(Query query, Frequency frequency, List<String> addresses) {

        log.debug("Using email template: " + query.emailTemplate)
        QueryResult queryResult = QueryResult.findByQueryAndFrequency(query, frequency)

        def records = null
        //if theres a fire on property, then dont do a diff
        if (queryService.hasAFireProperty(query) && query.recordJsonPath) {
            records = diffService.getNewRecords(queryResult)
        } else if (query.recordJsonPath) {
            records = diffService.getNewRecordsFromDiff(queryResult)
        }

        Integer totalRecords = queryService.fireWhenNotZeroProperty(queryResult)

        if (grailsApplication.config.postie.enableEmail) {
            //split the mailing list into multiple lists
//            def sublists = addresses.collate(25, true)
//            sublists.each { sublist ->
//                sendGroupEmail(query, sublist, queryResult, records, frequency, totalRecords)
//            }

            //def sublists = addresses.collate(25, true)
            addresses.each { address ->
                sendGroupEmail(query, [address], queryResult, records, frequency, totalRecords)
            }
        } else {
            log.debug("Email would have been sent to: " + addresses.join(','))
            log.debug("message:" + query.updateMessage)
            log.debug("moreInfo:" + queryResult.queryUrlUIUsed)
            log.debug("stopNotification:" + grailsApplication.config.security.cas.appServerName + grailsApplication.config.security.cas.contextPath + '/notification/myAlerts')
            log.debug("records:" + records)
            log.debug("frequency:" + frequency)
            log.debug("totalRecords:" + totalRecords)
        }
    }

    private void sendGroupEmail(Query query, subsetOfAddresses, queryResult, records, Frequency frequency, int totalRecords) {
        try {
            sendMail {
                from grailsApplication.config.postie.emailAlertAddressTitle + "<" + grailsApplication.config.postie.emailSender + ">"
                subject query.name
                bcc subsetOfAddresses
                body(view: query.emailTemplate,
                    plugin: "email-confirmation",
                    model: [title: "Update - " + query.name,
                        message: query.updateMessage,
                        query: query,
                        moreInfo: queryResult.queryUrlUIUsed,
                        stopNotification: grailsApplication.config.security.cas.appServerName + grailsApplication.config.security.cas.contextPath + '/notification/myAlerts',
                        records: records,
                        frequency: frequency,
                        totalRecords: totalRecords
                    ])
            }
        } catch(Exception e){
            log.error("Error sending email to addresses: " + subsetOfAddresses, e)
        }
    }

    private String constructMoreInfoUrl(Query query, Frequency frequency) {
        QueryResult queryResult = QueryResult.findByQueryAndFrequency(query, frequency)
        String moreInfoUrl = query.baseUrlForUI + query.queryPathForUI
        if (query.dateFormat) {
            SimpleDateFormat sdf = new SimpleDateFormat(query.dateFormat)
            def dateValue = sdf.format(queryResult.lastChecked)
            moreInfoUrl = query.baseUrlForUI + query.queryPathForUI.replaceAll("___DATEPARAM___", dateValue)
        }
        moreInfoUrl
    }
}
