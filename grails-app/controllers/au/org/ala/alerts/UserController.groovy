package au.org.ala.alerts

import au.org.ala.web.AlaSecured
import grails.converters.JSON

@AlaSecured(value = 'ROLE_ADMIN', redirectController = 'notification', redirectAction = 'myAlerts', message = "You don't have permission to view that page.")
class UserController {
    def userService

    static scaffold = User

    /**
     * Test EhCache caching in UserService = check logs to see if userService.testEhCache()
     * method internals are run or not (5 min cache expiry).
     */
    def testCache = {
       render ([response: userService.testEhCache(params.q)] as JSON)
    }
}
