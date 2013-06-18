/**
 * This file is merged (at runtime) with the Grails "main" config by the ala-web-theme plugin.
 *
 * Edit this file to suit your app's dev/test/prod environments
 * OR override these values in your app's Config.groovy file and comment-out in this file
 */

postie.timezone = 'Australia/Sydney'
postie.emailSender = 'info@ala.org.au'
postie.emailAlertAddressTitle = 'Atlas alerts'

postie.emailInfoAddressTitle = 'Atlas of Living Australia'
postie.emailInfoSender = 'info@ala.org.au'

postie.defaultResourceName = 'Atlas'
postie.enableEmail = true

security.cas.casServerName = 'https://auth.ala.org.au'
security.cas.uriFilterPattern = '/,/testAuth.*,/query/.*,/admin/.*,/admin/user/.*,/admin/user/debug/.*,/admin/debug/all,/notification/myAlerts,/notification/changeFrequency,/notification/addMyAlert,/notification/addMyAlert/.*,/notification/deleteMyAlert/.*,/notification/deleteMyAlert/.*,/notification/deleteMyAlertWR/.*,/webservice/.*,/webservice/createTaxonAlert,/webservice/taxonAlerts,/webservice/createRegionAlert,/webservice/regionAlerts,/webservice/deleteTaxonAlert/.*,/webservice/create*,/webservice/createSpeciesGroupRegionAlert,/ws/.*,/ws/createTaxonAlert,/ws/taxonAlerts,/ws/createRegionAlert,/ws/regionAlerts,/ws/deleteTaxonAlert/.*,/ws/createTaxonRegionAlert,/ws/createSpeciesGroupRegionAlert,/admin/runChecksNow'
security.cas.uriExclusionFilterPattern = '/images.*,/css.*,/js.*,/less.*'
security.cas.authenticateOnlyIfLoggedInPattern = "" // pattern for pages that can optionally display info about the logged-in user
security.cas.loginUrl = 'https://auth.ala.org.au/cas/login'
security.cas.logoutUrl = 'https://auth.ala.org.au/cas/logout'
security.cas.casServerUrlPrefix = 'https://auth.ala.org.au/cas'
security.cas.bypass = false

appContext = 'ala-postie'
headerAndFooter.baseURL = 'http://www2.ala.org.au/commonui'
ala.baseURL = "http://www.ala.org.au"
bie.baseURL = "http://bie.ala.org.au"
bie.searchPath = "/search"
grails.project.groupId = "au.org.ala" // change this to alter the default package name and Maven publishing destination
ala.userDetailsURL = 'http://auth.ala.org.au/userdetails/userDetails/getUserListFull'

environments {
    development {
        grails.logging.jul.usebridge = true
        grails.host = "http://alerts-local.ala.org.au"
        grails.serverURL = "${grails.host}:8080/${appContext}"
        security.cas.appServerName = "${grails.host}:8080"
        security.cas.contextPath = "/${appContext}"
        grails.resources.debug = true // cached-resources plugin - keeps original filenames but adds cache-busting params
        postie.enableEmail = false
        grails {
           mail {
             host = "smtp.gmail.com"
             port = 465
             username = "******@gmail.com"
             password = "******:"
             props = ["mail.smtp.auth":"true",
                      "mail.smtp.socketFactory.port":"465",
                      "mail.smtp.socketFactory.class":"javax.net.ssl.SSLSocketFactory",
                      "mail.smtp.socketFactory.fallback":"false"]
           }
        }
    }
    test {
        grails.logging.jul.usebridge = false
        grails.host = "alerts-test.ala.org.au"
        grails.serverURL = "http://alerts-test.ala.org.au"
        security.cas.appServerName = grails.serverURL
        security.cas.contextPath = ""
        log4j.appender.'errors.File'="/var/log/tomcat/alerts-stacktrace.log"
    }
    production {
        grails.logging.jul.usebridge = false
        grails.host = "alerts.ala.org.au"
        grails.serverURL = "http://${grails.host}"
        security.cas.appServerName = grails.serverURL
        security.cas.contextPath = ""
        log4j.appender.'errors.File'="/var/log/tomcat6/alerts-stacktrace.log"
        grails {
          mail {
            host = "localhost"
            port = 25
            username = postie.emailSender
          }
        }
    }
}