package ala.postie


class BiosecurityQueriesJob {
    static triggers = {
        cron name: 'biosecurity', startDelay: 10000, cronExpression: '0 30 9 ? * WED'        //fire 9:30 every Wednesday
    }

    def notificationService

    def execute() {
        // execute task
        println("****** Scheduled biosecurity update ****** " + new Date())
        notificationService.biosecurityAlerts()
        println("****** Scheduled biosecurity update finished ******" + new Date())
    }
}
