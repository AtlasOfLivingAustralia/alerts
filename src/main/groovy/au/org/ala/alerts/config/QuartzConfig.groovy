/*
 *   Copyright (c) 2026.  Atlas of Living Australia
 *   All Rights Reserved.
 *   The contents of this file are subject to the Mozilla Public
 *   License Version 1.1 (the "License"); you may not use this file
 *   except in compliance with the License. You may obtain a copy of
 *   the License at http://www.mozilla.org/MPL/
 *   Software distributed under the License is distributed on an "AS
 *   IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 *   implied. See the License for the specific language governing
 *   rights and limitations under the License.
 *
 *   @author Qifeng Bai
 */

package au.org.ala.alerts.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.quartz.SchedulerFactoryBean

import javax.sql.DataSource

/**
 * Quartz configuration for Grails 6 using Spring Boot Quartz.
 *
 * This ensures that Quartz jobs are created through Spring’s JobFactory,
 * allowing Grails-managed services, transactions, and Hibernate sessions
 * to be injected correctly into job classes.
 *
 * IMPORTANT:
 * - The bean MUST be named "quartzScheduler". Grails and Spring Boot
 *   auto‑configuration rely on this exact name. Changing it will cause
 *   Quartz to initialize incorrectly or create multiple schedulers.
 * - Modify with care.
 */
@Configuration
class QuartzConfig {

    @Bean(name = "quartzScheduler")
    SchedulerFactoryBean quartzScheduler(SpringJobFactory jobFactory, DataSource dataSource) {
        def factory = new SchedulerFactoryBean()
        factory.setJobFactory(jobFactory)
        factory.setDataSource(dataSource)   // <- inject Spring Boot datasource
        factory.setOverwriteExistingJobs(false)
        factory.setWaitForJobsToCompleteOnShutdown(true)
        return factory
    }
}