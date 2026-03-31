package au.org.ala.alerts

class ErrorLog {
    String stackTrace
    Date executedAt
    String context
    String queryType
    Long queryId
    String queryName
    Boolean reviewed = false

    static constraints = {
        stackTrace nullable: true
        executedAt nullable: false
        context nullable: true
        queryType nullable: true
        queryId nullable: true
        queryName nullable: true
        reviewed nullable: false
    }
    static mapping = {
        version false
    }
}
