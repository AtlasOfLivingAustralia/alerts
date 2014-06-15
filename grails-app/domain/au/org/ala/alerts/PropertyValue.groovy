package au.org.ala.alerts

class PropertyValue {

  PropertyPath propertyPath
  QueryResult queryResult
  String currentValue
  String previousValue

  static constraints = {
    currentValue nullable:true
    previousValue nullable:true
  }
}
