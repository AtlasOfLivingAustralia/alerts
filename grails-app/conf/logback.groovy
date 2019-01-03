/*
 * Copyright (C) 2019 Atlas of Living Australia
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
import grails.util.Environment
import org.springframework.boot.logging.logback.ColorConverter
import org.springframework.boot.logging.logback.WhitespaceThrowableProxyConverter

conversionRule 'clr', ColorConverter
conversionRule 'wex', WhitespaceThrowableProxyConverter

def loggingDir = (System.getProperty('catalina.base') ? System.getProperty('catalina.base') + '/logs' : './logs')
def appName = 'alerts'
final APPENDER = 'APP_APPENDER'
switch (Environment.current) {
    case Environment.PRODUCTION:
        appender(APPENDER, RollingFileAppender) {
            file = "$loggingDir/${appName}.log"
            encoder(PatternLayoutEncoder) {
                pattern =
                        '%d{yyyy-MM-dd HH:mm:ss.SSS} ' + // Date
                                '%5p ' + // Log level
                                '--- [%15.15t] ' + // Thread
                                '%-40.40logger{39} : ' + // Logger
                                '%m%n%wex' // Message
            }
            rollingPolicy(FixedWindowRollingPolicy) {
                fileNamePattern = "$loggingDir/${appName}.%i.log.gz"
                minIndex=1
                maxIndex=4
            }
            triggeringPolicy(SizeBasedTriggeringPolicy) {
                maxFileSize = FileSize.valueOf('10MB')
            }
        }
        break
    case Environment.TEST:
        appender(APPENDER, RollingFileAppender) {
            file = "$loggingDir/${appName}.log"
            encoder(PatternLayoutEncoder) {
                pattern =
                        '%d{yyyy-MM-dd HH:mm:ss.SSS} ' + // Date
                                '%5p ' + // Log level
                                '--- [%15.15t] ' + // Thread
                                '%-40.40logger{39} : ' + // Logger
                                '%m%n%wex' // Message
            }
            rollingPolicy(FixedWindowRollingPolicy) {
                fileNamePattern = "$loggingDir/${appName}.%i.log.gz"
                minIndex=1
                maxIndex=4
            }
            triggeringPolicy(SizeBasedTriggeringPolicy) {
                maxFileSize = FileSize.valueOf('1MB')
            }
        }
        break
    case Environment.DEVELOPMENT:
        [
                (DEBUG): [ // DEBUG and TRACE should only be enabled for non-production environments
//                           'grails.app',
                           'au.org.ala.cas',
                           'au.org.ala.hub',
                           'au.org.ala.bootstrap3',
                           'au.org.ala.biocache.hubs',
                           'au.org.ala.downloads',
                           'au.org.ala.downloads.plugin',
                           'grails.app',
                           'org.grails.plugins',
                ],
                (TRACE): [
                ]
        ].each { level, names -> names.each { name -> logger(name, level) } }

    default:
        appender(APPENDER, ConsoleAppender) {
            encoder(PatternLayoutEncoder) {
                pattern = '%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} ' + // Date
                        '%clr(%5p) ' + // Log level
                        '%clr(---){faint} %clr([%15.15t]){faint} ' + // Thread
                        '%clr(%-40.40logger{39}){cyan} %clr(:){faint} ' + // Logger
                        '%m%n%wex' // Message
            }
        }
        break
}



root(WARN, [APPENDER])

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
                'grails.plugin.externalconfig.ExternalConfig',
//                'au.org.ala'
        ],
        (DEBUG): [ // DEBUG and TRACE should only be enabled for non-production environments
        ],
        (TRACE): [
        ]
].each { level, names -> names.each { name -> logger(name, level) } }
