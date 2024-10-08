package ala.postie

class DailyQueriesJob {

    static triggers = {
        cron name: 'daily', startDelay: 10000, cronExpression: '0 30 8 * * ?'     //8.30am
    }

    def notificationService

    def execute() {
        // execute task
        log.info("****** Scheduled daily update ****** " + new Date())
        notificationService.execQueryForFrequency('daily')
        log.info("****** Scheduled daily update finished ******" + new Date())
    }
}
