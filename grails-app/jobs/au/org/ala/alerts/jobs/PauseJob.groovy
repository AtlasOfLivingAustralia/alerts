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

package au.org.ala.alerts.jobs




import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.Scheduler
import org.quartz.TriggerKey

/**
 * A Quartz cron job that pauses a specific scheduled trigger in FUTURE.
 *
 * In Quartz, a "trigger" defines **when** and **how often** a job should run.
 * This au.org.ala.alerts.jobs.PauseJob will pause a trigger, which effectively stops the associated job
 * from executing until the trigger is resumed.
 */
class PauseJob implements Job {

    @Override
    void execute(JobExecutionContext context) {
        def triggerName = context.mergedJobDataMap.getString("triggerName")
        def triggerGroup = context.mergedJobDataMap.getString("triggerGroup")
        Scheduler quartzScheduler = context.getScheduler()
        quartzScheduler.pauseTrigger(new TriggerKey(triggerName, triggerGroup))
    }
}