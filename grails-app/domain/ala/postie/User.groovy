package ala.postie

class User {

  String email
  Frequency frequency
  static hasMany = [ notifications : Notification ]

  static constraints = {
     frequency nullable:true
  }
}
