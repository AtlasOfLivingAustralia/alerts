package au.org.ala.alerts

import au.org.ala.alerts.biosecurity.AdminController as BiosecurityAdminController
import au.org.ala.alerts.biosecurity.CsvController
import grails.testing.web.UrlMappingsUnitTest
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Guards the /biosecurity routes, which are declared inside a nested url mapping group and target
 * namespaced controllers. Namespaced controllers are not reachable via the default
 * "/$controller/$action" mapping, so every one of these URLs only works while its explicit entry
 * exists - a rename or a missing entry fails as "no endpoint" rather than a compile error.
 */
class UrlMappingsSpec extends Specification implements UrlMappingsUnitTest<UrlMappings> {

    def setup() {
        mockController(BiosecurityAdminController)
        mockController(CsvController)
    }

    @Unroll
    void "GET #url maps to biosecurity/admin##action"() {
        expect:
        assertForwardUrlMapping(url, controller: 'admin', action: action, namespace: 'biosecurity')

        where:
        url                                    | action
        '/biosecurity'                         | 'index'
        '/biosecurity/list'                    | 'list'
        '/biosecurity/delete'                  | 'delete'
        '/biosecurity/unsubscribe'             | 'unsubscribe'
        '/biosecurity/subscribe'               | 'addSubscribers'
        '/biosecurity/subscribers'             | 'getSubscribers'
        '/biosecurity/search'                  | 'search'
        '/biosecurity/triggerAlerts'           | 'triggerAlerts'
        '/biosecurity/triggerAlert'            | 'triggerAlert'
        '/biosecurity/triggerAlertSince'       | 'triggerAlertSince'
        '/biosecurity/getBioSecurityQuery'     | 'getBioSecurityQuery'
        '/biosecurity/countBioSecurityQuery'   | 'countBioSecurityQuery'
        '/biosecurity/newSubscription'         | 'newSubscription'
        '/biosecurity/preview'                 | 'preview'
        '/biosecurity/more'                    | 'moreSubscriptions'
    }

    @Unroll
    void "the .json suffix still resolves for #url"() {
        expect:
        assertForwardUrlMapping(url, controller: 'admin', action: action, namespace: 'biosecurity')

        where:
        url                              | action
        '/biosecurity/delete.json'       | 'delete'
        '/biosecurity/unsubscribe.json'  | 'unsubscribe'
        '/biosecurity/subscribe.json'    | 'addSubscribers'
        '/biosecurity/subscribers.json'  | 'getSubscribers'
    }

    @Unroll
    void "the optional id may be given as a path segment - #url"() {
        expect:
        assertForwardUrlMapping(url, controller: 'admin', action: action, namespace: 'biosecurity') {
            id = '309'
        }

        where:
        url                                  | action
        '/biosecurity/triggerAlert/309'      | 'triggerAlert'
        '/biosecurity/triggerAlertSince/309' | 'triggerAlertSince'
        '/biosecurity/preview/309'           | 'preview'
        '/biosecurity/newSubscription/309'   | 'newSubscription'
    }

    @Unroll
    void "the nested csv group still resolves #url"() {
        expect:
        assertForwardUrlMapping(url, controller: 'csv', action: action, namespace: 'biosecurity')

        where:
        url                              | action
        '/biosecurity/csv'               | 'list'
        '/biosecurity/csv/download'      | 'download'
        '/biosecurity/csv/delete'        | 'delete'
        '/biosecurity/csv/aggregate'     | 'aggregate'
        '/biosecurity/csv/downloads'     | 'downloads'
    }
}


