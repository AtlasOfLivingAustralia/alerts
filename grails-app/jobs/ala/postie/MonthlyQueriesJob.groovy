package ala.postie


class MonthlyQueriesJob {
    def timeout = 2419200000l // execute job once every month

    def startDelay = 90000l //delay

    def notificationService

    def execute() {
      // execute task
      println("****** Scheduled monthly update ****** " + new Date())
      notificationService.checkQueryForFrequency('monthly')
      println("****** Scheduled monthly update finished ******" + new Date())
    }
}
