/*
 * Copyright (C) 2024 Atlas of Living Australia
 * All Rights Reserved.
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 */

package au.org.ala.alerts
import au.org.ala.userdetails.UserDetailsFromIdListResponse
import au.org.ala.web.UserDetails
import grails.converters.JSON
import grails.plugin.cache.Cacheable
import grails.util.Holders
import grails.util.Environment

class UserService {

    static transactional = true

    def authService, queryService, messageSource, grailsApplication

    def siteLocale = new Locale.Builder().setLanguageTag(Holders.config.siteDefaultLanguage as String).build()

    def getUserAlertsConfig(User user) {

        log.debug('getUserAlertsConfig - Viewing my alerts :  ' + user)
        //enabled alerts
        def notificationInstanceList = Notification.findAllByUser(user)

        //split into custom and non-custom...
        //in case some queries which were removed
        def enabledQueries = notificationInstanceList.findAll { it?.query != null }
                .collect { it.query }
                .findAll { query ->
                    try {
                        Query.get(query.id) != null
                    } catch (Exception e) {
                        false
                    }
                }

        def enabledIds = enabledQueries.collect { it.id }

        // all standard queries + 'my annotations' queries
        // this might include 'my annotations' that belongs to others
        // we need to filter those out
        def allAlertTypes = Query.findAllByCustom(false)

        def myAnnotationQuery = queryService.createMyAnnotationQuery(user.getUserId())
        // collect standard queries + 'my annotations' belongs to current user
        allAlertTypes = allAlertTypes.findAll { it.name != myAnnotationQuery.name || it.queryPath == myAnnotationQuery.queryPath }

        allAlertTypes.removeAll { enabledIds.contains(it.id) }
        def customQueries = enabledQueries.findAll { it.custom }
        def standardQueries = enabledQueries.findAll { !it.custom }

        def userConfig = [disabledQueries: allAlertTypes,   // all disabled standard queries
                          enabledQueries : standardQueries, // all enabled standard queries
                          customQueries  : customQueries,   // all enabled custom queries
                          frequencies    : Frequency.listOrderByPeriodInSeconds(),
                          user           : user]

        if (grailsApplication.config.getProperty('myannotation.enabled', Boolean, false)) {
            userConfig.myannotation = userConfig.enabledQueries.findAll { it.name == myAnnotationQuery.name }
            userConfig.enabledQueries.removeAll { it.name == myAnnotationQuery.name }
        }

        userConfig
    }

    /**
     * Sync User table with UserDetails app via webservice
     *
     * @return total number of updates
     */

    int updateUserEmails() {
        final int pageSize = grailsApplication.config.getProperty('alerts.user-sync.batch-size', Integer, 1000)
        def toUpdate = []
        def total = User.count()
        log.warn "Checking all ${total} users in Alerts user table."
        def count = 0

        def page = 0

        boolean done = false

        while (!done) {
            User.withTransaction {
                List<User> users = User.findAll([sort: 'id', max: (page+1) * pageSize, offset: page * pageSize])
                done = users.size() < pageSize
                List<User> updates = []

                def ids = users*.userId
                UserDetailsFromIdListResponse results
                if (ids) {
                    try {
                        results = authService.getUserDetailsById(ids, false)
                    } catch (Exception e) {
                        log.warn("couldn't get user details from web service", e)
                    }
                }

                if (results && results.success) {
                    users.each {user ->
                        UserDetails userDetails = results.users[user.userId]
                        if (userDetails) {
                            // update email
                            boolean update = false
                            if (user.email != userDetails.email) {
                                user.email = userDetails.email
                                log.debug "Updating email address for user ${user.userId}: ${userDetails.userName}"
                                update = true
                            }

                            // update locked property
                            if (userDetails.locked != null) {
                                log.debug "Checking locked user: ${user.userId} -> ${userDetails.locked} vs ${user.locked}"

                                if ((user.locked == null && userDetails.locked == true) ||
                                        (user.locked != null && user.locked != userDetails.locked)) {
                                    user.locked = userDetails.locked
                                    log.debug "Updating locked status for user ${user.userId}: ${userDetails.locked}"
                                    update = true
                                }
                            }
                            if (update) {
                                updates << user
                            }
                        } else {
                            // we can't find a user in userdetails using userId - lock their account in alerts DB
                            if ((user.locked == null || user.locked != true) && Environment.current == Environment.PRODUCTION) {
                                user.locked = true
                                log.warn "Updating locked status for missing user ${user.userId}: true"
                                updates << user
                            }
                        }
                    }
                } else if (results && !results.success) {
                    log.warn("Unsuccessful response from userdetails: {}", results)
                }

                if (updates) {
                    updates.each {
                        log.warn "Modifying user: ${it as JSON}"
                    }
                    count += updates.size()
                    updates*.save()
                }
            }

            page++
            log.warn "Checked ${Math.min(total, page * pageSize)} users with ${count} changes, so far"

        }
        return count
    }

    User getUser(userDetailsParam = null) {

        def userDetails = !userDetailsParam ? authService.userDetails() : userDetailsParam
        log.debug "getUser - userDetails = ${userDetails}"

        if (!userDetails?.userId) {
            log.error("User isn't logged in - or there is a problem with CAS configuration")
            return null
        }

        User user = User.findByUserId(userDetails["userId"])
        log.debug "getUser - user = ${user} || userId = ${userDetails["userId"]}"
        if (user == null) {
            log.debug "User is not in user table - creating new record for " + userDetails
            user = new User([email: userDetails.email, userId: userDetails.userId, locked: userDetails.locked, frequency: Frequency.findByName("weekly")])
            User.withTransaction {
                if (!user.save(flush: true, failOnError: true)) {
                    user.errors.allErrors.each {
                        log.error(it)
                    }
                }
            }

            if (grailsApplication.config.getProperty('useBlogsAlerts', Boolean, true)) {
                // new user gets "Blogs and News" weekly by default (opt out)
                def notificationInstance = new Notification()
                notificationInstance.query = Query.findByName(messageSource.getMessage("query.ala.blog.title", null, siteLocale))
                notificationInstance.user = user
                Notification.withTransaction {
                    if (!notificationInstance.save(flush: true)) {
                        notificationInstance.errors.allErrors.each {
                            log.error(it)
                        }
                    }
                }
            }
        }
        user
    }

    // get user via userId, if not found in database create one
    User getUser(String userId) {
        if (!userId) {
            return null
        }

        // try to find in User database
        User user = User.findByUserId(userId)
        // if not in database try to create it
        if (user == null) {
            user = createUser(userId)
        }

        user
    }

    // get user via email, if not found in database create one
    User getUserByEmailOrCreate(String userEmail) {
        if (!userEmail) {
            return null
        }

        // try to find in User database
        User user = User.findByEmail(userEmail)
        // if not in database try to create it
        if (user == null) {
            user = createUser(userEmail)
        }

        user
    }

    User createUser(String userId) {
        User user = null
        UserDetails userDetails = authService.getUserForUserId(userId)
        if (userDetails?.userId && userDetails?.email) {
            log.debug "User is not in user table - creating new record for " + userDetails
            user = new User([email: userDetails.email, userId: userDetails.userId, locked: userDetails.locked, frequency: Frequency.findByName("weekly")])
            User.withTransaction {
                if (!user.save(flush: true, failOnError: true)) {
                    user.errors.allErrors.each {
                        log.error(it)
                    }
                }
            }
        }
        user
    }

    /**
     * Get user by sequence id
     * @param id
     * @return
     */
    User getUserBySequeceId(Long id) {
        User.get(id)
    }

    /**
     * Get user by userId
     * @param userId ALA user id
     * @return
     */
    User getUserById(String userId) {
        User.findByUserId(userId)
    }

    User getUserByEmail(String userEmail) {
        User.findByEmail(userEmail)
    }

    List<User> findUsers(String term) {
        User.findAllByEmailIlike("%${term}%")
    }

    @Cacheable("testCache")
    boolean testEhCache(String input = "not-set") {
        log.warn "Inside the testEhCache() method with ${input}... sleeping for 5 seconds"
        sleep(5000)
        log.warn "Exiting testEhCache() method"
        true
    }
}
