package au.org.ala.alerts

import java.util.regex.Pattern

class Query {
    String name
    String updateMessage
    String description
    String baseUrl
    String baseUrlForUI
    String resourceName     //e.g. Atlas or AVH
    String queryPath          // a URL that returns an XML/JSON response
    String queryPathForUI     // a URL that returns an XML/JSON response

    String emailTemplate = 'email/alert'
    //byte[] lastResultZipped
    boolean custom = false

    //optional properties
    String dateFormat

    String idJsonPath   //the json path for producing a list of IDs for change detection
    String recordJsonPath
    static hasMany = [notifications: Notification, queryResults: QueryResult, propertyPaths: PropertyPath]

    transient String listId //species list id
    // Date when the last execution performed.
    // NOTE: Except Biosecurity, other queries may have 4 lastChecked dates, matching the 4 frequencies
    // Only used for passing the checked date to the Email template
    transient Date lastChecked

    static constraints = {
        description nullable: true, maxSize: 400, widget: 'textarea'
        dateFormat nullable: true
        idJsonPath nullable: true
        recordJsonPath nullable: true
        description sqlType: 'text'
        updateMessage sqlType: 'text'
        queryPathForUI sqlType: 'text'
        queryPath sqlType: 'text'
    }

    static mapping = {
        //propertyPaths cascade: 'all-delete-orphan'
        queryResults cascade: 'all-delete-orphan'
        notifications cascade: 'all-delete-orphan'
    }

    Query() {
        this.notifications = []
        this.queryResults = []
        this.propertyPaths = []
    }

    String toString() {
        return name
    }

    /**
     * Count the number of ACTIVE subscribers for this query, optionally filtered by frequency.
     * @param frequency
     * @return
     */
    int countSubscribers(String frequency = null) {
        if (frequency) {
            return notifications.findAll { it.enabled }.collect { it.user }.count(it -> it.frequency?.name == frequency)
        } else {
            return notifications.findAll { it.enabled }.collect { it.user }.count()
        }
    }

    /**
     * return ACTIVE subscribers for this query, optionally filtered by frequency.
     * @param frequency
     * @return
     */
     def getSubscribers(String frequency = null) {
        def subscribers

        if (frequency) {
            subscribers = notifications.findAll { it.enabled }.collect { it.user }.findAll(it -> it.frequency?.name == frequency)
        } else {
            subscribers= notifications.findAll { it.enabled }.collect { it.user }
        }
        return subscribers
    }

    /**
     * return ACTIVE subscribers for this query, optionally filtered by frequency.
     * @param frequency
     * @return
     */
    def getInactiveSubscribers(String frequency = null) {
        def subscribers

        if (frequency) {
            subscribers = notifications.findAll { !it.enabled }.collect { it.user }.findAll(it -> it.frequency?.name == frequency)
        } else {
            subscribers= notifications.findAll { !it.enabled }.collect { it.user }
        }
        return subscribers
    }

    String getSubscriberEmails(String frequency = null) {
        def maxEmails = 10
        def emailList

        if (frequency) {
            def users = notifications.findAll { it.enabled }.collect { it.user }.findAll(it -> it.frequency?.name == frequency)
            emailList = users.collect(it -> it.email)
        } else {
            def users = notifications.findAll { it.enabled }.collect { it.user }
            emailList = users.collect(it -> it.email)
        }

        def subscribers = emailList.take(maxEmails).join('; ') // Take first 10
        if (emailList.size() > maxEmails) {
            subscribers += ' ......'
        }
        return subscribers
    }

    String getListId() {
        Pattern pattern = Pattern.compile(".*(?:species_list_uid|species_list):(drt?[0-9]+).*")
        def matcher = pattern.matcher(queryPath)
        if (matcher.find()) {
            return matcher.group(1)
        }
        return null
    }
    /**
     * return logs of a given frequency, or all logs if frequency is null
     * @param frequency
     * @return logs [array]
     */
    String[] getLogs(String frequency= null) {
        def logs = []
        if (frequency) {
            this.queryResults?.each { qr ->
                if (qr.frequency.isFrequency(frequency)) {
                    logs << qr.getLog()
                }
            }
        } else {
            this.queryResults?.each { qr ->
                logs << qr.getLog()
            }
        }
        return logs.flatten()
    }

    /**
     * return QueryResult of a given frequency
     */
    QueryResult getQueryResult(String frequency) {
        return this.queryResults.find { it.frequency.isFrequency(frequency) }
    }

    boolean isBiosecurity() {
        return this.emailTemplate == '/email/biosecurity'
    }

    /**
     * Is this the 'My Annotations' query belonging to the given user?
     *
     * The stored queryPath has more parameters after the user id
     * (e.g. /occurrences/search?fq=assertion_user_id:1234&dir=desc&pageSize=20&...), so this is a
     * prefix match.
     */
    boolean isMyAnnotation(String userId) {
        if (!userId || !queryPath) {
            return false
        }
        String prefix = '/occurrences/search?fq=assertion_user_id:' + userId
        return queryPath.toLowerCase().startsWith(prefix.toLowerCase()) &&
                emailTemplate?.equalsIgnoreCase('/email/myAnnotations')
    }
}
