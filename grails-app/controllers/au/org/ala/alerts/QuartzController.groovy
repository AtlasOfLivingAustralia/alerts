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
 *
 */

package au.org.ala.alerts

import au.org.ala.web.AlaSecured

@AlaSecured(value = 'ROLE_ADMIN', redirectController = 'notification', redirectAction = 'myAlerts', message = "You don't have permission to view that page.")
class QuartzController {
    def quartzService

    def index(){
        def jobs = quartzService.getJobs()

        // Build an HTML block for errors
        def errorHtml = new StringBuilder()

        jobs.each { job ->
            def err = quartzService.getLastError(job.jobName)
            if (err) {
                errorHtml << """
                <div class="alert alert-danger" style="margin-bottom:10px">
                    <strong>${job.jobName}</strong><br/>
                    ${err.encodeAsHTML()}
                </div>
            """
            }
        }

        if (errorHtml) {
            flash.message = errorHtml.toString()
        }


        [jobs: jobs]
    }

    def pause() {
        String jobName = params.jobName
        String jobGroup = params.jobGroup ?: "default"
        if (jobName && jobGroup) {
            quartzService.pause(jobName, jobGroup)
        } else {
            flash.message = "Job: ${jobName}:${jobGroup} is not found"
        }

        redirect(action: "index")
    }

    def resume() {
        String jobName = params.jobName
        String jobGroup = params.jobGroup ?: "default"
        if (jobName && jobGroup) {
            quartzService.resume(jobName, jobGroup)
        } else {
            flash.message = "Job: ${jobName}:${jobGroup} is not found"
        }

        redirect(action: "index")
    }


    def runNow() {
        String jobName = params.jobName
        String jobGroup = params.jobGroup ?: "default"
        quartzService.runNow(jobName,jobGroup)

        redirect(action: "index")
    }

}
