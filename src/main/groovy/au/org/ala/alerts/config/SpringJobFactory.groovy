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

import org.quartz.spi.TriggerFiredBundle
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.scheduling.quartz.SpringBeanJobFactory
import org.springframework.stereotype.Component

/**
 * Custom JobFactory that allows Quartz jobs to be created as fully
 * Spring-managed beans.
 *
 * Quartz normally instantiates job classes directly (via reflection),
 * which means Grails services, transactional proxies, and Hibernate
 * sessions are NOT injected or active inside job instances.
 *
 * By delegating job creation to Spring’s AutowireCapableBeanFactory,
 * this factory ensures:
 *   - Grails services (e.g. NotificationService) are injected correctly
 *   - @Transactional methods work inside jobs
 *   - Hibernate sessions are bound to Quartz threads
 *   - GORM dynamic methods and lazy loading function normally
 *
 * IMPORTANT:
 * - This class is tightly coupled with QuartzConfig, which registers it
 *   via factory.setJobFactory(...). Changing the wiring or removing this
 *   class will break autowiring and Hibernate integration for all jobs.
 * - Modify with care.
 */
@Component
class SpringJobFactory extends SpringBeanJobFactory implements ApplicationContextAware {

    private AutowireCapableBeanFactory beanFactory

    @Override
    void setApplicationContext(ApplicationContext applicationContext) {
        this.beanFactory = applicationContext.autowireCapableBeanFactory
    }

    @Override
    Object createJobInstance(TriggerFiredBundle bundle) {
        def job = super.createJobInstance(bundle)
        beanFactory.autowireBean(job)
        return job
    }
}