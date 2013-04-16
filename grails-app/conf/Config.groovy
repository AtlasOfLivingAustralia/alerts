// locations to search for config files that get merged into the main config
// config files can either be Java properties files or ConfigSlurper scripts

// grails.config.locations = [ "classpath:${appName}-config.properties",
//                             "classpath:${appName}-config.groovy",
//                             "file:${userHome}/.grails/${appName}-config.properties",
//                             "file:${userHome}/.grails/${appName}-config.groovy"]

// if(System.properties["${appName}.config.location"]) {
//    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
// }


/******************************************************************************\
 *  SECURITY
\******************************************************************************/
security.cas.casServerName = 'https://auth.ala.org.au'
security.cas.uriFilterPattern = '/,/query/.*,/admin/.*,/admin/user/.*,/admin/user/debug/.*,/admin/debug/all,/notification/myAlerts,/notification/changeFrequency,/notification/addMyAlert,/notification/addMyAlert/.*,/notification/deleteMyAlert/.*,/notification/deleteMyAlert/.*,/notification/deleteMyAlertWR/.*,/webservice/.*,/webservice/createTaxonAlert,/webservice/taxonAlerts,/webservice/createRegionAlert,/webservice/regionAlerts,/webservice/deleteTaxonAlert/.*,/webservice/create*,/webservice/createSpeciesGroupRegionAlert,/ws/.*,/ws/createTaxonAlert,/ws/taxonAlerts,/ws/createRegionAlert,/ws/regionAlerts,/ws/deleteTaxonAlert/.*,/ws/createTaxonRegionAlert,/ws/createSpeciesGroupRegionAlert,/admin/runChecksNow'
security.cas.uriExclusionFilterPattern = '/images.*,/css.*,/js.*'
security.cas.loginUrl = 'https://auth.ala.org.au/cas/login'
security.cas.logoutUrl = 'https://auth.ala.org.au/cas/logout'
security.cas.casServerUrlPrefix = 'https://auth.ala.org.au/cas'
security.cas.bypass = false

postie.timezone = 'Australia/Sydney'
postie.emailSender = 'alerts@ala.org.au'
postie.emailAlertAddressTitle = 'Atlas alerts'
postie.defaultResourceName = 'Atlas'
postie.enableEmail = true

/** Properties used in header and tails tag **/
headerAndFooter.baseURL = 'http://www2.ala.org.au/commonui'
ala.baseURL = 'http://www.ala.org.au'
ala.layout = 'ala2'
bie.baseURL = "http://bie.ala.org.au"
bie.searchPath = "/search"
ala.userDetailsURL = 'http://auth.ala.org.au/userdetails/userDetails/getUserListFull'

grails.project.groupId = au.org.ala.postie // change this to alter the default package name and Maven publishing destination
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [ html: ['text/html','application/xhtml+xml'],
                      xml: ['text/xml', 'application/xml'],
                      text: 'text/plain',
                      js: 'text/javascript',
                      rss: 'application/rss+xml',
                      atom: 'application/atom+xml',
                      css: 'text/css',
                      csv: 'text/csv',
                      all: '*/*',
                      json: ['application/json','text/json'],
                      form: 'application/x-www-form-urlencoded',
                      multipartForm: 'multipart/form-data'
                    ]

// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

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
// whether to install the java.util.logging bridge for sl4j. Disable for AppEngine!
grails.logging.jul.usebridge = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []

// request parameters to mask when logging exceptions
grails.exceptionresolver.params.exclude = ['password']

// inject REST methods into stuff
grails.rest.injectInto = ["Controller", "Service", "Routes"]

// set per-environment serverURL stem for creating absolute links
environments {
    production {
        grails.serverURL = "http://alerts.ala.org.au"
        serverName = 'http://alerts.ala.org.au'
        contextPath = ''
        grails {
          mail {
            host = "localhost"
            port = 25
            username = postie.emailSender
         }
        }
    }
    development {
        grails.serverURL = "http://alerts-local.ala.org.au:8080/${appName}"     //add a entry into /etc/hosts for this DNS to resolve to localhost
        serverName = 'http://alerts-local.ala.org.au:8080'
        contextPath = '/ala-postie'
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
        grails.serverURL = "http://alerts-local.ala.org.au:8080/${appName}"
    }
}

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
