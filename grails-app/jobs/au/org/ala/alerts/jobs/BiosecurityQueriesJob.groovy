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

import au.org.ala.alerts.BiosecurityService
import au.org.ala.alerts.QuartzService
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class BiosecurityQueriesJob implements Job {
    private static final Logger log = LoggerFactory.getLogger(BiosecurityQueriesJob)
    @Autowired
    BiosecurityService biosecurityService

    @Autowired
    QuartzService quartzService

    @Override
    void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String jobName = jobExecutionContext.jobDetail.key.name
        try {
            log.info("****** Scheduled Biosecurity update ****** " + new Date())
            biosecurityService.biosecurityAlerts()
            log.info("****** Scheduled Biosecurity update finished ******" + new Date())
            quartzService.clearError(jobName)
        } catch (Exception e) {
            log.error("Biosecurity job failed", e)
            quartzService.recordError(jobName, e)
        }
    }
}
