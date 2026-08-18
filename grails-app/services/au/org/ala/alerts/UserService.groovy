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
import au.org.ala.web.AuthService
import au.org.ala.web.UserDetails
import grails.converters.JSON
import grails.plugin.cache.Cacheable
import grails.util.Holders
import grails.util.Environment

class UserService {

    static transactional = true

    def authService, queryService, messageSource, grailsApplication

    def siteLocale = new Locale.Builder().setLanguageTag(Holders.config.siteDefaultLanguage as String).build()

    /**
     * Sync User table with UserDetails app via webservice
     *
     * @return total number of updates
     */

    int updateUserEmails() {
        final int pageSize = grailsApplication.config.getProperty('alerts.user-sync.batch-size', Integer, 1000)
        def toUpdate = []
        int total = 0
        User.withTransaction {
            total = User.count()
            log.warn "Checking all ${total} users in Alerts user table."
        }

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

    User createUser(String userIdOrEmail) {
        User user = null
        boolean isEmail = userIdOrEmail.contains('@')
        UserDetails userDetails = isEmail ?
             authService.getUserForEmailAddress(userIdOrEmail) :  authService.getUserForUserId(userIdOrEmail)

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

    /**
     * Find users whose email contains the given term, capped to 'max' results and sorted by email.
     * Used by the admin autocomplete, so the same term is typically requested repeatedly - cached
     * for a short time (see 'userSearchCache' in ehcache3.xml).
     *
     * Returns simple maps rather than User instances: cached domain objects outlive the Hibernate
     * session they were loaded in, and touching a lazy association on one throws
     * LazyInitializationException.
     *
     * @return [[userId: .., email: ..], ..]
     */
    @Cacheable("userSearchCache")
    List<Map> findUsers(String term, int max) {
        User.findAllByEmailIlike("%${term}%", [max: max, sort: 'email', order: 'asc']).collect { User user ->
            [userId: user.userId, email: user.email]
        }
    }

    @Cacheable("testCache")
    boolean testEhCache(String input = "not-set") {
        log.warn "Inside the testEhCache() method with ${input}... sleeping for 5 seconds"
        sleep(5000)
        log.warn "Exiting testEhCache() method"
        true
    }

    /**
     * Find the queries that would be left behind if the given user were deleted, i.e. the queries
     * that belong to this user alone and so must be deleted with them:
     *
     *  - their 'My Annotations' query (always personal to one user)
     *  - custom queries that no other user subscribes to
     *
     * Standard queries, and custom queries still subscribed to by somebody else, are NOT included -
     * removing those would break other users' alerts.
     *
     * @param user
     * @return [[id: .., name: ..], ..] - empty when there is nothing to clean up
     */
    List<Map> findQueriesRelatedToDeletedUser(User user) {
        if (!user) {
            return []
        }

        List<Map> queries = []
        Notification.findAllByUser(user).each { Notification notification ->
            Query query = notification.query
            if (!query) {
                return
            }

            boolean personalQuery = query.isMyAnnotation(user.userId)
            boolean onlySubscriber = query.custom && Notification.countByQuery(query) <= 1

            if (personalQuery || onlySubscriber) {
                queries << [id: query.id, name: query.name]
            }
        }

        queries
    }

    /**
     * Delete a user together with everything that only exists because of them: their notifications
     * (subscriptions) and the queries returned by #findQueriesRelatedToDeletedUser.
     *
     * Queries shared with other users are left alone - only this user's subscription to them goes.
     *
     * @param user
     * @return [status: 0|1, message: '..', deletedQueries: [..], deletedNotifications: n]
     */
    Map delete(User user) {
        if (!user) {
            return [status: 1, message: 'User not found.']
        }

        String email = user.email
        List<Map> queriesToDelete = findQueriesRelatedToDeletedUser(user)
        int deletedNotifications = 0

        try {
            User.withTransaction {
                // remove this user's subscriptions first, so the queries below are no longer referenced
                Notification.findAllByUser(user).each { Notification notification ->
                    notification.delete()
                    deletedNotifications++
                }

                // then the queries that only existed for this user (also clears their results / property paths)
                queriesToDelete.each { Map query ->
                    queryService.wipe(query.id)
                }

                user.delete()
            }
        } catch (Exception e) {
            log.error("Failed to delete user ${email}", e)
            return [status: 1, message: "Failed to delete ${email}: ${e.message}"]
        }

        log.warn("Deleted user ${email}, ${deletedNotifications} subscription(s) and ${queriesToDelete.size()} query(s)")
        [status              : 0,
         message             : "Deleted ${email}, ${deletedNotifications} subscription(s) and ${queriesToDelete.size()} query(s).",
         deletedQueries      : queriesToDelete,
         deletedNotifications: deletedNotifications]
    }
}
