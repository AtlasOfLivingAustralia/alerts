package au.org.ala.alerts

import au.org.ala.web.AlaSecured
import grails.converters.JSON
import grails.plugin.scaffolding.annotation.Scaffold

@Scaffold(User)
@AlaSecured(value = 'ROLE_ADMIN', redirectController = 'notification', redirectAction = 'myAlerts', message = "You don't have permission to view that page.")
class UserController {
    def userService

    def index() {
        redirect(action: "list", params: params)
    }

    /**
     * Test EhCache caching in UserService = check logs to see if userService.testEhCache()
     * method internals are run or not (5 min cache expiry).
     */
    def testCache = {
        render([response: userService.testEhCache(params.q)] as JSON)
    }
}
