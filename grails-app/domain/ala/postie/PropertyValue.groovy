package ala.postie

class PropertyValue {
  Query query
  String jsonPath
  String name
  String currentValue
  String previousValue
  boolean fireWhenNotZero = false
  boolean fireWhenChange = false

  static constraints = {
    currentValue nullable:true
    previousValue nullable:true
  }
}
