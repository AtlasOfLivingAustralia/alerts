package ala.postie

class UserService {

  static transactional = true

  def authService

  User getUser(){

    if(authService.username() == null){
      return null
    }

    User user = User.findByEmail(authService.username().toString().toLowerCase().trim())
    if(user == null){
      user = new User([email:authService.username().toString().toLowerCase().trim(), frequency:Frequency.findAll().first()])
      user.save(true)
    }
    user
  }

  def serviceMethod() {}
}
