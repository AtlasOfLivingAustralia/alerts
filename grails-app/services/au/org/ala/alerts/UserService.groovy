package au.org.ala.alerts

import au.org.ala.web.UserDetails

class UserService {

    static transactional = true

    def authService

    def serviceMethod() {}

    int updateUserEmails(){
        def toUpdate = []
        User.findAll().each {
            UserDetails userDetails = authService.getUserForUserId(it.userId)
            if(userDetails != null && it.email != userDetails.userName){
                it.email = userDetails.userName
                toUpdate << it
            }
        }
        toUpdate.each {
            it.save(flush:true)
        }
        toUpdate.size()
    }

    User getUser() {

        def userDetails = authService.userDetails()

        if (!userDetails?.userId) {
            log.error "User isn't logged in - or there is a problem with CAS configuration"
            return null
        }

        User user = User.findByUserId(userDetails["userId"])
        if (user == null) {
            log.debug "User is not in user table - creating new record for " + userDetails
            user = new User([email: userDetails["email"], userId: userDetails["userId"], frequency: Frequency.findByName("weekly")])
            user.save(true)
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

}
