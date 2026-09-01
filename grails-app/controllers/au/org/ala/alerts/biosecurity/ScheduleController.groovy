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

package au.org.ala.alerts.biosecurity

import au.org.ala.web.AlaSecured
import grails.converters.JSON

import java.time.DayOfWeek
import java.time.DateTimeException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeParseException

@AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true, redirectController = 'notification', redirectAction = 'myAlerts', message = "You don't have permission to schedule Biosecurity.")
class ScheduleController {
    static namespace = "biosecurity"
    def biosecurityJobService

    def pauseAlerts() {
        biosecurityJobService.pauseTrigger()
        render biosecurityJobService.getJobInfo() as JSON
    }

    def resumeAlerts() {
        biosecurityJobService.resumeTrigger()
        render biosecurityJobService.getJobInfo() as JSON
    }

    def runNow() {
        biosecurityJobService.runNow()
        render ([success: true, message:"Biosecurity is triggered"] as JSON)
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
        biosecurityJobService.updateTrigger(cron)
        def jobStatus = biosecurityJobService.getJobInfo()
        render jobStatus as JSON
    }

    /**
     * Schedules a pause and resume window for the Biosecurity job.
     *
     * @param pauseDate compulsory, a local date ("yyyy-MM-dd") or a full ISO-8601 instant
     * @param resumeDate compulsory, a local date ("yyyy-MM-dd") or a full ISO-8601 instant
     * @param localTimeZone optional IANA zone id (e.g. "Australia/Sydney"), defaults to UTC.
     *                      Plain dates are resolved to start-of-day in this zone.
     * @return JSON containing pause and resume dates
     */
    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true)
    def pauseResumeAlerts() {

        ZoneId clientZone
        try {
            clientZone = params.localTimeZone ? ZoneId.of(params.localTimeZone as String) : ZoneOffset.UTC
        } catch (DateTimeException ignored) {
            render([success: false, message: "Invalid timezone: ${params.localTimeZone}"] as JSON)
            return
        }

        Date pauseDate
        Date resumeDate
        try {
            pauseDate = toDate(params.pauseDate as String, clientZone)
            resumeDate = toDate(params.resumeDate as String, clientZone)
        } catch (DateTimeParseException e) {
            render([success: false, message: "Invalid dates: ${e.message}"] as JSON)
            return
        }

        if (pauseDate && resumeDate) {
            biosecurityJobService.pauseResumeAlerts(pauseDate, resumeDate)
            def window = biosecurityJobService.getPauseWindow()
            render([success: true, window: window] as JSON)
        } else {
            render([success: false, message: "Invalid dates"] as JSON)
        }
    }

    /**
     * Converts a date supplied by the UI into an absolute point in time.
     *
     * A bare local date ("yyyy-MM-dd") is resolved to midnight in {@code zone}, so the window
     * starts/ends when the user expects it to in their own timezone. A value that already carries
     * an offset (e.g. "2026-08-31T00:00:00Z") is used as-is.
     *
     * @param value the raw request parameter, may be null/blank
     * @param zone the client's timezone, used only for bare local dates
     * @return the resolved Date, or null when the value is blank
     */
    private static Date toDate(String value, ZoneId zone) {
        if (!value?.trim()) {
            return null
        }
        String trimmed = value.trim()
        // Bare local date, e.g. "2026-08-31" -> midnight in the client's zone.
        if (trimmed ==~ /\d{4}-\d{2}-\d{2}/) {
            return Date.from(LocalDate.parse(trimmed).atStartOfDay(zone).toInstant())
        }
        // Local date-time without a zone, e.g. "2026-08-31T09:30" -> that wall time in the client's zone.
        if (trimmed ==~ /\d{4}-\d{2}-\d{2}T\d{2}:\d{2}(:\d{2}(\.\d+)?)?/) {
            return Date.from(LocalDateTime.parse(trimmed).atZone(zone).toInstant())
        }
        // Already carries an offset/zone, e.g. "2026-08-31T00:00:00Z" or "...+10:00".
        return Date.from(OffsetDateTime.parse(trimmed).toInstant())
    }

    /**
     * Cancel any scheduled jobs responsible for pausing or resuming
     * the Biosecurity Job in the future.
     */
    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true)
    def cancelScheduledPauseResumeJob() {
        biosecurityJobService.cancelScheduledPauseResumeJob()
        redirect(namespace: "biosecurity", controller: "admin", action: "index")
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

