modules = {
    application {
        resource url:'js/application.js'
    }

    bootstrapSwitch {
        dependsOn 'bootstrap'
        resource url:[dir:'js', file:'bootstrapSwitch.js',  disposition: 'head']
        resource url:[dir:'css', file:'bootstrapSwitch.css'], attrs:[media:'screen, projection, print']
    }

    alerts {
        resource url:[dir:'css', file:'alerts.css'], attrs:[media:'screen, projection, print']
    }

}



