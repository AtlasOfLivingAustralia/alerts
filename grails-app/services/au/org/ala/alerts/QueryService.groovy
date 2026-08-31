package au.org.ala.alerts

import grails.gorm.transactions.NotTransactional
import grails.util.Holders
import groovy.sql.Sql
import org.apache.http.entity.ContentType
import org.springframework.dao.DataIntegrityViolationException
import grails.gorm.transactions.Transactional


class QueryService {

    def serviceMethod() {}
    def grailsApplication, notificationService, webService,utilService
    def messageSource, dataSource
    def siteLocale = new Locale.Builder().setLanguageTag(Holders.config.siteDefaultLanguage as String).build()

    def get(id){
        Query.get(id)
    }

    def getFrequency(name) {
        Frequency.withTransaction {
           return Frequency.findByName(name)
        }
    }

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

    /**
     * return true if a query does not have a propertyPath with fireWhenChange AND fireWhenNotZero set to true
     * @param query
     * @return
     */
    @NotTransactional
    Boolean checkChangeByDiff(Query query) {
        !hasAFireProperty(query) && (query.idJsonPath || isMyAnnotation(query))
    }
    @NotTransactional
    Boolean hasAFireProperty(Query query) {
        query.propertyPaths.any { it.fireWhenChange || it.fireWhenNotZero }
    }

    Boolean firesWhenNotZero(query){
        query.propertyPaths.any { it.fireWhenNotZero }
    }

    Boolean isMyAnnotation(Query query) {
        query.emailTemplate == '/email/myAnnotations'
    }

    Boolean isAnnotation(Query query) {
        query.emailTemplate == '/email/annotations'
    }

    Boolean isBioSecurityQuery(Query query) {
        query.emailTemplate == '/email/biosecurity'
    }

    Boolean isDatasetQuery(Query query) {
        query.emailTemplate == '/email/datasets'
    }

    Boolean isDatasetResource(Query query) {
        query.emailTemplate == '/email/dataresource'
    }

    Boolean isBiocacheImages(Query query) {
        query.emailTemplate == '/email/biocacheImages'
    }

    /**
     * When fireWhenNotZero is true,  it should has a digit propertyVale, e.g. totalNumber
     * It is supposed to be totalNumber of NEW records
     * NOT SAFE
     *
     * @param queryResult
     * @return the last valid number in propertyValues. Assume 'totalNumber' is the last propertyPath with 'fireWhenNotZero' set to 'true
     *
     *
     */
    Integer totalNumberWhenNotZeroPropertyEnabled(QueryResult queryResult) {
        Integer fireWhenNotZeroValue = 0
        queryResult.propertyValues.each { pv ->
            if (pv.propertyPath.fireWhenNotZero) {
                // true is 1, false is 0
                fireWhenNotZeroValue = pv?.currentValue? pv?.currentValue.toInteger() : 0
            }
        }
        fireWhenNotZeroValue
    }

    /**
     * duplicate with wipe()
     * Validate the constraints, cannot delete
     * @param queryInstance
     * @return
     * @throws DataIntegrityViolationException
     */
    @Transactional
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

    @Transactional
    Map deleteOrphanedQueries() {
        def toBeRemoved = []
        Query.findAll().each {
            if (it.notifications.size() == 0 && it.custom == true) {
                toBeRemoved << it
            }
        }

        toBeRemoved.each {
            deleteQuery(it)
        }

        //remove notifications which its query has been removed
        int ophanedNotifications = 0;
        Notification.findAll().each {
            if (!Query.findById(it.queryId)) {
                it.delete(flush: true)
                ophanedNotifications++
            }
        }
        log.info("Deleted ${toBeRemoved.size()} orphaned queries")
        log.info("Deleted ${ophanedNotifications} orphaned notifications")
        [OrphanQuery: toBeRemoved.size(), OrphanNotification: ophanedNotifications]
    }

    // return true if a new query is created, otherwise return false
    /**
     * Legacy support for other queries creation
     * @param newQuery
     * @param user
     * @param setPropertyPath
     * @param enabled
     * @return
     */
    boolean createQueryForUserIfNotExists(Query newQuery, User user, boolean setPropertyPath = true, boolean enabled = false) {
      def successOne = addUserToQuery(newQuery, user, setPropertyPath, enabled)
        if (successOne && successOne.id) {
            return true
        } else {
            return false
        }
    }

    /**
     * implemented for Biosecurity.
     * However, the method createQueryForUserIfNotExists is used in other places, e.g. for My Annotations, and it is not clear if this method is needed for those cases.
     * We should only use this one, and remove the other.
     *
     * Add a user to a query, creating the query if it does not exist.
     *
     * @param user
     * @param setPropertyPath
     * @param enabled
     * @return Query object, which may be a new query or an existing one.
     */
    Query addUserToQuery(Query newQuery, User user, boolean setPropertyPath = true, boolean enabled = false) {
        Query.withTransaction {
            Query retrievedQuery = Query.findByBaseUrlAndQueryPath(newQuery.baseUrl, newQuery.queryPath)
            if (retrievedQuery == null) {
                log.debug("Query does not exist....")
                if (setPropertyPath) {
                    PropertyPath totalRecordPP = new PropertyPath([name: "totalRecords", jsonPath: "totalRecords", query: newQuery, fireWhenNotZero: true])
                    PropertyPath lastLoadedPP = new PropertyPath([name: "last_loaded_record", jsonPath: "occurrences[0].uuid", query: newQuery])
                    newQuery.propertyPaths.add(totalRecordPP)
                    newQuery.propertyPaths.add(lastLoadedPP)
                }
                // Persist the query first so it gets an id, enabling child objects to reference it
                newQuery.save()
            } else {
                newQuery = retrievedQuery
            }
            //does the notification already exist?
            def exists = Notification.findByQueryAndUser(newQuery, user)
            if (!exists) {
                Notification n = new Notification([query: newQuery, user: user, enabled: enabled])
                newQuery.notifications.add(n)
            }

            newQuery.save(validate: true, flush: true)
        }
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
                queryPath     : biocacheWebserviceQueryPath + '&fq=first_loaded_date:' + '[___DATEPARAM___ TO *]'.encodeAsURL() + '&sort=first_loaded_date&dir=desc&pageSize=20&facets=basis_of_record',
                queryPathForUI: biocacheUIQueryPath + '&fq=first_loaded_date:' + '[___DATEPARAM___ TO *]'.encodeAsURL() + '&sort=first_loaded_date&dir=desc',
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
                queryPath     : biocacheWebserviceQueryPath + '&fq=(user_assertions:50005%20OR%20user_assertions:50003%20OR%20user_assertions:50002)&fq=last_assertion_date:'+'[___DATEPARAM___ TO *]'.encodeAsURL()+'&sort=last_assertion_date&dir=desc&pageSize=20&facets=basis_of_record',
                queryPathForUI: biocacheUIQueryPath + '&fq=(user_assertions:50005%20OR%20user_assertions:50003%20OR%20user_assertions:50002)&fq=last_assertion_date:'+'[___DATEPARAM___ TO *]'.encodeAsURL()+'&sort=last_assertion_date&dir=desc',
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
                resourceName  : grailsApplication.config.mail.details.defaultResourceName,
                updateMessage : messageSource.getMessage("query.service.occurrences.update.msg", [taxonName] as Object[], siteLocale),
                description   : messageSource.getMessage("query.service.occurrences.desc", [taxonName] as Object[], siteLocale),
                queryPath     : '/occurrences/taxon/' + taxonGuid + '?fq=first_loaded_date:'+'[___DATEPARAM___ TO *]'.encodeAsURL()+'&sort=first_loaded_date&dir=desc&pageSize=20&facets=basis_of_record',
                queryPathForUI: '/occurrences/taxa/' + taxonGuid + '?fq=first_loaded_date:'+'[___DATEPARAM___ TO *]'.encodeAsURL()+'&sort=first_loaded_date&dir=desc',
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
                resourceName  : grailsApplication.config.mail.details.defaultResourceName,
                updateMessage : messageSource.getMessage("query.service.occurrences.recorded.update.msg", [taxonName, regionName] as Object[], siteLocale),
                description   : messageSource.getMessage("query.service.occurrences.recorded.desc", [taxonName, regionName] as Object[], siteLocale),
                queryPath     : '/occurrences/taxon/' + taxonGuid + '?fq=' + layerId + ':%22' + regionName.encodeAsURL() + '%22&fq=first_loaded_date:'+'[___DATEPARAM___ TO *]'.encodeAsURL()+'&sort=first_loaded_date&dir=desc&pageSize=20&facets=basis_of_record',
                queryPathForUI: '/occurrences/taxa/' + taxonGuid + '?fq=' + layerId + ':%22' + regionName.encodeAsURL() + '%22&fq=first_loaded_date:'+'[___DATEPARAM___ TO *]'.encodeAsURL()+'&sort=first_loaded_date&dir=desc',
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
                resourceName  : grailsApplication.config.mail.details.defaultResourceName,
                updateMessage : messageSource.getMessage("query.service.occurrences.recorded.update.msg", [speciesGroup, regionName] as Object[], siteLocale),
                description   : messageSource.getMessage("query.service.occurrences.recorded.desc", [speciesGroup, regionName] as Object[], siteLocale),
                queryPath     : '/occurrences/search?q=' + layerId + ':%22' + regionName.encodeAsURL() + '%22&fq=species_group:' + speciesGroup + '&fq=first_loaded_date:'+'[___DATEPARAM___ TO *]'.encodeAsURL()+'&sort=first_loaded_date&dir=desc&pageSize=20&facets=basis_of_record',
                queryPathForUI: '/occurrences/search?q=' + layerId + ':"' + regionName.encodeAsURL() + '"&fq=species_group:' + speciesGroup + '&fq=first_loaded_date:'+'[___DATEPARAM___ TO *]'.encodeAsURL()+'&sort=first_loaded_date&dir=desc',
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
                resourceName  : grailsApplication.config.mail.details.defaultResourceName,
                updateMessage : messageSource.getMessage("query.service.occurrences.update.msg", [regionName] as Object[], siteLocale),
                description   : messageSource.getMessage("query.service.occurrences.desc", [regionName] as Object[], siteLocale),
                queryPath     : '/occurrences/search?q=' + layerId + ':%22' + regionName.encodeAsURL() + '%22&fq=first_loaded_date:'+'[___DATEPARAM___ TO *]'.encodeAsURL()+'&sort=first_loaded_date&dir=desc&pageSize=20&facets=basis_of_record',
                queryPathForUI: '/occurrences/search?q=' + layerId + ':%22' + regionName.encodeAsURL() + '%22&fq=first_loaded_date:'+'[___DATEPARAM___ TO *]'.encodeAsURL()+'&sort=first_loaded_date&dir=desc',
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
                resourceName  : grailsApplication.config.mail.details.defaultResourceName,
                name          : messageSource.getMessage("query.myannotations.title", null, siteLocale),
                updateMessage : messageSource.getMessage("myannotations.update.message", null, siteLocale),
                description   : messageSource.getMessage("query.myannotations.descr", null, siteLocale),
                queryPath     : "/occurrences/search?fq=assertion_user_id:${userId}&dir=desc&pageSize=${grailsApplication.config.biocacheService.pageSize}&fq=lastAssertionDate:[___DATEPARAM___%20TO%20*]&sort=lastAssertionDate",
                queryPathForUI: "/occurrences/search?fq=assertion_user_id:${userId}&dir=desc&pageSize=${grailsApplication.config.biocacheService.pageSize}&fq=lastAssertionDate:[___DATEPARAM___%20TO%20*]&sort=lastAssertionDate",
                dateFormat    : """yyyy-MM-dd'T'HH:mm:ss'Z'""",
                emailTemplate : '/email/myAnnotations',
                recordJsonPath: '\$.occurrences[*]',
                custom: false
        ])
    }

    Query findMyAnnotationQuery(String userId) {
        def myAnnotations = Query.findAllByQueryPathLikeAndEmailTemplate('/occurrences/search?fq=assertion_user_id:' + userId+'%', '/email/myAnnotations')
        if (myAnnotations.size() > 1) {
            log.warn("More than 1 MyAnnotation in query table found for user: ${userId}")
        }
        if (myAnnotations.size() >= 1) {
            return myAnnotations[0]
        } else {
            return null
        }
    }

    /**
     * NOTE: Biosecurity query code does not use the queryPath stored in the database
     * @param listid
     * @return
     */

    Query createBioSecurityQuery(String listid) {
        def sList = getSpeciesListName(listid)
        String speciesListName = sList.name
        //differentiate non-authoritative / authoritative list
        //demo purpose only, the queryPath is not used in Biosecurity query process
        String queryPathForUITemplate = grailsApplication.config.getProperty("biosecurity.query.template.nonAuthoritativeList", String, "/occurrences/search?q=species_list:___LISTIDPARAM___&fq=decade:2020&fq=country:Australia&fq=first_loaded_date:"+"[___DATEPARAM___ TO *]".encodeAsURL()+"&fq=occurrence_date:"+"[___LASTYEARPARAM___ TO *]".encodeAsURL() +"&sort=first_loaded_date&dir=desc&disableAllQualityFilters=true")
        if (sList.isAuthoritative) {
            queryPathForUITemplate = grailsApplication.config.getProperty("biosecurity.query.template.authoritativeList", String, "/occurrences/search?q=species_list_uid:___LISTIDPARAM___&fq=decade:2020&fq=country:Australia&fq=first_loaded_date:"+"[___DATEPARAM___ TO *]".encodeAsURL()+"&fq=occurrence_date:"+"[___LASTYEARPARAM___ TO *]".encodeAsURL()+"&sort=first_loaded_date&dir=desc&disableAllQualityFilters=true")
        }

        String queryPathForUI = queryPathForUITemplate.replaceAll("___LISTIDPARAM___", listid)

        new Query([
                //Not used
                baseUrl       : grailsApplication.config.biocacheService.baseURL,
                baseUrlForUI  : grailsApplication.config.biocache.baseURL,
                name          : messageSource.getMessage("query.biosecurity.title", null, siteLocale) + ' ' + speciesListName,
                resourceName  : grailsApplication.config.mail.details.defaultResourceName,
                updateMessage : 'more.biosecurity.update.message',
                description   : messageSource.getMessage("query.biosecurity.descr", null, siteLocale) + ' ' + speciesListName,
                //Not used
                queryPath     : queryPathForUI + '&pageSize=20&facets=basis_of_record',
                //Not used
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
        query = addUserToQuery(query, user, true, true)
        return query
    }

    // remove all user notifications for the specified query
    def unsubscribeAllUsers(Long queryId) {
        def activeUsers = getSubscribers(Long.valueOf(queryId))
        def inActiveUsers = getInactiveSubscribers(Long.valueOf(queryId))
        activeUsers?.forEach{user -> notificationService.deleteAlertForUser((User)user, queryId)}
        inActiveUsers?.forEach{user -> notificationService.deleteAlertForUser((User)user, queryId)}
    }

    // todo check if we can only use one
    // duplicate with wipe(id)
    //
    // delete a query (also remove all subscriptions)
    def deleteQuery(Long queryId) {
        unsubscribeAllUsers(queryId)
        def queryInstance = Query.get(queryId)
        if (queryInstance) {
            deleteQuery(queryInstance)
        }
    }

    // return the number of biosecurity queries
    def countBiosecurityQuery() {
        int count = 0
        Query.withTransaction {
            count = Query.countByEmailTemplate('/email/biosecurity')
        }
        return count
    }

    // get all biosecurity queries
    def getALLBiosecurityQuery() {
        def queries
        Query.withTransaction {
            queries = Query.findAllByEmailTemplate('/email/biosecurity')
        }
        return queries
    }

    // get biosecurity queries with offset and limit
    def getBiosecurityQuery(int offset,int limit) {
        def criteria = Query.createCriteria()
        List<Query> queries = criteria.list(max: limit, offset: offset) {
            eq('emailTemplate', '/email/biosecurity')
            order('id', 'desc')
        }


        def results = queries.collect{ query ->
            // Bioseurity queries are weekly ONLY, so filter out the other frequencies
            def filteredQueryResults = query.queryResults.findAll { it.frequency?.name == 'weekly' }
            // Get the last QueryResult from the filtered list, if it exists
            QueryResult qr = !filteredQueryResults.isEmpty() ? filteredQueryResults.first() : null

            // Update the query's lastChecked property if a QueryResult was found
            if (qr) {
                query.lastChecked = qr.lastChecked
            }
            query
        }

        return results.toList()
    }


    def findBiosecurityQueryById(id) {
        Query subscription = Query.get(id)
        QueryResult qr = QueryResult.findByQuery(subscription)
        if (qr) {
            subscription.lastChecked = qr.lastChecked
        }
        return subscription
    }


    // get all subscribers to the specified query (only enabled notifications)
    // NOTE: Some standard queries (e.g. New records) may have a further filter on the frequency which users select (e.g. daily, weekly, monthly).
    // This method does not filter on frequency, it returns all users who have enabled notifications for the query.
    def getSubscribers(Long queryId) {
        Query query = Query.findById(queryId)
        return query ? query.getSubscribers() : []
    }

    // get all inactive subscribers to the specified query (only disabled notifications)
    // NOTE: Some standard queries (e.g. New records) may have a further filter on the frequency which users select (e.g. daily, weekly, monthly).
    // This method does not filter on frequency, it returns all users who have enabled notifications for the query.
    def getInactiveSubscribers(Long queryId) {
        Query query = Query.findById(queryId)
        return query ? query.getInactiveSubscribers() : []
    }

    boolean speciesListExists(String listid) {
        boolean exists = false
        try {
            def resp = webService.get(grailsApplication.config.getProperty('lists.baseURL') + "/ws/speciesListInternal/" + listid, [:], ContentType.APPLICATION_JSON, true, false)
            //If the list exists, the response will contain the listName, otherwise it returns all lists
            if (resp?.resp?.listName) {
                exists = true
            }

        } catch (Exception ex) {
            log.error(ex.message)
        }
        return exists
    }

    def getSpeciesListName(String listid) {
        def info = [id: listid, name: listid, isAuthoritative: false]
        try {
            def resp = webService.get(grailsApplication.config.getProperty('lists.baseURL') + "/ws/speciesListInternal/" + listid, [:], ContentType.APPLICATION_JSON, true, false)

            if (resp?.resp?.listName) {
               info.name = resp?.resp?.listName
            }
            info.isAuthoritative =resp?.resp?.isAuthoritative
        } catch (Exception ex) {
            log.error("Failed to get species list detail from " + listURL, ex)
        }

        info
    }

    def searchBiosecuritySubscriptions(keywords) {
        try {
            List<Query> queries = Query.createCriteria().list(max: 10) {
                or {
                    ilike('name', "%${keywords}%")
                    ilike('queryPath', "%${keywords}%") //contains listId
                }
                eq('emailTemplate', '/email/biosecurity')
            }

            return queries
        } catch (Exception e) {
            log.error(e.message)
            return []
        }
    }

    // delete a query (also remove all subscriptions)
    def wipe(id) {
        def result = ['status': 1, 'message': 'Runtime error, check logs']
        if (id) {
            def query = Query.findById(id)
            if (query) {
                Query.withTransaction {
                    //Manually delete all related PropertyPath and PropertyValue, since the cascade delete does not work
                    PropertyPath.findAllByQuery(query).each { PropertyPath pp ->
                        log.debug("Deleting property path of : ${id}")
                        PropertyValue.findAllByPropertyPath(pp).each { PropertyValue pv ->
                            pv.delete()
                        }
                        pp.delete()
                    }

                    //Manually delete all related Notifications
                    Notification.findAllByQuery(query).each { Notification n ->
                        log.debug("Deleting notification [${n.id}] of query: ${id} ")
                        n.delete()
                    }

                    //Manually delete all related QueryResults
                    QueryResult.findAllByQuery(query).each { QueryResult qr ->
                        log.debug("Deleting query result [${qr.id}] of query: ${id} ")
                        qr.delete()
                    }

                    query.delete()
                }
                result['status'] = 0
                result['message'] = "Query ${id} removed"
            } else {
                result['status'] = 0
                result['message'] = "Query ${id} not found"
            }
        } else {
            result['status'] = 1
            result['message'] = "Query id not provided"
        }
        result
    }

    def getLastCheckedDate(Query query) {
        def lastCheckedDate = null
        QueryResult.withTransaction {
            def queryResult = QueryResult.findByQuery(query)
            if (queryResult) {
                lastCheckedDate = queryResult.lastChecked
            }
        }
        lastCheckedDate
    }

    /**
     * List all queries grouped by email template
     */
    def summarize() {
        List<Query> queries =  Query.createCriteria().list {
            ne("name", "My Annotations")
            ne("emailTemplate", "/email/biosecurity")
        }

        List<Query> myAnnotations =  Query.createCriteria().list {
            eq("name", "My Annotations")
            createAlias("notifications", "n")
            createAlias("n.user", "u")
            order("u.email")
        }

        Map<String, List<Query>> groupedByTemplate = queries.groupBy { it.emailTemplate }.collectEntries { key, value ->
            [ (key.replace("/email/", "")) : value.sort { it.name }]
        }
        groupedByTemplate['myAnnotations'] = myAnnotations

        return groupedByTemplate
    }
}
