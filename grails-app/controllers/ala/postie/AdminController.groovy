package ala.postie


class AdminController {

  def index() { }

  def authService

  def notificationService

  def runChecksNow = {
      if(authService.userInRole("ROLE_ADMIN") ){
          log.info("Run checks....")
          println("runchecks....AUTH")
          if(params.frequency){
            log.info('Manual start of ' + params.frequency + ' checks')
            notificationService.checkQueryForFrequency(params.frequency)
          }
          response.setContentType("text/plain")
          response.setStatus(200)
      } else {
          log.info("Run checks UNAUTHORIZED....")
          response.sendError(401)
      }
      null
  }

  def addUsers = {}

  def saveUsers = {

    def emailAddresses = params.usersToAdd.toString().split("\n")

    def monthlyFrequency = Frequency.findByName("monthly")

    def blogQuery = Query.findByName("Blogs and News")

    log.debug("Retrieved blog query: " + blogQuery)

    emailAddresses.each { email ->
      if (email.trim().length() >0){
        log.debug('Adding user: ' + email.trim().toLowerCase())
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
