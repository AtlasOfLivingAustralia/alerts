package au.org.ala.alerts

import grails.converters.JSON
import io.micronaut.http.HttpStatus

class NotificationController {

    def notificationService
    def emailService
    def userService
    def authService
    def diffService
    def queryResultService

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
            response.status = HttpStatus.NOT_FOUND.code
            response.sendError(HttpStatus.NOT_FOUND.code, "Unrecognised user")
        } else {
            notificationService.addAlertForUser(user, Long.valueOf(params.id))
            return null
        }
    }

    def deleteMyAlert = {
        def user = getUser()

        if (!user) {
            response.status = HttpStatus.NOT_FOUND.code
            response.sendError(HttpStatus.NOT_FOUND.code, "Unrecognised user")
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
            response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.code, "failed to subscribe to 'my annotation' alert for user " + user?.getUserId())
        }

    }

    def unsubscribeMyAnnotation = {
        def user = getUser()
        try {
            notificationService.unsubscribeMyAnnotation(user)
            render ([success: true] as JSON)
        } catch (ignored) {
            response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.code, "failed to unsubscribe 'my annotation' alert for user " + user?.getUserId())
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
            Notification.withTransaction {
                notificationInstance.each { it.delete(flush: true) }
            }

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
        def user = getUser()
        log.debug("Changing frequency to: " + params.frequency + " for user ${user}")
        notificationService.updateFrequency(user, params.frequency)
        return null
    }

    /**
     * todo check if it works?
     */
    def checkNow = {
        Notification notification = Notification.get(params.id)
        // no such method
        boolean sendUpdateEmail = notificationService.executeQuery(notification.query)?.hasChanged
        if (sendUpdateEmail) {
            emailService.sendNotificationEmail(notification)
        }
        redirect(action: "show", params: params)
    }

    /**
     * Debug the algorithm used to detect changes in the latest result / previous result
     *
     */
    def evaluateChangeDetectionAlgorithm = {
        def query = Query.get(params.queryId)

        def queryResult = queryResultService.get(params.queryResultId)
        //NOTE: this is a hack since the lastResult will be copied into the previousResult in RefreshProperties method
        //We need to hack the current result with previousResult.
        def lastResult = queryResult.decompress(queryResult.lastResult)
        queryResult.lastResult = queryResult.previousResult
        //Assume ONLY one property value
        queryResult.propertyValues?[0]?.currentValue = queryResult.propertyValues?[0]?.previousValue

        notificationService.refreshProperties(queryResult, lastResult)
        boolean hasChanged = diffService.hasChanged(queryResult)

        //Update the results
        queryResult.previousResult = queryResult.lastResult
        queryResult.lastResult =  queryResult.compress(lastResult)

        def records = notificationService.collectUpdatedRecords(queryResult)

        def emailSent = false
        if (params.emailMe) {
            User currentUser = userService.getUser()
            if (currentUser) {
                def recipient =
                        [email: currentUser.email, userUnsubToken: currentUser.unsubscribeToken, notificationUnsubToken: '']
                //Pseudo Frequency
                Frequency fre = Frequency.findByName("weekly")
                emailService.sendGroupNotification(queryResult, fre, [recipient])
                emailSent = true
            } else {
                log.warn("No user found to send email to.")
            }
        }

        def results = ["hasChanged": hasChanged, emailSent: emailSent, totalRecords: queryResult.totalRecords, "brief": queryResult.brief(),  "records": records]
        render results as JSON
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
