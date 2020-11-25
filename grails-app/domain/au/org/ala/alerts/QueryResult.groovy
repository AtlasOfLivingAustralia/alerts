package au.org.ala.alerts

class QueryResult {

  Query query
  Frequency frequency
  User user                 // query result can be user specific, for example my annotations alerts
  Date lastChecked          // timestamp of last check
  Date previousCheck        // timestamp of previous check
  String queryUrlUsed       //query URL used
  String queryUrlUIUsed       //query URL used
  Boolean hasChanged = false
  Date lastChanged
  byte[] lastResult
  byte[] previousResult

  static hasMany = [propertyValues : PropertyValue]

  static constraints = {
    user nullable:true
    lastResult nullable: true //, minSize:0, maxSize:200000
    previousResult nullable: true //, minSize:0, maxSize:200000
    previousCheck nullable: true
    lastChanged nullable:true
    lastChecked nullable:true
    queryUrlUsed nullable:true
    queryUrlUIUsed nullable:true
  }

  static mapping = {
    lastResult sqlType: 'longblob' //,  minSize:0, maxSize: 200000
    previousResult sqlType: 'longblob' //,  minSize:0, maxSize: 200000
    queryUrlUsed  sqlType: 'text'
    queryUrlUIUsed  sqlType: 'text'
  }

  String toString(){
     "Last checked: " + lastChecked
  }
}
