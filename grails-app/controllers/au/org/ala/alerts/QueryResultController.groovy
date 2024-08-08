package au.org.ala.alerts

import au.org.ala.web.AlaSecured
import grails.converters.JSON


class QueryResultController {
    static scaffold = QueryResult
    def queryResultService

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
