package au.org.ala.alerts

import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

class NotificationServiceSpec extends Specification implements ServiceUnitTest<NotificationService> {
    def notificationService

    def setup() {
        notificationService = new NotificationService()
    }

    def "test url cleanup"() {
        when:
        def output = notificationService.cleanUpUrl(input)

        then:
        output == expected

        where:
        input | expected
        "non url" || "non url"
        "https://url.without/query[params ]" || "https://url.without/query[params ]"
        "https://url.with/emtpy-query-params?" || "https://url.with/emtpy-query-params?"
        "https://url.with/emtpy-query-params?non-value-param1&non value:param2" || "https://url.with/emtpy-query-params?non-value-param1&non%20value%3Aparam2"
        "https://url.with/query-param?with=multiple=equals=encode=non=first&with=multiple=equals=encode=non=first" || "https://url.with/query-param?with=multiple%3Dequals%3Dencode%3Dnon%3Dfirst&with=multiple%3Dequals%3Dencode%3Dnon%3Dfirst"
        "https://example.com/path/to/resource?  p aram1 = sp  lit by spaces" || "https://example.com/path/to/resource?%20%20p%20aram1%20=%20sp%20%20lit%20by%20spaces"
        "https://example.com/path/to/resource?param1=split by spaces&param2=[has some brackets]&param3=explicitely%3Aescape%5Dspecial%20characters" || "https://example.com/path/to/resource?param1=split%20by%20spaces&param2=%5Bhas%20some%20brackets%5D&param3=explicitely%253Aescape%255Dspecial%2520characters"
        "https://biocache-ws.ala.org.au/ws/occurrences/search?q=institution_uid:in82&qualityProfile=ALA&qc=-_nest_parent_:*&fq=(user_assertions:50005 OR user_assertions:50003 OR user_assertions:50002)&fq=last_assertion_date:[2025-03-20T03:59:44Z TO *]&sort=last_assertion_date&dir=desc&pageSize=20&facets=basis_of_record" || "https://biocache-ws.ala.org.au/ws/occurrences/search?q=institution_uid%3Ain82&qualityProfile=ALA&qc=-_nest_parent_%3A*&fq=%28user_assertions%3A50005%20OR%20user_assertions%3A50003%20OR%20user_assertions%3A50002%29&fq=last_assertion_date%3A%5B2025-03-20T03%3A59%3A44Z%20TO%20*%5D&sort=last_assertion_date&dir=desc&pageSize=20&facets=basis_of_record"
    }
}
