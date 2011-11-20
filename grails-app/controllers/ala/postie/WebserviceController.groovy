package ala.postie

class WebserviceController {

  def authService
  def queryService
  def userService

  def index = {}

  def taxonAlerts = {

    println("taxonAlerts lookup for...." + params.guid)

    //check for notifications for this query and this user
    Boolean alertExists = false

    Query query = queryService.createTaxonQuery(params.guid, params.taxonName)
    Notification n = null
    //find the query
    Query taxonQuery = Query.findByBaseUrlAndQueryPath(query.baseUrl,query.queryPath)

    if(taxonQuery!=null){
      println("Query already exists...." + taxonQuery.id)

      //does a notification exist???
      n = Notification.findByQueryAndUser(taxonQuery, userService.getUser())
      if(n != null){
        println("Notification for this user exists...." + authService.username())
        alertExists = true
      } else {
        println("Notification for this user DOES NOT exist...." + authService.username())
      }
    }

    response.addHeader("Cache-Control","no-cache")
    response.addHeader("Cache-Control","no-store")
    response.addHeader("Pragma","no-cache")

    [alertExists:alertExists, guid:params.guid, taxonName:params.taxonName, notification:n]
  }

  def createTaxonAlert = {

    Query newQuery = queryService.createTaxonQuery(params.guid, params.taxonName)

    //find the query
    Query taxonQuery = Query.findByBaseUrlAndQueryPath(newQuery.baseUrl,newQuery.queryPath)
    if(taxonQuery == null){
      newQuery = newQuery.save(true)
      new ala.postie.PropertyPath([name: "totalRecords", jsonPath: "totalRecords", query: newQuery, fireWhenNotZero: true]).save(true)
      new ala.postie.PropertyPath([name: "last_loaded_record", jsonPath: "occurrences[0].rowKey", query: newQuery]).save(true)
    } else {
      newQuery = taxonQuery
    }

    //does the notification already exist?
    def exists = Notification.findByQueryAndUser(newQuery, userService.getUser())
    if(!exists){
      (new Notification([query: newQuery, user: userService.getUser()])).save(true)
    }

    redirect([url:params.redirect])
  }

  def deleteAlert = {
    Notification n = Notification.findById(params.id)
    n.delete(flush:true)
    redirect([url:params.redirect])
  }
}
