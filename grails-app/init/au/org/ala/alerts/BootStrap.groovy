package au.org.ala.alerts

import ala.postie.BiosecurityQueriesJob

class BootStrap {

    def grailsApplication
    def messageSource
    def siteLocale
    def grailsUrlMappingsHolder

    def init = { servletContext ->
        log.info("Running bootstrap queries.")

        // if my annotation feature turned on, add url mapping to handle add/remove alert requests
        if (grailsApplication.config.getProperty('myannotation.enabled', Boolean, false)) {
            grailsUrlMappingsHolder.addMappings({
                "/admin/user/subscribeMyAnnotation/"(controller: 'notification', action: 'subscribeMyAnnotation')
                "/admin/user/unsubscribeMyAnnotation/"(controller: 'notification', action: 'unsubscribeMyAnnotation')

                "/api/alerts/user/$userId/subscribeMyAnnotation"(controller: 'webservice', action: [POST: 'subscribeMyAnnotationWS'])
                "/api/alerts/user/$userId/unsubscribeMyAnnotation"(controller: 'webservice', action: [POST: 'unsubscribeMyAnnotationWS'])
            })
        }

        messageSource.setBasenames(
                "file:///var/opt/atlas/i18n/alerts/messages",
                "file:///opt/atlas/i18n/alerts/messages",
                "WEB-INF/grails-app/alerts/messages",
                "classpath:messages"
        )

        siteLocale = new Locale.Builder().setLanguageTag(grailsApplication.config.siteDefaultLanguage).build();
        Locale.setDefault(siteLocale)

        preloadQueries()
        log.info("Done bootstrap queries.")

        // dynamic job
        def cron = grailsApplication.config.getProperty("biosecurity.cronExpression")
        if (cron) {
            BiosecurityQueriesJob.schedule(cron)
        }
    }

    private void preloadQueries() {
        log.info("start of preloadQueries")
        if(Frequency.findAll().isEmpty()){
            (new Frequency([name: 'hourly', periodInSeconds:3600])).save()
            (new Frequency([name: 'daily'])).save()
            (new Frequency([name: 'weekly', periodInSeconds:604800])).save()
            (new Frequency([name: 'monthly', periodInSeconds:2419200])).save()
        }

        def title = messageSource.getMessage("query.annotations.title", null, siteLocale)
        def descr = messageSource.getMessage("query.annotations.descr", null, siteLocale)
        if(Query.findAllByName(title).isEmpty()){
            Query newAssertions = (new Query([
                    baseUrl: grailsApplication.config.biocacheService.baseURL,
                    baseUrlForUI: grailsApplication.config.biocache.baseURL,
                    resourceName:  grailsApplication.config.mail.details.defaultResourceName,
                    name: title,
                    updateMessage: 'annotations.update.message',
                    description: descr,
                    queryPath: '/occurrences/search?fq=user_assertions:*&q=last_assertion_date:\' + \'[___DATEPARAM___ TO *]\'.encodeAsURL() + \'&sort=last_assertion_date&dir=desc&pageSize=20&facets=basis_of_record',
                    queryPathForUI: '/occurrences/search?fq=user_assertions:*&q=last_assertion_date:\' + \'[___DATEPARAM___ TO *]\'.encodeAsURL() + \'&sort=last_assertion_date&dir=desc\'',
                    dateFormat: """yyyy-MM-dd'T'HH:mm:ss'Z'""",
                    emailTemplate: '/email/annotations',
                    recordJsonPath: '\$.occurrences[*]',
                    idJsonPath: 'uuid'
            ])).save()
            new PropertyPath([name: "totalRecords", jsonPath: "totalRecords", query: newAssertions, fireWhenNotZero: true]).save()
            new PropertyPath([name: "last_assertion_record", jsonPath: "occurrences[0].uuid", query: newAssertions]).save()
        }

        title = messageSource.getMessage("query.new.records.title", null, siteLocale)
        descr = messageSource.getMessage("query.new.records.descr", null, siteLocale)
        if(Query.findAllByName(title).isEmpty()){
            Query newRecords = (new Query([
                    baseUrl: grailsApplication.config.biocacheService.baseURL,
                    baseUrlForUI: grailsApplication.config.biocache.baseURL,
                    name: title,
                    resourceName:  grailsApplication.config.mail.details.defaultResourceName,
                    updateMessage: 'more.records.update.message',
                    description: descr,
                    queryPath: '/occurrences/search?q=first_loaded_date:\' + \'[___DATEPARAM___ TO *]\'.encodeAsURL() + \'&sort=first_loaded_date&dir=desc&pageSize=20&facets=basis_of_record',
                    queryPathForUI: '/occurrences/search?q=first_loaded_date:\' + \'[___DATEPARAM___ TO *]\'.encodeAsURL() + \'&sort=first_loaded_date&dir=desc',
                    dateFormat: """yyyy-MM-dd'T'HH:mm:ss'Z'""",
                    emailTemplate: '/email/biocache',
                    recordJsonPath: '\$.occurrences[*]',
                    idJsonPath: 'uuid'
            ])).save()
            new PropertyPath([name: "totalRecords", jsonPath: "totalRecords", query: newRecords, fireWhenNotZero: true]).save()
            new PropertyPath([name: "last_loaded_record", jsonPath: "occurrences[0].uuid", query: newRecords]).save()
        }

        title = messageSource.getMessage("query.new.images.title", null, siteLocale)
        descr = messageSource.getMessage("query.new.images.descr", null, siteLocale)
        if(Query.findAllByName(title).isEmpty()){
            Query newRecordsWithImages = (new Query([
                    baseUrl: grailsApplication.config.biocacheService.baseURL,
                    baseUrlForUI: grailsApplication.config.biocache.baseURL,
                    name: title,
                    resourceName:  grailsApplication.config.mail.details.defaultResourceName,
                    updateMessage: 'more.images.update.message',
                    description: descr,
                    queryPath: '/occurrences/search?q=first_loaded_date:\' + \'[___DATEPARAM___ TO *]\'.encodeAsURL() + \'&sort=first_loaded_date&dir=desc&fq=multimedia:Image&pageSize=20&facets=basis_of_record',
                    queryPathForUI: '/occurrences/search?q=first_loaded_date:\' + \'[___DATEPARAM___ TO *]\'.encodeAsURL() + \'&sort=first_loaded_date&dir=desc&fq=multimedia:Image#tab_recordImages',
                    dateFormat: """yyyy-MM-dd'T'HH:mm:ss'Z'""",
                    emailTemplate: '/email/biocache',
                    fireWhenNotZero: true,
                    recordJsonPath: '\$.occurrences[*]', // jsonpath 2 syntax: '\$.occurrences[*]'
                    idJsonPath: 'uuid'
            ])).save()
            new PropertyPath([name: "totalRecords", jsonPath: "totalRecords", query: newRecordsWithImages, fireWhenNotZero: true]).save()
            new PropertyPath([name: "last_loaded_record", jsonPath: "occurrences[0].uuid", query: newRecordsWithImages]).save()
        }

        title = messageSource.getMessage("query.citizen.records.title", null, siteLocale)
        descr = messageSource.getMessage("query.citizen.records.descr", null, siteLocale)
        if (grailsApplication.config.useCitizenScienceAlerts?.toBoolean() &&
                Query.findAllByName(title).isEmpty()) {
            Query newCitizenScienceRecords = (new Query([
                    baseUrl: grailsApplication.config.biocacheService.baseURL,
                    baseUrlForUI: grailsApplication.config.biocache.baseURL,
                    name: title,
                    resourceName:  grailsApplication.config.mail.details.defaultResourceName,
                    updateMessage: 'more.cs.update.message',
                    description: descr,
                    queryPath: '/occurrences/search?q=first_loaded_date:' + '[___DATEPARAM___  TO *]'.encodeAsURL() + '&fq=data_resource_uid:dr364&sort=first_loaded_date&dir=desc&pageSize=20&facets=basis_of_record',
                    queryPathForUI: '/occurrences/search?q=first_loaded_date:' + '[___DATEPARAM___  TO *]'.encodeAsURL() + '&fq=data_resource_uid:dr364&sort=first_loaded_date&dir=desc',
                    dateFormat: """yyyy-MM-dd'T'HH:mm:ss'Z'""",
                    emailTemplate: '/email/biocache',
                    fireWhenNotZero: true,
                    recordJsonPath: '\$.occurrences[*]',
                    idJsonPath: 'uuid'
            ])).save()
            new PropertyPath([name: "totalRecords", jsonPath: "totalRecords", query: newCitizenScienceRecords, fireWhenNotZero: true]).save()
            new PropertyPath([name: "last_loaded_record", jsonPath: "occurrences[0].uuid", query: newCitizenScienceRecords]).save()
        }

        title = messageSource.getMessage("query.citizen.records.imgs.title", null, siteLocale)
        descr = messageSource.getMessage("query.citizen.records.imgs.descr", null, siteLocale)
        if (grailsApplication.config.useCitizenScienceAlerts?.toBoolean() &&
                Query.findAllByName(title).isEmpty()) {
            Query newCitizenScienceRecordsWithImages = (new Query([
                    baseUrl: grailsApplication.config.biocacheService.baseURL,
                    baseUrlForUI: grailsApplication.config.biocache.baseURL,
                    name: title,
                    resourceName:  grailsApplication.config.mail.details.defaultResourceName,
                    updateMessage: 'more.cs.images.update.message',
                    description: descr,
                    queryPath: '/occurrences/search?q=first_loaded_date:' + '[___DATEPARAM___  TO *]'.encodeAsURL() + '&fq=data_resource_uid:dr364&sort=first_loaded_date&dir=desc&pageSize=20&facets=basis_of_record&fq=multimedia:Image',
                    queryPathForUI: '/occurrences/search?q=first_loaded_date:' + '[___DATEPARAM___  TO *]'.encodeAsURL() + '&fq=data_resource_uid:dr364&sort=first_loaded_date&dir=desc&fq=multimedia:Image',
                    dateFormat: """yyyy-MM-dd'T'HH:mm:ss'Z'""",
                    emailTemplate: '/email/biocache',
                    recordJsonPath: '\$.occurrences[*]',
                    idJsonPath: 'uuid'
            ])).save()
            new PropertyPath([name: "totalRecords", jsonPath: "totalRecords", query: newCitizenScienceRecordsWithImages, fireWhenNotZero: true]).save()
            new PropertyPath([name: "last_loaded_record", jsonPath: "occurrences[0].uuid", query: newCitizenScienceRecordsWithImages]).save()
        }

        title = messageSource.getMessage("query.spatial.layers.title", null, siteLocale)
        descr = messageSource.getMessage("query.spatial.layers.descr", null, siteLocale)
        if (grailsApplication.config.useSpatialAlerts.toBoolean() &&
                Query.findAllByName(title).isEmpty()) {
            Query newSpatialLayers = (new Query([
                    baseUrl: grailsApplication.config.spatial.baseURL,
                    baseUrlForUI: grailsApplication.config.spatial.baseURL,
                    name: title,
                    resourceName:  grailsApplication.config.mail.details.defaultResourceName,
                    updateMessage: 'more.spatial.update.message',
                    description: descr,
                    queryPath: '/layers.json',
                    queryPathForUI: '/layers',
                    emailTemplate: '/email/layers',
                    recordJsonPath: '\$[*]',
                    idJsonPath: 'name'
            ])).save()
            new PropertyPath([name: "layer_count", jsonPath: "\$", query: newSpatialLayers, fireWhenChange: true]).save()
        }

        title = messageSource.getMessage("query.occurrence.datasets.title", null, siteLocale)
        descr = messageSource.getMessage("query.occurrence.datasets.descr", null, siteLocale)
        if(Query.findAllByName(title).isEmpty()){
            Query newOccurrenceDatasets = (new Query([
                    baseUrl:  grailsApplication.config.biocacheService.baseURL,
                    baseUrlForUI: grailsApplication.config.collectory.baseURL,
                    name: title,
                    resourceName:  grailsApplication.config.mail.details.defaultResourceName,
                    updateMessage: 'more.datasets.update.message',
                    description: descr,
                    queryPath: '/occurrences/search?q=*:*&facet=true&flimit=-1&facets=dataResourceUid&pageSize=0',
                    queryPathForUI: '/datasets#filters=status%3AdataAvailable%3BresourceType%3Arecords',
                    emailTemplate: '/email/dataresource',
                    recordJsonPath: '\$.facetResults[0].fieldResult[*]',
                    idJsonPath: 'i18nCode'
            ])).save()
            new PropertyPath([name: "dataset_count", jsonPath: "\$.facetResults[0].fieldResult", query: newOccurrenceDatasets, fireWhenChange: true]).save()
        }

/*        title = messageSource.getMessage("query.datasets.title", null, siteLocale)
        descr = messageSource.getMessage("query.datasets.descr", null, siteLocale)
        if(Query.findAllByName(title).isEmpty()){
            Query newDatasets = (new Query([
                    baseUrl:  grailsApplication.config.collectoryService.baseURL ?: grailsApplication.config.collectory.baseURL,
                    baseUrlForUI: grailsApplication.config.collectory.baseURL,
                    name: title,
                    resourceName:  grailsApplication.config.mail.details.defaultResourceName,
                    updateMessage: 'more.datasets.update.message',
                    description: descr,
                    queryPath: '/dataResource',
                    queryPathForUI: '/datasets',
                    emailTemplate: '/email/datasets',
                    recordJsonPath: '\$[*]',
                    idJsonPath: 'uid'
            ])).save()
            new PropertyPath([name: "dataset_count", jsonPath: "\$", query: newDatasets, fireWhenChange: true]).save()
        }*/

        title = messageSource.getMessage("query.species.lists.title", null, siteLocale)
        descr = messageSource.getMessage("query.species.lists.descr", null, siteLocale)
        if (grailsApplication.config.useSpeciesListsAlerts.toBoolean() &&
                Query.findAllByName(title).isEmpty()) {
            log.info "Creating species list query"
            Query newSpeciesLists = (new Query([
                    baseUrl: grailsApplication.config.lists.baseURL,
                    baseUrlForUI: grailsApplication.config.lists.baseURL,
                    name: title,
                    resourceName:  grailsApplication.config.mail.details.defaultResourceName,
                    updateMessage: 'more.specieslist.update.message',
                    description: descr,
                    queryPath: '/speciesList?max=___MAX___&offset=___OFFSET___&isAuthoritative=eq%3Atrue',
                    queryPathForUI: '/public/speciesLists?q=&max=25&sort=dateCreated&order=desc',
                    emailTemplate: '/email/specieslists',
                    recordJsonPath: '\$.lists[*]',
                    idJsonPath: 'dataResourceUid'
            ])).save()
            new PropertyPath([name: "species_list_count", jsonPath: "\$.lists", query: newSpeciesLists, fireWhenChange: true]).save()
        }

        title = messageSource.getMessage("query.ala.blog.title", null, siteLocale)
        descr = messageSource.getMessage("query.ala.blog.descr", null, siteLocale)
        // get_category_posts.json
        if (grailsApplication.config.useBlogsAlerts.toBoolean() &&
                Query.findAllByName(title).isEmpty()) {
            Query newBlogs = (new Query([
                    baseUrl: grailsApplication.config.ala.baseURL,
                    baseUrlForUI: grailsApplication.config.ala.baseURL,
                    name: title,
                    resourceName:  grailsApplication.config.mail.details.defaultResourceName,
                    updateMessage: 'more.blogsnews.update.message',
                    description: descr,
                    queryPath: '/recentposts.json',
                    queryPathForUI: '/blogs-news/',
                    emailTemplate: '/email/blogs',
                    recordJsonPath: '\$.[*]',
                    idJsonPath: 'id'
            ])).save()
            new PropertyPath([name: "last_blog_id", jsonPath: "\$", query: newBlogs]).save()
        }
        log.info("end of preloadQueries")
    }

    def destroy = {}
}
