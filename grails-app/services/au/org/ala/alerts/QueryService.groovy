package au.org.ala.alerts

import grails.util.Holders
import org.apache.http.entity.ContentType
import org.grails.web.json.JSONObject
import org.springframework.dao.DataIntegrityViolationException

import grails.gorm.transactions.Transactional

@Transactional
class QueryService {

    static transactional = true

    def serviceMethod() {}

    def grailsApplication, notificationService, alertsWebService, webService

    def messageSource
    def siteLocale = new Locale.Builder().setLanguageTag(Holders.config.siteDefaultLanguage as String).build()

    Notification getNotificationForUser(Query query, User user) {
        Notification n = null
        //find the query
        Query retrievedQuery = Query.findByBaseUrlAndQueryPath(query.baseUrl, query.queryPath)

        if (retrievedQuery != null) {
            log.debug("Query already exists...." + retrievedQuery.id)
            //does a notification exist???
            n = Notification.findByQueryAndUser(retrievedQuery, user)
            if (n != null) {
                log.debug("Notification for this user exists...." + user)
            } else {
                log.debug("Notification for this user DOES NOT exist...." + user)
            }
        }
        n
    }

    Boolean checkChangeByDiff(Query query) {
        !hasAFireProperty(query) && (query.idJsonPath || isMyAnnotation(query))
    }

    Boolean hasAFireProperty(Query query) {
        query.propertyPaths.any { it.fireWhenChange || it.fireWhenNotZero }
    }

    Boolean firesWhenNotZero(query){
        query.propertyPaths.any { it.fireWhenNotZero }
    }

    Boolean isMyAnnotation(Query query) {
        // Currently only 'my annotation' alert is user specific (among all non-custom queries)
        query.name.startsWith(messageSource.getMessage("query.myannotations.title", null, siteLocale))
    }

    Boolean isBioSecurityQuery(Query query) {
        query.emailTemplate == '/email/biosecurity'
    }

    String getUserId(Query query) {
        if (!isMyAnnotation(query)) return null
        String queryPath = query.queryPath
        queryPath.substring(queryPath.indexOf('assertion_user_id:') + 'assertion_user_id:'.length(), queryPath.indexOf('&dir=desc'))
    }

    Integer fireWhenNotZeroProperty(QueryResult queryResult) {
        Integer fireWhenNotZeroValue = -1
        queryResult.propertyValues.each { pv ->
            if (pv.propertyPath.fireWhenNotZero) {
                fireWhenNotZeroValue = pv.currentValue.toInteger()
            }
        }
        fireWhenNotZeroValue
    }

    def deleteQuery(Query queryInstance) throws DataIntegrityViolationException {
        def propertyPaths = PropertyPath.findAllByQuery(queryInstance)
        def queryResults = QueryResult.findAllByQuery(queryInstance)
        if (queryResults.size() > 0) {
            def propetyValues = PropertyValue.findAllByQueryResultInList(queryResults)
            PropertyValue.deleteAll(propetyValues)
        }
        QueryResult.deleteAll(queryResults)
        PropertyPath.deleteAll(propertyPaths)
        queryInstance.delete(flush: true)
    }

    int deleteOrphanedQueries() {
        def toBeRemoved = []
        Query.findAll().each {
            if (it.notifications.size() == 0 && it.custom == true) {
                toBeRemoved << it
            }
        }

        toBeRemoved.each {
            deleteQuery(it)
        }

        toBeRemoved.size()
    }

    // return true if a new query is created, otherwise return false
    boolean createQueryForUserIfNotExists(Query newQuery, User user, boolean setPropertyPath = true) {
        boolean newQueryCreated = false
        //find the query
        Query retrievedQuery = Query.findByBaseUrlAndQueryPath(newQuery.baseUrl, newQuery.queryPath)
        if (retrievedQuery == null) {
            try {
                newQuery = newQuery.save(true)
                newQueryCreated = true
                if (setPropertyPath) {
                    new PropertyPath([name: "totalRecords", jsonPath: "totalRecords", query: newQuery, fireWhenNotZero: true]).save(true)
                    new PropertyPath([name: "last_loaded_record", jsonPath: "occurrences[0].uuid", query: newQuery]).save(true)
                }
            } catch (Exception ex) {
                log.error("Error occurred when saving Query: " + ex.toString())
            }
        } else {
            newQuery = retrievedQuery
        }

        //does the notification already exist?
        def exists = Notification.findByQueryAndUser(newQuery, user)
        if (!exists) {

            Notification n = new Notification([query: newQuery, user: user])
            n.save(true)

            if (n.hasErrors()) {
                n.errors.allErrors.each { e -> log.error(e) }
            }
        }

        newQueryCreated
    }

    /**
     * Takes a URL of the form "/ws/occurrences/search?......"
     *
     * @param biocacheWebserviceQueryPath
     * @return
     */
    Query createBioCacheChangeQuery(String biocacheWebserviceQueryPath, String biocacheUIQueryPath, String queryDisplayName, String baseUrlForWS, String baseUrlForUI, String resourceName) {
        // truncate long name to avoid SQL error
        if (queryDisplayName.length() >= 250) {
            queryDisplayName = queryDisplayName.substring(0, 149) + "..."
        }

        new Query([
                baseUrl       : baseUrlForWS ?: grailsApplication.config.biocacheService.baseURL,
                baseUrlForUI  : baseUrlForUI ?: grailsApplication.config.biocache.baseURL,
                resourceName  : resourceName,
                name          : messageSource.getMessage("query.service.occurrences.name", [queryDisplayName] as Object[], siteLocale),
                updateMessage : messageSource.getMessage("query.service.occurrences.update.msg", [queryDisplayName] as Object[], siteLocale),
                description   : messageSource.getMessage("query.service.occurrences.desc", [queryDisplayName] as Object[], siteLocale),
                queryPath     : biocacheWebserviceQueryPath + '&fq=first_loaded_date:[___DATEPARAM___%20TO%20*]&sort=first_loaded_date&dir=desc&pageSize=20&facets=basis_of_record',
                queryPathForUI: biocacheUIQueryPath + '&fq=first_loaded_date:[___DATEPARAM___%20TO%20*]&sort=first_loaded_date&dir=desc',
                dateFormat    : """yyyy-MM-dd'T'HH:mm:ss'Z'""",
                emailTemplate : '/email/biocache',
                recordJsonPath: '\$.occurrences',
                idJsonPath    : 'uuid',
                custom        : true
        ])
    }

    Query createBioCacheAnnotationQuery(String biocacheWebserviceQueryPath, String biocacheUIQueryPath, String queryDisplayName, String baseUrlForWS, String baseUrlForUI, String resourceName) {
        // Alert for Unconfirmed (50005), Verified (50002), Corrected (50003)
        new Query([
                baseUrl       : baseUrlForWS ?: grailsApplication.config.biocacheService.baseURL,
                baseUrlForUI  : baseUrlForUI ?: grailsApplication.config.biocache.baseURL,
                resourceName  : resourceName,
                name          : messageSource.getMessage("query.service.annotations.name", [queryDisplayName] as Object[], siteLocale),
                updateMessage : messageSource.getMessage("query.service.annotations.update.msg", [queryDisplayName] as Object[], siteLocale),
                description   : messageSource.getMessage("query.service.annotations.desc", [queryDisplayName] as Object[], siteLocale),
                queryPath     : biocacheWebserviceQueryPath + '&fq=(user_assertions:50005%20OR%20user_assertions:50003%20OR%20user_assertions:50002)&fq=last_assertion_date:[___DATEPARAM___%20TO%20*]&sort=last_assertion_date&dir=desc&pageSize=20&facets=basis_of_record',
                queryPathForUI: biocacheUIQueryPath + '&fq=(user_assertions:50005%20OR%20user_assertions:50003%20OR%20user_assertions:50002)&fq=last_assertion_date:[___DATEPARAM___%20TO%20*]&sort=last_assertion_date&dir=desc',
                dateFormat    : """yyyy-MM-dd'T'HH:mm:ss'Z'""",
                emailTemplate : '/email/biocache',
                recordJsonPath: '\$.occurrences',
                idJsonPath    : 'uuid',
                custom        : true
        ])
    }

    Query createBioCacheQuery(String biocacheWebserviceQueryPath, String biocacheUIQueryPath, String queryDisplayName, String baseUrlForWS, String baseUrlForUI, String resourceName) {
        new Query([
                baseUrl       : baseUrlForWS ?: grailsApplication.config.biocacheService.baseURL,
                baseUrlForUI  : baseUrlForUI ?: grailsApplication.config.biocache.baseURL,
                resourceName  : resourceName,
                name          : messageSource.getMessage("query.service.occurrences.name", [queryDisplayName] as Object[], siteLocale),
                updateMessage : messageSource.getMessage("query.service.occurrences.resource.update.msg", [queryDisplayName, resourceName] as Object[], siteLocale),
                description   : messageSource.getMessage("query.service.occurrences.resource.desc", [queryDisplayName, resourceName] as Object[], siteLocale),
                queryPath     : biocacheWebserviceQueryPath + '&dir=desc&pageSize=20&facets=basis_of_record',
                queryPathForUI: biocacheUIQueryPath + '&dir=desc',
                dateFormat    : """yyyy-MM-dd'T'HH:mm:ss'Z'""",
                emailTemplate : '/email/biocache',
                recordJsonPath: '\$.occurrences',
                idJsonPath    : 'uuid',
                custom        : true
        ])
    }

    Query createTaxonQuery(String taxonGuid, String taxonName) {
        new Query([
                baseUrl       : grailsApplication.config.biocacheService.baseURL,
                baseUrlForUI  : grailsApplication.config.biocache.baseURL,
                name          : 'New records for ' + taxonName,
                name          : messageSource.getMessage("query.service.occurrences.name", [taxonName] as Object[], siteLocale),
                resourceName  : grailsApplication.config.postie.defaultResourceName,
                updateMessage : messageSource.getMessage("query.service.occurrences.update.msg", [taxonName] as Object[], siteLocale),
                description   : messageSource.getMessage("query.service.occurrences.desc", [taxonName] as Object[], siteLocale),
                queryPath     : '/occurrences/taxon/' + taxonGuid + '?fq=first_loaded_date:[___DATEPARAM___%20TO%20*]&sort=first_loaded_date&dir=desc&pageSize=20&facets=basis_of_record',
                queryPathForUI: '/occurrences/taxa/' + taxonGuid + '?fq=first_loaded_date:[___DATEPARAM___%20TO%20*]&sort=first_loaded_date&dir=desc',
                dateFormat    : """yyyy-MM-dd'T'HH:mm:ss'Z'""",
                emailTemplate : '/email/biocache',
                recordJsonPath: '\$.occurrences',
                idJsonPath    : 'uuid',
                custom        : true
        ])
    }

    Query createTaxonRegionQuery(String taxonGuid, String taxonName, String layerId, String regionName) {
        new Query([
                baseUrl       : grailsApplication.config.biocacheService.baseURL,
                baseUrlForUI  : grailsApplication.config.biocache.baseURL,
                name          : messageSource.getMessage("query.service.occurrences.recorded.name", [taxonName, regionName] as Object[], siteLocale),
                resourceName  : grailsApplication.config.postie.defaultResourceName,
                updateMessage : messageSource.getMessage("query.service.occurrences.recorded.update.msg", [taxonName, regionName] as Object[], siteLocale),
                description   : messageSource.getMessage("query.service.occurrences.recorded.desc", [taxonName, regionName] as Object[], siteLocale),
                queryPath     : '/occurrences/taxon/' + taxonGuid + '?fq=' + layerId + ':%22' + regionName.encodeAsURL() + '%22&fq=first_loaded_date:[___DATEPARAM___%20TO%20*]&sort=first_loaded_date&dir=desc&pageSize=20&facets=basis_of_record',
                queryPathForUI: '/occurrences/taxa/' + taxonGuid + '?fq=' + layerId + ':%22' + regionName.encodeAsURL() + '%22&fq=first_loaded_date:[___DATEPARAM___%20TO%20*]&sort=first_loaded_date&dir=desc',
                dateFormat    : """yyyy-MM-dd'T'HH:mm:ss'Z'""",
                emailTemplate : '/email/biocache',
                recordJsonPath: '\$.occurrences',
                idJsonPath    : 'uuid',
                custom        : true
        ])
    }

    Query createSpeciesGroupRegionQuery(String speciesGroup, String layerId, String regionName) {
        new Query([
                baseUrl       : grailsApplication.config.biocacheService.baseURL,
                baseUrlForUI  : grailsApplication.config.biocache.baseURL,
                name          : messageSource.getMessage("query.service.occurrences.recorded.name", [speciesGroup, regionName] as Object[], siteLocale),
                resourceName  : grailsApplication.config.postie.defaultResourceName,
                updateMessage : messageSource.getMessage("query.service.occurrences.recorded.update.msg", [speciesGroup, regionName] as Object[], siteLocale),
                description   : messageSource.getMessage("query.service.occurrences.recorded.desc", [speciesGroup, regionName] as Object[], siteLocale),
                queryPath     : '/occurrences/search?q=' + layerId + ':%22' + regionName.encodeAsURL() + '%22&fq=species_group:' + speciesGroup + '&fq=first_loaded_date:[___DATEPARAM___%20TO%20*]&sort=first_loaded_date&dir=desc&pageSize=20&facets=basis_of_record',
                queryPathForUI: '/occurrences/search?q=' + layerId + ':"' + regionName.encodeAsURL() + '"&fq=species_group:' + speciesGroup + '&fq=first_loaded_date:[___DATEPARAM___%20TO%20*]&sort=first_loaded_date&dir=desc',
                dateFormat    : """yyyy-MM-dd'T'HH:mm:ss'Z'""",
                emailTemplate : '/email/biocache',
                recordJsonPath: '\$.occurrences',
                idJsonPath    : 'uuid',
                custom        : true
        ])
    }

    Query createRegionQuery(String layerId, String regionName) {
        new Query([
                baseUrl       : grailsApplication.config.biocacheService.baseURL,
                baseUrlForUI  : grailsApplication.config.biocache.baseURL,
                name          : messageSource.getMessage("query.service.occurrences.name", [regionName] as Object[], siteLocale),
                resourceName  : grailsApplication.config.postie.defaultResourceName,
                updateMessage : messageSource.getMessage("query.service.occurrences.update.msg", [regionName] as Object[], siteLocale),
                description   : messageSource.getMessage("query.service.occurrences.desc", [regionName] as Object[], siteLocale),
                queryPath     : '/occurrences/search?q=' + layerId + ':%22' + regionName.encodeAsURL() + '%22&fq=first_loaded_date:[___DATEPARAM___%20TO%20*]&sort=first_loaded_date&dir=desc&pageSize=20&facets=basis_of_record',
                queryPathForUI: '/occurrences/search?q=' + layerId + ':%22' + regionName.encodeAsURL() + '%22&fq=first_loaded_date:[___DATEPARAM___%20TO%20*]&sort=first_loaded_date&dir=desc',
                dateFormat    : """yyyy-MM-dd'T'HH:mm:ss'Z'""",
                emailTemplate : '/email/biocache',
                recordJsonPath: '\$.occurrences',
                idJsonPath    : 'uuid',
                custom        : true
        ])
    }

    Query createMyAnnotationQuery(String userId) {
        new Query([
                baseUrl       : grailsApplication.config.biocacheService.baseURL,
                baseUrlForUI  : grailsApplication.config.biocache.baseURL,
                resourceName  : grailsApplication.config.postie.defaultResourceName,
                name          : messageSource.getMessage("query.myannotations.title", null, siteLocale),
                updateMessage : messageSource.getMessage("myannotations.update.message", null, siteLocale),
                description   : messageSource.getMessage("query.myannotations.descr", null, siteLocale),
                queryPath     : constructMyAnnotationQueryPath(userId),
                queryPathForUI: '/occurrences/search?fq=assertion_user_id:' + userId + '&dir=desc',
                emailTemplate : '/email/biocache',
                recordJsonPath: '\$.occurrences[*]',
        ])
    }

    String constructMyAnnotationQueryPath(String userId) {
        '/occurrences/search?fq=assertion_user_id:' + userId + '&dir=desc&pageSize=300'
    }

    Query createBioSecurityQuery(String listid) {
        String queryPathForUITemplate = grailsApplication.config.getProperty("biosecurity.queryurl.template", String, "/occurrences/search?q=species_list_uid:___LISTIDPARAM___&fq=decade:2020&fq=country:Australia&fq=first_loaded_date:[___DATEPARAM___%20TO%20*]&fq=occurrence_date:[___LASTYEARPARAM___%20TO%20*]&sort=first_loaded_date&dir=desc&disableAllQualityFilters=true")
        String queryPathForUI = queryPathForUITemplate.replaceAll("___LISTIDPARAM___", listid)
        String speciesListName = getSpeciesListName(listid)
        new Query([
                baseUrl       : grailsApplication.config.biocacheService.baseURL,
                baseUrlForUI  : grailsApplication.config.biocache.baseURL,
                name          : messageSource.getMessage("query.biosecurity.title", null, siteLocale) + ' ' + speciesListName,
                resourceName  : grailsApplication.config.postie.defaultResourceName,
                updateMessage : 'more.biosecurity.update.message',
                description   : messageSource.getMessage("query.biosecurity.descr", null, siteLocale) + ' ' + speciesListName,
                queryPath     : queryPathForUI + '&pageSize=20&facets=basis_of_record',
                queryPathForUI: queryPathForUI,
                dateFormat    : """yyyy-MM-dd'T'HH:mm:ss'Z'""",
                emailTemplate : '/email/biosecurity',
                recordJsonPath: '\$.occurrences[*]',
                idJsonPath    : 'uuid',
                custom        : true
        ])
    }

    def subscribeBioSecurity(User user, String listid) {
        Query query = createBioSecurityQuery(listid)
        createQueryForUserIfNotExists(query, user, true)
    }

    // remove all user notifications for the specified query
    def unsubscribeAllUsers(Long queryId) {
        def users = getSubscribers(Long.valueOf(queryId))
        users?.forEach{user -> notificationService.deleteAlertForUser((User)user, queryId)}
    }

    // delete a query (also remove all subscriptions)
    def deleteQuery(Long queryId) {
        unsubscribeAllUsers(queryId)
        def queryInstance = Query.get(queryId)
        if (queryInstance) {
            deleteQuery(queryInstance)
        }
    }

    // get all biosecurity queries
    def getALLBiosecurityQuery() {
        return Query.findAllByEmailTemplate('/email/biosecurity')
    }

    // get all subscribers to the specified query
    def getSubscribers(Long queryId) {
        Query query = Query.findById(queryId)
        return query ? Query.executeQuery(
                """select u
                  from User u
                  inner join u.notifications n
                  where n.query = :query
                  group by u""", [query: query]) : []
    }

    def getSpeciesListName(String listid) {
        try {
            def resp = webService.get(grailsApplication.config.getProperty('lists.baseURL') + "/ws/speciesListInternal/" + listid, [:], ContentType.APPLICATION_JSON, true, false)
            if (resp?.resp?.listName) {
                return '\"' + resp?.resp?.listName + '\"'
            }
        } catch (Exception ex) {
            log.error("Failed to get species list detail from " + listURL, ex)
        }
        listid
    }
}
