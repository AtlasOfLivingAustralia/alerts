package ala.postie


class MonthlyQueriesJob {

  static triggers = {
     cron name:'monthly', startDelay:10000, cronExpression: '0 15 17 27 * ?'        //fire 10:15 on last day of month
  }

  def notificationService

  def execute() {
    // execute task
    log.info("****** Scheduled monthly update ****** " + new Date())
    notificationService.checkQueryForFrequency('monthly')
    log.info("****** Scheduled monthly update finished ******" + new Date())
  }
}
