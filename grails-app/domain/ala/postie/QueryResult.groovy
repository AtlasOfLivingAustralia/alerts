package ala.postie

class QueryResult {

  Query query
  Frequency frequency
  Date lastChecked          // timestamp of last checked date
  Date previousCheck        // timestamp of last checked date
  Boolean hasChanged = false
  Date lastChanged
  byte[] lastResult
  byte[] previousResult

  static hasMany = [propertyValues : PropertyValue]

  static constraints = {
    lastResult nullable: true, minSize:0, maxSize:200000
    previousResult nullable: true, minSize:0, maxSize:200000
    previousCheck nullable: true
    lastChanged nullable:true
    lastChecked nullable:true
  }

  static mapping = {
    lastResult sqlType: 'blob',  minSize:0, maxSize: 200000
    previousResult sqlType: 'blob',  minSize:0, maxSize: 200000
  }
}
