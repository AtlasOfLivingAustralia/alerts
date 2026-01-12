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
 */

package au.org.ala.alerts.quartz

import au.org.ala.alerts.jobs.BiosecurityQueriesJob
import au.org.ala.alerts.jobs.EmailUpdateJob
import au.org.ala.alerts.jobs.JobKeys
import org.quartz.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class EmailUpdateScheduler {
    Scheduler quartzScheduler
    String cronExpression = '0 30 4 * * ?'     //4.30am

    EmailUpdateScheduler(Scheduler quartzScheduler) {
        this.quartzScheduler = quartzScheduler
    }

    @EventListener(ContextRefreshedEvent)
    void init() {
        JobKey jobKey = new JobKey(JobKeys.EMAIL_UPDATE_JOB_NAME, JobKeys.EMAIL_UPDATE_JOB_GROUP)
        TriggerKey triggerKey = new TriggerKey(JobKeys.EMAIL_UPDATE_TRIGGER_NAME, JobKeys.EMAIL_UPDATE_JOB_TRIGGER_GROUP)

        // Remove old job if exists (clean startup)
        if (quartzScheduler.checkExists(jobKey)) {
            quartzScheduler.deleteJob(jobKey)
        }

        JobDetail jobDetail = JobBuilder.newJob(EmailUpdateJob)
                .withIdentity(jobKey)
                .storeDurably()
                .build()

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey)
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .build()

        quartzScheduler.scheduleJob(jobDetail, trigger)
    }
}
