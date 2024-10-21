package au.org.ala.alerts

import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.PathNotFoundException
import grails.converters.JSON
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject


class AnnotationService {
    NotificationService notificationService

    String appendAssertions(Query query, JSONObject occurrences) {
        String baseUrl = query.baseUrl

        // get the user id from the query path
        // NOTE: oder of the query path is important
//        String userId = query.queryPath.substring(query.queryPath.indexOf('assertion_user_id:') + 'assertion_user_id:'.length(), query.queryPath.indexOf('&dir=desc'))

        if(occurrences.occurrences) {
            // reconstruct occurrences so that only those records with specified annotations are put into the list
            JSONArray reconstructedOccurrences = []
            for (JSONObject occurrence : occurrences.occurrences) {
                if (occurrence.uuid) {
                    // all the verified assertions of this occurrence record

                    String assertionUrl = baseUrl + '/occurrences/' + occurrence.uuid + '/assertions'
                    def assertionsData = notificationService.getWebserviceResults(assertionUrl)
                    JSONArray assertions = JSON.parse(assertionsData) as JSONArray
                    occurrence.put('user_assertions', assertions)

                    def (openAssertions, verifiedAssertions, correctedAssertions) = filterAssertions(assertions)


                    // only include record has at least 1 (50001/50002/50003) assertion
                    // They will be used for diffService (records that will be included in alert email)

                    // find the open/verfied/corrected annotations which comment on all the assertions created by the users
                    def proccessedAssertionIds = openAssertions.collect { it.uuid } + verifiedAssertions.collect { it.uuid } + correctedAssertions.collect { it.uuid }
                    def processedAssertions = assertions.findAll{
                        proccessedAssertionIds.contains(it.relatedUuid)
                    }
                    occurrence.put('processed_assertions', processedAssertions)

                    // Those open/verified/corrected assertions will be used to retrieve diff (records that will be included in alert email)
                    if (!openAssertions.isEmpty() || !verifiedAssertions.isEmpty() || !correctedAssertions.isEmpty()) {
                        //Display the content of the user assertions
                        openAssertions.sort { it.uuid }
                        verifiedAssertions.sort { it.uuid }
                        correctedAssertions.sort { it.uuid }
                        occurrence.put('open_assertions', openAssertions.collect { it.uuid }.join(','))
                        occurrence.put('verified_assertions', verifiedAssertions.collect { it.uuid }.join(','))
                        occurrence.put('corrected_assertions', correctedAssertions.collect { it.uuid }.join(','))
                        reconstructedOccurrences.push(occurrence)
                    }
                }
            }
            reconstructedOccurrences.sort { it.uuid }

            // reconstruct occurrences which will be used to retrieve diff (records that will be included in alert email)
            occurrences.put('occurrences', reconstructedOccurrences)
        }

        return occurrences.toString()
    }


    //Search the assertions which the USER has made
    //return those have been open-issued, verified or corrected
    //
    private static def filterMyAssertions(JSONArray assertions) {
        def openAssertions = []
        def verifiedAssertions = []
        def correctedAssertions = []
        if (assertions) {
            // all the 50001 (open issue) assertions (could belong to userId or other users)
            def openIssueIds = assertions.findAll { it.uuid && it.relatedUuid && it.code == 50000 && it.qaStatus == 50001 }.collect { it.relatedUuid }

            // all the 50002 (verified) assertions (could belong to userId or other users)
            def verifiedIds = assertions.findAll { it.uuid && it.relatedUuid && it.code == 50000 && it.qaStatus == 50002 }.collect { it.relatedUuid }

            // all the 50003 (corrected) assertions (could belong to userId or other users)
            def correctedIds = assertions.findAll { it.uuid && it.relatedUuid && it.code == 50000 && it.qaStatus == 50003 }.collect { it.relatedUuid }

            openAssertions = assertions.findAll { openIssueIds.contains(it.uuid) }
            verifiedAssertions = assertions.findAll { verifiedIds.contains(it.uuid) }
            correctedAssertions = assertions.findAll { correctedIds.contains(it.uuid) }
        }
        [openAssertions, verifiedAssertions, correctedAssertions,]
    }


    /**
     * for normal alerts, comparing occurrence uuid is enough to show the difference.
     * for my annotation alerts, same occurrence record could exist in both result but have different assertions.
     * so comparing occurrence uuid is not enough, we need to compare 50001/50002/50003 sections inside each occurrence record

     * return a list of records that their annotations have been changed or deleted
     * @param previous
     * @param last
     * @param recordJsonPath
     * @return
     */
    def diff(previous, last, recordJsonPath) {
        // uuid -> occurrence record map
        def oldRecordsMap = [:]
        def curRecordsMap = [:]
        try {
            oldRecordsMap = JsonPath.read(previous, recordJsonPath).collectEntries { [(it.uuid): it] }
        }catch (PathNotFoundException e){
            log.info("Previous result doesn't contain any records since the returned json does not contain any 'recordJsonPath'")
        }
        try {
             curRecordsMap = JsonPath.read(last, recordJsonPath).collectEntries { [(it.uuid): it] }
        }catch (PathNotFoundException e){
            log.info("Previous result doesn't contain any records since the returned json does not contain any 'recordJsonPath'")
        }
        // if an occurrence record doesn't exist in previous result (added) or has different open_assertions or verified_assertions or corrected_assertions than previous (changed).
        def records = curRecordsMap.findAll {
            def record = it.value
            !oldRecordsMap.containsKey(record.uuid) ||
                    record.open_assertions != oldRecordsMap.get(record.uuid).open_assertions ||
                    record.verified_assertions != oldRecordsMap.get(record.uuid).verified_assertions ||
                    record.corrected_assertions != oldRecordsMap.get(record.uuid).corrected_assertions
        }.values()


        //if an occurrence record exists in previous result but not in current, it means the annotation is deleted.
        //We need to add these records as a 'modified' record
        records.addAll(oldRecordsMap.findAll { !curRecordsMap.containsKey(it.value.uuid) }.values())

        return records
    }
}
