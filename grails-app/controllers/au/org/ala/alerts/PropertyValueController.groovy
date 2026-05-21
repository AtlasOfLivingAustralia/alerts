package au.org.ala.alerts

import grails.plugin.scaffolding.annotation.Scaffold

@Scaffold(PropertyValue)
class PropertyValueController {
    def index() {
        redirect(action: "list", params: params)
    }
}
