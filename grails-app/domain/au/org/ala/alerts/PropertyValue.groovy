package au.org.ala.alerts

class PropertyValue {

  PropertyPath propertyPath
  QueryResult queryResult
  String currentValue
  String previousValue

//  static belongsTo = [ propertyPath: PropertyPath, queryResult:  QueryResult]
  static constraints = {
//    propertyPath nullable:true
    currentValue nullable:true
    previousValue nullable:true
  }
}
