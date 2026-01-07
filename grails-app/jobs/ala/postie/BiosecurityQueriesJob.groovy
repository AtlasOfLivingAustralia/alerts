package ala.postie


class BiosecurityQueriesJob {
    def biosecurityService

    def execute() {
        // execute task
        log.info("****** Scheduled biosecurity update ****** " + new Date())
        biosecurityService.biosecurityAlerts()
        log.info("****** Scheduled biosecurity update finished ******" + new Date())
    }
}
