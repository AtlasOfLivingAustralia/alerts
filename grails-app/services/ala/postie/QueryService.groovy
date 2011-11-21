package ala.postie

class QueryService {

  static transactional = true

  def serviceMethod() {}

  Query createTaxonQuery(String guid, String displayName){
    new Query([
      baseUrl: 'http://biocache.ala.org.au',
      name: 'New records for ' + displayName,
      updateMessage: 'More occurrence records have been added for ' + displayName,
      description: 'Notify me when new records are added for ' + displayName,
      queryPath: '/ws/occurrences/taxon/'+ guid +'?fq=last_load_date:[___DATEPARAM___%20TO%20*]&sort=last_load_date&dir=desc&pageSize=5&facets=basis_of_record',
      queryPathForUI: '/occurrences/taxa/'+ guid +'?fq=last_load_date:[___DATEPARAM___%20TO%20*]&sort=last_load_date&dir=desc',
      dateFormat: """yyyy-MM-dd'T'HH:mm:ss'Z'""",
      emailTemplate: '/email/biocache',
      recordJsonPath: '\$.occurrences',
      idJsonPath: 'uuid',
      custom:true
    ])
  }

  Query createRegionQuery(String regionType, String regionName){
    new Query([
      baseUrl: 'http://biocache.ala.org.au',
      name: 'New records for ' + regionName,
      updateMessage: 'More occurrence records have been added for ' + regionName,
      description: 'Notify me when new records are added for ' + regionName,
      queryPath: '/ws/occurrences/search?q='+ regionType+':' + regionName +'?fq=last_load_date:[___DATEPARAM___%20TO%20*]&sort=last_load_date&dir=desc&pageSize=5&facets=basis_of_record',
      queryPathForUI: '/occurrences/search?q='+ regionType+':' + regionName +'&fq=last_load_date:[___DATEPARAM___%20TO%20*]&sort=last_load_date&dir=desc',
      dateFormat: """yyyy-MM-dd'T'HH:mm:ss'Z'""",
      emailTemplate: '/email/biocache',
      recordJsonPath: '\$.occurrences',
      idJsonPath: 'uuid',
      custom:true
    ])
  }
}
