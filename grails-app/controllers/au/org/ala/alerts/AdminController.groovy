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

import groovy.json.JsonSlurper
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.DefaultHttpClient

class AdminController {

  def index() {
      if(!authService.userInRole("ROLE_ADMIN")){
          response.sendError(401)
      }
  }

  def authService
  def notificationService
  def emailService
  def queryService
  def userService

  def findUser() {
      if (!authService.userInRole("ROLE_ADMIN")){
          response.sendError(401)
      } else {
          List users = []
          if (params.term) {
              users = userService.findUsers(params.term)
          }
          render view: "/admin/userAlerts", model: [users: users]
      }
  }

  def updateUserEmails(){
      if(authService.userInRole("ROLE_ADMIN")){
          def updated = userService.updateUserEmails()
          render(view:'index', model:[message:"""Updated ${updated} email addresses in system"""])
      } else {
          response.sendError(401)
      }
  }

  def createBulkEmail = {
      if(!authService.userInRole("ROLE_ADMIN")){
          response.sendError(401)
      }
  }

  def createBulkEmailForRegisteredUsers = {
      if(!authService.userInRole("ROLE_ADMIN")){
          response.sendError(401)
      }
  }

  def sendBulkEmailForRegisteredUsers = {
      if(!authService.userInRole("ROLE_ADMIN")){
          response.sendError(401)
      } else {
         // def users = [User.findByEmail("david.martin@csiro.au")]
         User.findAll().each { user ->
             log.info "Sending email to: "+ user.email
             try {
                 sendMail {
                          to user.email.toString()
                          from grailsApplication.config.postie.emailInfoAddressTitle + "<" + grailsApplication.config.postie.emailInfoSender + ">"
                          subject params.emailSubject
                          body (view: "/email/htmlEmail",
                                plugin:"email-confirmation",
                                model:[htmlBody:params.htmlEmailToSend]
                          )
                  }
             } catch (Exception e){
                 log.error("Problem sending email to ${user.email} - ${e.message}")
             }
          }
      }
      redirect(action:'index')
  }

  def fixupBiocacheQueries(){

      def toUpdate = []
      Query.findAllByQueryPathForUI('/occurrences/search?q=*:*&fq=first_loaded_date:[___DATEPARAM___%20TO%20*]&sort=first_loaded_date&dir=desc').each {
          it.queryPathForUI =  it.queryPath.substring(3)
          toUpdate << it
      }
      toUpdate.each {it.save(flush:true)}
      toUpdate.clear()


      Query.findAllByQueryPathForUI('/occurrences/search?q=*:*&fq=user_assertions:true&fq=last_assertion_date:[___DATEPARAM___%20TO%20*]&sort=last_assertion_date&dir=desc').each {
          it.queryPathForUI =  it.queryPath.substring(3)
          toUpdate << it
      }
      toUpdate.each {it.save(flush:true)}
      toUpdate.clear()


      Query.findAllByQueryPathForUI('/occurrences/search?q=*:*&fq=last_assertion_date:[___DATEPARAM___%20TO%20*]&sort=last_assertion_date&dir=desc').each {
          it.queryPathForUI =  it.queryPath.substring(3)
          toUpdate << it
      }
      toUpdate.each {it.save(flush:true)}
      toUpdate.clear()
  }

  def sendBulkEmail = {
      if(!authService.userInRole("ROLE_ADMIN")){
          response.sendError(401)
      } else {
          params.emailsToUse.trim().split("\n").each { email ->
             if(email){
                 try {
                     log.info "Sending email to: " + email
                     sendMail {
                              to email.toString()
                              from grailsApplication.config.postie.emailInfoAddressTitle + "<" + grailsApplication.config.postie.emailInfoSender + ">"
                              subject params.emailSubject
                              body (view: "/email/htmlEmail",
                                    plugin:"email-confirmation",
                                    model:[htmlBody:params.htmlEmailToSend]
                              )
                      }
                 } catch (Exception e){
                     log.error("Problem sending email to ${email} -- ${e.message}")
                 }
             }
          }
      }
  }

  def notificationReport = {
      [queryInstanceList: Query.list()]
  }

  def runChecksNow = {
      if(authService.userInRole("ROLE_ADMIN")){
          log.info("Run checks....")
          if(params.frequency){
            log.info('Manual start of ' + params.frequency + ' checks')
            notificationService.checkQueryForFrequency(params.frequency)
          }
          response.setContentType("text/plain")
          response.setStatus(200)
      } else {
          log.info("Run checks UNAUTHORIZED....")
          response.sendError(401)
      }
      null
  }

  def debugAlertsForUser(){
      if(authService.userInRole("ROLE_ADMIN")){
        User user = User.findByUserId(params.userId)
        if (user){
            log.debug "User id: " + user.email + ", frequency: " + user.frequency
            //trigger this users alerts
            response.setContentType("text/plain")
            notificationService.debugQueriesForUser(user, response.getWriter())
        } else {
            log.error "user with id " + params.userId + " not found."
            response.sendError(404)
        }
      }
  }

  def debugAllAlerts(){
      if(authService.userInRole("ROLE_ADMIN")){
          response.setContentType("text/plain")
          notificationService.checkAllQueries(response.getWriter())
      } else {
          response.sendError(403)
      }
  }

  def debugAlertEmail(){
      if(authService.userInRole("ROLE_ADMIN")){
          def frequency = params.frequency?:'weekly'
          def qcr = notificationService.checkQueryById(params.id, params.frequency?:'weekly')
          def model = emailService.generateEmailModel(qcr.query, frequency, qcr.queryResult)
          render(view: qcr.query.emailTemplate, model: model)
      } else {
          response.sendError(403)
      }
  }

  def debugAlert(){
      if(authService.userInRole("ROLE_ADMIN")){
          [ alerts:[
             hourly: notificationService.checkQueryById(params.id, params.frequency?:'hourly'),
             daily: notificationService.checkQueryById(params.id, params.frequency?:'daily'),
             weekly: notificationService.checkQueryById(params.id, params.frequency?:'weekly'),
             monthly: notificationService.checkQueryById(params.id, params.frequency?:'monthly')
            ]
          ]
      } else {
          response.sendError(403)
      }
  }

  def deleteOrphanAlerts() {
      if(authService.userInRole("ROLE_ADMIN")) {
          int noDeleted = queryService.deleteOrphanedQueries()
          render(view:'index', model:[message:"""Removed ${noDeleted} queries from system"""])
      } else {
          response.sendError(403)
      }
  }

  def showUsersAlerts(){
      if(authService.userInRole("ROLE_ADMIN")){
        User user = User.findByUserId(params.userId)
        if (user){
            def notificationInstanceList = Notification.findAllByUser(user)
              //split into custom and non-custom...
            def enabledQueries = notificationInstanceList.collect { it.query }
            def enabledIds =  enabledQueries.collect { it.id }

              //all types
            def allAlertTypes = Query.findAllByCustom(false)

            allAlertTypes.removeAll { enabledIds.contains(it.id) }
            def customQueries = enabledQueries.findAll { it.custom }
            def standardQueries = enabledQueries.findAll { !it.custom }

            render(view: "../notification/myAlerts", model:[disabledQueries:allAlertTypes,
                      enabledQueries:standardQueries, customQueries:customQueries,
                      frequencies:Frequency.listOrderByPeriodInSeconds(),
                      user:user,
                      adminUser:authService.userDetails()
            ])
        } else {
            log.info "user with id " + params.userId + " not found."
            response.sendError(404)
        }
      } else {
          log.info("Run checks UNAUTHORIZED....")
          response.sendError(401)
      }
      null
  }

  def refreshUserDetails(){
        try {
            def userListJson = doPost(grailsApplication.config.ala.userDetailsURL)
            log.info "Refreshing user ids...."
            if (userListJson && !userListJson.error) {
                def userEmailMap = [:]
                userListJson.resp.each {
                    userEmailMap.put(it.email.toLowerCase(),it.id.toString())
                }
                userEmailMap.each {email, id ->
                    def user = User.findByEmail(email)
                    if (user){
                        user.userId = id
                        user.save(true)
                    }
                }
            } else {
                log.info "error -  " + userListJson.getClass() + ":"+ userListJson
            }
        } catch (Exception e) {
            log.error ("Cache refresh error" + e.message, e)
        }
  }

  def doPost(String url) {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpPost post = new HttpPost(url)
        try {
            def response = httpclient.execute(post)
            def content = response.getEntity().getContent()
            def jsonSlurper = new JsonSlurper()
            def json = jsonSlurper.parse(new InputStreamReader(content))
            return [error:  null, resp: json]
        } catch (SocketTimeoutException e) {
            def error = [error: "Timed out calling web service. URL= \${url}."]
            log.error(error.error)
            return [error: error]
        } catch (Exception e) {
            def error = [error: "Failed calling web service. ${e.getClass()} ${e.getMessage()} ${e} URL= ${url}."]
            log.error(error.error)
            return [error: error]
        } finally {
            post.releaseConnection()
        }
  }

  def unsubscribeUser(String id) {

  }

    /**
     * Used to check if server can send emails externally.
     * Sends to email address of logged-in user
     *
     */
    def sendTestEmail () {
        def msg
        if (authService.userInRole("ROLE_ADMIN")) {
            User user = userService.getUser()
            if (user) {
                def query = Query.get(1)
                def frequency = Frequency.get(1)
                def queryResult = QueryResult.findByQuery(query) ?: new QueryResult(query: query, frequency: frequency)
                emailService.sendGroupEmail(query, [user.email], queryResult, [], frequency, 0, "", "")
                msg = "Email was sent to ${user.email} - check tomcat logs for ERROR message with value \"Error sending email to addresses:\""
            } else {
                msg = "User was not found or not logged in"
            }
        } else {
            msg = "User does not have required ROLE"
        }
        log.debug "#sendTestEmail - msg = ${msg}"
        flash.message = msg
        redirect(action: "index")
    }
}
