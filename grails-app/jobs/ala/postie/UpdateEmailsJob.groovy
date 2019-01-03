package ala.postie

class UpdateEmailsJob {

  static triggers = {
     cron name:'twiceDaily', startDelay:10000, cronExpression: '0 32 8,16 * * ?'
  }

  def userService

  def execute() {
      log.info("****** Starting hourly update ****** " + new Date())
      userService.updateUserEmails()
      log.info("****** Finished hourly update ******" + new Date())
  }
}