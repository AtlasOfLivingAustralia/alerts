package au.org.ala.alerts

import grails.gorm.transactions.Transactional
import io.micronaut.http.HttpStatus

@Transactional
class UnsubscribeController {

    static allowedMethods = [index: "GET", unsubscribe: "POST"]

    UserService userService
    NotificationService notificationService
    QueryService queryService

    def index() {
        Map userAndNotifications = findUserAndNotificationsForToken(params.token)

        if (!userAndNotifications?.user) {
            response.status = HttpStatus.BAD_REQUEST.code
            flash.message = message(code: 'email.unsubscribe.fail.alreadyunsubscribed', default: 'Unable to unsubscribe. You may have already unsubscribed.')
            render view: '../error'
        } else {
            render view: "/unsubscribe/index", model: userAndNotifications
        }
    }

    def unsubscribe() {
        Map userAndNotifications = findUserAndNotificationsForToken(params.token)

        if (!userAndNotifications?.user) {
            response.status = HttpStatus.BAD_REQUEST.code
            flash.message = message(code: 'email.unsubscribe.fail.alreadyunsubscribed', default: 'Unable to unsubscribe. You may have already unsubscribed.')
            render view: '../error'
        } else {
            if (userAndNotifications.notifications) {
                // delete notifications and update user
                Notification.deleteAll(userAndNotifications.notifications)
                userAndNotifications.user.notifications?.removeAll(userAndNotifications.notifications)
                userAndNotifications.user.save(flush: true)

                // for my annotation, we also need to delete the query and query result
                String myAnnotationQueryPath = queryService.constructMyAnnotationQueryPath(userAndNotifications.user.userId)
                if (userAndNotifications.notifications.any { it.query.queryPath == myAnnotationQueryPath }) {
                    notificationService.unsubscribeMyAnnotation(userAndNotifications.user)
                }
                render view: "unsubscribed"
            }
        }
    }

    def cancel() {
        redirect(controller: "notification", action: 'myAlerts')
    }

    private Map findUserAndNotificationsForToken(String token) {
        Map userAndNotifications = [:]

        List<Notification> notifications
        User user
        if (token) {
            notifications = Notification.findAllByUnsubscribeToken(token)
            if (notifications) {
                userAndNotifications = [user: notifications[0].user, notifications: notifications]
            } else {
                user = User.findByUnsubscribeToken(token)

                if (user) {
                    userAndNotifications = [user: user, notifications: user.notifications]
                }
            }
        } else {
            user = userService.getUser()
            if (user) {
                userAndNotifications = [user: user, notifications: user?.notifications]
            }
        }

        userAndNotifications
    }
}
