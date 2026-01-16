/**
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

package au.org.ala.alerts

import au.org.ala.web.AlaSecured
import grails.converters.JSON

import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class BiosecurityAdminController {
    def biosecurityJobService

    def pauseAlerts() {
        biosecurityJobService.pauseTrigger()
        redirect(controller: "admin", action: "biosecurity")
    }

    def resumeAlerts() {
        biosecurityJobService.resumeTrigger()
        redirect(controller: "admin", action: "biosecurity")
    }

    def updateWeeklySchedule() {
        def (hour, minute) = params.time.split(':')
        def weekday = params.weekday

        def cron = "0 ${minute} ${hour} ? * ${weekday}"
        biosecurityJobService.updateTrigger(cron)

        redirect(controller: "admin", action: "biosecurity")
    }

    /**
     * Schedules a pause and resume window for the Biosecurity job.
     *
     * @return JSON containing pause and resume dates
     */
    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true)
    def pauseResumeAlerts() {
        def formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        LocalDate pauseLocal = params.pauseDate ? LocalDate.parse(params.pauseDate, formatter) : null
        LocalDate resumeLocal = params.resumeDate ? LocalDate.parse(params.resumeDate, formatter) : null

        Date pauseDate = pauseLocal ? Date.from(pauseLocal.atStartOfDay(ZoneId.systemDefault()).toInstant()) : null
        Date resumeDate = resumeLocal ? Date.from(resumeLocal.atStartOfDay(ZoneId.systemDefault()).toInstant()) : null
        if (pauseDate && resumeDate) {
            biosecurityJobService.pauseResumeAlerts(pauseDate, resumeDate)
            def window = biosecurityJobService.getPauseWindow()

            render(window as JSON)
        } else {
            render([error: "Invalid dates"] as JSON)
        }
    }

    /**
     * Cancel any scheduled jobs responsible for pausing or resuming
     * the Biosecurity Job in the future.
     */
    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true)
    def cancelScheduledPauseResumeJob() {
        biosecurityJobService.cancelScheduledPauseResumeJob()
        redirect(controller: "admin", action: "biosecurity")
    }

    /**
     * @return JSON containing pause and resume dates
     */
    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true)
    def getAlertsPauseWindow() {
        def window = biosecurityJobService.getPauseWindow()
        render(window as JSON)
    }

    /**
     * @return details of the scheduled Biosecurity job
     */
    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true)
    def getJobStatus() {
        render biosecurityJobService.getJobInfo() as JSON
    }

}
