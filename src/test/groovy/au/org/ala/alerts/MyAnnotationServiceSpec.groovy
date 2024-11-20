package au.org.ala.alerts

import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

class MyAnnotationServiceSpec extends Specification implements ServiceUnitTest<MyAnnotationService>{

    def assertionService

    def setup() {
        assertionService = new MyAnnotationService()
    }

    def cleanup() {
    }

    void "diff check against empty previous records"() {
        given:
        def previous = "{}"
        def current = "{\"occurrences\":[{\"country\":\"Australia\",\"raw_countryCode\":\"AU\",\"scientificName\":\"Acacia petraea\",\"year\":2023,\"decimalLatitude\":-29.062978,\"uuid\":\"0e6de91f-d30c-4e52-b7d5-678e5527d0b5\",\"point01\":\"-29.1,145\",\"raw_locationRemarks\":\"locationRemarks withheld\",\"basisOfRecord\":\"HUMAN_OBSERVATION\",\"raw_scientificName\":\"Acacia petraea\",\"raw_vernacularName\":\"Lancewood\",\"taxonConceptID\":\"https://id.biodiversity.org.au/node/apni/2903503\",\"latLong\":\"-29.062978,144.994102\",\"user_assertions\":[{\"qaStatus\":50005,\"problemAsserted\":false,\"code\":20019,\"dataResourceUid\":\"dr368\",\"referenceRowKey\":\"0e6de91f-d30c-4e52-b7d5-678e5527d0b5\",\"created\":\"2024-10-14T22:41:33Z\",\"name\":\"userAssertionOther\",\"userDisplayName\":\"Luke Skywalker\",\"comment\":\"Alert test purpose\",\"uuid\":\"fb8dbbb3-e0d3-41eb-ab97-36b76fc75476\",\"userId\":\"666888\"}],\"assertions\":[\"COORDINATE_ROUNDED\",\"MISSING_GEOREFERENCE_DATE\",\"MISSING_GEOREFERENCEDBY\",\"MISSING_GEOREFERENCESOURCES\",\"MISSING_GEOREFERENCEVERIFICATIONSTATUS\",\"INDIVIDUAL_COUNT_INVALID\"],\"verified_assertions\":\"\",\"speciesGroups\":[\"Plants\",\"Flowering plants\",\"Dicots\"],\"spatiallyValid\":true,\"order\":\"Fabales\",\"taxonRankID\":7000,\"dataResourceName\":\"NSW BioNet Atlas\",\"raw_basisOfRecord\":\"HumanObservation\",\"stateProvince\":\"New South Wales\",\"decimalLongitude\":144.994102,\"occurrenceID\":\"urn:catalog:NSW Dept of Planning, Industry and Environment:BioNet Atlas of NSW Wildlife:SDFKI0142229\",\"raw_collectionCode\":\"BioNet Atlas of NSW Wildlife\",\"corrected_assertions\":\"\",\"license\":\"CC-BY 4.0 (Int)\",\"collectors\":[\"OMFZ2303270J\"],\"month\":\"09\",\"dataResourceUid\":\"dr368\",\"genus\":\"Acacia\",\"left\":588026,\"point0001\":\"-29.063,144.994\",\"eventDate\":1694217600000,\"coordinateUncertaintyInMeters\":10,\"taxonRank\":\"species\",\"point1\":\"-29,145\",\"collector\":[\"OMFZ2303270J\"],\"vernacularName\":\"Lancewood\",\"hasUserAssertions\":true,\"speciesGuid\":\"https://id.biodiversity.org.au/node/apni/2903503\",\"raw_occurrenceRemarks\":\"occurrenceRemarks withheld\",\"rights\":\"Unknown\",\"geospatialKosher\":\"true\",\"namesLsid\":\"Acacia petraea|https://id.biodiversity.org.au/node/apni/2903503|Lancewood|Plantae|Fabaceae\",\"point00001\":\"-29.063,144.9941\",\"raw_institutionCode\":\"NSW Dept of Planning, Industry and Environment\",\"point001\":\"-29.06,144.99\",\"right\":588026,\"raw_catalogNumber\":\"SDFKI0142229\",\"kingdom\":\"Plantae\",\"dataProviderUid\":\"dp34\",\"recordedBy\":[\"OMFZ2303270J\"],\"phylum\":\"Charophyta\",\"classs\":\"Equisetopsida\",\"species\":\"Acacia petraea\",\"dataProviderName\":\"Department of Planning, Industry and Environment representing the State of New South Wales\",\"stateConservation\":\"Endangered\",\"family\":\"Fabaceae\",\"genusGuid\":\"https://id.biodiversity.org.au/taxon/apni/51471290\",\"open_assertions\":\"\",\"userAssertions\":\"50005\"},{\"country\":\"Australia\",\"scientificName\":\"Acacia idiomorpha\",\"year\":1962,\"decimalLatitude\":-27.75,\"uuid\":\"539bd9f9-95bd-47b6-b50f-bf75089b9521\",\"point01\":\"-27.8,114.2\",\"basisOfRecord\":\"PRESERVED_SPECIMEN\",\"raw_scientificName\":\"Acacia idiomorpha Benth.\",\"taxonConceptID\":\"https://id.biodiversity.org.au/node/apni/2902734\",\"latLong\":\"-27.75,114.166667\",\"user_assertions\":[{\"qaStatus\":50005,\"problemAsserted\":false,\"code\":20019,\"dataResourceUid\":\"dr21662\",\"referenceRowKey\":\"539bd9f9-95bd-47b6-b50f-bf75089b9521\",\"created\":\"2023-11-14T04:43:35Z\",\"name\":\"userAssertionOther\",\"userDisplayName\":\"Luke Skywalker\",\"comment\":\"Alert test\",\"uuid\":\"340d3c4e-4979-45a3-934d-77f7cc1e9b53\",\"userId\":\"666888\"}],\"assertions\":[\"MISSING_TAXONRANK\",\"GEODETIC_DATUM_ASSUMED_WGS84\",\"MISSING_GEODETICDATUM\",\"COORDINATE_UNCERTAINTY_METERS_INVALID\",\"MISSING_GEOREFERENCE_DATE\",\"MISSING_GEOREFERENCEDBY\",\"MISSING_GEOREFERENCEPROTOCOL\",\"MISSING_GEOREFERENCESOURCES\",\"MISSING_GEOREFERENCEVERIFICATIONSTATUS\"],\"verified_assertions\":\"340d3c4e-4979-45a3-934d-77f7cc1e9b53\",\"speciesGroups\":[\"Plants\",\"Flowering plants\",\"Dicots\"],\"spatiallyValid\":true,\"order\":\"Fabales\",\"taxonRankID\":7000,\"dataResourceName\":\"Kings Park Botanical Gardens\",\"raw_basisOfRecord\":\"Preserved Specimen\",\"stateProvince\":\"Western Australia\",\"decimalLongitude\":114.166667,\"raw_collectionCode\":\"KPBG\",\"corrected_assertions\":\"\",\"license\":\"CC-BY 4.0 (Int)\",\"month\":\"09\",\"dataResourceUid\":\"dr21662\",\"genus\":\"Acacia\",\"left\":587962,\"point0001\":\"-27.75,114.167\",\"eventDate\":-229219200000,\"taxonRank\":\"species\",\"point1\":\"-28,114\",\"collectionName\":\"Kings Park and Botanic Garden\",\"hasUserAssertions\":true,\"speciesGuid\":\"https://id.biodiversity.org.au/node/apni/2902734\",\"geospatialKosher\":\"true\",\"namesLsid\":\"Acacia idiomorpha|https://id.biodiversity.org.au/node/apni/2902734||Plantae|Fabaceae\",\"collectionUid\":\"co32\",\"point00001\":\"-27.75,114.1667\",\"raw_institutionCode\":\"BGPA\",\"point001\":\"-27.75,114.17\",\"right\":587962,\"raw_catalogNumber\":\"37888\",\"kingdom\":\"Plantae\",\"phylum\":\"Charophyta\",\"classs\":\"Equisetopsida\",\"institutionUid\":\"in58\",\"species\":\"Acacia idiomorpha\",\"institutionName\":\"WA Botanic Gardens and Parks Authority\",\"family\":\"Fabaceae\",\"genusGuid\":\"https://id.biodiversity.org.au/taxon/apni/51471290\",\"open_assertions\":\"\",\"userAssertions\":\"50002\"},{\"country\":\"Australia\",\"scientificName\":\"Acacia obovata\",\"year\":1974,\"decimalLatitude\":-32.712222,\"uuid\":\"f7d3a852-7382-4999-bc2e-6d2676a11294\",\"point01\":\"-32.7,115.9\",\"basisOfRecord\":\"PRESERVED_SPECIMEN\",\"raw_scientificName\":\"Acacia obovata Benth.\",\"taxonConceptID\":\"https://id.biodiversity.org.au/node/apni/2899185\",\"latLong\":\"-32.712222,115.928611\",\"user_assertions\":[{\"qaStatus\":50005,\"problemAsserted\":false,\"code\":20019,\"dataResourceUid\":\"dr21662\",\"referenceRowKey\":\"f7d3a852-7382-4999-bc2e-6d2676a11294\",\"created\":\"2023-11-14T05:34:18Z\",\"name\":\"userAssertionOther\",\"userDisplayName\":\"Luke Skywalker\",\"comment\":\"alert test\",\"uuid\":\"81447f7f-65b3-4bde-b95c-f507f5e0c67a\",\"userId\":\"666888\"}],\"assertions\":[\"MISSING_TAXONRANK\",\"GEODETIC_DATUM_ASSUMED_WGS84\",\"MISSING_GEODETICDATUM\",\"COORDINATE_UNCERTAINTY_METERS_INVALID\",\"MISSING_GEOREFERENCE_DATE\",\"MISSING_GEOREFERENCEDBY\",\"MISSING_GEOREFERENCEPROTOCOL\",\"MISSING_GEOREFERENCESOURCES\",\"MISSING_GEOREFERENCEVERIFICATIONSTATUS\"],\"verified_assertions\":\"\",\"speciesGroups\":[\"Plants\",\"Flowering plants\",\"Dicots\"],\"spatiallyValid\":true,\"order\":\"Fabales\",\"taxonRankID\":7000,\"dataResourceName\":\"Kings Park Botanical Gardens\",\"raw_basisOfRecord\":\"Preserved Specimen\",\"stateProvince\":\"Western Australia\",\"decimalLongitude\":115.928611,\"raw_collectionCode\":\"KPBG\",\"corrected_assertions\":\"\",\"license\":\"CC-BY 4.0 (Int)\",\"month\":\"06\",\"dataResourceUid\":\"dr21662\",\"genus\":\"Acacia\",\"left\":587708,\"point0001\":\"-32.712,115.929\",\"eventDate\":141264000000,\"taxonRank\":\"species\",\"point1\":\"-33,116\",\"collectionName\":\"Kings Park and Botanic Garden\",\"hasUserAssertions\":true,\"speciesGuid\":\"https://id.biodiversity.org.au/node/apni/2899185\",\"geospatialKosher\":\"true\",\"namesLsid\":\"Acacia obovata|https://id.biodiversity.org.au/node/apni/2899185||Plantae|Fabaceae\",\"collectionUid\":\"co32\",\"point00001\":\"-32.7122,115.9286\",\"raw_institutionCode\":\"BGPA\",\"point001\":\"-32.71,115.93\",\"right\":587708,\"raw_catalogNumber\":\"38215\",\"kingdom\":\"Plantae\",\"phylum\":\"Charophyta\",\"classs\":\"Equisetopsida\",\"institutionUid\":\"in58\",\"species\":\"Acacia obovata\",\"institutionName\":\"WA Botanic Gardens and Parks Authority\",\"family\":\"Fabaceae\",\"genusGuid\":\"https://id.biodiversity.org.au/taxon/apni/51471290\",\"open_assertions\":\"\",\"userAssertions\":\"50005\"}],\"totalRecords\":3,\"query\":\"?q=*%3A*&fq=assertion_user_id%3A666888\",\"pageSize\":300,\"queryTitle\":\"[all records]\",\"sort\":\"score\",\"dir\":\"desc\",\"startIndex\":0,\"activeFacetMap\":{\"assertion_user_id\":{\"displayName\":\"Assertions by user:Luke Skywalker\",\"name\":\"assertion_user_id\",\"value\":\"666888\"}},\"activeFacetObj\":{\"assertion_user_id\":[{\"displayName\":\"Assertions by user:Luke Skywalker\",\"name\":\"assertion_user_id\",\"value\":\"assertion_user_id:666888\"}]},\"urlParameters\":\"?q=*%3A*&fq=assertion_user_id%3A666888\",\"facetResults\":[],\"status\":\"OK\"}"
        def jsonPath = "\$.occurrences[*]"
        when:
        def result = assertionService.diff(previous, current, jsonPath )

        then:
        result.size() == 3
    }

    void "diff check against empty existing records"() {
        given:
        def previous = "{\n" +
                "  \"occurrences\": [\n" +
                "    {\n" +
                "      \"country\": \"Australia\",\n" +
                "      \"raw_countryCode\": \"AU\",\n" +
                "      \"scientificName\": \"Acacia petraea\",\n" +
                "      \"year\": 2023,\n" +
                "      \"decimalLatitude\": -29.062978,\n" +
                "      \"uuid\": \"0e6de91f-d30c-4e52-b7d5-678e5527d0b5\",\n" +
                "      \"point01\": \"-29.1,145\",\n" +
                "      \"raw_locationRemarks\": \"locationRemarks withheld\",\n" +
                "      \"basisOfRecord\": \"HUMAN_OBSERVATION\",\n" +
                "      \"raw_scientificName\": \"Acacia petraea\",\n" +
                "      \"raw_vernacularName\": \"Lancewood\",\n" +
                "      \"taxonConceptID\": \"https://id.biodiversity.org.au/node/apni/2903503\",\n" +
                "      \"latLong\": \"-29.062978,144.994102\",\n" +
                "      \"user_assertions\": [\n" +
                "        {\n" +
                "          \"qaStatus\": 50005,\n" +
                "          \"problemAsserted\": false,\n" +
                "          \"code\": 20019,\n" +
                "          \"dataResourceUid\": \"dr368\",\n" +
                "          \"referenceRowKey\": \"0e6de91f-d30c-4e52-b7d5-678e5527d0b5\",\n" +
                "          \"created\": \"2024-10-14T22:41:33Z\",\n" +
                "          \"name\": \"userAssertionOther\",\n" +
                "          \"userDisplayName\": \"Luke Skywalker\",\n" +
                "          \"comment\": \"Alert test purpose\",\n" +
                "          \"uuid\": \"fb8dbbb3-e0d3-41eb-ab97-36b76fc75476\",\n" +
                "          \"userId\": \"666888\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"assertions\": [\n" +
                "        \"COORDINATE_ROUNDED\",\n" +
                "        \"MISSING_GEOREFERENCE_DATE\",\n" +
                "        \"MISSING_GEOREFERENCEDBY\",\n" +
                "        \"MISSING_GEOREFERENCESOURCES\",\n" +
                "        \"MISSING_GEOREFERENCEVERIFICATIONSTATUS\",\n" +
                "        \"INDIVIDUAL_COUNT_INVALID\"\n" +
                "      ],\n" +
                "      \"verified_assertions\": \"\",\n" +
                "      \"speciesGroups\": [\n" +
                "        \"Plants\",\n" +
                "        \"Flowering plants\",\n" +
                "        \"Dicots\"\n" +
                "      ],\n" +
                "      \"spatiallyValid\": true,\n" +
                "      \"order\": \"Fabales\",\n" +
                "      \"taxonRankID\": 7000,\n" +
                "      \"dataResourceName\": \"NSW BioNet Atlas\",\n" +
                "      \"raw_basisOfRecord\": \"HumanObservation\",\n" +
                "      \"stateProvince\": \"New South Wales\",\n" +
                "      \"decimalLongitude\": 144.994102,\n" +
                "      \"occurrenceID\": \"urn:catalog:NSW Dept of Planning, Industry and Environment:BioNet Atlas of NSW Wildlife:SDFKI0142229\",\n" +
                "      \"raw_collectionCode\": \"BioNet Atlas of NSW Wildlife\",\n" +
                "      \"corrected_assertions\": \"\",\n" +
                "      \"license\": \"CC-BY 4.0 (Int)\",\n" +
                "      \"collectors\": [\n" +
                "        \"OMFZ2303270J\"\n" +
                "      ],\n" +
                "      \"month\": \"09\",\n" +
                "      \"dataResourceUid\": \"dr368\",\n" +
                "      \"genus\": \"Acacia\",\n" +
                "      \"left\": 588026,\n" +
                "      \"point0001\": \"-29.063,144.994\",\n" +
                "      \"eventDate\": 1694217600000,\n" +
                "      \"coordinateUncertaintyInMeters\": 10,\n" +
                "      \"taxonRank\": \"species\",\n" +
                "      \"point1\": \"-29,145\",\n" +
                "      \"collector\": [\n" +
                "        \"OMFZ2303270J\"\n" +
                "      ],\n" +
                "      \"vernacularName\": \"Lancewood\",\n" +
                "      \"hasUserAssertions\": true,\n" +
                "      \"speciesGuid\": \"https://id.biodiversity.org.au/node/apni/2903503\",\n" +
                "      \"raw_occurrenceRemarks\": \"occurrenceRemarks withheld\",\n" +
                "      \"rights\": \"Unknown\",\n" +
                "      \"geospatialKosher\": \"true\",\n" +
                "      \"namesLsid\": \"Acacia petraea|https://id.biodiversity.org.au/node/apni/2903503|Lancewood|Plantae|Fabaceae\",\n" +
                "      \"point00001\": \"-29.063,144.9941\",\n" +
                "      \"raw_institutionCode\": \"NSW Dept of Planning, Industry and Environment\",\n" +
                "      \"point001\": \"-29.06,144.99\",\n" +
                "      \"right\": 588026,\n" +
                "      \"raw_catalogNumber\": \"SDFKI0142229\",\n" +
                "      \"kingdom\": \"Plantae\",\n" +
                "      \"dataProviderUid\": \"dp34\",\n" +
                "      \"recordedBy\": [\n" +
                "        \"OMFZ2303270J\"\n" +
                "      ],\n" +
                "      \"phylum\": \"Charophyta\",\n" +
                "      \"classs\": \"Equisetopsida\",\n" +
                "      \"species\": \"Acacia petraea\",\n" +
                "      \"dataProviderName\": \"Department of Planning, Industry and Environment representing the State of New South Wales\",\n" +
                "      \"stateConservation\": \"Endangered\",\n" +
                "      \"family\": \"Fabaceae\",\n" +
                "      \"genusGuid\": \"https://id.biodiversity.org.au/taxon/apni/51471290\",\n" +
                "      \"open_assertions\": \"\",\n" +
                "      \"userAssertions\": \"50005\"\n" +
                "    }  ],\n" +
                "  \"totalRecords\": 1,\n" +
                "  \"query\": \"?q=*%3A*&fq=assertion_user_id%3A666888\",\n" +
                "  \"pageSize\": 300,\n" +
                "  \"queryTitle\": \"[all records]\",\n" +
                "  \"sort\": \"score\",\n" +
                "  \"dir\": \"desc\",\n" +
                "  \"startIndex\": 0,\n" +
                "  \"activeFacetMap\": {\n" +
                "    \"assertion_user_id\": {\n" +
                "      \"displayName\": \"Assertions by user:Luke Skywalker\",\n" +
                "      \"name\": \"assertion_user_id\",\n" +
                "      \"value\": \"666888\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"activeFacetObj\": {\n" +
                "    \"assertion_user_id\": [\n" +
                "      {\n" +
                "        \"displayName\": \"Assertions by user:Luke Skywalker\",\n" +
                "        \"name\": \"assertion_user_id\",\n" +
                "        \"value\": \"assertion_user_id:666888\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"urlParameters\": \"?q=*%3A*&fq=assertion_user_id%3A666888\",\n" +
                "  \"facetResults\": [\n" +
                "    \n" +
                "  ],\n" +
                "  \"status\": \"OK\"\n" +
                "}"
        def current = "{\"occurrences\":[{\"country\":\"Australia\",\"raw_countryCode\":\"AU\",\"scientificName\":\"Acacia petraea\",\"year\":2023,\"decimalLatitude\":-29.062978,\"uuid\":\"0e6de91f-d30c-4e52-b7d5-678e5527d0b5\",\"point01\":\"-29.1,145\",\"raw_locationRemarks\":\"locationRemarks withheld\",\"basisOfRecord\":\"HUMAN_OBSERVATION\",\"raw_scientificName\":\"Acacia petraea\",\"raw_vernacularName\":\"Lancewood\",\"taxonConceptID\":\"https://id.biodiversity.org.au/node/apni/2903503\",\"latLong\":\"-29.062978,144.994102\",\"user_assertions\":[{\"qaStatus\":50005,\"problemAsserted\":false,\"code\":20019,\"dataResourceUid\":\"dr368\",\"referenceRowKey\":\"0e6de91f-d30c-4e52-b7d5-678e5527d0b5\",\"created\":\"2024-10-14T22:41:33Z\",\"name\":\"userAssertionOther\",\"userDisplayName\":\"Luke Skywalker\",\"comment\":\"Alert test purpose\",\"uuid\":\"fb8dbbb3-e0d3-41eb-ab97-36b76fc75476\",\"userId\":\"666888\"}],\"assertions\":[\"COORDINATE_ROUNDED\",\"MISSING_GEOREFERENCE_DATE\",\"MISSING_GEOREFERENCEDBY\",\"MISSING_GEOREFERENCESOURCES\",\"MISSING_GEOREFERENCEVERIFICATIONSTATUS\",\"INDIVIDUAL_COUNT_INVALID\"],\"verified_assertions\":\"\",\"speciesGroups\":[\"Plants\",\"Flowering plants\",\"Dicots\"],\"spatiallyValid\":true,\"order\":\"Fabales\",\"taxonRankID\":7000,\"dataResourceName\":\"NSW BioNet Atlas\",\"raw_basisOfRecord\":\"HumanObservation\",\"stateProvince\":\"New South Wales\",\"decimalLongitude\":144.994102,\"occurrenceID\":\"urn:catalog:NSW Dept of Planning, Industry and Environment:BioNet Atlas of NSW Wildlife:SDFKI0142229\",\"raw_collectionCode\":\"BioNet Atlas of NSW Wildlife\",\"corrected_assertions\":\"\",\"license\":\"CC-BY 4.0 (Int)\",\"collectors\":[\"OMFZ2303270J\"],\"month\":\"09\",\"dataResourceUid\":\"dr368\",\"genus\":\"Acacia\",\"left\":588026,\"point0001\":\"-29.063,144.994\",\"eventDate\":1694217600000,\"coordinateUncertaintyInMeters\":10,\"taxonRank\":\"species\",\"point1\":\"-29,145\",\"collector\":[\"OMFZ2303270J\"],\"vernacularName\":\"Lancewood\",\"hasUserAssertions\":true,\"speciesGuid\":\"https://id.biodiversity.org.au/node/apni/2903503\",\"raw_occurrenceRemarks\":\"occurrenceRemarks withheld\",\"rights\":\"Unknown\",\"geospatialKosher\":\"true\",\"namesLsid\":\"Acacia petraea|https://id.biodiversity.org.au/node/apni/2903503|Lancewood|Plantae|Fabaceae\",\"point00001\":\"-29.063,144.9941\",\"raw_institutionCode\":\"NSW Dept of Planning, Industry and Environment\",\"point001\":\"-29.06,144.99\",\"right\":588026,\"raw_catalogNumber\":\"SDFKI0142229\",\"kingdom\":\"Plantae\",\"dataProviderUid\":\"dp34\",\"recordedBy\":[\"OMFZ2303270J\"],\"phylum\":\"Charophyta\",\"classs\":\"Equisetopsida\",\"species\":\"Acacia petraea\",\"dataProviderName\":\"Department of Planning, Industry and Environment representing the State of New South Wales\",\"stateConservation\":\"Endangered\",\"family\":\"Fabaceae\",\"genusGuid\":\"https://id.biodiversity.org.au/taxon/apni/51471290\",\"open_assertions\":\"\",\"userAssertions\":\"50005\"},{\"country\":\"Australia\",\"scientificName\":\"Acacia idiomorpha\",\"year\":1962,\"decimalLatitude\":-27.75,\"uuid\":\"539bd9f9-95bd-47b6-b50f-bf75089b9521\",\"point01\":\"-27.8,114.2\",\"basisOfRecord\":\"PRESERVED_SPECIMEN\",\"raw_scientificName\":\"Acacia idiomorpha Benth.\",\"taxonConceptID\":\"https://id.biodiversity.org.au/node/apni/2902734\",\"latLong\":\"-27.75,114.166667\",\"user_assertions\":[{\"qaStatus\":50005,\"problemAsserted\":false,\"code\":20019,\"dataResourceUid\":\"dr21662\",\"referenceRowKey\":\"539bd9f9-95bd-47b6-b50f-bf75089b9521\",\"created\":\"2023-11-14T04:43:35Z\",\"name\":\"userAssertionOther\",\"userDisplayName\":\"Luke Skywalker\",\"comment\":\"Alert test\",\"uuid\":\"340d3c4e-4979-45a3-934d-77f7cc1e9b53\",\"userId\":\"666888\"}],\"assertions\":[\"MISSING_TAXONRANK\",\"GEODETIC_DATUM_ASSUMED_WGS84\",\"MISSING_GEODETICDATUM\",\"COORDINATE_UNCERTAINTY_METERS_INVALID\",\"MISSING_GEOREFERENCE_DATE\",\"MISSING_GEOREFERENCEDBY\",\"MISSING_GEOREFERENCEPROTOCOL\",\"MISSING_GEOREFERENCESOURCES\",\"MISSING_GEOREFERENCEVERIFICATIONSTATUS\"],\"verified_assertions\":\"340d3c4e-4979-45a3-934d-77f7cc1e9b53\",\"speciesGroups\":[\"Plants\",\"Flowering plants\",\"Dicots\"],\"spatiallyValid\":true,\"order\":\"Fabales\",\"taxonRankID\":7000,\"dataResourceName\":\"Kings Park Botanical Gardens\",\"raw_basisOfRecord\":\"Preserved Specimen\",\"stateProvince\":\"Western Australia\",\"decimalLongitude\":114.166667,\"raw_collectionCode\":\"KPBG\",\"corrected_assertions\":\"\",\"license\":\"CC-BY 4.0 (Int)\",\"month\":\"09\",\"dataResourceUid\":\"dr21662\",\"genus\":\"Acacia\",\"left\":587962,\"point0001\":\"-27.75,114.167\",\"eventDate\":-229219200000,\"taxonRank\":\"species\",\"point1\":\"-28,114\",\"collectionName\":\"Kings Park and Botanic Garden\",\"hasUserAssertions\":true,\"speciesGuid\":\"https://id.biodiversity.org.au/node/apni/2902734\",\"geospatialKosher\":\"true\",\"namesLsid\":\"Acacia idiomorpha|https://id.biodiversity.org.au/node/apni/2902734||Plantae|Fabaceae\",\"collectionUid\":\"co32\",\"point00001\":\"-27.75,114.1667\",\"raw_institutionCode\":\"BGPA\",\"point001\":\"-27.75,114.17\",\"right\":587962,\"raw_catalogNumber\":\"37888\",\"kingdom\":\"Plantae\",\"phylum\":\"Charophyta\",\"classs\":\"Equisetopsida\",\"institutionUid\":\"in58\",\"species\":\"Acacia idiomorpha\",\"institutionName\":\"WA Botanic Gardens and Parks Authority\",\"family\":\"Fabaceae\",\"genusGuid\":\"https://id.biodiversity.org.au/taxon/apni/51471290\",\"open_assertions\":\"\",\"userAssertions\":\"50002\"},{\"country\":\"Australia\",\"scientificName\":\"Acacia obovata\",\"year\":1974,\"decimalLatitude\":-32.712222,\"uuid\":\"f7d3a852-7382-4999-bc2e-6d2676a11294\",\"point01\":\"-32.7,115.9\",\"basisOfRecord\":\"PRESERVED_SPECIMEN\",\"raw_scientificName\":\"Acacia obovata Benth.\",\"taxonConceptID\":\"https://id.biodiversity.org.au/node/apni/2899185\",\"latLong\":\"-32.712222,115.928611\",\"user_assertions\":[{\"qaStatus\":50005,\"problemAsserted\":false,\"code\":20019,\"dataResourceUid\":\"dr21662\",\"referenceRowKey\":\"f7d3a852-7382-4999-bc2e-6d2676a11294\",\"created\":\"2023-11-14T05:34:18Z\",\"name\":\"userAssertionOther\",\"userDisplayName\":\"Luke Skywalker\",\"comment\":\"alert test\",\"uuid\":\"81447f7f-65b3-4bde-b95c-f507f5e0c67a\",\"userId\":\"666888\"}],\"assertions\":[\"MISSING_TAXONRANK\",\"GEODETIC_DATUM_ASSUMED_WGS84\",\"MISSING_GEODETICDATUM\",\"COORDINATE_UNCERTAINTY_METERS_INVALID\",\"MISSING_GEOREFERENCE_DATE\",\"MISSING_GEOREFERENCEDBY\",\"MISSING_GEOREFERENCEPROTOCOL\",\"MISSING_GEOREFERENCESOURCES\",\"MISSING_GEOREFERENCEVERIFICATIONSTATUS\"],\"verified_assertions\":\"\",\"speciesGroups\":[\"Plants\",\"Flowering plants\",\"Dicots\"],\"spatiallyValid\":true,\"order\":\"Fabales\",\"taxonRankID\":7000,\"dataResourceName\":\"Kings Park Botanical Gardens\",\"raw_basisOfRecord\":\"Preserved Specimen\",\"stateProvince\":\"Western Australia\",\"decimalLongitude\":115.928611,\"raw_collectionCode\":\"KPBG\",\"corrected_assertions\":\"\",\"license\":\"CC-BY 4.0 (Int)\",\"month\":\"06\",\"dataResourceUid\":\"dr21662\",\"genus\":\"Acacia\",\"left\":587708,\"point0001\":\"-32.712,115.929\",\"eventDate\":141264000000,\"taxonRank\":\"species\",\"point1\":\"-33,116\",\"collectionName\":\"Kings Park and Botanic Garden\",\"hasUserAssertions\":true,\"speciesGuid\":\"https://id.biodiversity.org.au/node/apni/2899185\",\"geospatialKosher\":\"true\",\"namesLsid\":\"Acacia obovata|https://id.biodiversity.org.au/node/apni/2899185||Plantae|Fabaceae\",\"collectionUid\":\"co32\",\"point00001\":\"-32.7122,115.9286\",\"raw_institutionCode\":\"BGPA\",\"point001\":\"-32.71,115.93\",\"right\":587708,\"raw_catalogNumber\":\"38215\",\"kingdom\":\"Plantae\",\"phylum\":\"Charophyta\",\"classs\":\"Equisetopsida\",\"institutionUid\":\"in58\",\"species\":\"Acacia obovata\",\"institutionName\":\"WA Botanic Gardens and Parks Authority\",\"family\":\"Fabaceae\",\"genusGuid\":\"https://id.biodiversity.org.au/taxon/apni/51471290\",\"open_assertions\":\"\",\"userAssertions\":\"50005\"}],\"totalRecords\":3,\"query\":\"?q=*%3A*&fq=assertion_user_id%3A666888\",\"pageSize\":300,\"queryTitle\":\"[all records]\",\"sort\":\"score\",\"dir\":\"desc\",\"startIndex\":0,\"activeFacetMap\":{\"assertion_user_id\":{\"displayName\":\"Assertions by user:Luke Skywalker\",\"name\":\"assertion_user_id\",\"value\":\"666888\"}},\"activeFacetObj\":{\"assertion_user_id\":[{\"displayName\":\"Assertions by user:Luke Skywalker\",\"name\":\"assertion_user_id\",\"value\":\"assertion_user_id:666888\"}]},\"urlParameters\":\"?q=*%3A*&fq=assertion_user_id%3A666888\",\"facetResults\":[],\"status\":\"OK\"}"
        def jsonPath = "\$.occurrences[*]"
        when:
        def result = assertionService.diff(previous, current, jsonPath )

        then:
        result.size() == 2
    }

    void "should collect the verified assertions from multiple annotations"() {
        given:
        def previous = "{}"
        def current = "{\"occurrences\":[{\"country\":\"Australia\",\"scientificName\":\"Acacia idiomorpha\",\"year\":1962,\"decimalLatitude\":-27.75,\"uuid\":\"539bd9f9-95bd-47b6-b50f-bf75089b9521\",\"point01\":\"-27.8,114.2\",\"basisOfRecord\":\"PRESERVED_SPECIMEN\",\"raw_scientificName\":\"Acacia idiomorpha Benth.\",\"taxonConceptID\":\"https://id.biodiversity.org.au/node/apni/2902734\",\"latLong\":\"-27.75,114.166667\",\"user_assertions\":[{\"qaStatus\":50005,\"problemAsserted\":false,\"code\":20019,\"dataResourceUid\":\"dr21662\",\"referenceRowKey\":\"539bd9f9-95bd-47b6-b50f-bf75089b9521\",\"created\":\"2023-11-14T04:43:35Z\",\"name\":\"userAssertionOther\",\"userDisplayName\":\"Qifeng Bai\",\"comment\":\"Alert test\",\"uuid\":\"340d3c4e-4979-45a3-934d-77f7cc1e9b53\",\"userId\":\"48604\"},{\"qaStatus\":50005,\"problemAsserted\":false,\"code\":19,\"dataResourceUid\":\"dr21662\",\"referenceRowKey\":\"539bd9f9-95bd-47b6-b50f-bf75089b9521\",\"created\":\"2024-10-18T01:00:55Z\",\"name\":\"habitatMismatch\",\"userDisplayName\":\"Qifeng Bai\",\"comment\":\"Second annotations for testing\",\"uuid\":\"a2aec599-dd50-4c8d-b7d9-d3a0adf76839\",\"userId\":\"48604\"},{\"qaStatus\":50005,\"problemAsserted\":false,\"code\":20,\"dataResourceUid\":\"dr21662\",\"referenceRowKey\":\"539bd9f9-95bd-47b6-b50f-bf75089b9521\",\"created\":\"2024-10-18T01:01:26Z\",\"name\":\"detectedOutlier\",\"userDisplayName\":\"Qifeng Bai\",\"comment\":\"Second flags for alerts\",\"uuid\":\"d22ad750-0856-4a2b-a76d-d3bbbeab29fe\",\"userId\":\"48604\"},{\"qaStatus\":50005,\"problemAsserted\":false,\"code\":0,\"dataResourceUid\":\"dr21662\",\"referenceRowKey\":\"539bd9f9-95bd-47b6-b50f-bf75089b9521\",\"created\":\"2024-10-18T00:56:08Z\",\"name\":\"geospatialIssue\",\"userDisplayName\":\"Kylie Morrow\",\"comment\":\"TEST flag for ALA alerts\",\"uuid\":\"3b67477e-68a2-49c4-86ee-fad96aaade5a\",\"userId\":\"56565\"},{\"relatedUuid\":\"a2aec599-dd50-4c8d-b7d9-d3a0adf76839\",\"qaStatus\":50003,\"problemAsserted\":false,\"code\":50000,\"dataResourceUid\":\"dr21662\",\"referenceRowKey\":\"539bd9f9-95bd-47b6-b50f-bf75089b9521\",\"created\":\"2024-10-18T01:35:55Z\",\"userDisplayName\":\"Kylie Morrow\",\"comment\":\"TEST verification for Alerts test by KM\",\"uuid\":\"b9ba6f0f-4299-44a6-ba7b-551013780a24\",\"userId\":\"56565\"},{\"relatedUuid\":\"3b67477e-68a2-49c4-86ee-fad96aaade5a\",\"qaStatus\":50001,\"problemAsserted\":false,\"code\":50000,\"dataResourceUid\":\"dr21662\",\"referenceRowKey\":\"539bd9f9-95bd-47b6-b50f-bf75089b9521\",\"created\":\"2024-10-18T00:59:13Z\",\"userDisplayName\":\"Qifeng Bai\",\"comment\":\"Verified By Bai for Alerts test\",\"uuid\":\"cf6ee3f5-487a-4f9a-84da-c20b94cf99d7\",\"userId\":\"48604\"},{\"relatedUuid\":\"340d3c4e-4979-45a3-934d-77f7cc1e9b53\",\"qaStatus\":50002,\"problemAsserted\":false,\"code\":50000,\"dataResourceUid\":\"dr21662\",\"referenceRowKey\":\"539bd9f9-95bd-47b6-b50f-bf75089b9521\",\"created\":\"2024-10-17T03:55:43Z\",\"userDisplayName\":\"Qifeng Bai\",\"comment\":\"Confirmed for alert test purpose\",\"uuid\":\"ebea3d0b-ac77-48fd-b599-ed919027ccee\",\"userId\":\"48604\"}],\"assertions\":[\"MISSING_TAXONRANK\",\"GEODETIC_DATUM_ASSUMED_WGS84\",\"MISSING_GEODETICDATUM\",\"COORDINATE_UNCERTAINTY_METERS_INVALID\",\"MISSING_GEOREFERENCE_DATE\",\"MISSING_GEOREFERENCEDBY\",\"MISSING_GEOREFERENCEPROTOCOL\",\"MISSING_GEOREFERENCESOURCES\",\"MISSING_GEOREFERENCEVERIFICATIONSTATUS\"],\"verified_assertions\":\"340d3c4e-4979-45a3-934d-77f7cc1e9b53\",\"speciesGroups\":[\"Plants\",\"Flowering plants\",\"Dicots\"],\"spatiallyValid\":true,\"order\":\"Fabales\",\"taxonRankID\":7000,\"dataResourceName\":\"Kings Park Botanical Gardens\",\"raw_basisOfRecord\":\"Preserved Specimen\",\"stateProvince\":\"Western Australia\",\"decimalLongitude\":114.166667,\"raw_collectionCode\":\"KPBG\",\"corrected_assertions\":\"a2aec599-dd50-4c8d-b7d9-d3a0adf76839\",\"license\":\"CC-BY 4.0 (Int)\",\"month\":\"09\",\"dataResourceUid\":\"dr21662\",\"genus\":\"Acacia\",\"left\":587962,\"point0001\":\"-27.75,114.167\",\"eventDate\":-229219200000,\"processed_assertions\":[{\"relatedUuid\":\"a2aec599-dd50-4c8d-b7d9-d3a0adf76839\",\"qaStatus\":50003,\"problemAsserted\":false,\"code\":50000,\"dataResourceUid\":\"dr21662\",\"referenceRowKey\":\"539bd9f9-95bd-47b6-b50f-bf75089b9521\",\"created\":\"2024-10-18T01:35:55Z\",\"userDisplayName\":\"Kylie Morrow\",\"comment\":\"TEST verification for Alerts test by KM\",\"uuid\":\"b9ba6f0f-4299-44a6-ba7b-551013780a24\",\"userId\":\"56565\"},{\"relatedUuid\":\"340d3c4e-4979-45a3-934d-77f7cc1e9b53\",\"qaStatus\":50002,\"problemAsserted\":false,\"code\":50000,\"dataResourceUid\":\"dr21662\",\"referenceRowKey\":\"539bd9f9-95bd-47b6-b50f-bf75089b9521\",\"created\":\"2024-10-17T03:55:43Z\",\"userDisplayName\":\"Qifeng Bai\",\"comment\":\"Confirmed for alert test purpose\",\"uuid\":\"ebea3d0b-ac77-48fd-b599-ed919027ccee\",\"userId\":\"48604\"}],\"taxonRank\":\"species\",\"point1\":\"-28,114\",\"collectionName\":\"Kings Park and Botanic Garden\",\"hasUserAssertions\":true,\"speciesGuid\":\"https://id.biodiversity.org.au/node/apni/2902734\",\"geospatialKosher\":\"true\",\"namesLsid\":\"Acacia idiomorpha|https://id.biodiversity.org.au/node/apni/2902734||Plantae|Fabaceae\",\"collectionUid\":\"co32\",\"point00001\":\"-27.75,114.1667\",\"raw_institutionCode\":\"BGPA\",\"point001\":\"-27.75,114.17\",\"right\":587962,\"raw_catalogNumber\":\"37888\",\"kingdom\":\"Plantae\",\"phylum\":\"Charophyta\",\"classs\":\"Equisetopsida\",\"institutionUid\":\"in58\",\"species\":\"Acacia idiomorpha\",\"institutionName\":\"WA Botanic Gardens and Parks Authority\",\"family\":\"Fabaceae\",\"genusGuid\":\"https://id.biodiversity.org.au/taxon/apni/51471290\",\"open_assertions\":\"\",\"userAssertions\":\"50005\"}],\"totalRecords\":3,\"query\":\"?q=*%3A*&fq=assertion_user_id%3A48604\",\"pageSize\":300,\"queryTitle\":\"[all records]\",\"sort\":\"score\",\"dir\":\"desc\",\"startIndex\":0,\"activeFacetMap\":{\"assertion_user_id\":{\"displayName\":\"Assertions by user:Qifeng Bai\",\"name\":\"assertion_user_id\",\"value\":\"48604\"}},\"activeFacetObj\":{\"assertion_user_id\":[{\"displayName\":\"Assertions by user:Qifeng Bai\",\"name\":\"assertion_user_id\",\"value\":\"assertion_user_id:48604\"}]},\"urlParameters\":\"?q=*%3A*&fq=assertion_user_id%3A48604\",\"facetResults\":[],\"status\":\"OK\"}"
        def jsonPath = "\$.occurrences[*]"
        when:
        def result = assertionService.diff(previous, current, jsonPath )

        then:
        result.size() == 1
        result.processed_assertions.size() == 1
    }
}
