package au.org.ala.alerts

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.apache.http.HttpStatus
import spock.lang.Specification

@TestFor(UnsubscribeController)
@Mock([Notification, User, Query, QueryResult])
class UnsubscribeControllerSpec extends Specification {

    def setup() {
        controller.userService = Mock(UserService)
        controller.queryService = Mock(QueryService)
        controller.notificationService = Mock(NotificationService)
    }

    def "index() should return a HTTP 400 (BAD_REQUEST) if there is no logged in user and no token"() {
        setup:
        controller.userService.getUser() >> null

        when:
        controller.index()

        then:
        response.status == HttpStatus.SC_BAD_REQUEST
    }

    def "index() should return a HTTP 400 (BAD_REQUEST) if there is no logged in user and the token is invalid"() {
        setup:
        controller.userService.getUser() >> null

        when:
        params.token = "invalid"
        controller.index()

        then:
        response.status == HttpStatus.SC_BAD_REQUEST
    }

    def "index() should render the index view with a user but no notifications when there is logged in user and no token"() {
        setup:
        controller.userService.getUser() >> new User(userId: "1", email: "fred@bla.com")

        when:
        controller.index()

        then:
        response.status == HttpStatus.SC_OK
        controller.modelAndView.viewName == "/unsubscribe/index"
        controller.modelAndView.model.user.userId == "1"
        !controller.modelAndView.model.notifications
    }

    def "index() should render the index view with a user but no notifications if the token matches the token for a user with no notifications"() {
        setup:
        controller.userService.getUser() >> null
        User user = new User(userId: "user1", email: "fred@bla.com", notifications: []).save(failOnError: true, flush: true)

        when:
        params.token = user.unsubscribeToken
        controller.index()

        then:
        response.status == HttpStatus.SC_OK
        controller.modelAndView.viewName == "/unsubscribe/index"
        controller.modelAndView.model.user.userId == "user1"
        !controller.modelAndView.model.notifications
    }

    def "index() should render the index view with a user and all their notifications if the token matches the token for a user with notifications"() {
        setup:
        controller.userService.getUser() >> null
        Query query1 = newQuery("query1")
        Query query2 = newQuery("query2")
        User user = new User(userId: "user1", email: "fred@bla.com")
        user.save(failOnError: true, flush: true)
        Notification notification1 = new Notification(user: user, query: query1)
        notification1.save(failOnError: true, flush: true)
        Notification notification2 = new Notification(user: user, query: query2)
        notification2.save(failOnError: true, flush: true)
        user.addToNotifications(notification1)
        user.addToNotifications(notification2)
        user.save(failOnError: true, flush: true)

        when:
        params.token = user.unsubscribeToken
        controller.index()

        then:
        response.status == HttpStatus.SC_OK
        controller.modelAndView.viewName == "/unsubscribe/index"
        controller.modelAndView.model.user.userId == "user1"
        controller.modelAndView.model.notifications.size() == 2
    }

    def "index() should render the index view with only 1 notification if the token matches the token for a notification"() {
        setup:
        controller.userService.getUser() >> null
        Query query1 = newQuery("query1")
        Query query2 = newQuery("query2")
        User user = new User(userId: "user1", email: "fred@bla.com")
        user.save(failOnError: true, flush: true)
        Notification notification1 = new Notification(user: user, query: query1)
        notification1.save(failOnError: true, flush: true)
        Notification notification2 = new Notification(user: user, query: query2)
        notification2.save(failOnError: true, flush: true)
        user.addToNotifications(notification1)
        user.addToNotifications(notification2)
        user.save(failOnError: true, flush: true)

        when:
        params.token = notification1.unsubscribeToken
        controller.index()

        then:
        response.status == HttpStatus.SC_OK
        controller.modelAndView.viewName == "/unsubscribe/index"
        controller.modelAndView.model.user.userId == "user1"
        controller.modelAndView.model.notifications.size() == 1
        println controller.modelAndView.model.notifications
        controller.modelAndView.model.notifications[0].query.name == "query1"
    }

    def "index() should render the index view with only 1 notification if the token matches the token for a notification even if the logged in user has multiple notifications"() {
        setup:
        Query query1 = newQuery("query1")
        Query query2 = newQuery("query2")
        User user = new User(userId: "user1", email: "fred@bla.com")
        user.save(failOnError: true, flush: true)
        Notification notification1 = new Notification(user: user, query: query1)
        notification1.save(failOnError: true, flush: true)
        Notification notification2 = new Notification(user: user, query: query2)
        notification2.save(failOnError: true, flush: true)
        user.addToNotifications(notification1)
        user.addToNotifications(notification2)
        user.save(failOnError: true, flush: true)
        controller.userService.getUser() >> user

        when:
        params.token = notification1.unsubscribeToken
        controller.index()

        then:
        response.status == HttpStatus.SC_OK
        controller.modelAndView.viewName == "/unsubscribe/index"
        controller.modelAndView.model.user.userId == "user1"
        controller.modelAndView.model.notifications.size() == 1
        println controller.modelAndView.model.notifications
        controller.modelAndView.model.notifications[0].query.name == "query1"
    }

    def "index() should return HTTP 403 (FORBIDDEN) if there is a logged in user but the token belongs to a different user (user level token)"() {
        setup:
        User user1 = new User(userId: "user1", email: "fred@bla.com")
        user1.save(failOnError: true, flush: true)

        User user2 = new User(userId: "user2", email: "fred@bla.com")
        user2.save(failOnError: true, flush: true)

        controller.userService.getUser() >> user1

        when: "the logged in user is user1 but the token belongs to user2"
        params.token = user2.unsubscribeToken
        controller.index()

        then:
        response.status == HttpStatus.SC_FORBIDDEN
    }

    def "index() should return HTTP 403 (FORBIDDEN) if there is a logged in user but the token belongs to a different user (notification level token)"() {
        setup:
        Query query1 = newQuery("query1")
        Query query2 = newQuery("query2")
        User user1 = new User(userId: "user1", email: "fred@bla.com")
        user1.save(failOnError: true, flush: true)
        Notification notification1 = new Notification(user: user1, query: query1)
        notification1.save(failOnError: true, flush: true)
        Notification notification2 = new Notification(user: user1, query: query2)
        notification2.save(failOnError: true, flush: true)
        user1.addToNotifications(notification1)
        user1.addToNotifications(notification2)
        user1.save(failOnError: true, flush: true)

        User user2 = new User(userId: "user2", email: "fred@bla.com")
        user2.save(failOnError: true, flush: true)
        Notification notification3 = new Notification(user: user2, query: query1)
        notification3.save(failOnError: true, flush: true)
        user2.addToNotifications(notification3)
        user2.save(failOnError: true, flush: true)

        controller.userService.getUser() >> user1

        when: "the logged in user is user1 but the token belongs to user2"
        params.token = notification3.unsubscribeToken
        controller.index()

        then:
        response.status == HttpStatus.SC_FORBIDDEN
    }

    def "unsubscribe() should return a HTTP 400 (BAD_REQUEST) if there is no logged in user and no token"() {
        setup:
        controller.userService.getUser() >> null

        when:
        request.method = 'POST'
        controller.unsubscribe()

        then:
        response.status == HttpStatus.SC_BAD_REQUEST
    }

    def "unsubscribe() should return a HTTP 400 (BAD_REQUEST) if there is no logged in user and the token is invalid"() {
        setup:
        controller.userService.getUser() >> null

        when:
        params.token = "invalid"
        request.method = 'POST'
        controller.unsubscribe()

        then:
        response.status == HttpStatus.SC_BAD_REQUEST
    }

    def "unsubscribe() should do nothing when there is logged in user with no notifications and no token"() {
        setup:
        controller.userService.getUser() >> new User(userId: "1", email: "fred@bla.com")

        when:
        controller.index()

        then:
        response.status == HttpStatus.SC_OK
    }

    def "unsubscribe() should do nothing if the token matches the token for a user with no notifications"() {
        setup:
        controller.userService.getUser() >> null
        User user = new User(userId: "user1", email: "fred@bla.com", notifications: []).save(failOnError: true, flush: true)

        when:
        params.token = user.unsubscribeToken
        controller.index()

        then:
        response.status == HttpStatus.SC_OK
    }

    def "unsubscribe() should delete all the user's notifications if the token matches the token for a user with notifications"() {
        setup:
        controller.userService.getUser() >> null
        controller.queryService.createMyAnnotationQuery(_ as String) >> new Query([name: 'emptyquery'])
        Query query1 = newQuery("query1")
        Query query2 = newQuery("query2")
        User user1 = new User(userId: "user1", email: "fred@bla.com")
        user1.save(failOnError: true, flush: true)
        Notification notification1 = new Notification(user: user1, query: query1)
        notification1.save(failOnError: true, flush: true)
        Notification notification2 = new Notification(user: user1, query: query2)
        notification2.save(failOnError: true, flush: true)
        user1.addToNotifications(notification1)
        user1.addToNotifications(notification2)
        user1.save(failOnError: true, flush: true)

        User user2 = new User(userId: "user2", email: "fred@bla.com")
        user2.save(failOnError: true, flush: true)
        Notification notification3 = new Notification(user: user2, query: query1)
        notification3.save(failOnError: true, flush: true)
        user2.addToNotifications(notification3)
        user2.save(failOnError: true, flush: true)

        when:
        params.token = user1.unsubscribeToken
        request.method = 'POST'
        controller.unsubscribe()

        then:
        log.info "token = ${params.token}"
        response.status == HttpStatus.SC_OK
        User.count() == 2
        !User.findByUserId("user1").notifications
        User.findByUserId("user2").notifications.size() == 1
        Notification.count() == 1
    }

    def "unsubscribe() should delete my annotaions alert if the token matches the token for a user with notifications"() {
        setup:
        User user = new User(userId: "userid", email: "fred@bla.com", frequency: new Frequency([name: 'hourly']))
        Query query = new Query([
                name: 'testquery',
                queryPath: '/occurrences/search?fq=assertion_user_id:' + 'userid' + '&dir=desc&facets=basis_of_record',
                updateMessage: 'updateMessage',
                baseUrl: 'baseUrl',
                baseUrlForUI: 'baseUrlForUI',
                resourceName: 'resourceName',
                queryPathForUI: 'queryPathForUI'
        ])

        controller.userService.getUser() >> null
        controller.queryService.constructMyAnnotationQueryPath(_ as String) >> '/occurrences/search?fq=assertion_user_id:' + user.userId + '&dir=desc&facets=basis_of_record'

        user.save(failOnError: true, flush: true)
        query.save(failOnError: true, flush: true)
        Notification notification = new Notification(user: user, query: query)
        notification.save(failOnError: true, flush: true)
        QueryResult queryResult = new QueryResult([query: query, frequency: user.frequency])
        queryResult.save(failOnError: true, flush: true)

        when:
        params.token = notification.unsubscribeToken
        request.method = 'POST'
        controller.unsubscribe()

        then:
        log.info "token = ${params.token}"
        1 * controller.notificationService.unsubscribeMyAnnotation(user)
        response.status == HttpStatus.SC_OK
    }

    def "unsubscribe() should delete only 1 notification if the token matches the token for a notification"() {
        setup:
        controller.userService.getUser() >> null
        controller.queryService.createMyAnnotationQuery(_ as String) >> new Query([name: 'emptyquery'])
        Query query1 = newQuery("query1")
        Query query2 = newQuery("query2")
        User user = new User(userId: "user1", email: "fred@bla.com")
        user.save(failOnError: true, flush: true)
        Notification notification1 = new Notification(user: user, query: query1)
        notification1.save(failOnError: true, flush: true)
        Notification notification2 = new Notification(user: user, query: query2)
        notification2.save(failOnError: true, flush: true)
        user.addToNotifications(notification1)
        user.addToNotifications(notification2)
        user.save(failOnError: true, flush: true)

        when:
        params.token = notification1.unsubscribeToken
        request.method = 'POST'
        controller.unsubscribe()

        then:
        response.status == HttpStatus.SC_OK
        User.count() == 1
        User.findByUserId("user1").notifications.size() == 1
        Notification.count() == 1
    }

    def "unsubscribe() should delete only 1 notification if the token matches the token for a notification even if the logged in user has multiple notifications"() {
        setup:
        Query query1 = newQuery("query1")
        Query query2 = newQuery("query2")
        User user = new User(userId: "user1", email: "fred@bla.com")
        user.save(failOnError: true, flush: true)
        Notification notification1 = new Notification(user: user, query: query1)
        notification1.save(failOnError: true, flush: true)
        Notification notification2 = new Notification(user: user, query: query2)
        notification2.save(failOnError: true, flush: true)
        user.addToNotifications(notification1)
        user.addToNotifications(notification2)
        user.save(failOnError: true, flush: true)
        controller.userService.getUser() >> user
        controller.queryService.createMyAnnotationQuery(_ as String) >> new Query([name: 'emptyquery'])

        when:
        params.token = notification1.unsubscribeToken
        request.method = 'POST'
        controller.unsubscribe()

        then:
        response.status == HttpStatus.SC_OK
        User.count() == 1
        User.findByUserId("user1").notifications.size() == 1
        Notification.count() == 1
    }

    def "unsubscribe() should return HTTP 403 (FORBIDDEN) if there is a logged in user but the token belongs to a different user (user level token)"() {
        setup:
        User user1 = new User(userId: "user1", email: "fred@bla.com")
        user1.save(failOnError: true, flush: true)

        User user2 = new User(userId: "user2", email: "fred@bla.com")
        user2.save(failOnError: true, flush: true)

        controller.userService.getUser() >> user1

        when: "the logged in user is user1 but the token belongs to user2"
        params.token = user2.unsubscribeToken
        request.method = 'POST'
        controller.unsubscribe()

        then:
        response.status == HttpStatus.SC_FORBIDDEN
    }

    def "unsubscribe() should return HTTP 403 (FORBIDDEN) if there is a logged in user but the token belongs to a different user (notification level token)"() {
        setup:
        Query query1 = newQuery("query1")
        Query query2 = newQuery("query2")
        User user1 = new User(userId: "user1", email: "fred@bla.com")
        user1.save(failOnError: true, flush: true)
        Notification notification1 = new Notification(user: user1, query: query1)
        notification1.save(failOnError: true, flush: true)
        Notification notification2 = new Notification(user: user1, query: query2)
        notification2.save(failOnError: true, flush: true)
        user1.addToNotifications(notification1)
        user1.addToNotifications(notification2)
        user1.save(failOnError: true, flush: true)

        User user2 = new User(userId: "user2", email: "fred@bla.com")
        user2.save(failOnError: true, flush: true)
        Notification notification3 = new Notification(user: user2, query: query1)
        notification3.save(failOnError: true, flush: true)
        user2.addToNotifications(notification3)
        user2.save(failOnError: true, flush: true)

        controller.userService.getUser() >> user1

        when: "the logged in user is user1 but the token belongs to user2"
        params.token = notification3.unsubscribeToken
        request.method = 'POST'
        controller.unsubscribe()

        then:
        response.status == HttpStatus.SC_FORBIDDEN
    }

    private Query newQuery(String name) {
        Query query = new Query(name: name, updateMessage: "a", queryPathForUI: "a", queryPath: "a", baseUrl: "a", baseUrlForUI: "a", resourceName: "a")
        query.save(failOnError: true, flush: true)
        query
    }
}
