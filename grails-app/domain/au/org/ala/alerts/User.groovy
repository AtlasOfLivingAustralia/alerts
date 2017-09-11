package au.org.ala.alerts

class User {

    String userId //ALA CAS ID
    String email
    Frequency frequency
    String unsubscribeToken
    Boolean locked // provided by userdetails, indicates emails should not be sent to this account
    static hasMany = [notifications: Notification]

    static constraints = {
        frequency nullable: true
        unsubscribeToken nullable: true
        locked nullable: true
    }

    /**
     * Generate a new unsubscribe token each time the user is updated - this reduces the chance that a token can be
     * maliciously re-used by ensuring any change the user produces a new token. This isn't perfect, of course.
     */
    def beforeUpdate() {
        unsubscribeToken = UUID.randomUUID().toString()
    }

    def beforeInsert() {
        unsubscribeToken = UUID.randomUUID().toString()
    }

    public String toString() { userId + " - " + email }
}
