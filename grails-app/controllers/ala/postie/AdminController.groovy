package ala.postie

import grails.converters.JSON
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.DefaultHttpClient


class AdminController {

  def index() { }

  def grailsApplication

  def authService

  def notificationService

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
          response.sendError(401, "Unauthorized")
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
      } else {
          log.info("Debug alerts UNAUTHORIZED....")
          response.sendError(401, "Unauthorized")
      }
  }

  def debugAllAlerts(){
      if(authService.userInRole("ROLE_ADMIN")){
          response.setContentType("text/plain")
          notificationService.checkAllQueries(response.getWriter())
      } else {
          response.sendError(401, "Unauthorized")
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
            response.sendError(404, "Record with id: " + params.userId + " was not found.")
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
                    println email
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
            println error.error
            return [error: error]
        } finally {
            post.releaseConnection()
        }
  }

//  def saveUsers = {
//
//    def emailAddresses = params.usersToAdd.toString().split("\n")
//
//    def monthlyFrequency = Frequency.findByName("monthly")
//
//    def blogQuery = Query.findByName("Blogs and News")
//
//    log.debug("Retrieved blog query: " + blogQuery)
//
//    emailAddresses.each { email ->
//      if (email.trim().length() >0){
//        log.debug('Adding user: ' + email.trim().toLowerCase())
//        //add to the DB
//        User user = User.findByEmail(email.trim())
//        if (user == null){
//          user = new User([email:email.trim().toLowerCase(), frequency:monthlyFrequency ])
//          user.save(flush:true)
//
//          //add notification to blogs
//          Notification n = new Notification([user: user, query:blogQuery])
//          n.save(flush: true)
//        }
//      }
//    }
//  }
}
