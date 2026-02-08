/**
 * todo reviews methods to check if they are used still
 */

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

    // Main action to show the user's alerts
    def myAlerts() {
        // Get the currently logged-in user
        User user = userService?.getUser()
        if (user) {
            // Retrieve the user's alert configuration
            Map userConfig = userService.getUserAlertsConfig(user)
            userConfig.put('isMyAlerts', true)

            render(view: "../notification/myAlerts", model: userConfig)
        } else {
            render status: HttpStatus.UNAUTHORIZED
        }

    }


    def addMyAlert() {
        def user = getUser()

        if (!user) {
            render status: HttpStatus.NOT_FOUND.value(), text: "Unrecognised user"
            return
        }

        notificationService.addAlertForUser(user, params.id as Long)
        render status: HttpStatus.OK.value(), text: "Alert added successfully"
    }


    // Deletes an alert for the currently logged-in user
    def deleteMyAlert() {
        // Get the current user
        def user = getUser()

        if (!user) {
            // Return 404 if user is not found
            response.status = HttpStatus.NOT_FOUND.code
            response.sendError(HttpStatus.NOT_FOUND.code, "Unrecognised user")
            return
        }

        // Delete the alert for this user
        notificationService.deleteAlertForUser(user, params.id as Long)
    }

    def subscribeMyAnnotation()  {
        def user = getUser()
        try {
            notificationService.subscribeMyAnnotation(user)
            render ([success: true] as JSON)
        } catch (ignored) {
            response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.code, "failed to subscribe to 'my annotation' alert for user " + user?.getUserId())
        }
    }

    def unsubscribeMyAnnotation() {
        def user = getUser()
        try {
            def done = notificationService.unsubscribeMyAnnotation(user)
            render ([success: done] as JSON)
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
        // Since the previous and current results are loaded from database,
        // the diffService.getRecordChanges() should be called to get the new records
        queryResult.newRecords = diffService.diff(queryResult)
        queryResult.succeeded = true
        boolean hasChanged =  queryResult.newRecords.size() > 0
        if (hasChanged != queryResult.hasChanged) {
            log.error("Warning: Calculated hasChanged flag is not consistent with result in database. Calculated: ${hasChanged}, in database: ${queryResult.hasChanged}")
        }

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

        def results = ["hasChanged": hasChanged, emailSent: emailSent, totalRecords: queryResult.totalRecords, "brief": queryResult.brief(),  "records": queryResult.newRecords]
        render results as JSON
    }

    def index(){
        redirect(action: "myAlerts")
    }

    def admin = {
        if (!authService.userInRole("ROLE_ADMIN")) {
            redirect(action: "myAlerts")
        }
    }
}
