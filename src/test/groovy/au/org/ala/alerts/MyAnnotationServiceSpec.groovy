package au.org.ala.alerts

import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

class MyAnnotationServiceSpec extends Specification implements ServiceUnitTest<MyAnnotationService> {

    def assertionService

    def setup() {
        assertionService = new MyAnnotationService()
    }

    def cleanup() {
    }
    /**
     * Compare 3 current records which only one has verified_assertions with empty previous records
     */
    void "diff check against empty previous records"() {
        given:
        def previous = "{}"
        def current = "{\"occurrences\":[{\"country\":\"Australia\",\"raw_countryCode\":\"AU\",\"scientificName\":\"Acacia petraea\",\"year\":2023,\"decimalLatitude\":-29.062978,\"uuid\":\"0e6de91f-d30c-4e52-b7d5-678e5527d0b5\",\"point01\":\"-29.1,145\",\"raw_locationRemarks\":\"locationRemarks withheld\",\"basisOfRecord\":\"HUMAN_OBSERVATION\",\"raw_scientificName\":\"Acacia petraea\",\"raw_vernacularName\":\"Lancewood\",\"taxonConceptID\":\"https://id.biodiversity.org.au/node/apni/2903503\",\"latLong\":\"-29.062978,144.994102\",\"user_assertions\":[{\"qaStatus\":50005,\"problemAsserted\":false,\"code\":20019,\"dataResourceUid\":\"dr368\",\"referenceRowKey\":\"0e6de91f-d30c-4e52-b7d5-678e5527d0b5\",\"created\":\"2024-10-14T22:41:33Z\",\"name\":\"userAssertionOther\",\"userDisplayName\":\"Luke Skywalker\",\"comment\":\"Alert test purpose\",\"uuid\":\"fb8dbbb3-e0d3-41eb-ab97-36b76fc75476\",\"userId\":\"666888\"}],\"assertions\":[\"COORDINATE_ROUNDED\",\"MISSING_GEOREFERENCE_DATE\",\"MISSING_GEOREFERENCEDBY\",\"MISSING_GEOREFERENCESOURCES\",\"MISSING_GEOREFERENCEVERIFICATIONSTATUS\",\"INDIVIDUAL_COUNT_INVALID\"],\"verified_assertions\":\"\",\"speciesGroups\":[\"Plants\",\"Flowering plants\",\"Dicots\"],\"spatiallyValid\":true,\"order\":\"Fabales\",\"taxonRankID\":7000,\"dataResourceName\":\"NSW BioNet Atlas\",\"raw_basisOfRecord\":\"HumanObservation\",\"stateProvince\":\"New South Wales\",\"decimalLongitude\":144.994102,\"occurrenceID\":\"urn:catalog:NSW Dept of Planning, Industry and Environment:BioNet Atlas of NSW Wildlife:SDFKI0142229\",\"raw_collectionCode\":\"BioNet Atlas of NSW Wildlife\",\"corrected_assertions\":\"\",\"license\":\"CC-BY 4.0 (Int)\",\"collectors\":[\"OMFZ2303270J\"],\"month\":\"09\",\"dataResourceUid\":\"dr368\",\"genus\":\"Acacia\",\"left\":588026,\"point0001\":\"-29.063,144.994\",\"eventDate\":1694217600000,\"coordinateUncertaintyInMeters\":10,\"taxonRank\":\"species\",\"point1\":\"-29,145\",\"collector\":[\"OMFZ2303270J\"],\"vernacularName\":\"Lancewood\",\"hasUserAssertions\":true,\"speciesGuid\":\"https://id.biodiversity.org.au/node/apni/2903503\",\"raw_occurrenceRemarks\":\"occurrenceRemarks withheld\",\"rights\":\"Unknown\",\"geospatialKosher\":\"true\",\"namesLsid\":\"Acacia petraea|https://id.biodiversity.org.au/node/apni/2903503|Lancewood|Plantae|Fabaceae\",\"point00001\":\"-29.063,144.9941\",\"raw_institutionCode\":\"NSW Dept of Planning, Industry and Environment\",\"point001\":\"-29.06,144.99\",\"right\":588026,\"raw_catalogNumber\":\"SDFKI0142229\",\"kingdom\":\"Plantae\",\"dataProviderUid\":\"dp34\",\"recordedBy\":[\"OMFZ2303270J\"],\"phylum\":\"Charophyta\",\"classs\":\"Equisetopsida\",\"species\":\"Acacia petraea\",\"dataProviderName\":\"Department of Planning, Industry and Environment representing the State of New South Wales\",\"stateConservation\":\"Endangered\",\"family\":\"Fabaceae\",\"genusGuid\":\"https://id.biodiversity.org.au/taxon/apni/51471290\",\"open_assertions\":\"\",\"userAssertions\":\"50005\"},{\"country\":\"Australia\",\"scientificName\":\"Acacia idiomorpha\",\"year\":1962,\"decimalLatitude\":-27.75,\"uuid\":\"539bd9f9-95bd-47b6-b50f-bf75089b9521\",\"point01\":\"-27.8,114.2\",\"basisOfRecord\":\"PRESERVED_SPECIMEN\",\"raw_scientificName\":\"Acacia idiomorpha Benth.\",\"taxonConceptID\":\"https://id.biodiversity.org.au/node/apni/2902734\",\"latLong\":\"-27.75,114.166667\",\"user_assertions\":[{\"qaStatus\":50005,\"problemAsserted\":false,\"code\":20019,\"dataResourceUid\":\"dr21662\",\"referenceRowKey\":\"539bd9f9-95bd-47b6-b50f-bf75089b9521\",\"created\":\"2023-11-14T04:43:35Z\",\"name\":\"userAssertionOther\",\"userDisplayName\":\"Luke Skywalker\",\"comment\":\"Alert test\",\"uuid\":\"340d3c4e-4979-45a3-934d-77f7cc1e9b53\",\"userId\":\"666888\"}],\"assertions\":[\"MISSING_TAXONRANK\",\"GEODETIC_DATUM_ASSUMED_WGS84\",\"MISSING_GEODETICDATUM\",\"COORDINATE_UNCERTAINTY_METERS_INVALID\",\"MISSING_GEOREFERENCE_DATE\",\"MISSING_GEOREFERENCEDBY\",\"MISSING_GEOREFERENCEPROTOCOL\",\"MISSING_GEOREFERENCESOURCES\",\"MISSING_GEOREFERENCEVERIFICATIONSTATUS\"],\"verified_assertions\":[ {\"uuid\":\"340d3c4e-4979-45a3-934d-77f7cc1e9b53\"}],\"speciesGroups\":[\"Plants\",\"Flowering plants\",\"Dicots\"],\"spatiallyValid\":true,\"order\":\"Fabales\",\"taxonRankID\":7000,\"dataResourceName\":\"Kings Park Botanical Gardens\",\"raw_basisOfRecord\":\"Preserved Specimen\",\"stateProvince\":\"Western Australia\",\"decimalLongitude\":114.166667,\"raw_collectionCode\":\"KPBG\",\"corrected_assertions\":\"\",\"license\":\"CC-BY 4.0 (Int)\",\"month\":\"09\",\"dataResourceUid\":\"dr21662\",\"genus\":\"Acacia\",\"left\":587962,\"point0001\":\"-27.75,114.167\",\"eventDate\":-229219200000,\"taxonRank\":\"species\",\"point1\":\"-28,114\",\"collectionName\":\"Kings Park and Botanic Garden\",\"hasUserAssertions\":true,\"speciesGuid\":\"https://id.biodiversity.org.au/node/apni/2902734\",\"geospatialKosher\":\"true\",\"namesLsid\":\"Acacia idiomorpha|https://id.biodiversity.org.au/node/apni/2902734||Plantae|Fabaceae\",\"collectionUid\":\"co32\",\"point00001\":\"-27.75,114.1667\",\"raw_institutionCode\":\"BGPA\",\"point001\":\"-27.75,114.17\",\"right\":587962,\"raw_catalogNumber\":\"37888\",\"kingdom\":\"Plantae\",\"phylum\":\"Charophyta\",\"classs\":\"Equisetopsida\",\"institutionUid\":\"in58\",\"species\":\"Acacia idiomorpha\",\"institutionName\":\"WA Botanic Gardens and Parks Authority\",\"family\":\"Fabaceae\",\"genusGuid\":\"https://id.biodiversity.org.au/taxon/apni/51471290\",\"open_assertions\":\"\",\"userAssertions\":\"50002\"},{\"country\":\"Australia\",\"scientificName\":\"Acacia obovata\",\"year\":1974,\"decimalLatitude\":-32.712222,\"uuid\":\"f7d3a852-7382-4999-bc2e-6d2676a11294\",\"point01\":\"-32.7,115.9\",\"basisOfRecord\":\"PRESERVED_SPECIMEN\",\"raw_scientificName\":\"Acacia obovata Benth.\",\"taxonConceptID\":\"https://id.biodiversity.org.au/node/apni/2899185\",\"latLong\":\"-32.712222,115.928611\",\"user_assertions\":[{\"qaStatus\":50005,\"problemAsserted\":false,\"code\":20019,\"dataResourceUid\":\"dr21662\",\"referenceRowKey\":\"f7d3a852-7382-4999-bc2e-6d2676a11294\",\"created\":\"2023-11-14T05:34:18Z\",\"name\":\"userAssertionOther\",\"userDisplayName\":\"Luke Skywalker\",\"comment\":\"alert test\",\"uuid\":\"81447f7f-65b3-4bde-b95c-f507f5e0c67a\",\"userId\":\"666888\"}],\"assertions\":[\"MISSING_TAXONRANK\",\"GEODETIC_DATUM_ASSUMED_WGS84\",\"MISSING_GEODETICDATUM\",\"COORDINATE_UNCERTAINTY_METERS_INVALID\",\"MISSING_GEOREFERENCE_DATE\",\"MISSING_GEOREFERENCEDBY\",\"MISSING_GEOREFERENCEPROTOCOL\",\"MISSING_GEOREFERENCESOURCES\",\"MISSING_GEOREFERENCEVERIFICATIONSTATUS\"],\"verified_assertions\":\"\",\"speciesGroups\":[\"Plants\",\"Flowering plants\",\"Dicots\"],\"spatiallyValid\":true,\"order\":\"Fabales\",\"taxonRankID\":7000,\"dataResourceName\":\"Kings Park Botanical Gardens\",\"raw_basisOfRecord\":\"Preserved Specimen\",\"stateProvince\":\"Western Australia\",\"decimalLongitude\":115.928611,\"raw_collectionCode\":\"KPBG\",\"corrected_assertions\":\"\",\"license\":\"CC-BY 4.0 (Int)\",\"month\":\"06\",\"dataResourceUid\":\"dr21662\",\"genus\":\"Acacia\",\"left\":587708,\"point0001\":\"-32.712,115.929\",\"eventDate\":141264000000,\"taxonRank\":\"species\",\"point1\":\"-33,116\",\"collectionName\":\"Kings Park and Botanic Garden\",\"hasUserAssertions\":true,\"speciesGuid\":\"https://id.biodiversity.org.au/node/apni/2899185\",\"geospatialKosher\":\"true\",\"namesLsid\":\"Acacia obovata|https://id.biodiversity.org.au/node/apni/2899185||Plantae|Fabaceae\",\"collectionUid\":\"co32\",\"point00001\":\"-32.7122,115.9286\",\"raw_institutionCode\":\"BGPA\",\"point001\":\"-32.71,115.93\",\"right\":587708,\"raw_catalogNumber\":\"38215\",\"kingdom\":\"Plantae\",\"phylum\":\"Charophyta\",\"classs\":\"Equisetopsida\",\"institutionUid\":\"in58\",\"species\":\"Acacia obovata\",\"institutionName\":\"WA Botanic Gardens and Parks Authority\",\"family\":\"Fabaceae\",\"genusGuid\":\"https://id.biodiversity.org.au/taxon/apni/51471290\",\"open_assertions\":\"\",\"userAssertions\":\"50005\"}],\"totalRecords\":3,\"query\":\"?q=*%3A*&fq=assertion_user_id%3A666888\",\"pageSize\":300,\"queryTitle\":\"[all records]\",\"sort\":\"score\",\"dir\":\"desc\",\"startIndex\":0,\"activeFacetMap\":{\"assertion_user_id\":{\"displayName\":\"Assertions by user:Luke Skywalker\",\"name\":\"assertion_user_id\",\"value\":\"666888\"}},\"activeFacetObj\":{\"assertion_user_id\":[{\"displayName\":\"Assertions by user:Luke Skywalker\",\"name\":\"assertion_user_id\",\"value\":\"assertion_user_id:666888\"}]},\"urlParameters\":\"?q=*%3A*&fq=assertion_user_id%3A666888\",\"facetResults\":[],\"status\":\"OK\"}"
        def jsonPath = "\$.occurrences[*]"
        when:
        def result = assertionService.diff(previous, current, jsonPath)

        then:
        result.size() == 1
        result[0].verified_assertions.size() == 1
        result[0].verified_assertions[0].uuid == "340d3c4e-4979-45a3-934d-77f7cc1e9b53"
    }

    void "diff check against one previous records which has valid verified_assertions"() {
        given:
        def previous = "{ " +
                "  \"occurrences\": [ " +
                "    { " +
                "      \"uuid\": \"539bd9f9-95bd-47b6-b50f-bf75089b9521\", " +
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
                "      \"verified_assertions\": [{\"uuid\":\"VALID_ASSERTION_ID\"}], " +
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
        def current = "{\"occurrences\":[{\"country\":\"Australia\",\"raw_countryCode\":\"AU\",\"scientificName\":\"Acacia petraea\",\"year\":2023,\"decimalLatitude\":-29.062978,\"uuid\":\"0e6de91f-d30c-4e52-b7d5-678e5527d0b5\",\"point01\":\"-29.1,145\",\"raw_locationRemarks\":\"locationRemarks withheld\",\"basisOfRecord\":\"HUMAN_OBSERVATION\",\"raw_scientificName\":\"Acacia petraea\",\"raw_vernacularName\":\"Lancewood\",\"taxonConceptID\":\"https://id.biodiversity.org.au/node/apni/2903503\",\"latLong\":\"-29.062978,144.994102\",\"user_assertions\":[{\"qaStatus\":50005,\"problemAsserted\":false,\"code\":20019,\"dataResourceUid\":\"dr368\",\"referenceRowKey\":\"0e6de91f-d30c-4e52-b7d5-678e5527d0b5\",\"created\":\"2024-10-14T22:41:33Z\",\"name\":\"userAssertionOther\",\"userDisplayName\":\"Luke Skywalker\",\"comment\":\"Alert test purpose\",\"uuid\":\"fb8dbbb3-e0d3-41eb-ab97-36b76fc75476\",\"userId\":\"666888\"}],\"assertions\":[\"COORDINATE_ROUNDED\",\"MISSING_GEOREFERENCE_DATE\",\"MISSING_GEOREFERENCEDBY\",\"MISSING_GEOREFERENCESOURCES\",\"MISSING_GEOREFERENCEVERIFICATIONSTATUS\",\"INDIVIDUAL_COUNT_INVALID\"],\"verified_assertions\":\"\",\"speciesGroups\":[\"Plants\",\"Flowering plants\",\"Dicots\"],\"spatiallyValid\":true,\"order\":\"Fabales\",\"taxonRankID\":7000,\"dataResourceName\":\"NSW BioNet Atlas\",\"raw_basisOfRecord\":\"HumanObservation\",\"stateProvince\":\"New South Wales\",\"decimalLongitude\":144.994102,\"occurrenceID\":\"urn:catalog:NSW Dept of Planning, Industry and Environment:BioNet Atlas of NSW Wildlife:SDFKI0142229\",\"raw_collectionCode\":\"BioNet Atlas of NSW Wildlife\",\"corrected_assertions\":\"\",\"license\":\"CC-BY 4.0 (Int)\",\"collectors\":[\"OMFZ2303270J\"],\"month\":\"09\",\"dataResourceUid\":\"dr368\",\"genus\":\"Acacia\",\"left\":588026,\"point0001\":\"-29.063,144.994\",\"eventDate\":1694217600000,\"coordinateUncertaintyInMeters\":10,\"taxonRank\":\"species\",\"point1\":\"-29,145\",\"collector\":[\"OMFZ2303270J\"],\"vernacularName\":\"Lancewood\",\"hasUserAssertions\":true,\"speciesGuid\":\"https://id.biodiversity.org.au/node/apni/2903503\",\"raw_occurrenceRemarks\":\"occurrenceRemarks withheld\",\"rights\":\"Unknown\",\"geospatialKosher\":\"true\",\"namesLsid\":\"Acacia petraea|https://id.biodiversity.org.au/node/apni/2903503|Lancewood|Plantae|Fabaceae\",\"point00001\":\"-29.063,144.9941\",\"raw_institutionCode\":\"NSW Dept of Planning, Industry and Environment\",\"point001\":\"-29.06,144.99\",\"right\":588026,\"raw_catalogNumber\":\"SDFKI0142229\",\"kingdom\":\"Plantae\",\"dataProviderUid\":\"dp34\",\"recordedBy\":[\"OMFZ2303270J\"],\"phylum\":\"Charophyta\",\"classs\":\"Equisetopsida\",\"species\":\"Acacia petraea\",\"dataProviderName\":\"Department of Planning, Industry and Environment representing the State of New South Wales\",\"stateConservation\":\"Endangered\",\"family\":\"Fabaceae\",\"genusGuid\":\"https://id.biodiversity.org.au/taxon/apni/51471290\",\"open_assertions\":\"\",\"userAssertions\":\"50005\"},{\"country\":\"Australia\",\"scientificName\":\"Acacia idiomorpha\",\"year\":1962,\"decimalLatitude\":-27.75,\"uuid\":\"539bd9f9-95bd-47b6-b50f-bf75089b9521\",\"point01\":\"-27.8,114.2\",\"basisOfRecord\":\"PRESERVED_SPECIMEN\",\"raw_scientificName\":\"Acacia idiomorpha Benth.\",\"taxonConceptID\":\"https://id.biodiversity.org.au/node/apni/2902734\",\"latLong\":\"-27.75,114.166667\",\"user_assertions\":[{\"qaStatus\":50005,\"problemAsserted\":false,\"code\":20019,\"dataResourceUid\":\"dr21662\",\"referenceRowKey\":\"539bd9f9-95bd-47b6-b50f-bf75089b9521\",\"created\":\"2023-11-14T04:43:35Z\",\"name\":\"userAssertionOther\",\"userDisplayName\":\"Luke Skywalker\",\"comment\":\"Alert test\",\"uuid\":\"340d3c4e-4979-45a3-934d-77f7cc1e9b53\",\"userId\":\"666888\"}],\"assertions\":[\"MISSING_TAXONRANK\",\"GEODETIC_DATUM_ASSUMED_WGS84\",\"MISSING_GEODETICDATUM\",\"COORDINATE_UNCERTAINTY_METERS_INVALID\",\"MISSING_GEOREFERENCE_DATE\",\"MISSING_GEOREFERENCEDBY\",\"MISSING_GEOREFERENCEPROTOCOL\",\"MISSING_GEOREFERENCESOURCES\",\"MISSING_GEOREFERENCEVERIFICATIONSTATUS\"],\"verified_assertions\":[ {\"uuid\":\"340d3c4e-4979-45a3-934d-77f7cc1e9b53\",\"created\": \"2024-11-27T05:54:51Z\"}],\"speciesGroups\":[\"Plants\",\"Flowering plants\",\"Dicots\"],\"spatiallyValid\":true,\"order\":\"Fabales\",\"taxonRankID\":7000,\"dataResourceName\":\"Kings Park Botanical Gardens\",\"raw_basisOfRecord\":\"Preserved Specimen\",\"stateProvince\":\"Western Australia\",\"decimalLongitude\":114.166667,\"raw_collectionCode\":\"KPBG\",\"corrected_assertions\":\"\",\"license\":\"CC-BY 4.0 (Int)\",\"month\":\"09\",\"dataResourceUid\":\"dr21662\",\"genus\":\"Acacia\",\"left\":587962,\"point0001\":\"-27.75,114.167\",\"eventDate\":-229219200000,\"taxonRank\":\"species\",\"point1\":\"-28,114\",\"collectionName\":\"Kings Park and Botanic Garden\",\"hasUserAssertions\":true,\"speciesGuid\":\"https://id.biodiversity.org.au/node/apni/2902734\",\"geospatialKosher\":\"true\",\"namesLsid\":\"Acacia idiomorpha|https://id.biodiversity.org.au/node/apni/2902734||Plantae|Fabaceae\",\"collectionUid\":\"co32\",\"point00001\":\"-27.75,114.1667\",\"raw_institutionCode\":\"BGPA\",\"point001\":\"-27.75,114.17\",\"right\":587962,\"raw_catalogNumber\":\"37888\",\"kingdom\":\"Plantae\",\"phylum\":\"Charophyta\",\"classs\":\"Equisetopsida\",\"institutionUid\":\"in58\",\"species\":\"Acacia idiomorpha\",\"institutionName\":\"WA Botanic Gardens and Parks Authority\",\"family\":\"Fabaceae\",\"genusGuid\":\"https://id.biodiversity.org.au/taxon/apni/51471290\",\"open_assertions\":\"\",\"userAssertions\":\"50002\"},{\"country\":\"Australia\",\"scientificName\":\"Acacia obovata\",\"year\":1974,\"decimalLatitude\":-32.712222,\"uuid\":\"f7d3a852-7382-4999-bc2e-6d2676a11294\",\"point01\":\"-32.7,115.9\",\"basisOfRecord\":\"PRESERVED_SPECIMEN\",\"raw_scientificName\":\"Acacia obovata Benth.\",\"taxonConceptID\":\"https://id.biodiversity.org.au/node/apni/2899185\",\"latLong\":\"-32.712222,115.928611\",\"user_assertions\":[{\"qaStatus\":50005,\"problemAsserted\":false,\"code\":20019,\"dataResourceUid\":\"dr21662\",\"referenceRowKey\":\"f7d3a852-7382-4999-bc2e-6d2676a11294\",\"created\":\"2023-11-14T05:34:18Z\",\"name\":\"userAssertionOther\",\"userDisplayName\":\"Luke Skywalker\",\"comment\":\"alert test\",\"uuid\":\"81447f7f-65b3-4bde-b95c-f507f5e0c67a\",\"userId\":\"666888\"}],\"assertions\":[\"MISSING_TAXONRANK\",\"GEODETIC_DATUM_ASSUMED_WGS84\",\"MISSING_GEODETICDATUM\",\"COORDINATE_UNCERTAINTY_METERS_INVALID\",\"MISSING_GEOREFERENCE_DATE\",\"MISSING_GEOREFERENCEDBY\",\"MISSING_GEOREFERENCEPROTOCOL\",\"MISSING_GEOREFERENCESOURCES\",\"MISSING_GEOREFERENCEVERIFICATIONSTATUS\"],\"verified_assertions\":\"\",\"speciesGroups\":[\"Plants\",\"Flowering plants\",\"Dicots\"],\"spatiallyValid\":true,\"order\":\"Fabales\",\"taxonRankID\":7000,\"dataResourceName\":\"Kings Park Botanical Gardens\",\"raw_basisOfRecord\":\"Preserved Specimen\",\"stateProvince\":\"Western Australia\",\"decimalLongitude\":115.928611,\"raw_collectionCode\":\"KPBG\",\"corrected_assertions\":\"\",\"license\":\"CC-BY 4.0 (Int)\",\"month\":\"06\",\"dataResourceUid\":\"dr21662\",\"genus\":\"Acacia\",\"left\":587708,\"point0001\":\"-32.712,115.929\",\"eventDate\":141264000000,\"taxonRank\":\"species\",\"point1\":\"-33,116\",\"collectionName\":\"Kings Park and Botanic Garden\",\"hasUserAssertions\":true,\"speciesGuid\":\"https://id.biodiversity.org.au/node/apni/2899185\",\"geospatialKosher\":\"true\",\"namesLsid\":\"Acacia obovata|https://id.biodiversity.org.au/node/apni/2899185||Plantae|Fabaceae\",\"collectionUid\":\"co32\",\"point00001\":\"-32.7122,115.9286\",\"raw_institutionCode\":\"BGPA\",\"point001\":\"-32.71,115.93\",\"right\":587708,\"raw_catalogNumber\":\"38215\",\"kingdom\":\"Plantae\",\"phylum\":\"Charophyta\",\"classs\":\"Equisetopsida\",\"institutionUid\":\"in58\",\"species\":\"Acacia obovata\",\"institutionName\":\"WA Botanic Gardens and Parks Authority\",\"family\":\"Fabaceae\",\"genusGuid\":\"https://id.biodiversity.org.au/taxon/apni/51471290\",\"open_assertions\":\"\",\"userAssertions\":\"50005\"}],\"totalRecords\":3,\"query\":\"?q=*%3A*&fq=assertion_user_id%3A666888\",\"pageSize\":300,\"queryTitle\":\"[all records]\",\"sort\":\"score\",\"dir\":\"desc\",\"startIndex\":0,\"activeFacetMap\":{\"assertion_user_id\":{\"displayName\":\"Assertions by user:Luke Skywalker\",\"name\":\"assertion_user_id\",\"value\":\"666888\"}},\"activeFacetObj\":{\"assertion_user_id\":[{\"displayName\":\"Assertions by user:Luke Skywalker\",\"name\":\"assertion_user_id\",\"value\":\"assertion_user_id:666888\"}]},\"urlParameters\":\"?q=*%3A*&fq=assertion_user_id%3A666888\",\"facetResults\":[],\"status\":\"OK\"}"
        def jsonPath = "\$.occurrences[*]"
        when:
        def result = assertionService.diff(previous, current, jsonPath)

        then:
        result.size() == 1
        result[0].verified_assertions.size() == 1
        result[0].verified_assertions[0].uuid == "340d3c4e-4979-45a3-934d-77f7cc1e9b53"
    }

    void "diff check against existing records with empty verified_assertions"() {
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
                "      \"verified_assertions\": [], " +
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
        result.size() == 1
        result.processed_assertions.size() == 1
    }

    /**
     * Compatible check
     * Compare 3 current records which only one has verified_assertions with one previous records which has "verified_assertions='' " - empty string, not empty array
     * Compare 1 current records which has verified_assertions = "STRING" - Legal value for verified_assertions . This records should be discarded
     */
    void "diff check against one existing records with verified_assertions='' "() {
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
        result.size() == 1
    }

    /**
     * Compatible check
     * Compare 3 current records which only one has verified_assertions with one previous records without "verified_assertions field
     * */
    void "diff check against existing records without verified_assertions"() {
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
        result.size() == 1
    }

    void "diff check against existing records with empty verified_assertions"() {
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
                "      \"verified_assertions\": [], " +
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
        def current = "{\"occurrences\":[{\"country\":\"Australia\",\"raw_countryCode\":\"AU\",\"scientificName\":\"Acacia petraea\",\"year\":2023,\"decimalLatitude\":-29.062978,\"uuid\":\"0e6de91f-d30c-4e52-b7d5-678e5527d0b5\",\"point01\":\"-29.1,145\",\"raw_locationRemarks\":\"locationRemarks withheld\",\"basisOfRecord\":\"HUMAN_OBSERVATION\",\"raw_scientificName\":\"Acacia petraea\",\"raw_vernacularName\":\"Lancewood\",\"taxonConceptID\":\"https://id.biodiversity.org.au/node/apni/2903503\",\"latLong\":\"-29.062978,144.994102\",\"user_assertions\":[{\"qaStatus\":50005,\"problemAsserted\":false,\"code\":20019,\"dataResourceUid\":\"dr368\",\"referenceRowKey\":\"0e6de91f-d30c-4e52-b7d5-678e5527d0b5\",\"created\":\"2024-10-14T22:41:33Z\",\"name\":\"userAssertionOther\",\"userDisplayName\":\"Luke Skywalker\",\"comment\":\"Alert test purpose\",\"uuid\":\"fb8dbbb3-e0d3-41eb-ab97-36b76fc75476\",\"userId\":\"666888\"}],\"assertions\":[\"COORDINATE_ROUNDED\",\"MISSING_GEOREFERENCE_DATE\",\"MISSING_GEOREFERENCEDBY\",\"MISSING_GEOREFERENCESOURCES\",\"MISSING_GEOREFERENCEVERIFICATIONSTATUS\",\"INDIVIDUAL_COUNT_INVALID\"],\"verified_assertions\":\"\",\"speciesGroups\":[\"Plants\",\"Flowering plants\",\"Dicots\"],\"spatiallyValid\":true,\"order\":\"Fabales\",\"taxonRankID\":7000,\"dataResourceName\":\"NSW BioNet Atlas\",\"raw_basisOfRecord\":\"HumanObservation\",\"stateProvince\":\"New South Wales\",\"decimalLongitude\":144.994102,\"occurrenceID\":\"urn:catalog:NSW Dept of Planning, Industry and Environment:BioNet Atlas of NSW Wildlife:SDFKI0142229\",\"raw_collectionCode\":\"BioNet Atlas of NSW Wildlife\",\"corrected_assertions\":\"\",\"license\":\"CC-BY 4.0 (Int)\",\"collectors\":[\"OMFZ2303270J\"],\"month\":\"09\",\"dataResourceUid\":\"dr368\",\"genus\":\"Acacia\",\"left\":588026,\"point0001\":\"-29.063,144.994\",\"eventDate\":1694217600000,\"coordinateUncertaintyInMeters\":10,\"taxonRank\":\"species\",\"point1\":\"-29,145\",\"collector\":[\"OMFZ2303270J\"],\"vernacularName\":\"Lancewood\",\"hasUserAssertions\":true,\"speciesGuid\":\"https://id.biodiversity.org.au/node/apni/2903503\",\"raw_occurrenceRemarks\":\"occurrenceRemarks withheld\",\"rights\":\"Unknown\",\"geospatialKosher\":\"true\",\"namesLsid\":\"Acacia petraea|https://id.biodiversity.org.au/node/apni/2903503|Lancewood|Plantae|Fabaceae\",\"point00001\":\"-29.063,144.9941\",\"raw_institutionCode\":\"NSW Dept of Planning, Industry and Environment\",\"point001\":\"-29.06,144.99\",\"right\":588026,\"raw_catalogNumber\":\"SDFKI0142229\",\"kingdom\":\"Plantae\",\"dataProviderUid\":\"dp34\",\"recordedBy\":[\"OMFZ2303270J\"],\"phylum\":\"Charophyta\",\"classs\":\"Equisetopsida\",\"species\":\"Acacia petraea\",\"dataProviderName\":\"Department of Planning, Industry and Environment representing the State of New South Wales\",\"stateConservation\":\"Endangered\",\"family\":\"Fabaceae\",\"genusGuid\":\"https://id.biodiversity.org.au/taxon/apni/51471290\",\"open_assertions\":\"\",\"userAssertions\":\"50005\"},{\"country\":\"Australia\",\"scientificName\":\"Acacia idiomorpha\",\"year\":1962,\"decimalLatitude\":-27.75,\"uuid\":\"539bd9f9-95bd-47b6-b50f-bf75089b9521\",\"point01\":\"-27.8,114.2\",\"basisOfRecord\":\"PRESERVED_SPECIMEN\",\"raw_scientificName\":\"Acacia idiomorpha Benth.\",\"taxonConceptID\":\"https://id.biodiversity.org.au/node/apni/2902734\",\"latLong\":\"-27.75,114.166667\",\"user_assertions\":[{\"qaStatus\":50005,\"problemAsserted\":false,\"code\":20019,\"dataResourceUid\":\"dr21662\",\"referenceRowKey\":\"539bd9f9-95bd-47b6-b50f-bf75089b9521\",\"created\":\"2023-11-14T04:43:35Z\",\"name\":\"userAssertionOther\",\"userDisplayName\":\"Luke Skywalker\",\"comment\":\"Alert test\",\"uuid\":\"340d3c4e-4979-45a3-934d-77f7cc1e9b53\",\"userId\":\"666888\"}],\"assertions\":[\"MISSING_TAXONRANK\",\"GEODETIC_DATUM_ASSUMED_WGS84\",\"MISSING_GEODETICDATUM\",\"COORDINATE_UNCERTAINTY_METERS_INVALID\",\"MISSING_GEOREFERENCE_DATE\",\"MISSING_GEOREFERENCEDBY\",\"MISSING_GEOREFERENCEPROTOCOL\",\"MISSING_GEOREFERENCESOURCES\",\"MISSING_GEOREFERENCEVERIFICATIONSTATUS\"],\"verified_assertions\":[\"340d3c4e-4979-45a3-934d-77f7cc1e9b53\"],\"speciesGroups\":[\"Plants\",\"Flowering plants\",\"Dicots\"],\"spatiallyValid\":true,\"order\":\"Fabales\",\"taxonRankID\":7000,\"dataResourceName\":\"Kings Park Botanical Gardens\",\"raw_basisOfRecord\":\"Preserved Specimen\",\"stateProvince\":\"Western Australia\",\"decimalLongitude\":114.166667,\"raw_collectionCode\":\"KPBG\",\"corrected_assertions\":\"\",\"license\":\"CC-BY 4.0 (Int)\",\"month\":\"09\",\"dataResourceUid\":\"dr21662\",\"genus\":\"Acacia\",\"left\":587962,\"point0001\":\"-27.75,114.167\",\"eventDate\":-229219200000,\"taxonRank\":\"species\",\"point1\":\"-28,114\",\"collectionName\":\"Kings Park and Botanic Garden\",\"hasUserAssertions\":true,\"speciesGuid\":\"https://id.biodiversity.org.au/node/apni/2902734\",\"geospatialKosher\":\"true\",\"namesLsid\":\"Acacia idiomorpha|https://id.biodiversity.org.au/node/apni/2902734||Plantae|Fabaceae\",\"collectionUid\":\"co32\",\"point00001\":\"-27.75,114.1667\",\"raw_institutionCode\":\"BGPA\",\"point001\":\"-27.75,114.17\",\"right\":587962,\"raw_catalogNumber\":\"37888\",\"kingdom\":\"Plantae\",\"phylum\":\"Charophyta\",\"classs\":\"Equisetopsida\",\"institutionUid\":\"in58\",\"species\":\"Acacia idiomorpha\",\"institutionName\":\"WA Botanic Gardens and Parks Authority\",\"family\":\"Fabaceae\",\"genusGuid\":\"https://id.biodiversity.org.au/taxon/apni/51471290\",\"open_assertions\":\"\",\"userAssertions\":\"50002\"},{\"country\":\"Australia\",\"scientificName\":\"Acacia obovata\",\"year\":1974,\"decimalLatitude\":-32.712222,\"uuid\":\"f7d3a852-7382-4999-bc2e-6d2676a11294\",\"point01\":\"-32.7,115.9\",\"basisOfRecord\":\"PRESERVED_SPECIMEN\",\"raw_scientificName\":\"Acacia obovata Benth.\",\"taxonConceptID\":\"https://id.biodiversity.org.au/node/apni/2899185\",\"latLong\":\"-32.712222,115.928611\",\"user_assertions\":[{\"qaStatus\":50005,\"problemAsserted\":false,\"code\":20019,\"dataResourceUid\":\"dr21662\",\"referenceRowKey\":\"f7d3a852-7382-4999-bc2e-6d2676a11294\",\"created\":\"2023-11-14T05:34:18Z\",\"name\":\"userAssertionOther\",\"userDisplayName\":\"Luke Skywalker\",\"comment\":\"alert test\",\"uuid\":\"81447f7f-65b3-4bde-b95c-f507f5e0c67a\",\"userId\":\"666888\"}],\"assertions\":[\"MISSING_TAXONRANK\",\"GEODETIC_DATUM_ASSUMED_WGS84\",\"MISSING_GEODETICDATUM\",\"COORDINATE_UNCERTAINTY_METERS_INVALID\",\"MISSING_GEOREFERENCE_DATE\",\"MISSING_GEOREFERENCEDBY\",\"MISSING_GEOREFERENCEPROTOCOL\",\"MISSING_GEOREFERENCESOURCES\",\"MISSING_GEOREFERENCEVERIFICATIONSTATUS\"],\"verified_assertions\":\"\",\"speciesGroups\":[\"Plants\",\"Flowering plants\",\"Dicots\"],\"spatiallyValid\":true,\"order\":\"Fabales\",\"taxonRankID\":7000,\"dataResourceName\":\"Kings Park Botanical Gardens\",\"raw_basisOfRecord\":\"Preserved Specimen\",\"stateProvince\":\"Western Australia\",\"decimalLongitude\":115.928611,\"raw_collectionCode\":\"KPBG\",\"corrected_assertions\":\"\",\"license\":\"CC-BY 4.0 (Int)\",\"month\":\"06\",\"dataResourceUid\":\"dr21662\",\"genus\":\"Acacia\",\"left\":587708,\"point0001\":\"-32.712,115.929\",\"eventDate\":141264000000,\"taxonRank\":\"species\",\"point1\":\"-33,116\",\"collectionName\":\"Kings Park and Botanic Garden\",\"hasUserAssertions\":true,\"speciesGuid\":\"https://id.biodiversity.org.au/node/apni/2899185\",\"geospatialKosher\":\"true\",\"namesLsid\":\"Acacia obovata|https://id.biodiversity.org.au/node/apni/2899185||Plantae|Fabaceae\",\"collectionUid\":\"co32\",\"point00001\":\"-32.7122,115.9286\",\"raw_institutionCode\":\"BGPA\",\"point001\":\"-32.71,115.93\",\"right\":587708,\"raw_catalogNumber\":\"38215\",\"kingdom\":\"Plantae\",\"phylum\":\"Charophyta\",\"classs\":\"Equisetopsida\",\"institutionUid\":\"in58\",\"species\":\"Acacia obovata\",\"institutionName\":\"WA Botanic Gardens and Parks Authority\",\"family\":\"Fabaceae\",\"genusGuid\":\"https://id.biodiversity.org.au/taxon/apni/51471290\",\"open_assertions\":\"\",\"userAssertions\":\"50005\"}],\"totalRecords\":3,\"query\":\"?q=*%3A*&fq=assertion_user_id%3A666888\",\"pageSize\":300,\"queryTitle\":\"[all records]\",\"sort\":\"score\",\"dir\":\"desc\",\"startIndex\":0,\"activeFacetMap\":{\"assertion_user_id\":{\"displayName\":\"Assertions by user:Luke Skywalker\",\"name\":\"assertion_user_id\",\"value\":\"666888\"}},\"activeFacetObj\":{\"assertion_user_id\":[{\"displayName\":\"Assertions by user:Luke Skywalker\",\"name\":\"assertion_user_id\",\"value\":\"assertion_user_id:666888\"}]},\"urlParameters\":\"?q=*%3A*&fq=assertion_user_id%3A666888\",\"facetResults\":[],\"status\":\"OK\"}"
        def jsonPath = "\$.occurrences[*]"
        when:
        def result = assertionService.diff(previous, current, jsonPath)

        then:
        result.size() == 1
        result.processed_assertions.size() == 1
    }

    /**
     * Test multiple verified annotations on a record
     * The record can be deleted, but no way of knowing whether some verified annotations have been sent to the user
     */
    void "multiple verified annotations"() {
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
                "      \"verified_assertions\": [ " +
                "      { " +
                "      \"qaStatus\": 50005, " +
                "      \"problemAsserted\": false, " +
                "      \"code\": 20019, " +
                "      \"referenceRowKey\": \"62401e1f-e0f4-43db-9727-b45a0e5c0d0e\", " +
                "      \"created\": \"2024-11-11T22:29:36Z\", " +
                "      \"name\": \"userAssertionOther\", " +
                "      \"userDisplayName\": \"Han Solo\", " +
                "      \"comment\": \"TEST - testing for Alerts - will delete shortly\", " +
                "      \"uuid\": \"fe0000f6-2a2d-42be-9cff-2248fcf55a31\", " +
                "      \"userId\": \"Han_Solo_ID\" " +
                "    }, " +
                "    { " +
                "      \"qaStatus\": 50005, " +
                "      \"problemAsserted\": false, " +
                "      \"code\": 20, " +
                "      \"referenceRowKey\": \"62401e1f-e0f4-43db-9727-b45a0e5c0d0e\", " +
                "      \"created\": \"2025-01-08T22:49:38Z\", " +
                "      \"name\": \"detectedOutlier\", " +
                "      \"userDisplayName\": \"Han_Solo\", " +
                "      \"comment\": \"This is 2nd annotation\", " +
                "      \"uuid\": \"ac44cfac-f113-4f1e-b6a6-64ae4de3c388\", " +
                "      \"userId\": \"Han_Solo_ID\" " +
                "    }, " +
                "    { " +
                "      \"relatedUuid\": \"ac44cfac-f113-4f1e-b6a6-64ae4de3c388\", " +
                "      \"qaStatus\": 50002, " +
                "      \"problemAsserted\": false, " +
                "      \"code\": 50000, " +
                "      \"referenceRowKey\": \"62401e1f-e0f4-43db-9727-b45a0e5c0d0e\", " +
                "      \"created\": \"2025-01-08T22:51:07Z\", " +
                "      \"userDisplayName\": \"Leia Organa\", " +
                "      \"comment\": \"2nd test\", " +
                "      \"uuid\": \"8aa91488-be1e-4b50-a0cd-8e1c745f4cfd\", " +
                "      \"userId\": \"Leia_Organa_ID\" " +
                "       }" +
                "       ]," +
                "      \"assertions\": [ " +
                "        \"COORDINATE_ROUNDED\", " +
                "        \"MISSING_GEOREFERENCE_DATE\", " +
                "        \"MISSING_GEOREFERENCEDBY\", " +
                "        \"MISSING_GEOREFERENCESOURCES\", " +
                "        \"MISSING_GEOREFERENCEVERIFICATIONSTATUS\", " +
                "        \"INDIVIDUAL_COUNT_INVALID\" " +
                "      ], " +
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
        def current = "{ " +
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
                "      \"verified_assertions\": [ " +
                "      { " +
                "      \"qaStatus\": 50005, " +
                "      \"problemAsserted\": false, " +
                "      \"code\": 20019, " +
                "      \"referenceRowKey\": \"62401e1f-e0f4-43db-9727-b45a0e5c0d0e\", " +
                "      \"created\": \"2024-11-11T22:29:36Z\", " +
                "      \"name\": \"userAssertionOther\", " +
                "      \"userDisplayName\": \"Han Solo\", " +
                "      \"comment\": \"TEST - testing for Alerts - will delete shortly\", " +
                "      \"uuid\": \"fe0000f6-2a2d-42be-9cff-2248fcf55a31\", " +
                "      \"userId\": \"Han_Solo_ID\" " +
                "    }, " +
                "    { " +
                "      \"qaStatus\": 50005, " +
                "      \"problemAsserted\": false, " +
                "      \"code\": 20, " +
                "      \"referenceRowKey\": \"62401e1f-e0f4-43db-9727-b45a0e5c0d0e\", " +
                "      \"created\": \"2025-01-08T22:49:38Z\", " +
                "      \"name\": \"detectedOutlier\", " +
                "      \"userDisplayName\": \"Han_Solo\", " +
                "      \"comment\": \"This is 2nd annotation\", " +
                "      \"uuid\": \"ac44cfac-f113-4f1e-b6a6-64ae4de3c388\", " +
                "      \"userId\": \"Han_Solo_ID\" " +
                "    }, " +
                "    { " +
                "      \"relatedUuid\": \"ac44cfac-f113-4f1e-b6a6-64ae4de3c388\", " +
                "      \"qaStatus\": 50002, " +
                "      \"problemAsserted\": false, " +
                "      \"code\": 50000, " +
                "      \"referenceRowKey\": \"62401e1f-e0f4-43db-9727-b45a0e5c0d0e\", " +
                "      \"created\": \"2025-01-08T22:51:07Z\", " +
                "      \"userDisplayName\": \"Leia Organa\", " +
                "      \"comment\": \"2nd test\", " +
                "      \"uuid\": \"8aa91488-be1e-4b50-a0cd-8e1c745f4cfd\", " +
                "      \"userId\": \"Leia_Organa_ID\" " +
                "    }, " +
                "    { " +
                "      \"relatedUuid\": \"fe0000f6-2a2d-42be-9cff-2248fcf55a31\", " +
                "      \"qaStatus\": 50003, " +
                "      \"problemAsserted\": false, " +
                "      \"code\": 50000, " +
                "      \"referenceRowKey\": \"62401e1f-e0f4-43db-9727-b45a0e5c0d0e\", " +
                "      \"created\": \"2025-01-08T22:48:22Z\", " +
                "      \"userDisplayName\": \"Leia Organa\", " +
                "      \"comment\": \"test2\", " +
                "      \"uuid\": \"016c1307-0a2d-4b31-b0ee-737fc1ee2421\", " +
                "      \"userId\": \"Leia_Organa_ID\" " +
                "    }]," +
                "      \"assertions\": [ " +
                "        \"COORDINATE_ROUNDED\", " +
                "        \"MISSING_GEOREFERENCE_DATE\", " +
                "        \"MISSING_GEOREFERENCEDBY\", " +
                "        \"MISSING_GEOREFERENCESOURCES\", " +
                "        \"MISSING_GEOREFERENCEVERIFICATIONSTATUS\", " +
                "        \"INDIVIDUAL_COUNT_INVALID\" " +
                "      ], " +
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
        def jsonPath = "\$.occurrences[*]"
        when:
        def result = assertionService.diff(previous, current, jsonPath)

        then:
        result.size() == 1
        result.processed_assertions.size() == 1
    }
}
