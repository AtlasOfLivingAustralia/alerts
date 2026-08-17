package au.org.ala.alerts

import org.springframework.http.HttpStatus

/**
 * Guards the NotificationController actions that resolve a target user through #getUser(), i.e. the
 * actions that accept a 'userId' param.
 *
 * The myAlerts panel is reused by AdminController#showUsersAlerts (/admin/user/$userId), so those
 * actions carry the target user's id. Only an admin may act on a user other than themselves.
 *
 * Doing this here (rather than inside the actions) means the 403 is written once, before the action
 * runs - previously the check lived in NotificationController#getUser(), which committed the
 * response with sendError() and then let the action write a second response.
 *
 * Actions that never call getUser() (myAlerts, index, admin, checkNow,
 * evaluateChangeDetectionAlgorithm) are deliberately NOT matched.
 */
class NotificationInterceptor {

    def authService
    def userService

    NotificationInterceptor() {
        // only the actions that act on the user returned by NotificationController#getUser()
        match(controller: 'notification', action: 'addMyAlert')
        match(controller: 'notification', action: 'deleteMyAlert')
        match(controller: 'notification', action: 'deleteMyAlertWR')
        match(controller: 'notification', action: 'enableAlert')
        match(controller: 'notification', action: 'disableAlert')
        match(controller: 'notification', action: 'subscribeMyAnnotation')
        match(controller: 'notification', action: 'unsubscribeMyAnnotation')
        match(controller: 'notification', action: 'changeFrequency')
    }

    boolean before() {
        String targetUserId = params.userId

        if (!targetUserId) {
            return true
        }

        User currentUser = userService.getUser()
        if (targetUserId == currentUser?.userId || authService.userInRole("ROLE_ADMIN")) {
            return true
        }

        log.warn("User ${currentUser?.userId} attempted to access alerts of user ${targetUserId} without the admin role")
        render status: HttpStatus.FORBIDDEN.value(), text: "You do not have permission to view or manage another user's alerts."
        return false
    }
}

