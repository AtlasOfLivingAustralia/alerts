package ala.postie

class UserService {

  static transactional = true

  def authService

  def serviceMethod() {}

  User getUser(){

    def userDetails = authService.userDetails()

    if(!userDetails["userId"]){
      println "User isnt logged in - or there is a problem with CAS configuration"
      return null
    }

    User user = User.findByUserId(userDetails["userId"])
    if(user == null){
      user = new User([email:userDetails["email"], userId:userDetails["userId"], frequency:Frequency.findAll().first()])
      user.save(true)
    }
    user
  }
}
