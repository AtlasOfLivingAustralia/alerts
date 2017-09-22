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

class UrlMappings {

    static mappings = {

        "/admin"(controller: 'admin', action: 'index')
        "/admin/user/$userId"(controller: 'admin', action: 'showUsersAlerts')
        "/admin/user/debug/$userId"(controller: 'admin', action: 'debugAlertsForUser')
        "/admin/user/deleteMyAlert/$id?"(controller: 'notification', action: 'deleteMyAlert')
        "/admin/user/deleteMyAlertWR/$id?"(controller: 'notification', action: 'deleteMyAlertWR')
        "/admin/user/addMyAlert/$id?"(controller: 'notification', action: 'addMyAlert')
        "/admin/user"(controller: 'admin', action: 'findUser')
        "/admin/debug/all"(controller: 'admin', action: 'debugAllAlerts')

        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

        "/ws/alerts/user/$userId"(controller: 'webservice', action: 'getUserAlerts')
        "/ws/$action"(controller: 'webservice')
        "/ws/noauth/$action"(controller: 'webservice')

        "/"(controller: 'notification', action: 'index')

        // 13/4/16 existing production config puts all ws/.* requests through CAS (even /ws/noauth!), which is fine if
        // they are always invoked via javascript (and have the CAS cookie), but doesn't work when invoked from a service.
        // /api/* will not be protected by CAS, but all operations should be protected with @RequireApiKey
        "/api/alerts/user/$userId/unsubscribe"(controller: 'webservice', action: [POST: 'deleteAllAlertsForUser'])
        "/api/alerts/user/createAlerts"(controller: 'webservice', action: [POST: 'createUserAlerts'])
        "/robots.txt"(view:'/notFound')
        "400"(view:'/error')
        "403"(view:'/error')
        "404"(view:'/notFound')
        "405"(view:'/error')
        "500"(view:'/error')
    }
}
