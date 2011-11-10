package ala.postie


class UpdateQueriesJob {
    def timeout = 360000l // execute job once every hour
    def startDelay = 30000l //delay

    def notificationService

    def execute() {
      println("****** Scheduled update ****** " + new Date())
      // execute task
      notificationService.checkAllQueries()

      println("****** Scheduled update finished ******" + new Date())
    }
}
