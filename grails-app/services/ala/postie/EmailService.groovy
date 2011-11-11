package ala.postie

import java.text.SimpleDateFormat
import com.jayway.jsonpath.JsonPath
import org.codehaus.groovy.grails.commons.ConfigurationHolder

class EmailService {

  static transactional = true

  def serviceMethod() {}

  def sendNotificationEmail(Notification notification){

    println("Using email template: " + notification.query.emailTemplate)

    def records = null
    if(notification.query.recordJsonPath){
      records = JsonPath.read(NotificationService.decompressZipped(notification.query.lastResult), notification.query.recordJsonPath)
    }

    sendMail {
      from ConfigurationHolder.config.postie.emailSender
      to ConfigurationHolder.config.postie.emailSender
      subject "Update - " + notification.query.name
      bcc notification.userEmail
      body (view: notification.query.emailTemplate,
            plugin:"email-confirmation",
            model:[title:"Update - " + notification.query.name,
                   message:notification.query.updateMessage,
                   moreInfo: constructMoreInfoUrl(notification.query),
                   stopNotification: ConfigurationHolder.config.security.cas.serverName + ConfigurationHolder.config.security.cas.contextPath  + '/notification/myAlerts',
                   records: records
            ]
      )
    }
  }

  def sendGroupNotification(Query query, List<String> addresses){

    println("Using email template: " + query.emailTemplate)

    def records = null
    if(query.recordJsonPath){
      records = JsonPath.read(NotificationService.decompressZipped(query.lastResult), query.recordJsonPath)
    }

    sendMail {
      from ConfigurationHolder.config.postie.emailSender
      to ConfigurationHolder.config.postie.emailSender
      subject "Update - " + query.name
      bcc addresses
      body (view: query.emailTemplate,
            plugin:"email-confirmation",
            model:[title:"Update - " + query.name,
                   message:query.updateMessage,
                   moreInfo: constructMoreInfoUrl(query),
                   stopNotification: ConfigurationHolder.config.security.cas.serverName + ConfigurationHolder.config.security.cas.contextPath  + '/notification/myAlerts',
                   records: records
      ])
    }
  }

  private String constructMoreInfoUrl(Query query) {
    String moreInfoUrl = query.baseUrl + query.queryPathForUI
    if(query.dateFormat){
      SimpleDateFormat sdf = new SimpleDateFormat(query.dateFormat)
      sdf.setTimeZone(TimeZone.getTimeZone(ConfigurationHolder.config.postie.timezone) )
      def dateValue = sdf.format(query.previousCheck)
      moreInfoUrl = query.baseUrl + query.queryPathForUI.replaceAll("___DATEPARAM___", dateValue)
    }
    moreInfoUrl
  }
}
