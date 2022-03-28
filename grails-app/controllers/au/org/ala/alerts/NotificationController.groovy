package au.org.ala.alerts

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
        def user = getUser()

        if (!user) {
            response.status = HttpStatus.SC_NOT_FOUND
            response.sendError(HttpStatus.SC_NOT_FOUND, "Unrecognised user")
        } else {
            notificationService.addAlertForUser(user, Long.valueOf(params.id))
            return null
        }
    }

    def deleteMyAlert = {
        def user = getUser()

        if (!user) {
            response.status = HttpStatus.SC_NOT_FOUND
            response.sendError(HttpStatus.SC_NOT_FOUND, "Unrecognised user")
        } else {
            notificationService.deleteAlertForUser(user, Long.valueOf(params.id))
        }
    }

    def subscribeMyAnnotation = {
        def user = getUser()
        try {
            notificationService.subscribeMyAnnotation(user)
            render ([success: true] as JSON)
        } catch (ignored) {
            response.sendError(HttpStatus.SC_INTERNAL_SERVER_ERROR, "failed to subscribe to 'my annotation' alert for user " + user?.getUserId())
        }

    }

    def unsubscribeMyAnnotation = {
        def user = getUser()
        try {
            notificationService.unsubscribeMyAnnotation(user)
            render ([success: true] as JSON)
        } catch (ignored) {
            response.sendError(HttpStatus.SC_INTERNAL_SERVER_ERROR, "failed to unsubscribe 'my annotation' alert for user " + user?.getUserId())
        }
    }

    def deleteMyAlertWR = {
        def user = getUser()

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

    private User getUser() {
        if (authService.userInRole("ROLE_ADMIN")) {
            return userService.getUserById(params.userId)
        } else {
            return userService.getUser()
        }
    }

    def changeFrequency = {
        def user = userService.getUser()
        log.debug("Changing frequency to: " + params.frequency + " for user ${user}")
        notificationService.updateFrequency(user, params.frequency)
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
}
