package ala.postie


class DailyQueriesJob {
    def timeout = 86400000l // execute job once every hour

    def startDelay = 30000l //delay

    def notificationService

    def execute() {
      // execute task
      println("****** Scheduled daily update ****** " + new Date())
      notificationService.checkQueryForFrequency('daily')
      println("****** Scheduled daily update finished ******" + new Date())
    }
}
