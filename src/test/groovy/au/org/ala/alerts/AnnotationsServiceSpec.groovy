package au.org.ala.alerts

import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

class AnnotationsServiceSpec extends Specification implements ServiceUnitTest<MyAnnotationService> {

    def assertionService

    def setup() {
        assertionService = new AnnotationsService()
    }

    def cleanup() {
    }


    /**
     * Only 1 Previous record and 3 current records which the first one is same as the previous record
     */
    void "diff check against records with the same user_assertions='' "() {
        given:
        def previous = "{ " +
                "  \"occurrences\": [ " +
                "    { " +
                "      \"country\": \"Australia\", " +
                "      \"raw_countryCode\": \"AU\", " +
                "      \"scientificName\": \"Acacia petraea\", " +
                "      \"year\": 2023, " +
                "      \"decimalLatitude\": -29.062978, " +
                "      \"uuid\": \"0e6de91f-d30c-4e52-b7d5-678e5527d0b5\", " +
                "      \"point01\": \"-29.1,145\", " +
                "      \"raw_locationRemarks\": \"locationRemarks withheld\", " +
                "      \"basisOfRecord\": \"HUMAN_OBSERVATION\", " +
                "      \"raw_scientificName\": \"Acacia petraea\", " +
                "      \"raw_vernacularName\": \"Lancewood\", " +
                "      \"taxonConceptID\": \"https://id.biodiversity.org.au/node/apni/2903503\", " +
                "      \"latLong\": \"-29.062978,144.994102\", " +
                "      \"user_assertions\": [ " +
                "        { " +
                "          \"qaStatus\": 50005, " +
                "          \"problemAsserted\": false, " +
                "          \"code\": 20019, " +
                "          \"dataResourceUid\": \"dr368\", " +
                "          \"referenceRowKey\": \"0e6de91f-d30c-4e52-b7d5-678e5527d0b5\", " +
                "          \"created\": \"2024-10-14T22:41:33Z\", " +
                "          \"name\": \"userAssertionOther\", " +
                "          \"userDisplayName\": \"Luke Skywalker\", " +
                "          \"comment\": \"Alert test purpose\", " +
                "          \"uuid\": \"fb8dbbb3-e0d3-41eb-ab97-36b76fc75476\", " +
                "          \"userId\": \"666888\" " +
                "        } " +
                "      ], " +
                "      \"assertions\": [ " +
                "        \"COORDINATE_ROUNDED\", " +
                "        \"MISSING_GEOREFERENCE_DATE\", " +
                "        \"MISSING_GEOREFERENCEDBY\", " +
                "        \"MISSING_GEOREFERENCESOURCES\", " +
                "        \"MISSING_GEOREFERENCEVERIFICATIONSTATUS\", " +
                "        \"INDIVIDUAL_COUNT_INVALID\" " +
                "      ], " +
                "      \"verified_assertions\": \"OLD_VERIFIED_ASSERTIONS\", " +
                "      \"speciesGroups\": [ " +
                "        \"Plants\", " +
                "        \"Flowering plants\", " +
                "        \"Dicots\" " +
                "      ], " +
                "      \"spatiallyValid\": true, " +
                "      \"order\": \"Fabales\", " +
                "      \"taxonRankID\": 7000, " +
                "      \"dataResourceName\": \"NSW BioNet Atlas\", " +
                "      \"raw_basisOfRecord\": \"HumanObservation\", " +
                "      \"stateProvince\": \"New South Wales\", " +
                "      \"decimalLongitude\": 144.994102, " +
                "      \"occurrenceID\": \"urn:catalog:NSW Dept of Planning, Industry and Environment:BioNet Atlas of NSW Wildlife:SDFKI0142229\", " +
                "      \"raw_collectionCode\": \"BioNet Atlas of NSW Wildlife\", " +
                "      \"corrected_assertions\": \"\", " +
                "      \"license\": \"CC-BY 4.0 (Int)\", " +
                "      \"collectors\": [ " +
                "        \"OMFZ2303270J\" " +
                "      ], " +
                "      \"month\": \"09\", " +
                "      \"dataResourceUid\": \"dr368\", " +
                "      \"genus\": \"Acacia\", " +
                "      \"left\": 588026, " +
                "      \"point0001\": \"-29.063,144.994\", " +
                "      \"eventDate\": 1694217600000, " +
                "      \"coordinateUncertaintyInMeters\": 10, " +
                "      \"taxonRank\": \"species\", " +
                "      \"point1\": \"-29,145\", " +
                "      \"collector\": [ " +
                "        \"OMFZ2303270J\" " +
                "      ], " +
                "      \"vernacularName\": \"Lancewood\", " +
                "      \"hasUserAssertions\": true, " +
                "      \"speciesGuid\": \"https://id.biodiversity.org.au/node/apni/2903503\", " +
                "      \"raw_occurrenceRemarks\": \"occurrenceRemarks withheld\", " +
                "      \"rights\": \"Unknown\", " +
                "      \"geospatialKosher\": \"true\", " +
                "      \"namesLsid\": \"Acacia petraea|https://id.biodiversity.org.au/node/apni/2903503|Lancewood|Plantae|Fabaceae\", " +
                "      \"point00001\": \"-29.063,144.9941\", " +
                "      \"raw_institutionCode\": \"NSW Dept of Planning, Industry and Environment\", " +
                "      \"point001\": \"-29.06,144.99\", " +
                "      \"right\": 588026, " +
                "      \"raw_catalogNumber\": \"SDFKI0142229\", " +
                "      \"kingdom\": \"Plantae\", " +
                "      \"dataProviderUid\": \"dp34\", " +
                "      \"recordedBy\": [ " +
                "        \"OMFZ2303270J\" " +
                "      ], " +
                "      \"phylum\": \"Charophyta\", " +
                "      \"classs\": \"Equisetopsida\", " +
                "      \"species\": \"Acacia petraea\", " +
                "      \"dataProviderName\": \"Department of Planning, Industry and Environment representing the State of New South Wales\", " +
                "      \"stateConservation\": \"Endangered\", " +
                "      \"family\": \"Fabaceae\", " +
                "      \"genusGuid\": \"https://id.biodiversity.org.au/taxon/apni/51471290\", " +
                "      \"open_assertions\": \"\", " +
                "      \"userAssertions\": \"50005\" " +
                "    }  ], " +
                "  \"totalRecords\": 1, " +
                "  \"query\": \"?q=*%3A*&fq=assertion_user_id%3A666888\", " +
                "  \"pageSize\": 300, " +
                "  \"queryTitle\": \"[all records]\", " +
                "  \"sort\": \"score\", " +
                "  \"dir\": \"desc\", " +
                "  \"startIndex\": 0, " +
                "  \"activeFacetMap\": { " +
                "    \"assertion_user_id\": { " +
                "      \"displayName\": \"Assertions by user:Luke Skywalker\", " +
                "      \"name\": \"assertion_user_id\", " +
                "      \"value\": \"666888\" " +
                "    } " +
                "  }, " +
                "  \"activeFacetObj\": { " +
                "    \"assertion_user_id\": [ " +
                "      { " +
                "        \"displayName\": \"Assertions by user:Luke Skywalker\", " +
                "        \"name\": \"assertion_user_id\", " +
                "        \"value\": \"assertion_user_id:666888\" " +
                "      } " +
                "    ] " +
                "  }, " +
                "  \"urlParameters\": \"?q=*%3A*&fq=assertion_user_id%3A666888\", " +
                "  \"facetResults\": [ " +
                "     " +
                "  ], " +
                "  \"status\": \"OK\" " +
                "}"
        def current = "{\"occurrences\":[{\"country\":\"Australia\",\"raw_countryCode\":\"AU\",\"scientificName\":\"Acacia petraea\",\"year\":2023,\"decimalLatitude\":-29.062978,\"uuid\":\"0e6de91f-d30c-4e52-b7d5-678e5527d0b5\",\"point01\":\"-29.1,145\",\"raw_locationRemarks\":\"locationRemarks withheld\",\"basisOfRecord\":\"HUMAN_OBSERVATION\",\"raw_scientificName\":\"Acacia petraea\",\"raw_vernacularName\":\"Lancewood\",\"taxonConceptID\":\"https://id.biodiversity.org.au/node/apni/2903503\",\"latLong\":\"-29.062978,144.994102\",\"user_assertions\":[{\"qaStatus\":50005,\"problemAsserted\":false,\"code\":20019,\"dataResourceUid\":\"dr368\",\"referenceRowKey\":\"0e6de91f-d30c-4e52-b7d5-678e5527d0b5\",\"created\":\"2024-10-14T22:41:33Z\",\"name\":\"userAssertionOther\",\"userDisplayName\":\"Luke Skywalker\",\"comment\":\"Alert test purpose\",\"uuid\":\"fb8dbbb3-e0d3-41eb-ab97-36b76fc75476\",\"userId\":\"666888\"}],\"assertions\":[\"COORDINATE_ROUNDED\",\"MISSING_GEOREFERENCE_DATE\",\"MISSING_GEOREFERENCEDBY\",\"MISSING_GEOREFERENCESOURCES\",\"MISSING_GEOREFERENCEVERIFICATIONSTATUS\",\"INDIVIDUAL_COUNT_INVALID\"],\"verified_assertions\":\"\",\"speciesGroups\":[\"Plants\",\"Flowering plants\",\"Dicots\"],\"spatiallyValid\":true,\"order\":\"Fabales\",\"taxonRankID\":7000,\"dataResourceName\":\"NSW BioNet Atlas\",\"raw_basisOfRecord\":\"HumanObservation\",\"stateProvince\":\"New South Wales\",\"decimalLongitude\":144.994102,\"occurrenceID\":\"urn:catalog:NSW Dept of Planning, Industry and Environment:BioNet Atlas of NSW Wildlife:SDFKI0142229\",\"raw_collectionCode\":\"BioNet Atlas of NSW Wildlife\",\"corrected_assertions\":\"\",\"license\":\"CC-BY 4.0 (Int)\",\"collectors\":[\"OMFZ2303270J\"],\"month\":\"09\",\"dataResourceUid\":\"dr368\",\"genus\":\"Acacia\",\"left\":588026,\"point0001\":\"-29.063,144.994\",\"eventDate\":1694217600000,\"coordinateUncertaintyInMeters\":10,\"taxonRank\":\"species\",\"point1\":\"-29,145\",\"collector\":[\"OMFZ2303270J\"],\"vernacularName\":\"Lancewood\",\"hasUserAssertions\":true,\"speciesGuid\":\"https://id.biodiversity.org.au/node/apni/2903503\",\"raw_occurrenceRemarks\":\"occurrenceRemarks withheld\",\"rights\":\"Unknown\",\"geospatialKosher\":\"true\",\"namesLsid\":\"Acacia petraea|https://id.biodiversity.org.au/node/apni/2903503|Lancewood|Plantae|Fabaceae\",\"point00001\":\"-29.063,144.9941\",\"raw_institutionCode\":\"NSW Dept of Planning, Industry and Environment\",\"point001\":\"-29.06,144.99\",\"right\":588026,\"raw_catalogNumber\":\"SDFKI0142229\",\"kingdom\":\"Plantae\",\"dataProviderUid\":\"dp34\",\"recordedBy\":[\"OMFZ2303270J\"],\"phylum\":\"Charophyta\",\"classs\":\"Equisetopsida\",\"species\":\"Acacia petraea\",\"dataProviderName\":\"Department of Planning, Industry and Environment representing the State of New South Wales\",\"stateConservation\":\"Endangered\",\"family\":\"Fabaceae\",\"genusGuid\":\"https://id.biodiversity.org.au/taxon/apni/51471290\",\"open_assertions\":\"\",\"userAssertions\":\"50005\"},{\"country\":\"Australia\",\"scientificName\":\"Acacia idiomorpha\",\"year\":1962,\"decimalLatitude\":-27.75,\"uuid\":\"539bd9f9-95bd-47b6-b50f-bf75089b9521\",\"point01\":\"-27.8,114.2\",\"basisOfRecord\":\"PRESERVED_SPECIMEN\",\"raw_scientificName\":\"Acacia idiomorpha Benth.\",\"taxonConceptID\":\"https://id.biodiversity.org.au/node/apni/2902734\",\"latLong\":\"-27.75,114.166667\",\"user_assertions\":[{\"qaStatus\":50005,\"problemAsserted\":false,\"code\":20019,\"dataResourceUid\":\"dr21662\",\"referenceRowKey\":\"539bd9f9-95bd-47b6-b50f-bf75089b9521\",\"created\":\"2023-11-14T04:43:35Z\",\"name\":\"userAssertionOther\",\"userDisplayName\":\"Luke Skywalker\",\"comment\":\"Alert test\",\"uuid\":\"340d3c4e-4979-45a3-934d-77f7cc1e9b53\",\"userId\":\"666888\"}],\"assertions\":[\"MISSING_TAXONRANK\",\"GEODETIC_DATUM_ASSUMED_WGS84\",\"MISSING_GEODETICDATUM\",\"COORDINATE_UNCERTAINTY_METERS_INVALID\",\"MISSING_GEOREFERENCE_DATE\",\"MISSING_GEOREFERENCEDBY\",\"MISSING_GEOREFERENCEPROTOCOL\",\"MISSING_GEOREFERENCESOURCES\",\"MISSING_GEOREFERENCEVERIFICATIONSTATUS\"],\"verified_assertions\":[ {\"uuid\":\"340d3c4e-4979-45a3-934d-77f7cc1e9b53\"}],\"speciesGroups\":[\"Plants\",\"Flowering plants\",\"Dicots\"],\"spatiallyValid\":true,\"order\":\"Fabales\",\"taxonRankID\":7000,\"dataResourceName\":\"Kings Park Botanical Gardens\",\"raw_basisOfRecord\":\"Preserved Specimen\",\"stateProvince\":\"Western Australia\",\"decimalLongitude\":114.166667,\"raw_collectionCode\":\"KPBG\",\"corrected_assertions\":\"\",\"license\":\"CC-BY 4.0 (Int)\",\"month\":\"09\",\"dataResourceUid\":\"dr21662\",\"genus\":\"Acacia\",\"left\":587962,\"point0001\":\"-27.75,114.167\",\"eventDate\":-229219200000,\"taxonRank\":\"species\",\"point1\":\"-28,114\",\"collectionName\":\"Kings Park and Botanic Garden\",\"hasUserAssertions\":true,\"speciesGuid\":\"https://id.biodiversity.org.au/node/apni/2902734\",\"geospatialKosher\":\"true\",\"namesLsid\":\"Acacia idiomorpha|https://id.biodiversity.org.au/node/apni/2902734||Plantae|Fabaceae\",\"collectionUid\":\"co32\",\"point00001\":\"-27.75,114.1667\",\"raw_institutionCode\":\"BGPA\",\"point001\":\"-27.75,114.17\",\"right\":587962,\"raw_catalogNumber\":\"37888\",\"kingdom\":\"Plantae\",\"phylum\":\"Charophyta\",\"classs\":\"Equisetopsida\",\"institutionUid\":\"in58\",\"species\":\"Acacia idiomorpha\",\"institutionName\":\"WA Botanic Gardens and Parks Authority\",\"family\":\"Fabaceae\",\"genusGuid\":\"https://id.biodiversity.org.au/taxon/apni/51471290\",\"open_assertions\":\"\",\"userAssertions\":\"50002\"},{\"country\":\"Australia\",\"scientificName\":\"Acacia obovata\",\"year\":1974,\"decimalLatitude\":-32.712222,\"uuid\":\"f7d3a852-7382-4999-bc2e-6d2676a11294\",\"point01\":\"-32.7,115.9\",\"basisOfRecord\":\"PRESERVED_SPECIMEN\",\"raw_scientificName\":\"Acacia obovata Benth.\",\"taxonConceptID\":\"https://id.biodiversity.org.au/node/apni/2899185\",\"latLong\":\"-32.712222,115.928611\",\"user_assertions\":[{\"qaStatus\":50005,\"problemAsserted\":false,\"code\":20019,\"dataResourceUid\":\"dr21662\",\"referenceRowKey\":\"f7d3a852-7382-4999-bc2e-6d2676a11294\",\"created\":\"2023-11-14T05:34:18Z\",\"name\":\"userAssertionOther\",\"userDisplayName\":\"Luke Skywalker\",\"comment\":\"alert test\",\"uuid\":\"81447f7f-65b3-4bde-b95c-f507f5e0c67a\",\"userId\":\"666888\"}],\"assertions\":[\"MISSING_TAXONRANK\",\"GEODETIC_DATUM_ASSUMED_WGS84\",\"MISSING_GEODETICDATUM\",\"COORDINATE_UNCERTAINTY_METERS_INVALID\",\"MISSING_GEOREFERENCE_DATE\",\"MISSING_GEOREFERENCEDBY\",\"MISSING_GEOREFERENCEPROTOCOL\",\"MISSING_GEOREFERENCESOURCES\",\"MISSING_GEOREFERENCEVERIFICATIONSTATUS\"],\"verified_assertions\":\"\",\"speciesGroups\":[\"Plants\",\"Flowering plants\",\"Dicots\"],\"spatiallyValid\":true,\"order\":\"Fabales\",\"taxonRankID\":7000,\"dataResourceName\":\"Kings Park Botanical Gardens\",\"raw_basisOfRecord\":\"Preserved Specimen\",\"stateProvince\":\"Western Australia\",\"decimalLongitude\":115.928611,\"raw_collectionCode\":\"KPBG\",\"corrected_assertions\":\"\",\"license\":\"CC-BY 4.0 (Int)\",\"month\":\"06\",\"dataResourceUid\":\"dr21662\",\"genus\":\"Acacia\",\"left\":587708,\"point0001\":\"-32.712,115.929\",\"eventDate\":141264000000,\"taxonRank\":\"species\",\"point1\":\"-33,116\",\"collectionName\":\"Kings Park and Botanic Garden\",\"hasUserAssertions\":true,\"speciesGuid\":\"https://id.biodiversity.org.au/node/apni/2899185\",\"geospatialKosher\":\"true\",\"namesLsid\":\"Acacia obovata|https://id.biodiversity.org.au/node/apni/2899185||Plantae|Fabaceae\",\"collectionUid\":\"co32\",\"point00001\":\"-32.7122,115.9286\",\"raw_institutionCode\":\"BGPA\",\"point001\":\"-32.71,115.93\",\"right\":587708,\"raw_catalogNumber\":\"38215\",\"kingdom\":\"Plantae\",\"phylum\":\"Charophyta\",\"classs\":\"Equisetopsida\",\"institutionUid\":\"in58\",\"species\":\"Acacia obovata\",\"institutionName\":\"WA Botanic Gardens and Parks Authority\",\"family\":\"Fabaceae\",\"genusGuid\":\"https://id.biodiversity.org.au/taxon/apni/51471290\",\"open_assertions\":\"\",\"userAssertions\":\"50005\"}],\"totalRecords\":3,\"query\":\"?q=*%3A*&fq=assertion_user_id%3A666888\",\"pageSize\":300,\"queryTitle\":\"[all records]\",\"sort\":\"score\",\"dir\":\"desc\",\"startIndex\":0,\"activeFacetMap\":{\"assertion_user_id\":{\"displayName\":\"Assertions by user:Luke Skywalker\",\"name\":\"assertion_user_id\",\"value\":\"666888\"}},\"activeFacetObj\":{\"assertion_user_id\":[{\"displayName\":\"Assertions by user:Luke Skywalker\",\"name\":\"assertion_user_id\",\"value\":\"assertion_user_id:666888\"}]},\"urlParameters\":\"?q=*%3A*&fq=assertion_user_id%3A666888\",\"facetResults\":[],\"status\":\"OK\"}"
        def jsonPath = "\$.occurrences[*]"
        when:
        def result = assertionService.diff(previous, current, jsonPath)

        then:
        result.size() == 2
    }

    /**
     * Compatible check
     * Only 1 Previous record
     * and 3 current records which the first one has is same as the previous record, but has different user_assertions
     */
    void "diff check same records with different user_assertions"() {
        given:
        def previous = "{ " +
                "  \"occurrences\": [ " +
                "    { " +
                "      \"country\": \"Australia\", " +
                "      \"raw_countryCode\": \"AU\", " +
                "      \"scientificName\": \"Acacia petraea\", " +
                "      \"year\": 2023, " +
                "      \"decimalLatitude\": -29.062978, " +
                "      \"uuid\": \"0e6de91f-d30c-4e52-b7d5-678e5527d0b5\", " +
                "      \"point01\": \"-29.1,145\", " +
                "      \"raw_locationRemarks\": \"locationRemarks withheld\", " +
                "      \"basisOfRecord\": \"HUMAN_OBSERVATION\", " +
                "      \"raw_scientificName\": \"Acacia petraea\", " +
                "      \"raw_vernacularName\": \"Lancewood\", " +
                "      \"taxonConceptID\": \"https://id.biodiversity.org.au/node/apni/2903503\", " +
                "      \"latLong\": \"-29.062978,144.994102\", " +
                "      \"user_assertions\": [ " +
                "        { " +
                "          \"qaStatus\": 50005, " +
                "          \"problemAsserted\": false, " +
                "          \"code\": 20019, " +
                "          \"dataResourceUid\": \"dr368\", " +
                "          \"referenceRowKey\": \"0e6de91f-d30c-4e52-b7d5-678e5527d0b5\", " +
                "          \"created\": \"2024-10-14T22:41:33Z\", " +
                "          \"name\": \"userAssertionOther\", " +
                "          \"userDisplayName\": \"WHO AM I\", " +
                "          \"comment\": \"Alert test purpose\", " +
                "          \"uuid\": \"fb8dbbb3-e0d3-41eb-ab97-36b76fc75476\", " +
                "          \"userId\": \"I DO NOT WANT TO SAY\" " +
                "        } " +
                "      ], " +
                "      \"assertions\": [ " +
                "        \"COORDINATE_ROUNDED\", " +
                "        \"MISSING_GEOREFERENCE_DATE\", " +
                "        \"MISSING_GEOREFERENCEDBY\", " +
                "        \"MISSING_GEOREFERENCESOURCES\", " +
                "        \"MISSING_GEOREFERENCEVERIFICATIONSTATUS\", " +
                "        \"INDIVIDUAL_COUNT_INVALID\" " +
                "      ], " +
                "      \"verified_assertions\": \"OLD_VERIFIED_ASSERTIONS\", " +
                "      \"speciesGroups\": [ " +
                "        \"Plants\", " +
                "        \"Flowering plants\", " +
                "        \"Dicots\" " +
                "      ], " +
                "      \"spatiallyValid\": true, " +
                "      \"order\": \"Fabales\", " +
                "      \"taxonRankID\": 7000, " +
                "      \"dataResourceName\": \"NSW BioNet Atlas\", " +
                "      \"raw_basisOfRecord\": \"HumanObservation\", " +
                "      \"stateProvince\": \"New South Wales\", " +
                "      \"decimalLongitude\": 144.994102, " +
                "      \"occurrenceID\": \"urn:catalog:NSW Dept of Planning, Industry and Environment:BioNet Atlas of NSW Wildlife:SDFKI0142229\", " +
                "      \"raw_collectionCode\": \"BioNet Atlas of NSW Wildlife\", " +
                "      \"corrected_assertions\": \"\", " +
                "      \"license\": \"CC-BY 4.0 (Int)\", " +
                "      \"collectors\": [ " +
                "        \"OMFZ2303270J\" " +
                "      ], " +
                "      \"month\": \"09\", " +
                "      \"dataResourceUid\": \"dr368\", " +
                "      \"genus\": \"Acacia\", " +
                "      \"left\": 588026, " +
                "      \"point0001\": \"-29.063,144.994\", " +
                "      \"eventDate\": 1694217600000, " +
                "      \"coordinateUncertaintyInMeters\": 10, " +
                "      \"taxonRank\": \"species\", " +
                "      \"point1\": \"-29,145\", " +
                "      \"collector\": [ " +
                "        \"OMFZ2303270J\" " +
                "      ], " +
                "      \"vernacularName\": \"Lancewood\", " +
                "      \"hasUserAssertions\": true, " +
                "      \"speciesGuid\": \"https://id.biodiversity.org.au/node/apni/2903503\", " +
                "      \"raw_occurrenceRemarks\": \"occurrenceRemarks withheld\", " +
                "      \"rights\": \"Unknown\", " +
                "      \"geospatialKosher\": \"true\", " +
                "      \"namesLsid\": \"Acacia petraea|https://id.biodiversity.org.au/node/apni/2903503|Lancewood|Plantae|Fabaceae\", " +
                "      \"point00001\": \"-29.063,144.9941\", " +
                "      \"raw_institutionCode\": \"NSW Dept of Planning, Industry and Environment\", " +
                "      \"point001\": \"-29.06,144.99\", " +
                "      \"right\": 588026, " +
                "      \"raw_catalogNumber\": \"SDFKI0142229\", " +
                "      \"kingdom\": \"Plantae\", " +
                "      \"dataProviderUid\": \"dp34\", " +
                "      \"recordedBy\": [ " +
                "        \"OMFZ2303270J\" " +
                "      ], " +
                "      \"phylum\": \"Charophyta\", " +
                "      \"classs\": \"Equisetopsida\", " +
                "      \"species\": \"Acacia petraea\", " +
                "      \"dataProviderName\": \"Department of Planning, Industry and Environment representing the State of New South Wales\", " +
                "      \"stateConservation\": \"Endangered\", " +
                "      \"family\": \"Fabaceae\", " +
                "      \"genusGuid\": \"https://id.biodiversity.org.au/taxon/apni/51471290\", " +
                "      \"open_assertions\": \"\", " +
                "      \"userAssertions\": \"50005\" " +
                "    }  ], " +
                "  \"totalRecords\": 1, " +
                "  \"query\": \"?q=*%3A*&fq=assertion_user_id%3A666888\", " +
                "  \"pageSize\": 300, " +
                "  \"queryTitle\": \"[all records]\", " +
                "  \"sort\": \"score\", " +
                "  \"dir\": \"desc\", " +
                "  \"startIndex\": 0, " +
                "  \"activeFacetMap\": { " +
                "    \"assertion_user_id\": { " +
                "      \"displayName\": \"Assertions by user:Luke Skywalker\", " +
                "      \"name\": \"assertion_user_id\", " +
                "      \"value\": \"666888\" " +
                "    } " +
                "  }, " +
                "  \"activeFacetObj\": { " +
                "    \"assertion_user_id\": [ " +
                "      { " +
                "        \"displayName\": \"Assertions by user:Luke Skywalker\", " +
                "        \"name\": \"assertion_user_id\", " +
                "        \"value\": \"assertion_user_id:666888\" " +
                "      } " +
                "    ] " +
                "  }, " +
                "  \"urlParameters\": \"?q=*%3A*&fq=assertion_user_id%3A666888\", " +
                "  \"facetResults\": [ " +
                "     " +
                "  ], " +
                "  \"status\": \"OK\" " +
                "}"
        def current = "{\"occurrences\":[{\"country\":\"Australia\",\"raw_countryCode\":\"AU\",\"scientificName\":\"Acacia petraea\",\"year\":2023,\"decimalLatitude\":-29.062978,\"uuid\":\"0e6de91f-d30c-4e52-b7d5-678e5527d0b5\",\"point01\":\"-29.1,145\",\"raw_locationRemarks\":\"locationRemarks withheld\",\"basisOfRecord\":\"HUMAN_OBSERVATION\",\"raw_scientificName\":\"Acacia petraea\",\"raw_vernacularName\":\"Lancewood\",\"taxonConceptID\":\"https://id.biodiversity.org.au/node/apni/2903503\",\"latLong\":\"-29.062978,144.994102\",\"user_assertions\":[{\"qaStatus\":50005,\"problemAsserted\":false,\"code\":20019,\"dataResourceUid\":\"dr368\",\"referenceRowKey\":\"0e6de91f-d30c-4e52-b7d5-678e5527d0b5\",\"created\":\"2024-10-14T22:41:33Z\",\"name\":\"userAssertionOther\",\"userDisplayName\":\"Luke Skywalker\",\"comment\":\"Alert test purpose\",\"uuid\":\"fb8dbbb3-e0d3-41eb-ab97-36b76fc75476\",\"userId\":\"666888\"}],\"assertions\":[\"COORDINATE_ROUNDED\",\"MISSING_GEOREFERENCE_DATE\",\"MISSING_GEOREFERENCEDBY\",\"MISSING_GEOREFERENCESOURCES\",\"MISSING_GEOREFERENCEVERIFICATIONSTATUS\",\"INDIVIDUAL_COUNT_INVALID\"],\"verified_assertions\":\"\",\"speciesGroups\":[\"Plants\",\"Flowering plants\",\"Dicots\"],\"spatiallyValid\":true,\"order\":\"Fabales\",\"taxonRankID\":7000,\"dataResourceName\":\"NSW BioNet Atlas\",\"raw_basisOfRecord\":\"HumanObservation\",\"stateProvince\":\"New South Wales\",\"decimalLongitude\":144.994102,\"occurrenceID\":\"urn:catalog:NSW Dept of Planning, Industry and Environment:BioNet Atlas of NSW Wildlife:SDFKI0142229\",\"raw_collectionCode\":\"BioNet Atlas of NSW Wildlife\",\"corrected_assertions\":\"\",\"license\":\"CC-BY 4.0 (Int)\",\"collectors\":[\"OMFZ2303270J\"],\"month\":\"09\",\"dataResourceUid\":\"dr368\",\"genus\":\"Acacia\",\"left\":588026,\"point0001\":\"-29.063,144.994\",\"eventDate\":1694217600000,\"coordinateUncertaintyInMeters\":10,\"taxonRank\":\"species\",\"point1\":\"-29,145\",\"collector\":[\"OMFZ2303270J\"],\"vernacularName\":\"Lancewood\",\"hasUserAssertions\":true,\"speciesGuid\":\"https://id.biodiversity.org.au/node/apni/2903503\",\"raw_occurrenceRemarks\":\"occurrenceRemarks withheld\",\"rights\":\"Unknown\",\"geospatialKosher\":\"true\",\"namesLsid\":\"Acacia petraea|https://id.biodiversity.org.au/node/apni/2903503|Lancewood|Plantae|Fabaceae\",\"point00001\":\"-29.063,144.9941\",\"raw_institutionCode\":\"NSW Dept of Planning, Industry and Environment\",\"point001\":\"-29.06,144.99\",\"right\":588026,\"raw_catalogNumber\":\"SDFKI0142229\",\"kingdom\":\"Plantae\",\"dataProviderUid\":\"dp34\",\"recordedBy\":[\"OMFZ2303270J\"],\"phylum\":\"Charophyta\",\"classs\":\"Equisetopsida\",\"species\":\"Acacia petraea\",\"dataProviderName\":\"Department of Planning, Industry and Environment representing the State of New South Wales\",\"stateConservation\":\"Endangered\",\"family\":\"Fabaceae\",\"genusGuid\":\"https://id.biodiversity.org.au/taxon/apni/51471290\",\"open_assertions\":\"\",\"userAssertions\":\"50005\"},{\"country\":\"Australia\",\"scientificName\":\"Acacia idiomorpha\",\"year\":1962,\"decimalLatitude\":-27.75,\"uuid\":\"539bd9f9-95bd-47b6-b50f-bf75089b9521\",\"point01\":\"-27.8,114.2\",\"basisOfRecord\":\"PRESERVED_SPECIMEN\",\"raw_scientificName\":\"Acacia idiomorpha Benth.\",\"taxonConceptID\":\"https://id.biodiversity.org.au/node/apni/2902734\",\"latLong\":\"-27.75,114.166667\",\"user_assertions\":[{\"qaStatus\":50005,\"problemAsserted\":false,\"code\":20019,\"dataResourceUid\":\"dr21662\",\"referenceRowKey\":\"539bd9f9-95bd-47b6-b50f-bf75089b9521\",\"created\":\"2023-11-14T04:43:35Z\",\"name\":\"userAssertionOther\",\"userDisplayName\":\"Luke Skywalker\",\"comment\":\"Alert test\",\"uuid\":\"340d3c4e-4979-45a3-934d-77f7cc1e9b53\",\"userId\":\"666888\"}],\"assertions\":[\"MISSING_TAXONRANK\",\"GEODETIC_DATUM_ASSUMED_WGS84\",\"MISSING_GEODETICDATUM\",\"COORDINATE_UNCERTAINTY_METERS_INVALID\",\"MISSING_GEOREFERENCE_DATE\",\"MISSING_GEOREFERENCEDBY\",\"MISSING_GEOREFERENCEPROTOCOL\",\"MISSING_GEOREFERENCESOURCES\",\"MISSING_GEOREFERENCEVERIFICATIONSTATUS\"],\"verified_assertions\":[ {\"uuid\":\"340d3c4e-4979-45a3-934d-77f7cc1e9b53\"}],\"speciesGroups\":[\"Plants\",\"Flowering plants\",\"Dicots\"],\"spatiallyValid\":true,\"order\":\"Fabales\",\"taxonRankID\":7000,\"dataResourceName\":\"Kings Park Botanical Gardens\",\"raw_basisOfRecord\":\"Preserved Specimen\",\"stateProvince\":\"Western Australia\",\"decimalLongitude\":114.166667,\"raw_collectionCode\":\"KPBG\",\"corrected_assertions\":\"\",\"license\":\"CC-BY 4.0 (Int)\",\"month\":\"09\",\"dataResourceUid\":\"dr21662\",\"genus\":\"Acacia\",\"left\":587962,\"point0001\":\"-27.75,114.167\",\"eventDate\":-229219200000,\"taxonRank\":\"species\",\"point1\":\"-28,114\",\"collectionName\":\"Kings Park and Botanic Garden\",\"hasUserAssertions\":true,\"speciesGuid\":\"https://id.biodiversity.org.au/node/apni/2902734\",\"geospatialKosher\":\"true\",\"namesLsid\":\"Acacia idiomorpha|https://id.biodiversity.org.au/node/apni/2902734||Plantae|Fabaceae\",\"collectionUid\":\"co32\",\"point00001\":\"-27.75,114.1667\",\"raw_institutionCode\":\"BGPA\",\"point001\":\"-27.75,114.17\",\"right\":587962,\"raw_catalogNumber\":\"37888\",\"kingdom\":\"Plantae\",\"phylum\":\"Charophyta\",\"classs\":\"Equisetopsida\",\"institutionUid\":\"in58\",\"species\":\"Acacia idiomorpha\",\"institutionName\":\"WA Botanic Gardens and Parks Authority\",\"family\":\"Fabaceae\",\"genusGuid\":\"https://id.biodiversity.org.au/taxon/apni/51471290\",\"open_assertions\":\"\",\"userAssertions\":\"50002\"},{\"country\":\"Australia\",\"scientificName\":\"Acacia obovata\",\"year\":1974,\"decimalLatitude\":-32.712222,\"uuid\":\"f7d3a852-7382-4999-bc2e-6d2676a11294\",\"point01\":\"-32.7,115.9\",\"basisOfRecord\":\"PRESERVED_SPECIMEN\",\"raw_scientificName\":\"Acacia obovata Benth.\",\"taxonConceptID\":\"https://id.biodiversity.org.au/node/apni/2899185\",\"latLong\":\"-32.712222,115.928611\",\"user_assertions\":[{\"qaStatus\":50005,\"problemAsserted\":false,\"code\":20019,\"dataResourceUid\":\"dr21662\",\"referenceRowKey\":\"f7d3a852-7382-4999-bc2e-6d2676a11294\",\"created\":\"2023-11-14T05:34:18Z\",\"name\":\"userAssertionOther\",\"userDisplayName\":\"Luke Skywalker\",\"comment\":\"alert test\",\"uuid\":\"81447f7f-65b3-4bde-b95c-f507f5e0c67a\",\"userId\":\"666888\"}],\"assertions\":[\"MISSING_TAXONRANK\",\"GEODETIC_DATUM_ASSUMED_WGS84\",\"MISSING_GEODETICDATUM\",\"COORDINATE_UNCERTAINTY_METERS_INVALID\",\"MISSING_GEOREFERENCE_DATE\",\"MISSING_GEOREFERENCEDBY\",\"MISSING_GEOREFERENCEPROTOCOL\",\"MISSING_GEOREFERENCESOURCES\",\"MISSING_GEOREFERENCEVERIFICATIONSTATUS\"],\"verified_assertions\":\"\",\"speciesGroups\":[\"Plants\",\"Flowering plants\",\"Dicots\"],\"spatiallyValid\":true,\"order\":\"Fabales\",\"taxonRankID\":7000,\"dataResourceName\":\"Kings Park Botanical Gardens\",\"raw_basisOfRecord\":\"Preserved Specimen\",\"stateProvince\":\"Western Australia\",\"decimalLongitude\":115.928611,\"raw_collectionCode\":\"KPBG\",\"corrected_assertions\":\"\",\"license\":\"CC-BY 4.0 (Int)\",\"month\":\"06\",\"dataResourceUid\":\"dr21662\",\"genus\":\"Acacia\",\"left\":587708,\"point0001\":\"-32.712,115.929\",\"eventDate\":141264000000,\"taxonRank\":\"species\",\"point1\":\"-33,116\",\"collectionName\":\"Kings Park and Botanic Garden\",\"hasUserAssertions\":true,\"speciesGuid\":\"https://id.biodiversity.org.au/node/apni/2899185\",\"geospatialKosher\":\"true\",\"namesLsid\":\"Acacia obovata|https://id.biodiversity.org.au/node/apni/2899185||Plantae|Fabaceae\",\"collectionUid\":\"co32\",\"point00001\":\"-32.7122,115.9286\",\"raw_institutionCode\":\"BGPA\",\"point001\":\"-32.71,115.93\",\"right\":587708,\"raw_catalogNumber\":\"38215\",\"kingdom\":\"Plantae\",\"phylum\":\"Charophyta\",\"classs\":\"Equisetopsida\",\"institutionUid\":\"in58\",\"species\":\"Acacia obovata\",\"institutionName\":\"WA Botanic Gardens and Parks Authority\",\"family\":\"Fabaceae\",\"genusGuid\":\"https://id.biodiversity.org.au/taxon/apni/51471290\",\"open_assertions\":\"\",\"userAssertions\":\"50005\"}],\"totalRecords\":3,\"query\":\"?q=*%3A*&fq=assertion_user_id%3A666888\",\"pageSize\":300,\"queryTitle\":\"[all records]\",\"sort\":\"score\",\"dir\":\"desc\",\"startIndex\":0,\"activeFacetMap\":{\"assertion_user_id\":{\"displayName\":\"Assertions by user:Luke Skywalker\",\"name\":\"assertion_user_id\",\"value\":\"666888\"}},\"activeFacetObj\":{\"assertion_user_id\":[{\"displayName\":\"Assertions by user:Luke Skywalker\",\"name\":\"assertion_user_id\",\"value\":\"assertion_user_id:666888\"}]},\"urlParameters\":\"?q=*%3A*&fq=assertion_user_id%3A666888\",\"facetResults\":[],\"status\":\"OK\"}"
        def jsonPath = "\$.occurrences[*]"
        when:
        def result = assertionService.diff(previous, current, jsonPath)

        then:
        result.size() == 3
    }
}
