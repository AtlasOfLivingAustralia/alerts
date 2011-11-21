package ala.postie


class HourlyQueriesJob {
    def timeout = 3600000l // execute job once every hour

    def startDelay = 30000l //delay

    def notificationService

    def execute() {
      // execute task
      println("****** Scheduled hourly update ****** " + new Date())
      notificationService.checkQueryForFrequency('hourly')
      println("****** Scheduled hourly update finished ******" + new Date())
    }
}
