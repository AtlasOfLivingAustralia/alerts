package ala.postie


class WeeklyQueriesJob {
    static triggers = {
        cron name: 'weekly', startDelay: 10000, cronExpression: '0 30 10 ? * MON'        //fire 10:30 every monday
    }

    def notificationService

    def execute() {
        // execute task
        println("****** Scheduled weekly update ****** " + new Date())
        notificationService.checkQueryForFrequency('weekly')
        println("****** Scheduled weekly update finished ******" + new Date())
    }
}
