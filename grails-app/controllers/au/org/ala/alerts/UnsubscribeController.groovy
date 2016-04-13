package au.org.ala.alerts

import org.apache.http.HttpStatus

class UnsubscribeController {

    static allowedMethods = [index: "GET", unsubscribe: "POST"]

    UserService userService

    def index() {
        User user = userService.getUser()

        if (!user && !params.token || (user && params.token && user.unsubscribeToken != params.token)) {
            response.status = HttpStatus.SC_BAD_REQUEST
            response.sendError(HttpStatus.SC_BAD_REQUEST)
        } else {
            Map userAndNotifications = user ? [user: user, notifications: user.notifications] : findUserAndNotificationsForToken(params.token)

            if (userAndNotifications) {
                render view: "/unsubscribe/index", model: userAndNotifications
            } else {
                response.status = HttpStatus.SC_BAD_REQUEST
                response.sendError(HttpStatus.SC_BAD_REQUEST, "Unrecognized token")
            }
        }
    }

    def unsubscribe() {
        User user = userService.getUser()

        if (!user && !params.token || (user && params.token && user.unsubscribeToken != params.token)) {
            response.status = HttpStatus.SC_BAD_REQUEST
            response.sendError(HttpStatus.SC_BAD_REQUEST)
        } else {
            Map userAndNotifications = user ? [user: user, notifications: user.notifications] : findUserAndNotificationsForToken(params.token)

            if (userAndNotifications.notifications) {
                Notification.deleteAll(userAndNotifications.notifications)
                userAndNotifications.user.notifications?.clear()
                userAndNotifications.user.save(flush: true)

                render view: "unsubscribed"
            } else {
                response.status = HttpStatus.SC_NOT_FOUND
                response.sendError(HttpStatus.SC_NOT_FOUND, "Unrecognised token")
            }
        }
    }

    def cancel() {
        redirect(controller: "notification", action:'myAlerts')
    }

    private Map findUserAndNotificationsForToken(String token) {
        User user

        List<Notification> notifications = Notification.findAllByUnsubscribeToken(token)
        if (notifications) {
            user = notifications[0].user
        } else {
            user = User.findByUnsubscribeToken(token)

            if (user) {
                notifications = user.notifications as List
            }
        }

        [user: user, notifications: notifications]
    }

}
