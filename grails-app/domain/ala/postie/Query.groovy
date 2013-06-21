package ala.postie

class Query {

  String name
  String updateMessage
  String description
  String baseUrl
  String baseUrlForUI
  String resourceName     //e.g. Atlas or AVH
  String queryPath          // a URL that returns an XML/JSON response
  String queryPathForUI     // a URL that returns an XML/JSON response

  String emailTemplate = 'email/alert'
  //byte[] lastResultZipped
  boolean custom = false

  //optional properties
  String dateFormat

  String idJsonPath   //the json path for producing a list of IDs for change detection
  String recordJsonPath

  static hasMany = [ notifications : Notification, queryResults:QueryResult, propertyPaths:PropertyPath ]

  static constraints = {
    description nullable: true, maxSize: 400, widget:'textarea'
    dateFormat nullable: true
    idJsonPath nullable: true
    recordJsonPath nullable: true
    description  sqlType: 'text'
    updateMessage  sqlType: 'text'
    queryPathForUI  sqlType: 'text'
    queryPath  sqlType: 'text'
  }

  public String toString() {
    return name
  }
}