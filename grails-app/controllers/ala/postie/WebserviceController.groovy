package ala.postie

class WebserviceController {

  def authService
  def queryService

  def index = {}

  def taxonAlerts = {

     println("taxonAlerts lookup for...." + params.guid)

    //check for notifications for this query and this user
    Boolean alertExists = false

    Query query = queryService.createTaxonQuery(params.guid, params.taxonName)

    //find the query
    Query taxonQuery = Query.findByBaseUrlAndQueryPath(query.baseUrl,query.queryPath)

    if(taxonQuery!=null){
      println("Query already exists...." + taxonQuery.id)

      //does a notification exist???
      Notification n = Notification.findByQueryAndUserEmail(taxonQuery, authService.username())
      if(n != null){
        println("Notification for this user exists...." + authService.username())
        alertExists = true
      } else {
        println("Notification for this user DOES NOT exist...." + authService.username())
      }
    }

    [alertExists:alertExists, guid:params.guid, taxonName:params.taxonName]
  }

  def createTaxonAlert = {

    println("Create alert redirect: " + params.redirect)
    println("Create alert redirect: " + params.guid)
    println("Create alert redirect: " + params.taxonName)
    println("Create alert redirect - user name: " + authService.username())

    Query newQuery = queryService.createTaxonQuery(params.guid, params.taxonName)

    //find the query
    Query taxonQuery = Query.findByBaseUrlAndQueryPath(newQuery.baseUrl,newQuery.queryPath)
    if(taxonQuery == null){
      newQuery = newQuery.save(true)
      new ala.postie.PropertyValue([name: "totalRecords", jsonPath: "totalRecords", query: newQuery, fireWhenNotZero: true]).save(true)
      new ala.postie.PropertyValue([name: "last_loaded_record", jsonPath: "occurrences[0].rowKey", query: newQuery]).save(true)
    } else {
      newQuery = taxonQuery
    }

    //add a notification
    (new Notification([query: newQuery, userEmail: authService.username()])).save(true)

    redirect([url:params.redirect])
  }

  def deleteAlert = {




  }

  def disable = {





  }
}
