/*
 * Copyright (C) 2017 Atlas of Living Australia
 * All Rights Reserved.
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 */

package au.org.ala.alerts

import au.ala.org.ws.security.RequireApiKey
import au.org.ala.plugins.openapi.Path
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import io.micronaut.http.HttpStatus
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement

import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH
import static io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY
import static io.swagger.v3.oas.annotations.enums.ParameterIn.HEADER

@Transactional
class WebserviceController {

    def queryService
    def userService
    def notificationService

    def index = {}
    def test = {}

    private def getMyAlertsLink = {
        getServerRoot() + '/notification/myAlerts'
    }

    private def getServerRoot = {
        grailsApplication.config.serverName + grailsApplication.config.contextPath
    }

    def listAlertsForUser = {
    }

    def getUserAlerts = {
        def model = [:]
        User user = userService.getUserById(params.userId)
        log.debug('#getUserAlerts - Viewing my alerts :  ' + user)
        model = userService.getUserAlertsConfig(user)
        render model as JSON
    }

    /**
     * Service that returns a JSON callback response allowing consuming apps to create links
     * to create an alert or remove an alert
     */
    def taxonAlerts = {

        log.debug("TaxonAlerts lookup for...." + params.guid)

        String taxonGuid = params.guid
        if (taxonGuid == null) taxonGuid = params.taxonGuid

        //check for notifications for this query and this user
        Query query = queryService.createTaxonQuery(taxonGuid, params.taxonName)

        Notification notification = queryService.getNotificationForUser(query, retrieveUser(params))

        String link = null
        if (notification != null) {
            //construct a link to delete the alert & manage alerts
            link = getMyAlertsLink()
        } else {
            //construct a create alert link
            link = getServerRoot() + '/webservice/createTaxonAlert?redirect=' +
                    params.redirect + '&taxonGuid=' + taxonGuid + '&taxonName=' + params.taxonName
        }

        render(view: 'alerts', model: [link: link, deleteLink: getDeleteLink(notification), displayName: params.taxonName, notification: notification])
    }

    def createTaxonAlert = {
        if ((params.guid || params.taxonGuid) && params.taxonName) {

            String taxonGuid = params.guid
            if (taxonGuid == null) taxonGuid = params.taxonGuid

            Query newQuery = queryService.createTaxonQuery(taxonGuid, params.taxonName)
            queryService.createQueryForUserIfNotExists(newQuery, userService.getUser())
            redirectIfSupplied(params)
        } else {
            response.sendError(400)
        }
    }

    def regionAlerts = {

        log.debug("RegionAlerts lookup for...." + params.layerId)

        //check for notifications for this query and this user
        Query query = queryService.createRegionQuery(params.layerId, params.regionName)

        Notification notification = queryService.getNotificationForUser(query, retrieveUser(params))

        String link = null
        if (notification != null) {
            //construct a link to delete the alert & manage alerts
            link = getMyAlertsLink()
        } else {
            //construct a create alert link
            link = getServerRoot() + '/webservice/createRegionAlert?layerId=' + params.layerId +
                    '&regionName=' + params.regionName +
                    '&redirect=' + params.redirect
        }

        render(view: 'alerts', model: [link: link, deleteLink: getDeleteLink(notification), displayName: params.regionName, notification: notification])
    }

    def taxonRegionAlerts = {

        log.debug("TaxonRegionAlerts lookup for...." + params.layerId)

        //check for notifications for this query and this user
        Query query = queryService.createTaxonRegionQuery(params.taxonGuid, params.taxonName, params.layerId, params.regionName)

        Notification notification = queryService.getNotificationForUser(query, retrieveUser(params))

        String link = null
        if (notification != null) {
            //construct a link to delete the alert & manage alerts
            link = getMyAlertsLink()
        } else {
            //construct a create alert link
            link = getServerRoot() + '/webservice/createTaxonRegionAlert' +
                    '?layerId=' + params.layerId +
                    '&regionName=' + params.regionName +
                    '&taxonGuid=' + params.taxonGuid +
                    '&taxonName=' + params.taxonName +
                    '&redirect=' + params.redirect
        }

        String displayName = params.taxonName + " in " + params.regionName

        render(view: 'alerts', model: [link: link, deleteLink: getDeleteLink(notification), displayName: displayName, notification: notification])
    }

    def speciesGroupRegionAlerts = {

        log.debug("SpeciesGroupRegionAlerts lookup for...." + params.layerId)

        //check for notifications for this query and this user
        Query query = queryService.createSpeciesGroupRegionQuery(params.speciesGroup, params.layerId, params.regionName)

        Notification notification = queryService.getNotificationForUser(query, retrieveUser(params))

        String link = null
        if (notification != null) {
            //construct a link to delete the alert & manage alerts
            link = getMyAlertsLink()
        } else {
            //construct a create alert link
            link = getServerRoot() + '/webservice/createSpeciesGroupRegionAlert?layerId=' + params.layerId +
                    '&regionName=' + params.regionName +
                    '&speciesGroup=' + params.speciesGroup +
                    '&redirect=' + params.redirect
        }

        String displayName = params.speciesGroup + " in " + params.regionName

        render(view: 'alerts', model: [link: link, deleteLink: getDeleteLink(notification), displayName: displayName, notification: notification])
    }

    private String getDeleteLink(Notification notification) {
        if (notification == null) ""
        else getServerRoot() + '/webservice/deleteAlert/' + notification.id
    }

    def createBiocacheNewRecordsAlert = {
        log.debug("Create biocache new records alert for " + params.resourceName ?: "all resources")
        //biocacheWebserviceQueryPath, String biocacheUIQueryPath, String queryDisplayName
        if (params.webserviceQuery && params.uiQuery && params.queryDisplayName && params.baseUrlForWS && params.baseUrlForUI && params.resourceName) {
            //region + species group
            Query newQuery = queryService.createBioCacheChangeQuery(params.webserviceQuery, params.uiQuery, params.queryDisplayName, params.baseUrlForWS, params.baseUrlForUI, params.resourceName)
            queryService.createQueryForUserIfNotExists(newQuery, userService.getUser())
            redirectIfSupplied(params)
        } else {
            response.sendError(400)
        }
    }

    def biocacheNewRecordAlerts = {

        log.debug("Biocache new records alerts lookup for...." + params.webserviceQuery)

        //check for notifications for this query and this user
        Query query = queryService.createBioCacheChangeQuery(params.webserviceQuery, params.uiQuery, params.queryDisplayName,
                params.baseUrlForWS, params.baseUrlForUI, params.resourceName)

        Notification notification = queryService.getNotificationForUser(query, retrieveUser(params))

        String link = null
        if (notification != null) {
            //construct a link to delete the alert & manage alerts
            link = getMyAlertsLink()
        } else {
            //construct a create alert link
            link = getServerRoot() + '/webservice/createBiocacheAlert?' +
                    'webserviceQuery=' + params.webserviceQuery +
                    '&uiQuery=' + params.uiQuery +
                    '&queryDisplayName=' + params.queryDisplayName +
                    '&redirect=' + params.redirect +
                    '&baseUrlForWS=' + params.baseUrlForWS +
                    '&baseUrlForUI=' + params.baseUrlForUI +
                    '&resourceName=' + params.resourceName
        }
        render(view: 'alerts', model: [link: link, deleteLink: getDeleteLink(notification), displayName: params.queryDisplayName, notification: notification])
    }

    def createBiocacheNewAnnotationsAlert = {
        log.debug("Create biocache new annotations alert for " + params.resourceName ?: "all resources")
        //biocacheWebserviceQueryPath, String biocacheUIQueryPath, String queryDisplayName
        if (params.webserviceQuery && params.uiQuery && params.queryDisplayName) {
            //region + species group
            Query newQuery = queryService.createBioCacheAnnotationQuery(params.webserviceQuery, params.uiQuery, params.queryDisplayName, params.baseUrlForWS, params.baseUrlForUI, params.resourceName)
            queryService.createQueryForUserIfNotExists(newQuery, userService.getUser())
            redirectIfSupplied(params)
        } else {
            response.sendError(400)
        }
    }

    def biocacheNewAnnotationAlerts = {

        log.debug("Biocache annotation alerts lookup for...." + params.webserviceQuery)

        //check for notifications for this query and this user
        Query query = queryService.createBioCacheAnnotationQuery(params.webserviceQuery, params.uiQuery, params.queryDisplayName,
                params.baseUrlForWS, params.baseUrlForUI, params.resourceName)

        Notification notification = queryService.getNotificationForUser(query, retrieveUser(params))

        String link = null
        if (notification != null) {
            //construct a link to delete the alert & manage alerts
            link = getMyAlertsLink()
        } else {
            //construct a create alert link
            link = getServerRoot() + '/webservice/createBiocacheAlert?' +
                    'webserviceQuery=' + params.webserviceQuery +
                    '&uiQuery=' + params.uiQuery +
                    '&queryDisplayName=' + params.queryDisplayName +
                    '&redirect=' + params.redirect +
                    '&baseUrlForWS=' + params.baseUrlForWS +
                    '&baseUrlForUI=' + params.baseUrlForUI +
                    '&resourceName=' + params.resourceName
        }
        render(view: 'alerts', model: [link: link, deleteLink: getDeleteLink(notification), displayName: params.queryDisplayName, notification: notification])
    }

    def createBiocacheAlert = {
        log.debug("Create biocache alert for " + params.resourceName ?: "all resources")
        if (params.webserviceQuery && params.uiQuery && params.queryDisplayName) {
            //region + species group
            Query newQuery = queryService.createBioCacheQuery(params.webserviceQuery, params.uiQuery, params.queryDisplayName, params.baseUrlForWS, params.baseUrlForUI, params.resourceName)
            queryService.createQueryForUserIfNotExists(newQuery, userService.getUser())
            redirectIfSupplied(params)
        } else {
            response.sendError(400)
        }
    }

    def biocacheAlerts = {

        log.debug("Biocache annotation alerts lookup for...." + params.webserviceQuery)

        //check for notifications for this query and this user
        Query query = queryService.createBioCacheQuery(params.webserviceQuery, params.uiQuery, params.queryDisplayName,
                params.baseUrlForWS, params.baseUrlForUI, params.resourceName)

        Notification notification = queryService.getNotificationForUser(query, retrieveUser(params))

        String link = null
        if (notification != null) {
            //construct a link to delete the alert & manage alerts
            link = getMyAlertsLink()
        } else {
            //construct a create alert link
            link = getServerRoot() + '/webservice/createBiocacheAlert?' +
                    'webserviceQuery=' + params.webserviceQuery +
                    '&uiQuery=' + params.uiQuery +
                    '&queryDisplayName=' + params.queryDisplayName +
                    '&redirect=' + params.redirect +
                    '&baseUrlForWS=' + params.baseUrlForWS +
                    '&baseUrlForUI=' + params.baseUrlForUI +
                    '&resourceName=' + params.resourceName
        }
        render(view: 'alerts', model: [link: link, deleteLink: getDeleteLink(notification), displayName: params.queryDisplayName, notification: notification])
    }

    def createRegionAlert = {
        if (params.regionName && params.layerId) {
            //region + species group
            Query newQuery = queryService.createRegionQuery(params.layerId, params.regionName)
            queryService.createQueryForUserIfNotExists(newQuery, userService.getUser())
            redirectIfSupplied(params)
        } else {
            response.sendError(400)
        }
    }

    def createTaxonRegionAlert = {

        log.debug('createTaxonRegionAlert ' + params.regionName + ' : ' + params.layerId)
        if (params.regionName && params.layerId && params.taxonGuid && params.taxonName) {
            //region + taxon
            Query newQuery = queryService.createTaxonRegionQuery(params.taxonGuid, params.taxonName, params.layerId, params.regionName)
            queryService.createQueryForUserIfNotExists(newQuery, userService.getUser())
            redirectIfSupplied(params)
        } else {
            response.sendError(400)
        }
    }

    def createSpeciesGroupRegionAlert = {
        if (params.regionName && params.layerId && params.speciesGroup) {
            //region + species group
            Query newQuery = queryService.createSpeciesGroupRegionQuery(params.speciesGroup, params.layerId, params.regionName)
            queryService.createQueryForUserIfNotExists(newQuery, userService.getUser())
            redirectIfSupplied(params)
        } else {
            response.sendError(400)
        }
    }

    private def redirectIfSupplied(GrailsParameterMap params) {
        if (params.redirect) {
            redirect([url: params.redirect])
        } else {
            redirect([uri: '/'])
        }
    }

    def deleteAlert = {
        log.debug("Deleting an alert")
        Notification n = Notification.findById(params.id)
        n.delete(flush: true)
        redirectIfSupplied(params)
    }

    @Operation(
            method = "POST",
            tags = "alerts",
            operationId = "Unsubscribe",
            summary = "Unsubscribe",
            description = "Unsubscribe",
            parameters = [
                    @Parameter(name = "userId",
                            in = PATH,
                            required = true,
                            description = "userId"),
                    @Parameter(name = "Authorization",
                            in = HEADER,
                            required = true)
            ],
            responses = [
                    @ApiResponse(
                            description = "Unsubscribed",
                            responseCode = "200",
                            content = [
                                    @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = SuccessResponse)
                                    )
                            ]
                    )
            ],
            security = [@SecurityRequirement(name = 'openIdConnect')]
    )
    @RequireApiKey
    @Path("api/alerts/user/{userId}/unsubscribe")
    def deleteAllAlertsForUser() {
        if (!params.userId) {
            response.status = HttpStatus.BAD_REQUEST.code
            response.sendError(HttpStatus.BAD_REQUEST.code, "userId is a required parameter")
        } else {
            def user = userService.getUserById(params.userId)

            if (user) {
                List<Notification> notifications = Notification.findAllByUser(user)
                if (notifications) {
                    Notification.deleteAll(notifications)
                    user.notifications?.clear()
                    user.save(flush: true)
                }

                render([success: true] as JSON)
            } else {
                response.status = HttpStatus.NOT_FOUND.code
                response.sendError(HttpStatus.NOT_FOUND.code, "Unable to find user with userId ${params.userId}")
            }
        }
    }

    @Operation(
            method = "POST",
            tags = "alerts",
            operationId = "Create User Alerts",
            summary = "Create User Alerts  and returns the list of enabled queries names",
            description = "Create User Alerts and returns the list of enabled queries names",
            parameters = [
                    @Parameter(name = "userId",
                            in = QUERY,
                            required = true,
                            description = "userId"),
                    @Parameter(name = "email",
                            in = QUERY,
                            required = true,
                            description = "email"),
                    @Parameter(name = "firstName",
                            in = QUERY,
                            required = false,
                            description = "firstName"),
                    @Parameter(name = "lastName",
                            in = QUERY,
                            required = false,
                            description = "lastName"),
                    @Parameter(name = "Authorization",
                            in = HEADER,
                            required = true)
            ],
            responses = [
                    @ApiResponse(
                            description = "Create User Alerts",
                            responseCode = "200",
                            content = [
                                    @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = List)
                                    )
                            ]
                    )
            ],
            security = [@SecurityRequirement(name = 'openIdConnect')]
    )
    @RequireApiKey
    @Path("api/alerts/user/createAlerts")
    def createUserAlerts() {
        if (!params.userId) {
            response.status = HttpStatus.BAD_REQUEST.code
            response.sendError(HttpStatus.BAD_REQUEST.code, "userId is a required parameter")
        } else {
            def user = userService.getUserById(params.userId)
            if (!user) {
                Map userDetails = ["userId": params.userId, "email": params.email, "userDisplayName": params.firstName + " " + params.lastName]
                user = userService.getUser(userDetails)
                response.status = HttpStatus.CREATED.code
            } else {
                response.status = HttpStatus.OK.code
            }

            def notificationInstanceList = Notification.findAllByUser(user)
            def enabledQueries = notificationInstanceList.collect { it.query?.name }
            render(enabledQueries as JSON)
        }
    }


    private User retrieveUser(params) {
        User user = userService.getUser()
        if (user == null && params.userName) {
            user = userService.getUserByUserName(params.userName)
        }
        user
    }

    @Operation(
            method = "GET",
            tags = "alerts",
            operationId = "Get User Alerts.",
            summary = "Get User Alerts",
            description = "Get User Alerts",
            parameters = [
                    @Parameter(name = "userId",
                            in = PATH,
                            required = true,
                            description = "userId"),
                    @Parameter(name = "Authorization",
                            in = HEADER,
                            required = true)
            ],
            responses = [
                    @ApiResponse(
                            description = "Get User Alerts",
                            responseCode = "200",
                            content = [
                                    @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = GetUserAlertsResponse)
                                    )
                            ]
                    )
            ],
            security = [@SecurityRequirement(name = 'openIdConnect')]
    )
    @RequireApiKey
    @Path("api/alerts/user/{userId}")
    def getUserAlertsWS() {
        User user = userService.getUserById(params.userId)
        if (user == null) {
            response.status = HttpStatus.NOT_FOUND.code
            render ([error : "can't find a user with userId " + params.userId] as JSON)
        } else {
            render(userService.getUserAlertsConfig(user) as JSON)
        }
    }

    @Operation(
            method = "POST",
            tags = "alerts",
            operationId = "Subscribe to my annotation",
            summary = "Subscribe to my annotation",
            description = "Subscribe to my annotation",
            parameters = [
                    @Parameter(name = "userId",
                            in = PATH,
                            required = true,
                            description = "userId"),
                    @Parameter(name = "Authorization",
                            in = HEADER,
                            required = true)
            ],
            responses = [
                    @ApiResponse(
                            description = "Subscribed to my annotation",
                            responseCode = "200",
                            content = [
                                    @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = SuccessResponse)
                                    )
                            ]
                    )
            ],
            security = [@SecurityRequirement(name = 'openIdConnect')]
            // hidden = grailsApplication.config.myannotation.enabled
    )
    @RequireApiKey
    @Path("api/alerts/user/{userId}/subscribeMyAnnotation")
    def subscribeMyAnnotationWS() {
        if (!grailsApplication.config.myannotation.enabled) {
            return
        }

        User user = userService.getUser((String)params.userId)
        if (user == null) {
            response.status = HttpStatus.NOT_FOUND.code
            render ([error : "can't find a user with userId " + params.userId] as JSON)

        } else {
            try {
                notificationService.subscribeMyAnnotation(user)
                render([success: true] as JSON)
            } catch (ignored) {
                render text: "failed to subscribe to my annotation for user " + params.userId, contentType: 'text/plain', status: 500
            }
        }
    }

    @Operation(
            method = "POST",
            tags = "alerts",
            operationId = "Unsubscribe my annotation",
            summary = "Unsubscribe my annotation",
            description = "Unsubscribe my annotation",
            parameters = [
                    @Parameter(name = "userId",
                            in = PATH,
                            required = true,
                            description = "userId"),
                    @Parameter(name = "Authorization",
                            in = HEADER,
                            required = true)
            ],
            responses = [
                    @ApiResponse(
                            description = "Unsubscribed my annotation",
                            responseCode = "200",
                            content = [
                                    @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = SuccessResponse)
                                    )
                            ]
                    )
            ],
            security = [@SecurityRequirement(name = 'openIdConnect')]
            // hidden = grailsApplication.config.myannotation.enabled
    )
    @RequireApiKey
    @Path("api/alerts/user/{userId}/unsubscribeMyAnnotation")
    def unsubscribeMyAnnotationWS() {
        if (!grailsApplication.config.myannotation.enabled) {
            return
        }

        User user = userService.getUserById(params.userId)
        if (user == null) {
            response.status = HttpStatus.NOT_FOUND.code
            render ([error : "can't find a user with userId " + params.userId] as JSON)
        } else {
            try {
                notificationService.unsubscribeMyAnnotation(user)
                render([success: true] as JSON)
            } catch (ignored) {
                render text: "failed to unsubscribe my annotation for user " + params.userId, contentType: 'text/plain', status: 500
            }
        }
    }

    // classes used for the OpenAPI definition generator
    @JsonIgnoreProperties('metaClass')
    static class GetUserAlertsResponse {
        User user
        List<Query> disabledQueries
        List<Notification> enabledQueries
        List<Notification> customQueries
        List<Frequency> frequencies
    }

    @JsonIgnoreProperties('metaClass')
    static class SuccessResponse {
        boolean success = true
    }
}
