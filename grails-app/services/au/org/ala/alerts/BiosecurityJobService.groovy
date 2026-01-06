/**
 *   Copyright (c) 2024.  Atlas of Living Australia
 *   All Rights Reserved.
 *   @author Qifeng Bai
 *
 */

package au.org.ala.alerts

import org.quartz.JobBuilder
import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.Trigger
import org.quartz.TriggerBuilder
import org.quartz.TriggerKey
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import org.quartz.impl.matchers.GroupMatcher

/**
 * Schedule Biosecurity jobs
 */
class BiosecurityJobService {
    Scheduler quartzScheduler

    String pauseJobId= "pauseBiosecurity"
    String resumeJobId = "resumeBiosecurity"
    String biosecurityJobName = "ala.postie.BiosecurityQueriesJob"

    void pauseTrigger() {
        def currentJobInfo = getJobInfo()
        if (currentJobInfo) {
            String triggerName = currentJobInfo.triggerName
            String triggerGroup = currentJobInfo.triggerGroup

            TriggerKey key = new TriggerKey(triggerName, triggerGroup)
            quartzScheduler.pauseTrigger(key)
        }
    }

    void resumeTrigger() {
        def currentJobInfo = getJobInfo()
        if (currentJobInfo) {
            String triggerName = currentJobInfo.triggerName
            String triggerGroup = currentJobInfo.triggerGroup

            TriggerKey key = new TriggerKey(triggerName, triggerGroup)
            quartzScheduler.resumeTrigger(key)
        }
    }

    /**
     * It is a webservice call
     * @param pauseDate
     * @param resumeDate
     */
    void pauseResumeAlerts(Date pauseDate, Date resumeDate) {
        def currentJobInfo = getJobInfo()
        if (currentJobInfo) {
            String biosecurityJobTriggerName = currentJobInfo.triggerName
            String biosecurityJobTriggerGroup = currentJobInfo.triggerGroup

            def pauseJobKey = new JobKey(pauseJobId)
            def resumeJobKey = new JobKey(resumeJobId)

            if (quartzScheduler.checkExists(pauseJobKey)) {
                quartzScheduler.deleteJob(pauseJobKey)
            }
            if (quartzScheduler.checkExists(resumeJobKey)) {
                quartzScheduler.deleteJob(resumeJobKey)
            }

            def pauseJob = JobBuilder.newJob(PauseJob)
                    .withIdentity(pauseJobId)
                    .usingJobData("triggerName", biosecurityJobTriggerName)
                    .usingJobData("triggerGroup", biosecurityJobTriggerGroup)
                    .build()

            def trigger = TriggerBuilder.newTrigger()
                    .withIdentity(pauseJobId)
                    .startAt(pauseDate)
                    .build()

            quartzScheduler.scheduleJob(pauseJob, trigger)

            def resumeJob = JobBuilder.newJob(ResumeJob)
                    .withIdentity(resumeJobId)
                    .usingJobData("triggerName", biosecurityJobTriggerName)
                    .usingJobData("triggerGroup", biosecurityJobTriggerGroup)
                    .build()

            def resumeTrigger = TriggerBuilder.newTrigger()
                    .withIdentity(resumeJobId)
                    .startAt(resumeDate)
                    .build()

            quartzScheduler.scheduleJob(resumeJob, resumeTrigger)
        }
    }

    /**
     * Cancel any scheduled jobs responsible for pausing or resuming
     * the Biosecurity Job in the future.
     */
    void cancelScheduledPauseResumeJob() {
        def pauseJobKey = new JobKey(pauseJobId)
        def resumeJobKey = new JobKey(resumeJobId)

        if (quartzScheduler.checkExists(pauseJobKey)) {
            quartzScheduler.deleteJob(pauseJobKey)
        }
        if (quartzScheduler.checkExists(resumeJobKey)) {
            quartzScheduler.deleteJob(resumeJobKey)
        }
    }

    /**
     * Returns the scheduled pause and resume times.
     *
     * @return a JSON object containing the pause and resume timestamps
     */
    def getPauseWindow() {
        def zone = ZoneId.systemDefault()   // e.g., Australia/Sydney
        def formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")

        def pauseTrigger = quartzScheduler.getTrigger(new TriggerKey(pauseJobId))
        def resumeTrigger = quartzScheduler.getTrigger(new TriggerKey(resumeJobId))

        return [
                pause : pauseTrigger?.startTime?
                        ZonedDateTime.ofInstant(pauseTrigger.startTime.toInstant(), zone).format(formatter) :
                        null,
                resume: resumeTrigger?.startTime ?
                        ZonedDateTime.ofInstant(resumeTrigger.startTime.toInstant(), zone).format(formatter) :
                        null
        ]
    }

    /**
     *
     * @return The scheduled job info for Biosecurity
     */
    def getJobInfo() {
        List results = listAllScheduledJobs()
        return results.find{ it->it.jobName === biosecurityJobName}
    }

    /**
     * NOTE: Biosecurity Job uses the old Quartz plugin, so its trigger name keeps changing
     * @return
     */
    List<Map> listAllScheduledJobs() {
        def results = []
        quartzScheduler.getJobGroupNames().each { group ->
            quartzScheduler.getJobKeys(GroupMatcher.jobGroupEquals(group)).each { JobKey jobKey ->
                def triggers = quartzScheduler.getTriggersOfJob(jobKey)
                triggers.each { Trigger trigger ->
                    results << [
                            jobName      : jobKey.name,
                            jobGroup     : jobKey.group,
                            triggerName  : trigger.key.name,
                            triggerGroup : trigger.key.group,
                            nextFireTime : trigger.nextFireTime,
                            previousFire : trigger.previousFireTime,
                            state        : quartzScheduler.getTriggerState(trigger.key).toString()
                    ]
                }
            }
        }
        return results
    }

}
