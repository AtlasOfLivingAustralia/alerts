package au.org.ala.alerts
/**
 * Created by mar759 on 17/10/2014.
 */
class QueryCheckResult {

    Query query
    QueryResult queryResult
    String frequency
    String urlChecked
    String response
    boolean errored = false
    long timeTaken = 0

    public String toString(){
        "Query ID:" + queryId +
                "\nURL: " + urlChecked +
                "\nfireAlert: " + fireAlert +
                "\nerrored: " + errored
    }
}
