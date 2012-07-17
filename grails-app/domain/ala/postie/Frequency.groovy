package ala.postie

class Frequency {

  String name
  Integer periodInSeconds = 86400l   //default to daily
  Date lastChecked

  static constraints = {
    lastChecked nullable : true
  }

  static mapping = {
    version false
  }

  public String toString() { name }
}
