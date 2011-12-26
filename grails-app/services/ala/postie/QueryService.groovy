package ala.postie

class QueryService {

  static transactional = true

  def serviceMethod() {}

  def userService

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

  Boolean hasAFireWhenNotZeroProperty(Query query){
    Boolean hasFireProperty = false
    query.propertyPaths.each { pp ->
      if( pp.fireWhenNotZero) hasFireProperty = true
    }
    hasFireProperty
  }

  Integer fireWhenNotZeroProperty(QueryResult queryResult){
    Integer fireWhenNotZeroValue = -1
    queryResult.propertyValues.each { pv ->
      if( pv.propertyPath.fireWhenNotZero) fireWhenNotZeroValue = pv.currentValue.toInteger()
    }
    fireWhenNotZeroValue
  }

  def createQueryForUserIfNotExists(Query newQuery, User user){
    //find the query
    Query retrievedQuery = Query.findByBaseUrlAndQueryPath(newQuery.baseUrl,newQuery.queryPath)
    if(retrievedQuery == null){
      newQuery = newQuery.save(true)
      new ala.postie.PropertyPath([name: "totalRecords", jsonPath: "totalRecords", query: newQuery, fireWhenNotZero: true]).save(true)
      new ala.postie.PropertyPath([name: "last_loaded_record", jsonPath: "occurrences[0].rowKey", query: newQuery]).save(true)
    } else {
      newQuery = retrievedQuery
    }

    //does the notification already exist?
    def exists = Notification.findByQueryAndUser(newQuery, user)
    if(!exists){

      Notification n = new Notification([query: newQuery, user: user])
      n.save(true)
      
      if(n.hasErrors()){
        //n.errors.fieldErrors.each { e -> println(e)}
        //n.errors.globalErrors.each { e -> println(e)}
        n.errors.allErrors.each { e -> println(e)}
        
      }
    }
  }

  Query createTaxonQuery(String taxonGuid, String taxonName){
    new Query([
      baseUrl: 'http://biocache.ala.org.au',
      name: 'New records for ' + taxonName,
      updateMessage: 'More occurrence records have been added for ' + taxonName,
      description: 'Notify me when new records are added for ' + taxonName,
      queryPath: '/ws/occurrences/taxon/'+ taxonGuid + '?fq=last_load_date:[___DATEPARAM___%20TO%20*]&sort=last_load_date&dir=desc&pageSize=20&facets=basis_of_record',
      queryPathForUI: '/occurrences/taxa/'+ taxonGuid + '?fq=last_load_date:[___DATEPARAM___%20TO%20*]&sort=last_load_date&dir=desc',
      dateFormat: """yyyy-MM-dd'T'HH:mm:ss'Z'""",
      emailTemplate: '/email/biocache',
      recordJsonPath: '\$.occurrences',
      idJsonPath: 'uuid',
      custom:true
    ])
  }

  Query createTaxonRegionQuery(String taxonGuid, String taxonName, String layerId, String regionName){
    new Query([
      baseUrl: 'http://biocache.ala.org.au',
      name: 'New records for ' + taxonName + ' recorded in ' + regionName,
      updateMessage: 'More occurrence records have been added for ' + taxonName + ' recorded in ' + regionName,
      description: 'Notify me when new records are added for ' + taxonName + ' recorded in ' + regionName,
      queryPath: '/ws/occurrences/taxon/'+ taxonGuid +'?' + layerId + ':%22'+regionName.encodeAsURL()+'%22&fq=last_load_date:[___DATEPARAM___%20TO%20*]&sort=last_load_date&dir=desc&pageSize=20&facets=basis_of_record',
      queryPathForUI: '/occurrences/taxa/'+ taxonGuid +'?' + layerId + ':%22'+regionName.encodeAsURL()+'%22&fq=last_load_date:[___DATEPARAM___%20TO%20*]&sort=last_load_date&dir=desc',
      dateFormat: """yyyy-MM-dd'T'HH:mm:ss'Z'""",
      emailTemplate: '/email/biocache',
      recordJsonPath: '\$.occurrences',
      idJsonPath: 'uuid',
      custom:true
    ])
  }

  Query createSpeciesGroupRegionQuery(String speciesGroup, String layerId, String regionName){
    new Query([
      baseUrl: 'http://biocache.ala.org.au',
      name: 'New records for ' + speciesGroup + ' recorded in ' + regionName,
      updateMessage: 'More occurrence records have been added for ' + speciesGroup + ' recorded in ' + regionName,
      description: 'Notify me when new records are added for ' + speciesGroup + ' recorded in ' + regionName,
      queryPath: '/ws/occurrences/search?q='+layerId+':%22'+regionName.encodeAsURL()+'%22&fq=species_group:'+speciesGroup+'&fq=last_load_date:[___DATEPARAM___%20TO%20*]&sort=last_load_date&dir=desc&pageSize=20&facets=basis_of_record',
      queryPathForUI: '/occurrences/search?q='+layerId+':"'+regionName.encodeAsURL()+'"&fq=species_group:'+speciesGroup+'&fq=last_load_date:[___DATEPARAM___%20TO%20*]&sort=last_load_date&dir=desc',
      dateFormat: """yyyy-MM-dd'T'HH:mm:ss'Z'""",
      emailTemplate: '/email/biocache',
      recordJsonPath: '\$.occurrences',
      idJsonPath: 'uuid',
      custom:true
    ])
  }


  Query createRegionQuery(String layerId, String regionName){
    new Query([
      baseUrl: 'http://biocache.ala.org.au',
      name: 'New records for ' + regionName,
      updateMessage: 'More occurrence records have been added for ' + regionName,
      description: 'Notify me when new records are added for ' + regionName,
      queryPath: '/ws/occurrences/search?q='+ layerId+':%22' + regionName.encodeAsURL() +'%22&fq=last_load_date:[___DATEPARAM___%20TO%20*]&sort=last_load_date&dir=desc&pageSize=20&facets=basis_of_record',
      queryPathForUI: '/occurrences/search?q='+ layerId+':%22' + regionName.encodeAsURL() +'%22&fq=last_load_date:[___DATEPARAM___%20TO%20*]&sort=last_load_date&dir=desc',
      dateFormat: """yyyy-MM-dd'T'HH:mm:ss'Z'""",
      emailTemplate: '/email/biocache',
      recordJsonPath: '\$.occurrences',
      idJsonPath: 'uuid',
      custom:true
    ])
  }
}
