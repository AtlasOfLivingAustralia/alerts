// locations to search for config files that get merged into the main config;
// config files can be ConfigSlurper scripts, Java properties files, or classes
// in the classpath in ConfigSlurper format

// grails.config.locations = [ "classpath:${appName}-config.properties",
//                             "classpath:${appName}-config.groovy",
//                             "file:${userHome}/.grails/${appName}-config.properties",
//                             "file:${userHome}/.grails/${appName}-config.groovy"]

// if (System.properties["${appName}.config.location"]) {
//    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
// }

grails.project.groupId = au.org.ala.postie // change this to alter the default package name and Maven publishing destination
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [
    all:           '*/*',
    atom:          'application/atom+xml',
    css:           'text/css',
    csv:           'text/csv',
    form:          'application/x-www-form-urlencoded',
    html:          ['text/html','application/xhtml+xml'],
    js:            'text/javascript',
    json:          ['application/json', 'text/json'],
    multipartForm: 'multipart/form-data',
    rss:           'application/rss+xml',
    text:          'text/plain',
    xml:           ['text/xml', 'application/xml']
]

// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

// What URL patterns should be processed by the resources plugin
grails.resources.adhoc.patterns = ['/images/*', '/css/*', '/js/*', '/plugins/*']

// The default codec used to encode data with ${}
grails.views.default.codec = "none" // none, html, base64
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"
// enable Sitemesh preprocessing of GSP pages
grails.views.gsp.sitemesh.preprocess = true
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []
// whether to disable processing of multi part requests
grails.web.disable.multipart=false

// request parameters to mask when logging exceptions
grails.exceptionresolver.params.exclude = ['password']

// configure auto-caching of queries by default (if false you can cache individual queries with 'cache: true')
grails.hibernate.cache.queries = false


// log4j configuration
log4j = {
    appenders {
        environments {
            production {
                rollingFile name: "postie-prod",
                    maxFileSize: 104857600,
                    file: "/var/log/tomcat6/postie.log",
                    threshold: org.apache.log4j.Level.DEBUG,
                    layout: pattern(conversionPattern: "%d [%c{1}]  %m%n")
                rollingFile name: "stacktrace", maxFileSize: 1024, file: "/var/log/tomcat6/postie-stacktrace.log"
            }
            development{
                console name: "stdout", layout: pattern(conversionPattern: "%d [%c{1}]  %m%n"), threshold: org.apache.log4j.Level.DEBUG
            }
        }
    }

    root {
        debug  'postie-prod'
    }

    error  'org.codehaus.groovy.grails.web.servlet',  //  controllers
           'org.codehaus.groovy.grails.web.pages', //  GSP
           'org.codehaus.groovy.grails.web.sitemesh', //  layouts
	         'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
           'org.codehaus.groovy.grails.web.mapping', // URL mapping
	         'org.codehaus.groovy.grails.commons', // core / classloading
	         'org.codehaus.groovy.grails.plugins', // plugins
           'org.springframework.jdbc',
           'org.springframework.transaction',
           'org.codehaus.groovy',
           'org.grails',
           'org.apache',
           'grails.spring',
           'grails.util.GrailsUtil',
           'net.sf.ehcache'

    debug  'ala'
}

/************** Custom config ************************/

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
