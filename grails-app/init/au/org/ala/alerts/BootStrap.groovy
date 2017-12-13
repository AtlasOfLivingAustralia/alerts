package au.org.ala.alerts

class BootStrap {

    javax.sql.DataSource dataSource
    def grailsApplication

    def init = { servletContext ->
        log.info("Running bootstrap queries.")
        preloadQueries()
        log.info("Done bootstrap queries.")
    }

    private void preloadQueries() {
        log.info("start of preloadQueries")
        if(Frequency.findAll().isEmpty()){
            (new Frequency([name:'hourly', periodInSeconds:3600])).save()
            (new Frequency([name:'daily'])).save()
            (new Frequency([name:'weekly', periodInSeconds:604800])).save()
            (new Frequency([name:'monthly', periodInSeconds:2419200])).save()
        }

        if(Query.findAllByName('Annotations').isEmpty()){
            Query newAssertions = (new Query([
                    baseUrl: grailsApplication.config.biocacheService.baseURL,
                    baseUrlForUI: grailsApplication.config.biocache.baseURL,
                    resourceName:  grailsApplication.config.postie.defaultResourceName,
                    name: 'Annotations',
                    updateMessage: 'annotations.update.message',
                    description: 'Notify me when annotations are made on any record.',
                    queryPath: '/ws/occurrences/search?fq=user_assertions:true&q=user_assertions:true AND last_assertion_date:[___DATEPARAM___%20TO%20*]&sort=last_assertion_date&dir=desc&pageSize=20&facets=basis_of_record',
                    queryPathForUI: '/occurrences/search?fq=user_assertions:true&q=last_assertion_date:[___DATEPARAM___%20TO%20*]&sort=last_assertion_date&dir=desc',
                    dateFormat: """yyyy-MM-dd'T'HH:mm:ss'Z'""",
                    emailTemplate: '/email/biocache',
                    recordJsonPath: '\$.occurrences[*]',
                    idJsonPath: 'uuid'
            ])).save()
            new PropertyPath([name: "totalRecords", jsonPath: "totalRecords", query: newAssertions, fireWhenNotZero: true]).save()
            new PropertyPath([name: "last_assertion_record", jsonPath: "occurrences[0].rowKey", query: newAssertions]).save()
        }

        if(Query.findAllByName('New records').isEmpty()){
            Query newRecords = (new Query([
                    baseUrl: grailsApplication.config.biocacheService.baseURL,
                    baseUrlForUI: grailsApplication.config.biocache.baseURL,
                    name: 'New records',
                    resourceName:  grailsApplication.config.postie.defaultResourceName,
                    updateMessage: 'more.records.update.message',
                    description: 'Notify me when new records are added.',
                    queryPath: '/ws/occurrences/search?q=first_loaded_date:[___DATEPARAM___%20TO%20*]&sort=first_loaded_date&dir=desc&pageSize=20&facets=basis_of_record',
                    queryPathForUI: '/occurrences/search?q=first_loaded_date:[___DATEPARAM___%20TO%20*]&sort=first_loaded_date&dir=desc',
                    dateFormat: """yyyy-MM-dd'T'HH:mm:ss'Z'""",
                    emailTemplate: '/email/biocache',
                    recordJsonPath: '\$.occurrences[*]',
                    idJsonPath: 'uuid'
            ])).save()
            new PropertyPath([name: "totalRecords", jsonPath: "totalRecords", query: newRecords, fireWhenNotZero: true]).save()
            new PropertyPath([name: "last_loaded_record", jsonPath: "occurrences[0].rowKey", query: newRecords]).save()
        }

        if(Query.findAllByName('New images').isEmpty()){
            Query newRecordsWithImages = (new Query([
                    baseUrl: grailsApplication.config.biocacheService.baseURL,
                    baseUrlForUI: grailsApplication.config.biocache.baseURL,
                    name: 'New images',
                    resourceName:  grailsApplication.config.postie.defaultResourceName,
                    updateMessage: 'more.images.update.message',
                    description: 'Notify me when new images are added.',
                    queryPath: '/ws/occurrences/search?q=first_loaded_date:[___DATEPARAM___%20TO%20*]&sort=first_loaded_date&dir=desc&fq=multimedia:Image&pageSize=20&facets=basis_of_record',
                    queryPathForUI: '/occurrences/search?q=first_loaded_date:[___DATEPARAM___%20TO%20*]&sort=first_loaded_date&dir=desc&fq=multimedia:Image',
                    dateFormat: """yyyy-MM-dd'T'HH:mm:ss'Z'""",
                    emailTemplate: '/email/biocache',
                    fireWhenNotZero: true,
                    recordJsonPath: '\$.occurrences[*]', // jsonpath 2 syntax: '\$.occurrences[*]'
                    idJsonPath: 'uuid'
            ])).save()
            new PropertyPath([name: "totalRecords", jsonPath: "totalRecords", query: newRecordsWithImages, fireWhenNotZero: true]).save()
            new PropertyPath([name: "last_loaded_record", jsonPath: "occurrences[0].rowKey", query: newRecordsWithImages]).save()
        }

        if(Query.findAllByName('Citizen science records').isEmpty()){
            Query newCitizenScienceRecords = (new Query([
                    baseUrl: grailsApplication.config.biocacheService.baseURL,
                    baseUrlForUI: grailsApplication.config.biocache.baseURL,
                    name: 'Citizen science records',
                    updateMessage: 'more.cs.update.message',
                    description: 'Notify me when new citizen science records are added.',
                    queryPath: '/ws/occurrences/search?q=first_loaded_date:[___DATEPARAM___%20TO%20*]&fq=data_resource_uid:dr364&sort=first_loaded_date&dir=desc&pageSize=20&facets=basis_of_record',
                    queryPathForUI: '/occurrences/search?q=first_loaded_date:[___DATEPARAM___%20TO%20*]&fq=data_resource_uid:dr364&sort=first_loaded_date&dir=desc',
                    dateFormat: """yyyy-MM-dd'T'HH:mm:ss'Z'""",
                    emailTemplate: '/email/biocache',
                    fireWhenNotZero: true,
                    recordJsonPath: '\$.occurrences[*]',
                    idJsonPath: 'uuid'
            ])).save()
            new PropertyPath([name: "totalRecords", jsonPath: "totalRecords", query: newCitizenScienceRecords, fireWhenNotZero: true]).save()
            new PropertyPath([name: "last_loaded_record", jsonPath: "occurrences[0].rowKey", query: newCitizenScienceRecords]).save()
        }

        if(Query.findAllByName('Citizen science records with images').isEmpty()){
            Query newCitizenScienceRecordsWithImages = (new Query([
                    baseUrl: grailsApplication.config.biocacheService.baseURL,
                    baseUrlForUI: grailsApplication.config.biocache.baseURL,
                    name: 'Citizen science records with images',
                    resourceName:  grailsApplication.config.postie.defaultResourceName,
                    updateMessage: 'more.cs.images.update.message',
                    description: 'Notify me when new citizen science records with images are added.',
                    queryPath: '/ws/occurrences/search?q=first_loaded_date:[___DATEPARAM___%20TO%20*]&fq=data_resource_uid:dr364&sort=first_loaded_date&dir=desc&pageSize=20&facets=basis_of_record&fq=multimedia:Image',
                    queryPathForUI: '/occurrences/search?q=first_loaded_date:[___DATEPARAM___%20TO%20*]&fq=data_resource_uid:dr364&sort=first_loaded_date&dir=desc&fq=multimedia:Image',
                    dateFormat: """yyyy-MM-dd'T'HH:mm:ss'Z'""",
                    emailTemplate: '/email/biocache',
                    recordJsonPath: '\$.occurrences[*]',
                    idJsonPath: 'uuid'
            ])).save()
            new PropertyPath([name: "totalRecords", jsonPath: "totalRecords", query: newCitizenScienceRecordsWithImages, fireWhenNotZero: true]).save()
            new PropertyPath([name: "last_loaded_record", jsonPath: "occurrences[0].rowKey", query: newCitizenScienceRecordsWithImages]).save()
        }

        if(Query.findAllByName('Spatial layers').isEmpty()){
            Query newSpatialLayers = (new Query([
                    baseUrl: grailsApplication.config.spatial.baseURL,
                    baseUrlForUI: grailsApplication.config.spatial.baseURL,
                    name: 'Spatial layers',
                    resourceName:  grailsApplication.config.postie.defaultResourceName,
                    updateMessage: 'more.spatial.update.message',
                    description: 'Notify me when new spatial layers are added.',
                    queryPath: '/ws/layers.json',
                    queryPathForUI: '/layers',
                    emailTemplate: '/email/layers',
                    recordJsonPath: '\$[*]',
                    idJsonPath: 'name'
            ])).save()
            new PropertyPath([name: "layer_count", jsonPath: "layerList", query: newSpatialLayers, fireWhenChanged: true]).save()
        }

        if(Query.findAllByName('Datasets').isEmpty()){
            Query newDatasets = (new Query([
                    baseUrl: grailsApplication.config.collectory.baseURL,
                    baseUrlForUI: grailsApplication.config.collectory.baseURL,
                    name: 'Datasets',
                    resourceName:  grailsApplication.config.postie.defaultResourceName,
                    updateMessage: 'more.datasets.update.message',
                    description: 'Notify me when new datasets are added.',
                    queryPath: '/ws/dataResource',
                    queryPathForUI: '/datasets',
                    emailTemplate: '/email/datasets',
                    recordJsonPath: '\$[*]',
                    idJsonPath: 'uid'
            ])).save()
            new PropertyPath([name: "dataset_count", jsonPath: "\$", query: newDatasets, fireWhenChanged: true]).save()
        }

        if(Query.findAllByName('Species lists').isEmpty()){
            log.info "Creating species list query"
            Query newSpeciesLists = (new Query([
                    baseUrl: grailsApplication.config.collectory.baseURL,
                    baseUrlForUI: grailsApplication.config.collectory.baseURL,
                    name: 'Species lists',
                    resourceName:  grailsApplication.config.postie.defaultResourceName,
                    updateMessage: 'more.specieslist.update.message',
                    description: 'Notify me when new species lists are added.',
                    queryPath: '/ws/dataResource?resourceType=species-list',
                    queryPathForUI: '/datasets#filters=resourceType%3Aspecies-list',
                    emailTemplate: '/email/datasets',
                    recordJsonPath: '\$[*]',
                    idJsonPath: 'uid'
            ])).save()
            new PropertyPath([name: "species_list_count", jsonPath: "\$", query: newSpeciesLists, fireWhenChanged: true]).save()
        }

        // get_category_posts.json
        if(Query.findAllByName('ALA Blog').isEmpty()){
            Query newBlogs = (new Query([
                    baseUrl: grailsApplication.config.ala.baseURL,
                    baseUrlForUI: grailsApplication.config.ala.baseURL,
                    name: 'Blogs and News',
                    resourceName:  grailsApplication.config.postie.defaultResourceName,
                    updateMessage: 'more.blogsnews.update.message',
                    description: 'Notify me when ALA Blog items are added.',
                    queryPath: '/api/get_category_posts/?slug=blogs-news&count=5',
                    queryPathForUI: '/blogs-news/',
                    emailTemplate: '/email/blogs',
                    recordJsonPath: '\$.posts[*]',
                    idJsonPath: 'id'
            ])).save()
            new PropertyPath([name: "last_blog_id", jsonPath: "posts", query: newBlogs, fireWhenChanged: true]).save()
        }
        log.info("end of preloadQueries")
    }

    def destroy = {}
}
