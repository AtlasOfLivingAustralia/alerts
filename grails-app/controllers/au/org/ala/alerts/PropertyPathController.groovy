package au.org.ala.alerts

import au.org.ala.web.AlaSecured
import grails.plugin.scaffolding.annotation.Scaffold

@Scaffold(PropertyPath)
@AlaSecured(value = 'ROLE_ADMIN', redirectController = 'notification', redirectAction = 'myAlerts', message = "You don't have permission to view that page.")
class PropertyPathController {
    def index() {
        redirect(action: "list", params: params)
    }
}
