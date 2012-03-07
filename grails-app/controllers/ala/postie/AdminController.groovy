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

  def addUsers = {}

  def saveUsers = {

    def emailAddresses = params.usersToAdd.toString().split("\n")

    def monthlyFrequency = Frequency.findByName("monthly")

    def blogQuery = Query.findByName("Blogs and News")

    println("Retrieved blog query: " + blogQuery)

    emailAddresses.each { email ->
      if (email.trim().length() >0){
        println('Adding user: ' + email.trim().toLowerCase())
        //add to the DB
        User user = User.findByEmail(email.trim())
        if (user == null){
          user = new User([email:email.trim().toLowerCase(), frequency:monthlyFrequency ])
          user.save(flush:true)

          //add notification to blogs
          Notification n = new Notification([user: user, query:blogQuery])
          n.save(flush: true)
        }
      }
    }
  }
}
