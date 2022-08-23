package ala.postie

class UpdateEmailsJob {

    static triggers = {
        cron name: 'dailyEmails', startDelay: 10000, cronExpression: '0 30 4 * * ?'     //4.30am
    }

    def userService

    def execute() {
        log.info("****** Starting hourly update ****** " + new Date())
        userService.updateUserEmails()
        log.info("****** Finished hourly update ******" + new Date())
    }
}