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

import au.org.ala.alerts.jobs.JobKeys
import au.org.ala.alerts.jobs.RoutineQueriesJob
import org.quartz.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.context.event.EventListener
import org.springframework.context.event.ContextRefreshedEvent

@Component
class RoutineScheduler {
    Scheduler quartzScheduler
    String hourlyCronExpression
    String dailyCronExpression
    String weeklyCronExpression
    String monthlyCronExpression

    RoutineScheduler(
            Scheduler quartzScheduler,
            @Value('${cron.hourly}') String hourlyCronExpression,
            @Value('${cron.daily}') String dailyCronExpression,
            @Value('${cron.weekly}') String weeklyCronExpression,
            @Value('${cron.monthly}') String monthlyCronExpression
    )
    {
        this.quartzScheduler = quartzScheduler
        this.hourlyCronExpression = hourlyCronExpression
        this.dailyCronExpression = dailyCronExpression
        this.weeklyCronExpression = weeklyCronExpression
        this.monthlyCronExpression = monthlyCronExpression
    }


    @EventListener(ContextRefreshedEvent)
    void init() {

        scheduleRoutineJob(
                JobKeys.HOURLY_JOB_NAME,
                JobKeys.HOURLY_TRIGGER_NAME,
                "hourly",
                hourlyCronExpression
        )

        scheduleRoutineJob(
                JobKeys.DAILY_JOB_NAME,
                JobKeys.DAILY_TRIGGER_NAME,
                "daily",
                dailyCronExpression
        )

        scheduleRoutineJob(
                JobKeys.WEEKLY_JOB_NAME,
                JobKeys.WEEKLY_TRIGGER_NAME,
                "weekly",
                weeklyCronExpression
        )

        scheduleRoutineJob(
                JobKeys.MONTHLY_JOB_NAME,
                JobKeys.MONTHLY_TRIGGER_NAME,
                "monthly",
                monthlyCronExpression
        )

    }

    private void scheduleRoutineJob(String jobName, String triggerName, String frequency, String cron) {

        JobKey jobKey = new JobKey(jobName, JobKeys.ROUTINE_JOB_GROUP)
        TriggerKey triggerKey = new TriggerKey(triggerName, JobKeys.ROUTINE_TRIGGER_GROUP)

        if (!quartzScheduler.checkExists(jobKey)) {
            JobDetail jobDetail = JobBuilder.newJob(RoutineQueriesJob)
                    .withIdentity(jobKey)
                    .usingJobData("frequency", frequency)   // <-- pass frequency here
                    .storeDurably()
                    .build()

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(triggerKey)
                    .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                    .build()

            quartzScheduler.scheduleJob(jobDetail, trigger)
        }
    }
}
