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

import ch.qos.logback.core.util.FileSize
import grails.util.BuildSettings
import grails.util.Environment
import org.springframework.boot.logging.logback.ColorConverter
import org.springframework.boot.logging.logback.WhitespaceThrowableProxyConverter

import java.nio.charset.Charset

conversionRule 'clr', ColorConverter
conversionRule 'wex', WhitespaceThrowableProxyConverter

// See http://logback.qos.ch/manual/groovy.html for details on configuration
appender('STDOUT', ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        charset = Charset.forName('UTF-8')
        pattern =
                '%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} ' + // Date
                        '%clr(%5p) ' + // Log level
                        '%clr(---){faint} %clr([%15.15t]){faint} ' + // Thread
                        '%clr(%-40.40logger{39}){cyan} %clr(:){faint} ' + // Logger
                        '%m%n%wex' // Message
    }
}

def loggingDir = (System.getProperty('catalina.base') ? System.getProperty('catalina.base') + '/logs' : './logs')
def appName = 'alerts'
final TOMCAT_LOG = 'TOMCAT_LOG'
def appenderList = []

switch (Environment.current) {
    case Environment.PRODUCTION:
        appender(TOMCAT_LOG, RollingFileAppender) {
            file = "${loggingDir}/${appName}.log"
            encoder(PatternLayoutEncoder) {
                pattern =
                        '%d{yyyy-MM-dd HH:mm:ss.SSS} ' + // Date
                                '%5p ' + // Log level
                                '--- [%15.15t] ' + // Thread
                                '%-40.40logger{39} : ' + // Logger
                                '%m%n%wex' // Message
            }
            rollingPolicy(FixedWindowRollingPolicy) {
                fileNamePattern = "${loggingDir}/${appName}.%i.log.gz"
                minIndex=1
                maxIndex=4
            }
            triggeringPolicy(SizeBasedTriggeringPolicy) {
                maxFileSize = FileSize.valueOf('10MB')
            }
        }
        root(WARN, ['TOMCAT_LOG'])
        break
    case Environment.TEST:
        appender(TOMCAT_LOG, RollingFileAppender) {
            file = "${loggingDir}/${appName}.log"
            encoder(PatternLayoutEncoder) {
                pattern =
                        '%d{yyyy-MM-dd HH:mm:ss.SSS} ' + // Date
                                '%5p ' + // Log level
                                '--- [%15.15t] ' + // Thread
                                '%-40.40logger{39} : ' + // Logger
                                '%m%n%wex' // Message
            }
            rollingPolicy(FixedWindowRollingPolicy) {
                fileNamePattern = "${loggingDir}/${appName}.%i.log.gz"
                minIndex=1
                maxIndex=4
            }
            triggeringPolicy(SizeBasedTriggeringPolicy) {
                maxFileSize = FileSize.valueOf('1MB')
            }
        }
        root(INFO, ['TOMCAT_LOG'])
        break
    case Environment.DEVELOPMENT:
        def targetDir = BuildSettings.TARGET_DIR
        if (Environment.isDevelopmentMode() && targetDir != null) {
            appender("FULL_STACKTRACE", FileAppender) {
                file = "${targetDir}/stacktrace.log"
                append = true
                encoder(PatternLayoutEncoder) {
                    pattern = "%level %logger - %msg%n"
                }
            }
            logger("StackTrace", ERROR, ['FULL_STACKTRACE'], false)
        }
        appenderList.addAll(['FULL_STACKTRACE','STDOUT'])
        root(WARN, appenderList)
    default:
        root(WARN, ['STDOUT'])
        break
}

[
        (OFF): [],
        (ERROR): [
                'grails.spring.BeanBuilder',
                'grails.plugin.webxml',
                'grails.plugin.cache.web.filter',
                'grails.app.services.org.grails.plugin.resource',
                'grails.app.taglib.org.grails.plugin.resource',
                'grails.app.resourceMappers.org.grails.plugin.resource'
        ],
        (WARN): [
                'au.org.ala.cas.client'
        ],
        (INFO): [
                'grails.plugin.externalconfig.ExternalConfig'
        ],
        (DEBUG): [
                'grails.app',
                'au.org.ala.cas',
                'au.org.ala.alerts'
        ],
        (TRACE): [
        ]
].each { level, names ->
    names.each { name ->
        if (appenderList.size() > 0) {
            logger(name, level, appenderList, false )
        }

    }
}