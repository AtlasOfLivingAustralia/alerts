package ala.postie

class Query {

  public static final int GIGABYTES = 1024 * 1024 * 1024
  public static final int MAX_SIZE = 2 * GIGABYTES

  String name
  String updateMessage
  String description

  String baseUrl
  String queryPath          // a URL that returns an XML/JSON response
  String queryPathForUI     // a URL that returns an XML/JSON response

  Date lastChecked          // timestamp of last checked date
  Date previousCheck        // timestamp of last checked date

  Date lastChanged          //date of the last actual change

  byte[] lastResult
  byte[] previousResult

  String emailTemplate = 'email/alert'
  //byte[] lastResultZipped
  boolean custom = false

  //optional properties
  String dateFormat

  String idJsonPath   //the json path for producing a list of IDs for change detection
  String recordJsonPath

  static hasMany = [ propertyValues : PropertyValue, notifications : Notification ]

  static constraints = {
    description nullable: true, maxSize: 400, widget:'textarea'
    lastResult nullable: true, minSize:0, maxSize:200000
    previousResult nullable: true, minSize:0, maxSize:200000
    previousCheck nullable: true
    lastChanged nullable:true
    lastChecked nullable:true
    dateFormat nullable: true
    idJsonPath nullable: true
    recordJsonPath nullable: true
  }

  static mapping = {
    lastResult sqlType: 'blob',  minSize:0, maxSize: 200000
    previousResult sqlType: 'blob',  minSize:0, maxSize: 200000
  }

  public String toString() {
      return name
  }
}