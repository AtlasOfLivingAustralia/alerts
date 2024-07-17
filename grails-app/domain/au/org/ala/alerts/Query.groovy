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

    transient String listId //species list id
    transient Date lastChecked // Date when the last execution performed

    static hasMany = [notifications: Notification, queryResults: QueryResult, propertyPaths: PropertyPath]

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

    String toString() {
        return name
    }

    int countSubscribers(String frequency = null) {
        if (frequency) {
            return notifications.collect { it.user }.count(it -> it.frequency?.name == frequency)
        } else {
            return notifications.collect { it.user }.count()
        }
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
}
