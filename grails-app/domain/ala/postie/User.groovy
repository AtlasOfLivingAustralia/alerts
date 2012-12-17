package ala.postie

class User {

  String userId //ALA CAS ID
  String email
  Frequency frequency
  static hasMany = [ notifications : Notification ]

  static constraints = {
     frequency nullable:true
  }

  public String toString(){ email }
}
