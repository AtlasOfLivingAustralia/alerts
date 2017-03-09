package au.org.ala.alerts

class QueryService {

  static transactional = true

  def serviceMethod() {}

  def grailsApplication

  Notification getNotificationForUser(Query query, User user) {
    Notification n = null
    //find the query
    Query retrievedQuery = Query.findByBaseUrlAndQueryPath(query.baseUrl, query.queryPath)

    if(retrievedQuery!=null){
      log.debug("Query already exists...." + retrievedQuery.id)
      //does a notification exist???
      n = Notification.findByQueryAndUser(retrievedQuery, user)
      if(n != null){
        log.debug("Notification for this user exists...." + user)
      } else {
        log.debug("Notification for this user DOES NOT exist...." + user)
      }
    }
    n
  }

  Boolean hasAFireProperty(Query query){
    Boolean hasFireProperty = false
    query.propertyPaths.each { pp ->
      if(pp.fireWhenChange || pp.fireWhenNotZero) hasFireProperty = true
    }
    hasFireProperty
  }

  Integer fireWhenNotZeroProperty(QueryResult queryResult){
    Integer fireWhenNotZeroValue = -1
    queryResult.propertyValues.each { pv ->
      if( pv.propertyPath.fireWhenNotZero) {
          fireWhenNotZeroValue = pv.currentValue.toInteger()
      }
    }
    fireWhenNotZeroValue
  }

  int deleteOrphanedQueries(){

    def toBeRemoved = []
    Query.findAll().each {
        if(it.notifications.size() == 0){
            toBeRemoved << it
        }
    }

    toBeRemoved.each {
        Query.deleteAll(toBeRemoved)
    }

    toBeRemoved.size()
  }

  def createQueryForUserIfNotExists(Query newQuery, User user){
    //find the query
    Query retrievedQuery = Query.findByBaseUrlAndQueryPath(newQuery.baseUrl, newQuery.queryPath)
    if(retrievedQuery == null){
      newQuery = newQuery.save(true)
      new PropertyPath([name: "totalRecords", jsonPath: "totalRecords", query: newQuery, fireWhenNotZero: true]).save(true)
      new PropertyPath([name: "last_loaded_record", jsonPath: "occurrences[0].rowKey", query: newQuery]).save(true)
    } else {
      newQuery = retrievedQuery
    }

    //does the notification already exist?
    def exists = Notification.findByQueryAndUser(newQuery, user)
    if(!exists){

      Notification n = new Notification([query: newQuery, user: user])
      n.save(true)
      
      if(n.hasErrors()){
        n.errors.allErrors.each { e -> println(e)}
      }
    }
  }

  /**
   * Takes a URL of the form "/ws/occurrences/search?......"
   *
   * @param biocacheWebserviceQueryPath
   * @return
   */
  Query createBioCacheChangeQuery(String biocacheWebserviceQueryPath, String biocacheUIQueryPath, String queryDisplayName, String baseUrlForWS, String baseUrlForUI, String resourceName){
    new Query([
      baseUrl: baseUrlForWS?:grailsApplication.config.biocache.baseURL,
      baseUrlForUI: baseUrlForUI?:grailsApplication.config.biocache.baseURL,
      resourceName:  resourceName,
      name: 'New records for ' + queryDisplayName,
      updateMessage: 'More occurrence records have been added for ' + queryDisplayName,
      description: 'Notify me when new records are added for ' + queryDisplayName,
      queryPath: biocacheWebserviceQueryPath + '&fq=first_loaded_date:[___DATEPARAM___%20TO%20*]&sort=first_loaded_date&dir=desc&pageSize=20&facets=basis_of_record',
      queryPathForUI: biocacheUIQueryPath + '&fq=first_loaded_date:[___DATEPARAM___%20TO%20*]&sort=first_loaded_date&dir=desc',
      dateFormat: """yyyy-MM-dd'T'HH:mm:ss'Z'""",
      emailTemplate: '/email/biocache',
      recordJsonPath: '\$.occurrences',
      idJsonPath: 'uuid',
      custom:true
    ])
  }

  Query createBioCacheAnnotationQuery(String biocacheWebserviceQueryPath, String biocacheUIQueryPath, String queryDisplayName, String baseUrlForWS, String baseUrlForUI, String resourceName){
    // Alert for Unconfirmed (50005), Verified (50002), Corrected (50003)
    new Query([
      baseUrl: baseUrlForWS?:grailsApplication.config.biocache.baseURL,
      baseUrlForUI: baseUrlForUI?:grailsApplication.config.biocache.baseURL,
      resourceName:  resourceName,
      name: 'New annotations on records for ' + queryDisplayName,
      updateMessage: 'Annotations have been added for ' + queryDisplayName,
      description: 'Notify me when new annotations are added for ' + queryDisplayName,
      queryPath: biocacheWebserviceQueryPath + '&fq=(user_assertions:50005%20OR%20user_assertions:50003%20OR%20user_assertions:50002)&fq=last_assertion_date:[___DATEPARAM___%20TO%20*]&sort=last_assertion_date&dir=desc&pageSize=20&facets=basis_of_record',
      queryPathForUI: biocacheUIQueryPath + '&fq=(user_assertions:50005%20OR%20user_assertions:50003%20OR%20user_assertions:50002)&fq=last_assertion_date:[___DATEPARAM___%20TO%20*]&sort=last_assertion_date&dir=desc',
      dateFormat: """yyyy-MM-dd'T'HH:mm:ss'Z'""",
      emailTemplate: '/email/biocache',
      recordJsonPath: '\$.occurrences',
      idJsonPath: 'uuid',
      custom:true
    ])
  }

  Query createBioCacheQuery(String biocacheWebserviceQueryPath, String biocacheUIQueryPath, String queryDisplayName, String baseUrlForWS, String baseUrlForUI, String resourceName){
    new Query([
      baseUrl: baseUrlForWS?:grailsApplication.config.biocache.baseURL,
      baseUrlForUI: baseUrlForUI?:grailsApplication.config.biocache.baseURL,
      resourceName:  resourceName,
      name: 'New records for ' + queryDisplayName,
      updateMessage: 'More occurrence records have been added for ' + queryDisplayName + ' - '+resourceName,
      description: 'Notify me when new records are added for ' + queryDisplayName + ' - '+resourceName,
      queryPath: biocacheWebserviceQueryPath + '&dir=desc&pageSize=20&facets=basis_of_record',
      queryPathForUI: biocacheUIQueryPath + '&dir=desc',
      dateFormat: """yyyy-MM-dd'T'HH:mm:ss'Z'""",
      emailTemplate: '/email/biocache',
      recordJsonPath: '\$.occurrences',
      idJsonPath: 'uuid',
      custom:true
    ])
  }

  Query createTaxonQuery(String taxonGuid, String taxonName){
    new Query([
      baseUrl: grailsApplication.config.biocache.baseURL,
      baseUrlForUI: grailsApplication.config.biocache.baseURL,
      name: 'New records for ' + taxonName,
      resourceName:  grailsApplication.config.postie.defaultResourceName,
      updateMessage: 'More occurrence records have been added for ' + taxonName,
      description: 'Notify me when new records are added for ' + taxonName,
      queryPath: '/ws/occurrences/taxon/'+ taxonGuid + '?fq=first_loaded_date:[___DATEPARAM___%20TO%20*]&sort=first_loaded_date&dir=desc&pageSize=20&facets=basis_of_record',
      queryPathForUI: '/occurrences/taxa/'+ taxonGuid + '?fq=first_loaded_date:[___DATEPARAM___%20TO%20*]&sort=first_loaded_date&dir=desc',
      dateFormat: """yyyy-MM-dd'T'HH:mm:ss'Z'""",
      emailTemplate: '/email/biocache',
      recordJsonPath: '\$.occurrences',
      idJsonPath: 'uuid',
      custom:true
    ])
  }

  Query createTaxonRegionQuery(String taxonGuid, String taxonName, String layerId, String regionName){
    new Query([
      baseUrl: grailsApplication.config.biocache.baseURL,
      baseUrlForUI: grailsApplication.config.biocache.baseURL,
      name: 'New records for ' + taxonName + ' recorded in ' + regionName,
      resourceName:  grailsApplication.config.postie.defaultResourceName,
      updateMessage: 'More occurrence records have been added for ' + taxonName + ' recorded in ' + regionName,
      description: 'Notify me when new records are added for ' + taxonName + ' recorded in ' + regionName,
      queryPath: '/ws/occurrences/taxon/'+ taxonGuid +'?' + layerId + ':%22'+regionName.encodeAsURL()+'%22&fq=first_loaded_date:[___DATEPARAM___%20TO%20*]&sort=first_loaded_date&dir=desc&pageSize=20&facets=basis_of_record',
      queryPathForUI: '/occurrences/taxa/'+ taxonGuid +'?' + layerId + ':%22'+regionName.encodeAsURL()+'%22&fq=first_loaded_date:[___DATEPARAM___%20TO%20*]&sort=first_loaded_date&dir=desc',
      dateFormat: """yyyy-MM-dd'T'HH:mm:ss'Z'""",
      emailTemplate: '/email/biocache',
      recordJsonPath: '\$.occurrences',
      idJsonPath: 'uuid',
      custom:true
    ])
  }

  Query createSpeciesGroupRegionQuery(String speciesGroup, String layerId, String regionName){
    new Query([
      baseUrl: grailsApplication.config.biocache.baseURL,
      baseUrlForUI: grailsApplication.config.biocache.baseURL,
      name: 'New records for ' + speciesGroup + ' recorded in ' + regionName,
      resourceName:  grailsApplication.config.postie.defaultResourceName,
      updateMessage: 'More occurrence records have been added for ' + speciesGroup + ' recorded in ' + regionName,
      description: 'Notify me when new records are added for ' + speciesGroup + ' recorded in ' + regionName,
      queryPath: '/ws/occurrences/search?q='+layerId+':%22'+regionName.encodeAsURL()+'%22&fq=species_group:'+speciesGroup+'&fq=first_loaded_date:[___DATEPARAM___%20TO%20*]&sort=first_loaded_date&dir=desc&pageSize=20&facets=basis_of_record',
      queryPathForUI: '/occurrences/search?q='+layerId+':"'+regionName.encodeAsURL()+'"&fq=species_group:'+speciesGroup+'&fq=first_loaded_date:[___DATEPARAM___%20TO%20*]&sort=first_loaded_date&dir=desc',
      dateFormat: """yyyy-MM-dd'T'HH:mm:ss'Z'""",
      emailTemplate: '/email/biocache',
      recordJsonPath: '\$.occurrences',
      idJsonPath: 'uuid',
      custom:true
    ])
  }

  Query createRegionQuery(String layerId, String regionName){
    new Query([
      baseUrl: grailsApplication.config.biocache.baseURL,
      baseUrlForUI: grailsApplication.config.biocache.baseURL,
      name: 'New records for ' + regionName,
      resourceName:  grailsApplication.config.postie.defaultResourceName,
      updateMessage: 'More occurrence records have been added for ' + regionName,
      description: 'Notify me when new records are added for ' + regionName,
      queryPath: '/ws/occurrences/search?q='+ layerId+':%22' + regionName.encodeAsURL() +'%22&fq=first_loaded_date:[___DATEPARAM___%20TO%20*]&sort=first_loaded_date&dir=desc&pageSize=20&facets=basis_of_record',
      queryPathForUI: '/occurrences/search?q='+ layerId+':%22' + regionName.encodeAsURL() +'%22&fq=first_loaded_date:[___DATEPARAM___%20TO%20*]&sort=first_loaded_date&dir=desc',
      dateFormat: """yyyy-MM-dd'T'HH:mm:ss'Z'""",
      emailTemplate: '/email/biocache',
      recordJsonPath: '\$.occurrences',
      idJsonPath: 'uuid',
      custom:true
    ])
  }
}
