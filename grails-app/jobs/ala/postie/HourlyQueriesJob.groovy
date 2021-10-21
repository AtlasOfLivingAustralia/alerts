package ala.postie

class HourlyQueriesJob {

    static triggers = {
        cron name: 'hourly', startDelay: 10000, cronExpression: '0 48 * * * ?'
    }

    def notificationService

    def execute() {
        log.info("****** Starting hourly update ****** " + new Date())
        notificationService.checkQueryForFrequency('hourly')
        log.info("****** Finished hourly update ******" + new Date())
    }
}