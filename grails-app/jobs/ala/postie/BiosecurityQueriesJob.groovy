package ala.postie


class BiosecurityQueriesJob {
    // triggers set in BootStrap.groovy
    //  static triggers = {
    //  }

    def notificationService

    def execute() {
        // execute task
        log.info("****** Scheduled biosecurity update ****** " + new Date())
        notificationService.biosecurityAlerts()
        log.info("****** Scheduled biosecurity update finished ******" + new Date())
    }
}
