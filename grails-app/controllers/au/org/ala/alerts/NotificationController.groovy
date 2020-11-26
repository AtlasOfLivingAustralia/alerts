package au.org.ala.alerts

import au.ala.org.ws.security.RequireApiKey
import grails.converters.JSON
import org.apache.http.HttpStatus

class NotificationController {

    def notificationService
    def emailService
    def userService
    def authService

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def myalerts = { redirect(action: "myAlerts", params: params) }

    def myAlerts = {
        User user = userService.getUser()
        log.debug('Viewing my alerts :  ' + user)
        userService.getUserAlertsConfig(user)
    }

    def addMyAlert = {
        def user
        if (authService.userInRole("ROLE_ADMIN")) {
            user = userService.getUserById(params.userId)
        } else {
            user = userService.getUser()
        }

        if (!user) {
            response.status = HttpStatus.SC_NOT_FOUND
            response.sendError(HttpStatus.SC_NOT_FOUND, "Unrecognised user")
        } else {
            notificationService.addAlertForUser(user, params.id)
            return null
        }
    }

    def deleteMyAlert = {
        def user
        if (authService.userInRole("ROLE_ADMIN")) {
            user = userService.getUserById(params.userId)
        } else {
            user = userService.getUser()
        }

        if (!user) {
            response.status = HttpStatus.SC_NOT_FOUND
            response.sendError(HttpStatus.SC_NOT_FOUND, "Unrecognised user")
        } else {
            notificationService.deleteAlertForUser(user, params.id)
            return null
        }
    }

    def deleteMyAlertWR = {
        def user
        if (authService.userInRole("ROLE_ADMIN")) {
            user = userService.getUserById(params.userId)
        } else {
            user = userService.getUser()
        }

        //this is a hack to get around a CAS issue
        if (user == null) {
            user = User.findByEmail(params.userId)
        }

        def query = Query.get(params.id)
        log.debug('Deleting my alert :  ' + params.id + ' for user : ' + user)

        def notificationInstance = Notification.findByUserAndQuery(user, query)
        if (notificationInstance) {
            log.debug('Deleting my notification :  ' + params.id)
            notificationInstance.each { it.delete(flush: true) }
        } else {
            log.error('*** Unable to find  my notification - no delete :  ' + params.id)
        }
        redirect(action: 'myAlerts')
    }

    def changeFrequency = {
        def user = userService.getUser()
        log.debug("Changing frequency to: " + params.frequency + " for user ${user}")
        user.frequency = Frequency.findByName(params.frequency)
        user.save(flush: true)
        return null
    }

    def checkNow = {
        Notification notification = Notification.get(params.id)
        boolean sendUpdateEmail = notificationService.checkStatus(notification.query)
        if (sendUpdateEmail) {
            emailService.sendNotificationEmail(notification)
        }
        redirect(action: "show", params: params)
    }

    def index = {
        //if is ADMIN, then index page
        //else redirect to /notification/myAlerts
        if (authService.userInRole("ADMIN")) {
            redirect(action: "admin")
        } else {
            redirect(action: "myAlerts")
        }
    }

    def admin = {

    }

    @RequireApiKey
    def addMyAlertWS() {
        User user = userService.getUserById(params.userId)
        notificationService.addAlertForUser(user, Long.valueOf(params.queryId))
        render([success: true] as JSON)
    }

    @RequireApiKey
    def deleteMyAlertWS() {
        User user = userService.getUserById(params.userId)
        notificationService.deleteAlertForUser(user, Long.valueOf(params.queryId))
        render([success: true] as JSON)
    }
}
