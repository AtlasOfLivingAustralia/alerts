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

import au.org.ala.alerts.NotificationService
import au.org.ala.alerts.QuartzService
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class RoutineQueriesJob implements Job {
    private static final Logger log = LoggerFactory.getLogger(RoutineQueriesJob)
    @Autowired
    private NotificationService notificationService
    @Autowired
    QuartzService quartzService

    @Override
    void execute(JobExecutionContext context) throws JobExecutionException {
        String jobName = context.jobDetail.key.name
        String frequency = context.mergedJobDataMap.getString("frequency")
        try {
            log.info("****** Scheduled ${frequency} update ****** ${new Date()}")
            notificationService.execQueryForFrequency(frequency)
            log.info("****** Scheduled ${frequency} update finished ****** ${new Date()}")
        } catch (Exception e) {
            log.error("${frequency} job failed", e)
            quartzService.recordError(jobName, e)
        }
    }
}