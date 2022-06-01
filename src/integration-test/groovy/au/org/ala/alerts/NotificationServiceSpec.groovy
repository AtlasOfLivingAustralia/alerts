package au.org.ala.alerts

import au.org.ala.alerts.Frequency
import au.org.ala.alerts.Notification
import au.org.ala.alerts.Query
import au.org.ala.alerts.User
import com.jayway.jsonpath.JsonPath
import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import grails.util.Holders
import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Ignore
import spock.lang.Specification

import java.text.SimpleDateFormat

@Integration
@Rollback
@Ignore
// commented out test cases because they are data dependent, could break the build
// please uncomment the test cases and setup() and run locally
class NotificationServiceSpec extends Specification {

    @Autowired
    NotificationService service

    @Autowired
    DiffService diffService

    def messageSource

    def setup() {
        User user = new User([email: 'test.alerts@csiro.au', userId: 1234, locked: false, frequency: Frequency.findByName("hourly")])
        user.save(flush: true)
    }

    def cleanup() {
    }

    void "test set up database"() {
        def users = User.count()
        // predefined Queries created by BootStrap
        def queries = Query.findAllByCustom(false)
        def pPaths = PropertyPath.count()
        def pValues = PropertyValue.count()
        def qResults = QueryResult.count()

        expect: "correct entry numbers in the database"
        users == 1
        queries.size() == 9
        pPaths == 14
        pValues == 0
        qResults == 0
    }

    void "test annotations hourly checkStatus"() {
        // checkQueryForFrequency(String frequencyName)
        given:
        // preconditions: 1. hourly Queries exist; 2. User(s) subscribed to hourly Annotations; 3. User is not locked.
        Query annotations = Query.findByName(messageSource.getMessage("query.annotations.title", null,
                new Locale.Builder().setLanguageTag(Holders.config.siteDefaultLanguage as String).build()))
        Frequency hourly = Frequency.findByName("hourly")

        Notification notification = new Notification()
        notification.query = annotations
        notification.user = User.findById(1)
        notification.save(flush: true)

        def users = Query.executeQuery(
                """select u.email, max(u.unsubscribeToken), max(n.unsubscribeToken)
                  from User u
                  inner join u.notifications n
                  where n.query = :query
                  and u.frequency = :frequency
                  and (u.locked is null or u.locked != 1)
                  group by u""", [query: annotations, frequency: hourly])

        assert annotations != null
        assert hourly != null
        assert notification != null
        assert users.size() > 0

        when:
        // check if any change (totalRecords > 0 in returned Json) since last run
        def changed = service.checkStatus(annotations, hourly)

        def urls = service.getQueryUrl(annotations, hourly)
        def json = IOUtils.toString(new URL(urls.first()).newReader())
        def totalRecords = JsonPath.read(json, "totalRecords")

        then:
        changed == (totalRecords > 0)
    }

    void "test sending email for annotations hourly"() {
        // checkQueryForFrequency(String frequencyName)
        given:
        // preconditions: 1. hourly Queries exist; 2. User(s) subscribed to hourly Annotations; 3. User is not locked.
        Frequency hourly = Frequency.findByName("hourly")
        Query annotations = Query.findByName(messageSource.getMessage("query.annotations.title", null,
                new Locale.Builder().setLanguageTag(Holders.config.siteDefaultLanguage as String).build()))

        // update Query queryPath to search annotations of past 60 days, increase it if no annotations found
        def dateToUse = org.apache.commons.lang.time.DateUtils.addDays(new Date(), -60)
        SimpleDateFormat sdf = new SimpleDateFormat(annotations.dateFormat)
        def updatedQP = annotations.queryPath.replaceAll("___DATEPARAM___", sdf.format(dateToUse))
        annotations.queryPath = updatedQP
        annotations.save(flush: true)

        Notification notification = new Notification()
        notification.query = annotations
        notification.user = User.findById(1)
        notification.save(flush: true)

        def users = Query.executeQuery(
                """select u.email, max(u.unsubscribeToken), max(n.unsubscribeToken)
                  from User u
                  inner join u.notifications n
                  where n.query = :query
                  and u.frequency = :frequency
                  and (u.locked is null or u.locked != 1)
                  group by u""", [query: annotations, frequency: hourly])

        assert annotations != null
        assert hourly != null
        assert notification != null
        assert users.size() > 0

        when:
        def recipients = service.checkQueryForFrequency(hourly, true)
        then: recipients.size() > 0
    }

    void "test sending email for new records hourly"() {
        // checkQueryForFrequency(String frequencyName)
        given:
        // preconditions: 1. hourly Queries exist; 2. User(s) subscribed to hourly Annotations; 3. User is not locked.
        Frequency hourly = Frequency.findByName("hourly")
        Query newRecords = Query.findByName(messageSource.getMessage("query.new.records.title", null,
                new Locale.Builder().setLanguageTag(Holders.config.siteDefaultLanguage as String).build()))

        // update Query queryPath to search annotations of past 60 days, increase it if no annotations found
        def dateToUse = org.apache.commons.lang.time.DateUtils.addDays(new Date(), -60)
        SimpleDateFormat sdf = new SimpleDateFormat(newRecords.dateFormat)
        def updatedQP = newRecords.queryPath.replaceAll("___DATEPARAM___", sdf.format(dateToUse))
        newRecords.queryPath = updatedQP
        newRecords.save(flush: true)

        Notification notification = new Notification()
        notification.query = newRecords
        notification.user = User.findById(1)
        notification.save(flush: true)

        def users = Query.executeQuery(
                """select u.email, max(u.unsubscribeToken), max(n.unsubscribeToken)
                  from User u
                  inner join u.notifications n
                  where n.query = :query
                  and u.frequency = :frequency
                  and (u.locked is null or u.locked != 1)
                  group by u""", [query: newRecords, frequency: hourly])

        assert newRecords != null
        assert hourly != null
        assert notification != null
        assert users.size() > 0

        when:
        def recipients = service.checkQueryForFrequency(hourly, true)
        then: recipients.size() > 0
    }

    void "test sending email for new images hourly"() {
        // checkQueryForFrequency(String frequencyName)
        given:
        // preconditions: 1. hourly Queries exist; 2. User(s) subscribed to hourly Annotations; 3. User is not locked.
        Frequency hourly = Frequency.findByName("hourly")
        Query newImages = Query.findByName(messageSource.getMessage("query.new.images.title", null,
                new Locale.Builder().setLanguageTag(Holders.config.siteDefaultLanguage as String).build()))

        // update Query queryPath to search annotations of past 60 days, increase it if no annotations found
        def dateToUse = org.apache.commons.lang.time.DateUtils.addDays(new Date(), -60)
        SimpleDateFormat sdf = new SimpleDateFormat(newImages.dateFormat)
        def updatedQP = newImages.queryPath.replaceAll("___DATEPARAM___", sdf.format(dateToUse))
        newImages.queryPath = updatedQP
        newImages.save(flush: true)

        Notification notification = new Notification()
        notification.query = newImages
        notification.user = User.findById(1)
        notification.save(flush: true)

        def users = Query.executeQuery(
                """select u.email, max(u.unsubscribeToken), max(n.unsubscribeToken)
                  from User u
                  inner join u.notifications n
                  where n.query = :query
                  and u.frequency = :frequency
                  and (u.locked is null or u.locked != 1)
                  group by u""", [query: newImages, frequency: hourly])

        assert newImages != null
        assert hourly != null
        assert notification != null
        assert users.size() > 0

        when:
        def recipients = service.checkQueryForFrequency(hourly, true)
        then: recipients.size() > 0
    }

    void "test sending email for citizen science records hourly"() {
        // checkQueryForFrequency(String frequencyName)
        given:
        // preconditions: 1. hourly Queries exist; 2. User(s) subscribed to hourly Annotations; 3. User is not locked.
        Frequency hourly = Frequency.findByName("hourly")
        Query citizen = Query.findByName(messageSource.getMessage("query.citizen.records.title", null,
                new Locale.Builder().setLanguageTag(Holders.config.siteDefaultLanguage as String).build()))

        // update Query queryPath to search annotations of past 60 days, increase it if no annotations found
        def dateToUse = org.apache.commons.lang.time.DateUtils.addDays(new Date(), -30*7)
        SimpleDateFormat sdf = new SimpleDateFormat(citizen.dateFormat)
        def updatedQP = citizen.queryPath.replaceAll("___DATEPARAM___", sdf.format(dateToUse))
        citizen.queryPath = updatedQP
        citizen.save(flush: true)

        Notification notification = new Notification()
        notification.query = citizen
        notification.user = User.findById(1)
        notification.save(flush: true)

        def users = Query.executeQuery(
                """select u.email, max(u.unsubscribeToken), max(n.unsubscribeToken)
                  from User u
                  inner join u.notifications n
                  where n.query = :query
                  and u.frequency = :frequency
                  and (u.locked is null or u.locked != 1)
                  group by u""", [query: citizen, frequency: hourly])

        assert citizen != null
        assert hourly != null
        assert notification != null
        assert users.size() > 0

        when:
        def recipients = service.checkQueryForFrequency(hourly, true)
        then: recipients.size() > 0
    }

    void "test sending email for citizen science records with images hourly"() {
        // checkQueryForFrequency(String frequencyName)
        given:
        // preconditions: 1. hourly Queries exist; 2. User(s) subscribed to hourly Annotations; 3. User is not locked.
        Frequency hourly = Frequency.findByName("hourly")
        Query newCitizenImages = Query.findByName(messageSource.getMessage("query.citizen.records.imgs.title", null,
                new Locale.Builder().setLanguageTag(Holders.config.siteDefaultLanguage as String).build()))

        // update Query queryPath to search annotations of past 60 days, increase it if no annotations found
        def dateToUse = org.apache.commons.lang.time.DateUtils.addDays(new Date(), -30*17)
        SimpleDateFormat sdf = new SimpleDateFormat(newCitizenImages.dateFormat)
        def updatedQP = newCitizenImages.queryPath.replaceAll("___DATEPARAM___", sdf.format(dateToUse))
        newCitizenImages.queryPath = updatedQP
        newCitizenImages.save(flush: true)

        Notification notification = new Notification()
        notification.query = newCitizenImages
        notification.user = User.findById(1)
        notification.save(flush: true)

        def users = Query.executeQuery(
                """select u.email, max(u.unsubscribeToken), max(n.unsubscribeToken)
                  from User u
                  inner join u.notifications n
                  where n.query = :query
                  and u.frequency = :frequency
                  and (u.locked is null or u.locked != 1)
                  group by u""", [query: newCitizenImages, frequency: hourly])

        assert newCitizenImages != null
        assert hourly != null
        assert notification != null
        assert users.size() > 0

        when:
        def recipients = service.checkQueryForFrequency(hourly, true)
        then: recipients.size() > 0
    }

    void "test sending email for datasets hourly"() {
        given:
        // preconditions: 1. hourly Queries exist; 2. User(s) subscribed to hourly Annotations; 3. User is not locked.
        Frequency hourly = Frequency.findByName("hourly")
        Query datasets = Query.findByName(messageSource.getMessage("query.datasets.title", null,
                new Locale.Builder().setLanguageTag(Holders.config.siteDefaultLanguage as String).build()))

        Notification notification = new Notification()
        notification.query = datasets
        notification.user = User.findById(1)
        notification.save(flush: true)

        def users = Query.executeQuery(
                """select u.email, max(u.unsubscribeToken), max(n.unsubscribeToken)
                  from User u
                  inner join u.notifications n
                  where n.query = :query
                  and u.frequency = :frequency
                  and (u.locked is null or u.locked != 1)
                  group by u""", [query: datasets, frequency: hourly])

        assert datasets != null
        assert hourly != null
        assert notification != null
        assert users.size() > 0

        def layersJson = IOUtils.toString(new URL("https://collections.ala.org.au/ws/dataResource").newReader())
//         previousResult is null the very first time, so run checkStatus to populate it
        service.checkStatus(datasets, hourly)
        QueryResult queryResult = service.getQueryResult(datasets, hourly)
        // manually change saved Json to trigger email sending
        queryResult.lastResult = service.gzipResult(layersJson)
        queryResult.save(flush: true)

        when:
        def recipients = service.checkQueryForFrequency(hourly, true)
        then: recipients.size() > 0
    }

    void "test sending email for species lists"() {
        given:
        // preconditions: 1. hourly Queries exist; 2. User(s) subscribed to hourly Annotations; 3. User is not locked.
        Frequency hourly = Frequency.findByName("hourly")
        Query lists = Query.findByName(messageSource.getMessage("query.species.lists.title", null,
                new Locale.Builder().setLanguageTag(Holders.config.siteDefaultLanguage as String).build()))

        Notification notification = new Notification()
        notification.query = lists
        notification.user = User.findById(1)
        notification.save(flush: true)

        def users = Query.executeQuery(
                """select u.email, max(u.unsubscribeToken), max(n.unsubscribeToken)
                  from User u
                  inner join u.notifications n
                  where n.query = :query
                  and u.frequency = :frequency
                  and (u.locked is null or u.locked != 1)
                  group by u""", [query: lists, frequency: hourly])

        assert lists != null
        assert hourly != null
        assert notification != null
        assert users.size() > 0

        def layersJson = IOUtils.toString(new URL("https://collections.ala.org.au/ws/dataResource?resourceType=species-list").newReader())
        // previousResult is null the very first time, so run checkStatus to populate it
        service.checkStatus(lists, hourly)
        QueryResult queryResult = service.getQueryResult(lists, hourly)
        // manually change saved Json to trigger email sending
        queryResult.lastResult = service.gzipResult(layersJson)
        queryResult.save(flush: true)

        when:
        def recipients = service.checkQueryForFrequency(hourly, true)
        then: recipients.size() > 0
    }

    void "test sending email for spatial layers"() {
        given:
        // preconditions: 1. hourly Queries exist; 2. User(s) subscribed to hourly Annotations; 3. User is not locked.
        Frequency hourly = Frequency.findByName("hourly")
        Query layers = Query.findByName(messageSource.getMessage("query.spatial.layers.title", null,
                new Locale.Builder().setLanguageTag(Holders.config.siteDefaultLanguage as String).build()))

        Notification notification = new Notification()
        notification.query = layers
        notification.user = User.findById(1)
        notification.save(flush: true)

        def users = Query.executeQuery(
                """select u.email, max(u.unsubscribeToken), max(n.unsubscribeToken)
                  from User u
                  inner join u.notifications n
                  where n.query = :query
                  and u.frequency = :frequency
                  and (u.locked is null or u.locked != 1)
                  group by u""", [query: layers, frequency: hourly])

        assert layers != null
        assert hourly != null
        assert notification != null
        assert users.size() > 0

        def layersJson = IOUtils.toString(new URL("https://spatial-test.ala.org.au/ws/layers.json").newReader())
        // previousResult is null the very first time, so run checkStatus to populate it
        service.checkStatus(layers, hourly)
        QueryResult queryResult = service.getQueryResult(layers, hourly)
        // manually change saved Json to trigger email sending
        queryResult.lastResult = service.gzipResult(layersJson)
        queryResult.save(flush: true)

        when:
        def recipients = service.checkQueryForFrequency(hourly, true)
        then: recipients.size() > 0
    }

    void "test sending email for news hourly"() {
        // checkQueryForFrequency(String frequencyName)
        given:
        // preconditions: 1. hourly Queries exist; 2. User(s) subscribed to hourly Annotations; 3. User is not locked.
        Frequency hourly = Frequency.findByName("hourly")
        Query blogs = Query.findByName(messageSource.getMessage("query.ala.blog.title", null,
                new Locale.Builder().setLanguageTag(Holders.config.siteDefaultLanguage as String).build()))

        Notification notification = new Notification()
        notification.query = blogs
        notification.user = User.findById(1)
        notification.save(flush: true)

        def users = Query.executeQuery(
                """select u.email, max(u.unsubscribeToken), max(n.unsubscribeToken)
                  from User u
                  inner join u.notifications n
                  where n.query = :query
                  and u.frequency = :frequency
                  and (u.locked is null or u.locked != 1)
                  group by u""", [query: blogs, frequency: hourly])

        assert blogs != null
        assert hourly != null
        assert notification != null
        assert users.size() > 0

        def blogsJson = """
                 {"id":44342,"date":"2020-09-30T10:09:11","date_gmt":"2020-09-30T00:09:11","guid":{"rendered":"https://www.ala.org.au/?p=44342"},"modified":"2020-09-30T10:09:12","modified_gmt":"2020-09-30T00:09:12","slug":"data-in-the-ala-bushfire-affected-areas-2019-2020-bushfire-season","status":"publish","type":"post","link":"https://www.ala.org.au/blogs-news/data-in-the-ala-bushfire-affected-areas-2019-2020-bushfire-season/","title":{"rendered":"Data in the ALA: bushfire affected areas (2019-2020 bushfire season)"}}
                 """.stripIndent()
        // previousResult is null the very first time, so run checkStatus to populate it
        service.checkStatus(blogs, hourly)
        QueryResult queryResult = service.getQueryResult(blogs, hourly)
        // manually change saved Json to trigger email sending
        queryResult.lastResult = service.gzipResult(blogsJson)
        queryResult.save(flush: true)

        when:
        def recipients = service.checkQueryForFrequency(hourly, true)
        then: recipients.size() > 0
    }
}
