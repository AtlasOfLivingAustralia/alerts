package au.org.ala.alerts

class User {

  String userId //ALA CAS ID
  String email
  Frequency frequency
  static hasMany = [ notifications : Notification ]

  static constraints = {
     frequency nullable:true
  }

  public String toString(){ userId + " - " + email }
}
