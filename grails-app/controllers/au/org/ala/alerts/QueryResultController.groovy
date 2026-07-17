package au.org.ala.alerts

import au.org.ala.web.AlaSecured
import grails.converters.JSON
import grails.plugin.scaffolding.annotation.Scaffold

@Scaffold(QueryResult)
@AlaSecured(value = 'ROLE_ADMIN', redirectController = 'notification', redirectAction = 'myAlerts', message = "You don't have permission to view that page.")
class QueryResultController {
    def queryResultService

    def index() {
        redirect(action: "list", params: params)
    }

    def getDetails = {
        def queryResult = queryResultService.get(params.id)
        if (!queryResult) {
            response.status = 404
            response.sendError(404, "Query result not found")
        } else {
            render queryResult.details() as JSON
        }
    }
}
