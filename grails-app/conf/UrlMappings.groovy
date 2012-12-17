class UrlMappings {

	static mappings = {

    "/admin/user/$userId"(controller:'admin', action: 'showUsersAlerts')
    "/admin/user/debug/$userId"(controller:'admin', action: 'debugAlertsForUser')
    "/admin/debug/all"(controller:'admin', action: 'debugAllAlerts')

		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

    "/ws/$action"(controller:'webservice')
    "/ws/noauth/$action"(controller:'webservice')
		"/"(controller:'notification', action:'index')
		"500"(view:'/error')
	}
}
