package ala.postie


class BiosecurityQueriesJob {
    // triggers set in BootStrap.groovy
//    static triggers = {
//    }

    def notificationService

    def execute() {
        // execute task
        println("****** Scheduled biosecurity update ****** " + new Date())
        notificationService.biosecurityAlerts()
        println("****** Scheduled biosecurity update finished ******" + new Date())
    }
}
