grails.servlet.version = "3.0" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.6
grails.project.source.level = 1.6
//grails.project.war.file = "target/${appName}-${appVersion}.war"

// uncomment (and adjust settings) to fork the JVM to isolate classpaths
//grails.project.fork = [
//   run: [maxMemory:1024, minMemory:64, debug:false, maxPerm:256]
//]

grails.project.dependency.resolver = "maven"

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // specify dependency exclusions here; for example, uncomment this to disable ehcache:
        // excludes 'ehcache'
    }
    log "error" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    checksums true // Whether to verify checksums on resolve
    legacyResolve false // whether to do a secondary resolve on plugin installation, not advised and here for backwards compatibility

    repositories {
        mavenLocal()
        mavenRepo ("http://nexus.ala.org.au/content/groups/public/") {
            updatePolicy 'always'
        }
    }

    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.
        runtime 'mysql:mysql-connector-java:5.1.40'
        runtime 'commons-lang:commons-lang:2.6'
        runtime 'org.hamcrest:hamcrest-core:1.3'
        runtime 'org.hamcrest:hamcrest-library:1.3'
        runtime 'net.minidev:json-smart:1.3.1'
        runtime 'xalan:xalan:2.7.2'
        runtime 'org.apache.httpcomponents:httpcore:4.2.2'
        runtime 'org.apache.httpcomponents:httpclient:4.2.2'
        runtime 'org.apache.httpcomponents:httpcore:4.2.2'
        runtime 'org.apache.httpcomponents:httpclient:4.2.2'
        test 'com.jayway.jsonpath:json-path:0.5.6'
        test 'com.jayway.jsonpath:json-path-assert:0.5.6'
    }

    plugins {
        build ":release:3.0.1"
        build ":tomcat:7.0.70"

        runtime ":hibernate:3.6.10.18"
        runtime ":resources:1.2.14"
        runtime ":pretty-time:0.3"
        runtime ":quartz:1.0.1"
        runtime ":quartz-monitor:1.0"

        compile ":mail:1.0.7"
        compile ":scaffolding:2.1.2"
        compile ":markdown:1.1.1"
        compile ':cache:1.1.8'

        runtime ":ala-bootstrap3:2.0.0"
        runtime ":ala-admin-plugin:1.3"
        runtime ":ala-auth:2.1.3"
        runtime ":ala-ws-security:1.4"
    }
}
