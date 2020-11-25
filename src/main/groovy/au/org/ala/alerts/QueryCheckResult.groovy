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

    @Override
    String toString() {
        "Query ID:" + query?.id +
                "\nURL: " + urlChecked +
//                "\nfireAlert: " + fireAlert +
                "\nerrored: " + errored
    }
}
