package au.org.ala.alerts

import au.org.ala.web.AlaSecured

@AlaSecured(value = 'ROLE_ADMIN', redirectController = 'notification', redirectAction = 'myAlerts', message = "You don't have permission to view that page.")
class UserController {

    static scaffold = User
}
