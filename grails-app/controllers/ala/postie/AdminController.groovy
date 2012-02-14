package ala.postie


class AdminController {

  def index() { }
  
  def notificationService
  def authService
  
  def runChecksNow = {
    println("Run checks....")

    //if(authService.userInRole("ADMIN") || authService.username() == 'david.martin@csiro.au'){
      if(params.frequency){
        println('Manual start of ' + params.frequency + ' checks')
        notificationService.checkQueryForFrequency(params.frequency)
      }
      response.setContentType("text/plain")
      response.sendError(200)
      null
  }
}
