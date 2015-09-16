/******************************************************************************\
 *  CONFIG MANAGEMENT
 \******************************************************************************/
def appName = 'alerts'
def ENV_NAME = "${appName.toUpperCase()}_CONFIG"
default_config = "/data/${appName}/config/${appName}-config.properties"
if(!grails.config.locations || !(grails.config.locations instanceof List)) {
    grails.config.locations = []
}

if(System.getenv(ENV_NAME) && new File(System.getenv(ENV_NAME)).exists()) {
    println "[${appName}] Including configuration file specified in environment: " + System.getenv(ENV_NAME);
    grails.config.locations.add "file:" + System.getenv(ENV_NAME)
} else if(System.getProperty(ENV_NAME) && new File(System.getProperty(ENV_NAME)).exists()) {
    println "[${appName}] Including configuration file specified on command line: " + System.getProperty(ENV_NAME);
    grails.config.locations.add "file:" + System.getProperty(ENV_NAME)
} else if(new File(default_config).exists()) {
    println "[${appName}] Including default configuration file: " + default_config
    grails.config.locations.add "file:" + default_config
} else if(!new File(default_config).exists()) {
    println "[${appName}] WARNING!!!! unable to load: " + default_config
} else {
    println "[${appName}] No external configuration file defined."
}

println "[${appName}] (*) grails.config.locations = ${grails.config.locations}"
println "default_config = ${default_config}"

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

//override this in external configuration
logging.dir = (System.getProperty('catalina.base') ? System.getProperty('catalina.base') + '/logs'  : '/var/log/tomcat6/')

// log4j configuration
log4j = {

    off 'grails.app.services.org.grails.plugin.resource',
            'grails.app.taglib.org.grails.plugin.resource',
            'grails.app.resourceMappers.org.grails.plugin.resource',
            'au.org.ala.cas',
            'org.quartz.core',
            'com.jayway.jsonpath'

    appenders {
        environments {
            production {
                rollingFile name: "postie-prod",
                    maxFileSize: 104857600,
                    file: logging.dir + "/alerts.log",
                    threshold: org.apache.log4j.Level.ERROR,
                    layout: pattern(conversionPattern: "%d [%c{1}]  %m%n")
                rollingFile name: "stacktrace", maxFileSize: 1024, file: logging.dir + "/alerts-stacktrace.log"
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
           'grails.app.service.org.grails.plugin.resource'
           'grails.app.service'
           'org.ala'
           'au.org.ala'
           'grails.app.service.org.grails.plugin.resource.ResourceTagLib'

    debug  'au.org.ala.alerts'
}

/************** Custom config ************************/

postie.timezone = 'Australia/Sydney'
postie.emailSender = 'atlas-alerts@ala.org.au'
postie.emailAlertAddressTitle = 'Atlas alerts'

postie.emailInfoAddressTitle = 'Atlas of Living Australia'
postie.emailInfoSender = 'atlas-alerts@ala.org.au'

postie.defaultResourceName = 'Atlas'
postie.enableEmail = true

security.cas.casServerName = 'https://auth.ala.org.au'
security.cas.uriFilterPattern = ''
security.cas.uriExclusionFilterPattern = '/images.*,/css.*,/js.*,/less.*'
security.cas.authenticateOnlyIfLoggedInPattern = "" // pattern for pages that can optionally display info about the logged-in user
security.cas.loginUrl = 'https://auth.ala.org.au/cas/login'
security.cas.logoutUrl = 'https://auth.ala.org.au/cas/logout'
security.cas.casServerUrlPrefix = 'https://auth.ala.org.au/cas'
security.cas.bypass = false

appContext = 'alerts'
ala.baseURL = "http://www.ala.org.au"
bie.baseURL = "http://bie.ala.org.au"
bie.searchPath = "/search"
grails.project.groupId = "au.org.ala" // change this to alter the default package name and Maven publishing destination
ala.userDetailsURL = 'http://auth.ala.org.au/userdetails/userDetails/getUserListFull'

biocache.baseURL = "http://biocache.ala.org.au"
spatial.baseURL = "http://spatial.ala.org.au"
collectory.baseURL = "http://collections.ala.org.au"


environments {
    development {
        grails.logging.jul.usebridge = true
        grails.host = "http://dev.ala.org.au"
        grails.serverURL = "${grails.host}:8080/${appContext}"
        security.cas.appServerName = "${grails.host}:8080"
        security.cas.contextPath = "/${appContext}"
        grails.resources.debug = true // cached-resources plugin - keeps original filenames but adds cache-busting params
        grails {
          mail {
            host = "localhost"
            port = 1025
            username = postie.emailSender
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
        grails {
          mail {
            host = "localhost"
            port = 25
            username = postie.emailSender
          }
        }
    }
}

// Uncomment and edit the following lines to start using Grails encoding & escaping improvements

/* remove this line
// GSP settings
grails {
    views {
        gsp {
            encoding = 'UTF-8'
            htmlcodec = 'xml' // use xml escaping instead of HTML4 escaping
            codecs {
                expression = 'html' // escapes values inside null
                scriptlet = 'none' // escapes output from scriptlets in GSPs
                taglib = 'none' // escapes output from taglibs
                staticparts = 'none' // escapes output from static template parts
            }
        }
        // escapes all not-encoded output at final stage of outputting
        filteringCodecForContentType {
            //'text/html' = 'html'
        }
    }
}
remove this line */
