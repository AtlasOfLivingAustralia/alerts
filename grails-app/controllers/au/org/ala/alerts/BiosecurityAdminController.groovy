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

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.LocalTime
import java.time.ZoneOffset


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
        def (hourStr, minuteStr) = params.time.split(':')
        int hour = hourStr.toInteger()
        int minute = minuteStr.toInteger()
        def weekday = params.weekday

        ZoneId clientZone = params.localTimeZone ?
                ZoneId.of(params.localTimeZone) :
                ZoneId.of("UTC")

        // Local weekday from UI (MON, TUE, ...)
        DayOfWeek localWeekday = DayOfWeek.valueOf(weekday)

        // Pick a date that matches the requested weekday
        LocalDate baseDate = LocalDate.now(clientZone)
        while (baseDate.dayOfWeek != localWeekday) {
            baseDate = baseDate.plusDays(1)
        }

        // Local datetime
        ZonedDateTime localDateTime = ZonedDateTime.of(
                baseDate,
                LocalTime.of(hour, minute),
                clientZone
        )

        // Convert to UTC
        ZonedDateTime utcDateTime =
                localDateTime.withZoneSameInstant(ZoneOffset.UTC)

        int utcHour = utcDateTime.hour
        int utcMinute = utcDateTime.minute

        // ⚠️ Recalculate weekday in UTC
        String utcWeekday = utcDateTime.dayOfWeek.name()

        // Quartz cron (UTC)
        def cron = "0 ${utcMinute} ${utcHour} ? * ${utcWeekday}"

        //def cron = "0 ${minute} ${hour} ? * ${weekday}"
        biosecurityJobService.updateTrigger(cron)

        redirect(controller: "admin", action: "biosecurity")
    }

    /**
     * Schedules a pause and resume window for the Biosecurity job.
     *
     * @param pauseDate compulsory, ISO UTC Format
     * @param resumeDate compulsory, ISO UTC Format
     * @return JSON containing pause and resume dates
     */
    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true)
    def pauseResumeAlerts() {
        Date pauseDate  = params.pauseDate
                ? Date.from(Instant.parse(params.pauseDate))
                : null

        Date resumeDate = params.resumeDate
                ? Date.from(Instant.parse(params.resumeDate))
                : null
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
