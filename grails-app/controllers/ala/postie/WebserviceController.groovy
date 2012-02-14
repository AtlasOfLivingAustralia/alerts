package ala.postie

import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap

class WebserviceController {

  def authService
  def queryService
  def userService
  def grailsApplication

  def index = {}

  def test = {}

  private def getMyAlertsLink = {
    getServerRoot() + '/notification/myAlerts'
  }

  private def getServerRoot = {
    grailsApplication.config.serverName + grailsApplication.config.contextPath
  }

  /**
   * Service that returns a JSON callback response allowing consuming apps to create links
   * to create an alert or remove an alert
   */
  def taxonAlerts = {

    log.debug("TaxonAlerts lookup for...." + params.guid)

    String taxonGuid = params.guid
    if (taxonGuid == null) taxonGuid =  params.taxonGuid

    //check for notifications for this query and this user
    Query query = queryService.createTaxonQuery(taxonGuid, params.taxonName)

    Notification notification = queryService.getNotificationForUser(query, retrieveUser(params))

    String link = null
    if (notification  != null){
      //construct a link to delete the alert & manage alerts
      link = getMyAlertsLink()
    } else {
      //construct a create alert link
      link = getServerRoot() + '/webservice/createTaxonAlert?redirect='+
              params.redirect + '&taxonGuid=' + taxonGuid + '&taxonName=' + params.taxonName
    }

    render(view: 'alerts', model:[link:link, deleteLink:getDeleteLink(notification), displayName:params.taxonName, notification: notification])
  }

  def createTaxonAlert = {
    if( (params.guid || params.taxonGuid) && params.taxonName){
      
      String taxonGuid = params.guid
      if (taxonGuid == null) taxonGuid =  params.taxonGuid

      Query newQuery = queryService.createTaxonQuery(taxonGuid, params.taxonName)
      queryService.createQueryForUserIfNotExists(newQuery, userService.getUser())
      redirectIfSupplied(params)
    } else {
      response.sendError(400)
    }
  }

  def regionAlerts = {

    log.debug("RegionAlerts lookup for...." + params.layerId)

    //check for notifications for this query and this user
    Query query = queryService.createRegionQuery(params.layerId, params.regionName)

    Notification notification = queryService.getNotificationForUser(query, retrieveUser(params))

    String link = null
    if (notification  != null){
      //construct a link to delete the alert & manage alerts
      link = getMyAlertsLink()
    } else {
      //construct a create alert link
      link = getServerRoot() + '/webservice/createRegionAlert?layerId=' + params.layerId +
        '&regionName=' + params.regionName +
        '&redirect='+ params.redirect
    }

    render(view: 'alerts', model:[link:link, deleteLink:getDeleteLink(notification), displayName:params.regionName, notification: notification])
  }

  def taxonRegionAlerts = {

    log.debug("TaxonRegionAlerts lookup for...." + params.layerId)

    //check for notifications for this query and this user
    Query query = queryService.createTaxonRegionQuery(params.taxonGuid, params.taxonName, params.layerId, params.regionName)

    Notification notification = queryService.getNotificationForUser(query, retrieveUser(params))

    String link = null
    if (notification  != null){
      //construct a link to delete the alert & manage alerts
      link = getMyAlertsLink()
    } else {
      //construct a create alert link
      link = getServerRoot() + '/webservice/createTaxonRegionAlert' +
        '?layerId=' + params.layerId +
        '&regionName=' + params.regionName +
        '&taxonGuid=' + params.taxonGuid +
        '&taxonName=' + params.taxonName +
        '&redirect='+ params.redirect
    }

    String displayName = params.taxonName + " in " + params.regionName
    
    render(view: 'alerts', model:[link:link, deleteLink:getDeleteLink(notification), displayName:displayName, notification: notification])
  }

  def speciesGroupRegionAlerts = {

    log.debug("SpeciesGroupRegionAlerts lookup for...." + params.layerId)

    //check for notifications for this query and this user
    Query query = queryService.createSpeciesGroupRegionQuery(params.speciesGroup, params.layerId, params.regionName)

    Notification notification = queryService.getNotificationForUser(query, retrieveUser(params))

    String link = null
    if (notification  != null){
      //construct a link to delete the alert & manage alerts
      link = getMyAlertsLink()
    } else {
      //construct a create alert link
      link = getServerRoot() + '/webservice/createSpeciesGroupRegionAlert?layerId=' + params.layerId +
        '&regionName=' + params.regionName +
        '&speciesGroup=' + params.speciesGroup +
        '&redirect='+ params.redirect
    }

    String displayName = params.speciesGroup + " in " + params.regionName

    render(view: 'alerts', model:[link:link, deleteLink:getDeleteLink(notification), displayName:displayName, notification: notification])
  }

  private String getDeleteLink(Notification notification){
    if (notification ==null) ""
    else getServerRoot() + '/webservice/deleteAlert/'+notification.id
  }

  def createRegionAlert = {
    if(params.regionName && params.layerId){
      //region + species group
      Query newQuery = queryService.createRegionQuery(params.layerId, params.regionName)
      queryService.createQueryForUserIfNotExists(newQuery, userService.getUser())
      redirectIfSupplied(params)
    } else {
      response.sendError(400)
    }
  }

  def createTaxonRegionAlert = {

    log.debug('createTaxonRegionAlert ' + params.regionName + ' : ' + params.layerId)

    if(params.regionName && params.layerId && params.taxonGuid && params.taxonName){
      //region + taxon
      Query newQuery = queryService.createTaxonRegionQuery(params.taxonGuid, params.taxonName, params.layerId, params.regionName)
      queryService.createQueryForUserIfNotExists(newQuery, userService.getUser())
      redirectIfSupplied(params)
    } else {
      response.sendError(400)
    }
  }

  def createSpeciesGroupRegionAlert = {
    if(params.regionName && params.layerId && params.speciesGroup){
        //region + species group
      Query newQuery = queryService.createSpeciesGroupRegionQuery(params.speciesGroup, params.layerId, params.regionName)
      queryService.createQueryForUserIfNotExists(newQuery, userService.getUser())
      redirectIfSupplied(params)
    } else {
      response.sendError(400)
    }
  }

  private def redirectIfSupplied(GrailsParameterMap params) {
    if(params.redirect){
      redirect([url:params.redirect])
    } else {
      redirect([uri:'/'])
    }
  }

  def deleteAlert = {
    log.debug("Deleting an alert")
    Notification n = Notification.findById(params.id)
    n.delete(flush:true)
    redirectIfSupplied(params)
  }
  
  private User retrieveUser(params){
    User user = userService.getUser()
    if(user == null && params.userName){
      user = userService.getUserByUserName(params.userName)
    }
    user
  }
}