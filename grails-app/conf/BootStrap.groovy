import ala.postie.Notification
import ala.postie.Query
import ala.postie.PropertyValue
import ala.postie.Frequency
import org.springframework.scheduling.support.CronTrigger

class BootStrap {

    javax.sql.DataSource dataSource

    def init = { servletContext ->

      log.info("Running bootstrap queries")

      //if(Query.findAll().isEmpty()){
      preloadQueries()
      //}

      log.info("Done bootstrap queries.")
    }

  private def preloadQueries() {

    if(Frequency.findAll().isEmpty()){
      (new Frequency([name:'hourly', periodInSeconds:3600])).save()
      (new Frequency([name:'daily'])).save()
      (new Frequency([name:'weekly', periodInSeconds:604800])).save()
      (new Frequency([name:'monthly', periodInSeconds:2419200])).save()
    }

    if(Query.findAllByName('Annotations').isEmpty()){
      Query newAssertions = (new Query([
              baseUrl: 'http://biocache.ala.org.au',
              name: 'Annotations',
              updateMessage: 'Occurrence records have been annotated.',
              description: 'Notify me when annotations are made on any record.',
              queryPath: '/ws/occurrences/search?q=last_assertion_date:[___DATEPARAM___%20TO%20*]&sort=last_assertion_date&dir=desc&pageSize=20&facets=basis_of_record',
              queryPathForUI: '/occurrences/search?q=last_assertion_date:[___DATEPARAM___%20TO%20*]&sort=last_assertion_date&dir=desc',
              dateFormat: """yyyy-MM-dd'T'HH:mm:ss'Z'""",
              emailTemplate: '/email/biocache',
              recordJsonPath: '\$.occurrences',
              idJsonPath: 'uuid'
      ])).save()
      new ala.postie.PropertyPath([name: "totalRecords", jsonPath: "totalRecords", query: newAssertions, fireWhenNotZero: true]).save()
      new ala.postie.PropertyPath([name: "last_assertion_record", jsonPath: "occurrences[0].rowKey", query: newAssertions]).save()
    }

    if(Query.findAllByName('New records').isEmpty()){
      Query newRecords = (new Query([
              baseUrl: 'http://biocache.ala.org.au',
              name: 'New records',
              updateMessage: 'More occurrence records have been added.',
              description: 'Notify me when new records are added.',
              queryPath: '/ws/occurrences/search?q=last_load_date:[___DATEPARAM___%20TO%20*]&sort=last_load_date&dir=desc&pageSize=20&facets=basis_of_record',
              queryPathForUI: '/occurrences/search?q=last_load_date:[___DATEPARAM___%20TO%20*]&sort=last_load_date&dir=desc',
              dateFormat: """yyyy-MM-dd'T'HH:mm:ss'Z'""",
              emailTemplate: '/email/biocache',
              recordJsonPath: '\$.occurrences',
              idJsonPath: 'uuid'
      ])).save()
      new ala.postie.PropertyPath([name: "totalRecords", jsonPath: "totalRecords", query: newRecords, fireWhenNotZero: true]).save()
      new ala.postie.PropertyPath([name: "last_loaded_record", jsonPath: "occurrences[0].rowKey", query: newRecords]).save()
    }

    if(Query.findAllByName('New images').isEmpty()){
      Query newRecordsWithImages = (new Query([
              baseUrl: 'http://biocache.ala.org.au',
              name: 'New images',
              updateMessage: 'More occurrence records with images have been added.',
              description: 'Notify me when new images are added.',
              queryPath: '/ws/occurrences/search?q=last_load_date:[___DATEPARAM___%20TO%20*]&sort=last_load_date&dir=desc&fq=multimedia:Multimedia&pageSize=20&facets=basis_of_record',
              queryPathForUI: '/occurrences/search?q=last_load_date:[___DATEPARAM___%20TO%20*]&sort=last_load_date&dir=desc&fq=multimedia:Multimedia',
              dateFormat: """yyyy-MM-dd'T'HH:mm:ss'Z'""",
              emailTemplate: '/email/biocache',
              fireWhenNotZero: true,
              recordJsonPath: '\$.occurrences',
              idJsonPath: 'uuid'
      ])).save()
      new ala.postie.PropertyPath([name: "totalRecords", jsonPath: "totalRecords", query: newRecordsWithImages, fireWhenNotZero: true]).save()
      new ala.postie.PropertyPath([name: "last_loaded_record", jsonPath: "occurrences[0].rowKey", query: newRecordsWithImages]).save()
    }

    if(Query.findAllByName('Citizen science records').isEmpty()){
      Query newCitizenScienceRecords = (new Query([
              baseUrl: 'http://biocache.ala.org.au',
              name: 'Citizen science records',
              updateMessage: 'More citizen science records have been added.',
              description: 'Notify me when new citizen science records are added.',
              queryPath: '/ws/occurrences/search?q=last_load_date:[___DATEPARAM___%20TO%20*]&fq=data_resource_uid:dr364&sort=last_load_date&dir=desc&pageSize=20&facets=basis_of_record',
              queryPathForUI: '/occurrences/search?q=last_load_date:[___DATEPARAM___%20TO%20*]&fq=data_resource_uid:dr364&sort=last_load_date&dir=desc',
              dateFormat: """yyyy-MM-dd'T'HH:mm:ss'Z'""",
              emailTemplate: '/email/biocache',
              fireWhenNotZero: true,
              recordJsonPath: '\$.occurrences',
              idJsonPath: 'uuid'
      ])).save()
      new ala.postie.PropertyPath([name: "totalRecords", jsonPath: "totalRecords", query: newCitizenScienceRecords, fireWhenNotZero: true]).save()
      new ala.postie.PropertyPath([name: "last_loaded_record", jsonPath: "occurrences[0].rowKey", query: newCitizenScienceRecords]).save()
    }

    if(Query.findAllByName('Citizen science records with images').isEmpty()){
      Query newCitizenScienceRecordsWithImages = (new Query([
              baseUrl: 'http://biocache.ala.org.au',
              name: 'Citizen science records with images',
              updateMessage: 'More citizen science records with images have been added.',
              description: 'Notify me when new citizen science records with images are added.',
              queryPath: '/ws/occurrences/search?q=last_load_date:[___DATEPARAM___%20TO%20*]&fq=data_resource_uid:dr364&sort=last_load_date&dir=desc&pageSize=20&facets=basis_of_record&fq=multimedia:Multimedia',
              queryPathForUI: '/occurrences/search?q=last_load_date:[___DATEPARAM___%20TO%20*]&fq=data_resource_uid:dr364&sort=last_load_date&dir=desc&fq=multimedia:Multimedia',
              dateFormat: """yyyy-MM-dd'T'HH:mm:ss'Z'""",
              emailTemplate: '/email/biocache',
              recordJsonPath: '\$.occurrences',
              idJsonPath: 'uuid'
      ])).save()
      new ala.postie.PropertyPath([name: "totalRecords", jsonPath: "totalRecords", query: newCitizenScienceRecordsWithImages, fireWhenNotZero: true]).save()
      new ala.postie.PropertyPath([name: "last_loaded_record", jsonPath: "occurrences[0].rowKey", query: newCitizenScienceRecordsWithImages]).save()
    }

    if(Query.findAllByName('Spatial layers').isEmpty()){
      Query newSpatialLayers = (new Query([
              baseUrl: 'http://spatial.ala.org.au',
              name: 'Spatial layers',
              updateMessage: 'More spatial layers have been added.',
              description: 'Notify me when new spatial layers are added.',
              queryPath: '/layers.json',
              queryPathForUI: '/layers',
              emailTemplate: '/email/layers',
              recordJsonPath: '\$.layerList',
              idJsonPath: 'name'
      ])).save()
      new ala.postie.PropertyPath([name: "layer_count", jsonPath: "layerList", query: newSpatialLayers, fireWhenChanged: true]).save()
    }

    if(Query.findAllByName('Datasets').isEmpty()){
      Query newDatasets = (new Query([
              baseUrl: 'http://collections.ala.org.au',
              name: 'Datasets',
              updateMessage: 'More datasets have been added.',
              description: 'Notify me when new datasets are added.',
              queryPath: '/ws/dataResource',
              queryPathForUI: '/datasets',
              emailTemplate: '/email/datasets',
              recordJsonPath: '\$',
              idJsonPath: 'uid'
      ])).save()
      new ala.postie.PropertyPath([name: "dataset_count", jsonPath: "\$", query: newDatasets, fireWhenChanged: true]).save()
    }

//      (new Notification([query: newAssertions, userEmail:"moyesyside@gmail.com"])).save()
    //      (new Notification([query: newRecords, userEmail:"moyesyside@gmail.com"])).save()
    //      (new Notification([query: newRecordsWithImages, userEmail:"moyesyside@gmail.com"])).save()
    //      (new Notification([query: newCitizenScienceRecords, userEmail:"moyesyside@gmail.com"])).save()
    //      (new Notification([query: newCitizenScienceRecordsWithImages, userEmail:"moyesyside@gmail.com"])).save()
    //      (new Notification([query: newSpatialLayers, userEmail:"moyesyside@gmail.com"])).save()
  }

  def destroy = {}
}
