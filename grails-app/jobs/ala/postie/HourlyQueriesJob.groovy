package ala.postie

import grails.plugins.schwartz.monitor.listener.QuartzJobListener
import org.quartz.JobExecutionContext

class HourlyQueriesJob {

    static triggers = {
        cron name: 'hourly', startDelay: 10000, cronExpression: '0 48 * * * ?'
    }

    def notificationService

    def execute() {
        log.info("****** Starting hourly update ****** " + new Date())
        notificationService.execQueryForFrequency('hourly')
        log.info("****** Finished hourly update ****** " + new Date())
    }
}