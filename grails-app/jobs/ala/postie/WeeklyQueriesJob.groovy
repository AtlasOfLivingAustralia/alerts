package ala.postie


class WeeklyQueriesJob {
    def timeout = 604800000l // execute job once every hour

    def startDelay = 60000l //delay

    def notificationService

    def execute() {
      // execute task
      println("****** Scheduled weekly update ****** " + new Date())
      notificationService.checkQueryForFrequency(FrequencyType.weekly)
      println("****** Scheduled weekly update finished ******" + new Date())
    }
}
