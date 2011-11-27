package ala.postie

import java.text.SimpleDateFormat
import com.jayway.jsonpath.JsonPath
import org.codehaus.groovy.grails.commons.ConfigurationHolder

class EmailService {

  static transactional = true

  def diffService

  def serviceMethod() {}

  def sendNotificationEmail(Notification notification){

    log.debug("Using email template: " + notification.query.emailTemplate)

    QueryResult queryResult = QueryResult.findByQueryAndFrequency(notification.query, notification.user.frequency)

    def records = null
    if(notification.query.recordJsonPath){
      records = diffService.getNewRecordsFromDiff(queryResult)
    }

    sendMail {
      from "Atlas alerts<" + ConfigurationHolder.config.postie.emailSender + ">"
      subject "Update - " + notification.query.name
      bcc notification.user.email
      body (view: notification.query.emailTemplate,
            plugin:"email-confirmation",
            model:[title:notification.query.name,
                   message:notification.query.updateMessage,
                   moreInfo: constructMoreInfoUrl(notification.query),
                   stopNotification: ConfigurationHolder.config.security.cas.serverName + ConfigurationHolder.config.security.cas.contextPath  + '/notification/myAlerts',
                   records: records,
                   frequency: notification.user.frequency
            ]
      )
    }
  }

  def sendGroupNotification(Query query, Frequency frequency, List<String> addresses){

    log.debug("Using email template: " + query.emailTemplate)
    QueryResult queryResult = QueryResult.findByQueryAndFrequency(query, frequency)

    def records = null
    if(query.recordJsonPath){
      //records = JsonPath.read(NotificationService.decompressZipped(queryResult.lastResult), query.recordJsonPath)
      records = diffService.getNewRecordsFromDiff(queryResult)
    }

    sendMail {
      from "Atlas alerts<" + ConfigurationHolder.config.postie.emailSender + ">"
      subject query.name
      bcc addresses
      body (view: query.emailTemplate,
            plugin:"email-confirmation",
            model:[title:"Update - " + query.name,
                   message:query.updateMessage,
                   moreInfo: constructMoreInfoUrl(query, frequency),
                   stopNotification: ConfigurationHolder.config.security.cas.serverName + ConfigurationHolder.config.security.cas.contextPath  + '/notification/myAlerts',
                   records: records,
                   frequency: frequency
      ])
    }
  }

  private String constructMoreInfoUrl(Query query, Frequency frequency) {
    QueryResult queryResult = QueryResult.findByQueryAndFrequency(query, frequency)
    String moreInfoUrl = query.baseUrl + query.queryPathForUI
    if(query.dateFormat){
      SimpleDateFormat sdf = new SimpleDateFormat(query.dateFormat)
      def dateValue = sdf.format(queryResult.previousCheck)
      moreInfoUrl = query.baseUrl + query.queryPathForUI.replaceAll("___DATEPARAM___", dateValue)
    }
    moreInfoUrl
  }
}
