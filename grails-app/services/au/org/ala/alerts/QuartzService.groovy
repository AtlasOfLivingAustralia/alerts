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

package au.org.ala.alerts

import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.SchedulerException
import org.quartz.Trigger
import org.quartz.impl.matchers.GroupMatcher

class QuartzService {
    Scheduler quartzScheduler

    /**
     * NOTE: Biosecurity Job uses the old Quartz plugin, so its trigger name keeps changing
     * @return
     */
    List<Map> getJobs() {
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

        results.each { job ->
            job.error = getLastError(job.jobName)
        }

        return results.sort { a, b ->
            a.jobGroup <=> b.jobGroup ?: a.jobName <=> b.jobName
        }
    }

    void pause(String jobName, String jobGroup) {
        JobKey jobKey = new JobKey(jobName, jobGroup)

        if (quartzScheduler.checkExists(jobKey)) {
            quartzScheduler.pauseJob(jobKey)
            log.info "Paused job: ${jobName} (${jobGroup})"
        } else {
            log.warn("Cannot pause — job not found: ${jobName} (${jobGroup})")
        }
    }

    void resume(String jobName, String jobGroup) {
        JobKey jobKey = new JobKey(jobName, jobGroup)

        if (quartzScheduler.checkExists(jobKey)) {
            quartzScheduler.resumeJob(jobKey)
            log.info("Resumed job: ${jobName} (${jobGroup})")
        } else {
            log.warn("Cannot resume — job not found: ${jobName} (${jobGroup})")
        }
    }

    void runNow(String jobName, String jobGroup) throws SchedulerException {
        quartzScheduler.triggerJob(new JobKey(jobName, jobGroup))
    }

    // store last error per job
    Map<String, String> lastJobErrors = [:]

    void recordError(String jobName, Exception e) {
        lastJobErrors[jobName] = e.message
    }

    String getLastError(String jobName) {
        return lastJobErrors[jobName]
    }

    void clearError(String jobName) {
        lastJobErrors.remove(jobName)
    }

}
