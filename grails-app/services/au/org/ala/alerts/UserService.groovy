/*
 * Copyright (C) 2017 Atlas of Living Australia
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

import au.org.ala.web.UserDetails
import grails.converters.JSON

class UserService {

    static transactional = true

    def authService

    def serviceMethod() {}

    int updateUserEmails(){
        def toUpdate = []
        User.findAll().each {
            UserDetails userDetails = authService.getUserForUserId(it.userId, true)

            if (userDetails != null && it.email != userDetails.userName){
                log.debug "userDetails = ${userDetails as JSON}"
                it.email = userDetails.userName

                if (userDetails.hasProperty("locked")) {
                    it.locked = userDetails.locked
                }

                toUpdate << it
            }
        }
        toUpdate.each {
            it.save(flush:true)
        }
        toUpdate.size()
    }

    User getUser(userDetailsParam = null) {

        def userDetails = !userDetailsParam? authService.userDetails(): userDetailsParam
        log.debug "#getUser - userDetails = ${userDetails}"
       // def userDetails = authService.userDetails()

        if (!userDetails?.userId) {
            log.error "User isn't logged in - or there is a problem with CAS configuration"
            return null
        }

        User user = User.findByUserId(userDetails["userId"])
        log.debug "#getUser - user = ${user} || userId = ${userDetails["userId"]}"
        if (user == null) {
            log.debug "User is not in user table - creating new record for " + userDetails
            user = new User([email: userDetails["email"], userId: userDetails["userId"], frequency: Frequency.findByName("weekly")])
            user.save(flush:true, failOnError: true)
            // new user gets "Blogs and News" by default (opt out)
            def notificationInstance = new Notification()
            notificationInstance.query = Query.findByName("Blogs and News") //Query.findById(params.id)
            notificationInstance.user = user
            notificationInstance.save(flush: true)
        }
        user
    }

    User getUserById(userId) {
        User.findByUserId(userId)
    }

    List<User> findUsers(String term) {
        User.findAllByEmailIlike("%${term}%")
    }
}
