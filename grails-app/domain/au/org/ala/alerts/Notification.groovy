package au.org.ala.alerts

class Notification {

    Query query
    User user
    String unsubscribeToken

    static constraints = {
        unsubscribeToken nullable: true
    }

    def beforeInsert() {
        unsubscribeToken = UUID.randomUUID().toString()
    }

    String toString() {
        "Query: " + query.id + " for user ID: " + user.id + ", Email: " + user.email
    }
}
