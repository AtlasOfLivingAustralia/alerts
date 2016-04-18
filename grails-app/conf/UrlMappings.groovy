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

        "/$controller/$action?/$id?" {
            constraints {
                // apply constraints here
            }
        }

        "/ws/alerts/user/$userId"(controller: 'notification', action: 'getUserAlerts')
        "/ws/$action"(controller: 'webservice')
        "/ws/noauth/$action"(controller: 'webservice')
        "/"(controller: 'notification', action: 'index')
        "500"(view: '/error')

        // 13/4/16 existing production config puts all ws/.* requests through CAS (even /ws/noauth!), which is fine if
        // they are always invoked via javascript (and have the CAS cookie), but doesn't work when invoked from a service.
        // /api/* will not be protected by CAS, but all operations should be protected with @RequireApiKey
        "/api/alerts/user/$userId/unsubscribe"(controller: 'webservice', action: [POST: 'deleteAllAlertsForUser'])
    }

}
