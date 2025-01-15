package au.org.ala.alerts
import com.jayway.jsonpath.JsonPath
import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import grails.util.Holders
import groovy.json.JsonBuilder
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.time.DateUtils
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Ignore
import spock.lang.Specification

import java.text.SimpleDateFormat

@Integration
@Rollback
/*
 * Note: The @Ignore annotation below prevents the execution of the NotificationServiceSpec during build or explicit execution of integration tests.
 * Please comment out @Ignore in order to run the integration tests locally.
 *
 * Since most of test cases contained in NotificationServiceSpec are dependant on data from external services, failure to resolve any of the services will fail the tests and break the application build.
 *
 */
@Ignore
class NotificationServiceSpec extends Specification {

    @Autowired
    NotificationService service

    @Autowired
    DiffService diffService

    def messageSource

    def setup() {
        User user = new User([email: 'test.alerts@csiro.au', userId: 1234, locked: false, frequency: Frequency.findByName("hourly")])
        user.save(flush: true)


        User user2 = new User([email: 'test2.alerts@csiro.au', userId: 12345, locked: false, frequency: Frequency.findByName("daily")])
        user2.save(flush: true)

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
        users == 2
        queries.size() == 10
        pPaths == 15
        pValues == 0
        qResults == 0
    }

    void "test annotations hourly checkStatus"() {
        given:
        // preconditions: 1. hourly Queries exist. 2. User(s) subscribed to hourly Annotations. 3. User is not locked.
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
        QueryResult qs =  service.executeQuery(annotations, hourly)
        def changed = qs.hasChanged

        def urls = service.buildQueryUrl(annotations, hourly)
        def json = IOUtils.toString(new URL(urls.first()).newReader())
        def totalRecords = JsonPath.read(json, "totalRecords")
        then:
        changed == (totalRecords > 0)
    }

    void "test sending email for annotations hourly"() {
        given:
        // preconditions: 1. hourly Queries exist. 2. User(s) subscribed to hourly Annotations. 3. User is not locked.
        Frequency hourly = Frequency.findByName("hourly")
        Query annotations = Query.findByName(messageSource.getMessage("query.annotations.title", null,
                new Locale.Builder().setLanguageTag(Holders.config.siteDefaultLanguage as String).build()))

        // update Query queryPath to search annotations of past 60 days, increase it if no annotations found
        def dateToUse = DateUtils.addDays(new Date(), -60)
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
        def recipients = service.execQueryForFrequency(hourly, true)
        then: recipients.size() > 0
    }

    void "test sending email for new records hourly"() {
        given:
        // preconditions: 1. hourly Queries exist. 2. User(s) subscribed to hourly New records . 3. User is not locked.
        Frequency hourly = Frequency.findByName("hourly")
        Query newRecords = Query.findByName(messageSource.getMessage("query.new.records.title", null,
                new Locale.Builder().setLanguageTag(Holders.config.siteDefaultLanguage as String).build()))

        // update Query queryPath to search annotations of past 60 days, increase it if no annotations found
        def dateToUse = DateUtils.addDays(new Date(), -60)
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
        def recipients = service.execQueryForFrequency(hourly, true)
        then: recipients.size() > 0
    }

    void "test sending email for new images hourly"() {
        given:
        // preconditions: 1. hourly Queries exist. 2. User(s) subscribed to hourly New images. 3. User is not locked.
        Frequency hourly = Frequency.findByName("hourly")
        Query newImages = Query.findByName(messageSource.getMessage("query.new.images.title", null,
                new Locale.Builder().setLanguageTag(Holders.config.siteDefaultLanguage as String).build()))

        // update Query queryPath to search annotations of past 60 days, increase it if no annotations found
        def dateToUse = DateUtils.addDays(new Date(), -60)
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
        def recipients = service.execQueryForFrequency(hourly, true)
        then: recipients.size() > 0
    }

    void "test sending email for citizen science records hourly"() {
        given:
        // preconditions: 1. hourly Queries exist. 2. User(s) subscribed to hourly Citizen science records. 3. User is not locked.
        Frequency hourly = Frequency.findByName("hourly")
        Query citizen = Query.findByName(messageSource.getMessage("query.citizen.records.title", null,
                new Locale.Builder().setLanguageTag(Holders.config.siteDefaultLanguage as String).build()))

        // update Query queryPath to search annotations of past 60 days, increase it if no annotations found
        def dateToUse = DateUtils.addDays(new Date(), -30*7)
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
        def recipients = service.execQueryForFrequency(hourly, true)
        then: recipients.size() > 0
    }

    void "test sending email for citizen science records with images hourly"() {
        given:
        // preconditions: 1. hourly Queries exist. 2. User(s) subscribed to hourly Citizen science records with images 3. User is not locked.
        Frequency hourly = Frequency.findByName("hourly")
        Query newCitizenImages = Query.findByName(messageSource.getMessage("query.citizen.records.imgs.title", null,
                new Locale.Builder().setLanguageTag(Holders.config.siteDefaultLanguage as String).build()))

        // update Query queryPath to search annotations of past 60 days, increase it if no annotations found
        def dateToUse = DateUtils.addDays(new Date(), -30*17)
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
        def recipients = service.execQueryForFrequency(hourly, true)
        then: recipients.size() > 0
    }

    void "test sending email for datasets hourly"() {
        given:
        // preconditions: 1. hourly Queries exist. 2. User(s) subscribed to hourly Datasets. 3. User is not locked.
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

        // sampleJson objet includes a sample of a standard JSON response as returned by the relevant query for this alert
        def sampleJson = new JsonBuilder([["name":"'A Genome' Oryza species","uri":"https://collections.ala.org.au/ws/dataResource/dr8221","uid":"dr8221000"]])

        //  previousResult is null the very first time, so run checkStatus to populate it
        service.executeQuery(datasets, hourly)
        QueryResult queryResult = service.getQueryResult(datasets, hourly)
        // manually change saved Json and saved property values to trigger email sending
        queryResult.lastResult = queryResult.compress(sampleJson.toString())
        queryResult.propertyValues.first().currentValue = "1"
        queryResult.save(flush: true)
        when:
        //  due the the values set above, a change will be detected on checkQueryForFrequency,  in turn triggering a diff email.
        def recipients = service.execQueryForFrequency(hourly, true)
        then: recipients.size() > 0
    }

    void "test sending email for datasets daily"() {
        given:
        // preconditions: 1. daily Queries exist. 2. User(s) subscribed to daily Datasets. 3. User is not locked.
        Frequency daily = Frequency.findByName("daily")
        Query datasets = Query.findByName(messageSource.getMessage("query.datasets.title", null,
                new Locale.Builder().setLanguageTag(Holders.config.siteDefaultLanguage as String).build()))

        Notification notification = new Notification()
        notification.query = datasets
        notification.user = User.findById(2)
        notification.save(flush: true)

        def users = Query.executeQuery(
                """select u.email, max(u.unsubscribeToken), max(n.unsubscribeToken)
                  from User u
                  inner join u.notifications n
                  where n.query = :query
                  and u.frequency = :frequency
                  and (u.locked is null or u.locked != 1)
                  group by u""", [query: datasets, frequency: daily])

        assert datasets != null
        assert daily != null
        assert notification != null
        assert users.size() > 0

        // sampleJson objet includes a sample of a standard JSON response as returned by the relevant query for this alert
        def sampleJson = new JsonBuilder([["name":"'A Genome' Oryza species","uri":"https://collections.ala.org.au/ws/dataResource/dr8221","uid":"dr8221000"]])

        //  previousResult is null the very first time, so run checkStatus to populate it
        service.executeQuery(datasets, daily)
        QueryResult queryResult = service.getQueryResult(datasets, daily)
        // manually change saved Json and saved property values to trigger email sending
        queryResult.lastResult = queryResult.compress(sampleJson.toString())
        queryResult.propertyValues.first().currentValue = "1"
        queryResult.save(flush: true)
        when:
        //  due the the values set above, a change will be detected on checkQueryForFrequency,  in turn triggering a diff email.
        def recipients = service.execQueryForFrequency(daily, true)
        then: recipients.size() > 0
    }

    void "test sending email for species lists hourly"() {
        given:
        // preconditions: 1. hourly  Queries exist. 2. User(s) subscribed to hourly Species lists. 3. User is not locked.
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

        // sampleJson objet includes a sample of a standard JSON response as returned by the relevant query for this alert
        def sampleJson = new JsonBuilder([["name":" River Torrens Linear Park Species List","uri":"https://collections.ala.org.au/ws/dataResource/dr14140","uid":"dr14140000"]])

        // previousResult is null the very first time, so run checkStatus to populate it
        service.executeQuery(lists, hourly)
        QueryResult queryResult = service.getQueryResult(lists, hourly)
        // manually change saved Json and saved property values to trigger email sending
        queryResult.lastResult = queryResult.compress(sampleJson.toString())
        queryResult.propertyValues.first().currentValue = "1"
        queryResult.save(flush: true)
        when:
        //  due the the values set above, a change will be detected on checkQueryForFrequency,  in turn triggering a diff email.
        def recipients = service.execQueryForFrequency(hourly, true)
        then: recipients.size() > 0
    }

    void "test sending email for species lists daily"() {
        given:
        // preconditions: 1. daily  Queries exist. 2. User(s) subscribed to daily Species lists. 3. User is not locked.
        Frequency daily = Frequency.findByName("daily")
        Query lists = Query.findByName(messageSource.getMessage("query.species.lists.title", null,
                new Locale.Builder().setLanguageTag(Holders.config.siteDefaultLanguage as String).build()))

        Notification notification = new Notification()
        notification.query = lists
        notification.user = User.findById(2)
        notification.save(flush: true)

        def users = Query.executeQuery(
                """select u.email, max(u.unsubscribeToken), max(n.unsubscribeToken)
                  from User u
                  inner join u.notifications n
                  where n.query = :query
                  and u.frequency = :frequency
                  and (u.locked is null or u.locked != 1)
                  group by u""", [query: lists, frequency: daily])

        assert lists != null
        assert daily != null
        assert notification != null
        assert users.size() > 0

        // sampleJson objet includes a sample of a standard JSON response as returned by the relevant query for this alert
        def sampleJson = new JsonBuilder([["name":" River Torrens Linear Park Species List","uri":"https://collections.ala.org.au/ws/dataResource/dr14140","uid":"dr14140000"]])

        // previousResult is null the very first time, so run checkStatus to populate it
        service.executeQuery(lists, daily)
        QueryResult queryResult = service.getQueryResult(lists, daily)
        // manually change saved Json and saved property values to trigger email sending
        queryResult.lastResult = queryResult.compress(sampleJson.toString())
        queryResult.propertyValues.first().currentValue = "1"
        queryResult.save(flush: true)
        when:
        //  due the the values set above, a change will be detected on checkQueryForFrequency,  in turn triggering a diff email.
        def recipients = service.execQueryForFrequency(daily, true)
        then: recipients.size() > 0
    }

    void "test sending email for spatial layers"() {
        given:
        // preconditions: 1. hourly Queries exist. 2. User(s) subscribed to hourly Spatial layers. 3. User is not locked.
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

        // sampleJson objet includes a sample of a standard JSON response as returned by the relevant query for this alert
        def sampleJson = new JsonBuilder([["path_1km":"","source_link":"","datalang":"","environmentalvaluemin":"0","shape":false,"lookuptablepath":"","respparty_role":"","classification2":"Phylogenetic Diversity","pid":"","displayname":"Mammals – Fritz et al 2009 - 0.5 degree","id":2043,"grid":true,"path_orig":"layer/phylo_tree_4152","mdhrlv":"","minlongitude":112.0,"maxlatitude":-11.5,"licence_link":"","mddatest":"","maxlongitude":154.5,"dt_added":1428847200000,"minlatitude":-44.0,"scale":"","displaypath":"https://spatial-test.ala.org.au/geoserver/geoserver/gwc/service/wms?service=WMS&version=1.1.0&request=GetMap&layers=ALA:phylo_tree_4152&format=image/png&styles=","path":"/data/ala/data/layers/ready/diva","name":"test name  for integration testing - this should not match anything","metadatapath":"http://www.ala.org.au/","type":"Environmental","notes":"Fritz, S.A., Bininda-Emonds, O.R.P. & Purvis, A. (2009) Geographical variation in predictors of mammalian extinction risk: Big is bad, but only in the tropics. Ecology Letters, 12, 538-549","keywords":"","uid":"2043","licence_notes":"","environmentalvaluemax":"0.87019997835159302","classification1":"Biodiversity","environmentalvalueunits":"","enabled":true,"domain":"Terrestrial,Marine","description":"Mammals – Fritz et al 2009","licence_level":"0","source":"ALA-SPATIAL","citation_date":""]])

        //  previousResult is null the very first time, so run checkStatus to populate it
        service.executeQuery(layers, hourly)
        QueryResult queryResult = service.getQueryResult(layers, hourly)
        // manually change saved Json and saved property values to trigger email sending
        queryResult.lastResult = queryResult.compress(sampleJson.toString())
        queryResult.propertyValues.first().currentValue = "1"
        queryResult.save(flush: true)
        when:
        //  due the the values set above, a change will be detected on checkQueryForFrequency, in turn triggering a diff email.
        def recipients = service.execQueryForFrequency(hourly, true)
        then: recipients.size() > 0
    }

    void "test sending email for news hourly"() {

        given:
        // preconditions: 1. hourly Queries exist. 2. User(s) subscribed to hourly Blogs and News. 3. User is not locked.
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

        // sampleJsonString is a partial stingified sample of a standard JSON response as returned by the relevant query for this alert
        def sampleJsonString = """[{"id":46796000,"date":"2022-05-04T11:50:49","date_gmt":"2022-05-04T01:50:49","guid":{"rendered":"https:\\/\\/www.ala.org.au\\/?p=46796"},"modified":"2022-05-18T16:29:30","modified_gmt":"2022-05-18T06:29:30","slug":"ala-labs-new-site-offering-technical-solutions-to-scientific-problems","status":"publish","type":"post","link":"https:\\/\\/www.ala.org.au\\/blogs-news\\/ala-labs-new-site-offering-technical-solutions-to-scientific-problems\\/","title":{"rendered":"ALA Labs: new site offering technical solutions to scientific problems\\u00a0"},"content":{"rendered":"\\n<div class=\\"wp-block-image\\"><figure class=\\"alignright size-full is-resized\\"><img loading=\\"lazy\\" src=\\"https:\\/\\/www.ala.org.au\\/app\\/uploads\\/2022\\/05\\/chrome_2022-05-04_10-51-16.png\\" alt=\\"\\" class=\\"wp-image-46800\\" width=\\"386\\" height=\\"257\\" srcset=\\"https:\\/\\/www.ala.org.au\\/app\\/uploads\\/2022\\/05\\/chrome_2022-05-04_10-51-16.png 783w, https:\\/\\/www.ala.org.au\\/app\\/uploads\\/2022\\/05\\/chrome_2022-05-04_10-51-16-300x200.png 300w, https:\\/\\/www.ala.org.au\\/app\\/uploads\\/2022\\/05\\/chrome_2022-05-04_10-51-16-768x511.png 768w\\" sizes=\\"(max-width: 386px) 100vw, 386px\\" \\/><\\/figure><\\/div>\\n\\n\\n\\n<p>Do you conduct research using Australian biodiversity data? Do you analyse data or make data visualisations?<\\/p>\\n\\n\\n\\n<p>ALA\\u2019s newest website, <a href=\\"https:\\/\\/labs.ala.org.au\\/\\">ALA Labs<\\/a>, is here to help. ALA Labs provides technical know-how and detailed solutions to specific scientific problems using data in the ALA.<\\/p>\\n\\n\\n\\n<p><strong>Learn how with ALA Labs posts<\\/strong><\\/p>\\n\\n\\n\\n<p>The main showcase of ALA Labs is the <strong>Posts <\\/strong>section. Posts are \\u201chow-to\\u201d articles on how to conduct specific statistical analyses or how to create data visualisations. Each post provides step-by-step commentary on how to do a specific task along with code so you can reproduce the analysis.<\\/p>\\n\\n\\n\\n<p><\\/p>\\n\\n\\n\\n<p>ALA Labs is managed by <a href=\\"https:\\/\\/labs.ala.org.au\\/about.html\\">our Science and Decision Support team<\\/a>. Team members will share solutions to common problems for users, including those that they have used in their own work.<\\/p>\\n\\n\\n\\n<p>Solutions described in ALA Labs posts are not intended to be the \\u201conly\\u201d or \\u201ccorrect\\u201d way to solve a problem. Rather, each post offers one <em>possible<\\/em> way to do or make something. The goal is to provide ALA users with working examples to implement in their own work, hopefully making everyone\\u2019s analyses and visualisations better!<\\/p>\\n\\n\\n\\n<figure class=\\"wp-block-image size-large\\"><img loading=\\"lazy\\" width=\\"1024\\" height=\\"630\\" src=\\"https:\\/\\/www.ala.org.au\\/app\\/uploads\\/2022\\/05\\/ALA-labs-sunburst-plot-1024x630.png\\" alt=\\"\\" class=\\"wp-image-46797\\" srcset=\\"https:\\/\\/www.ala.org.au\\/app\\/uploads\\/2022\\/05\\/ALA-labs-sunburst-plot-1024x630.png 1024w, https:\\/\\/www.ala.org.au\\/app\\/uploads\\/2022\\/05\\/ALA-labs-sunburst-plot-300x185.png 300w, https:\\/\\/www.ala.org.au\\/app\\/uploads\\/2022\\/05\\/ALA-labs-sunburst-plot-768x473.png 768w, https:\\/\\/www.ala.org.au\\/app\\/uploads\\/2022\\/05\\/ALA-labs-sunburst-plot.png 1248w\\" sizes=\\"(max-width: 1024px) 100vw, 1024px\\" \\/><figcaption>Sunburst plots for taxonomic data.&nbsp;<br>This visualisation of ALA\\u2019s taxonomy was created by Dr Martin Westgate in R using {galah} and {ggplot2}. The <a rel=\\"noreferrer noopener\\" href=\\"https:\\/\\/labs.ala.org.au\\/posts\\/2022-02-17_sunburst-plots-for-taxonomic-data\\/\\" target=\\"_blank\\">full details of how to recreate this analysis and visualisation<\\/a> are available on labs.ala.org.au.&nbsp;<\\/figcaption><\\/figure>\\n\\n\\n\\n<p>\\u201cI always love hearing about the problems people are trying to solve using the biodiversity data in the ALA. We made ALA Labs to be a community space for people to share and learn about detailed solutions to data-related problems. I can\\u2019t wait to see how it grows,\\u201d said Dr Dax Kellie, evolutionary biologist, data analyst, and ALA Labs author.<\\/p>\\n\\n\\n\\n<p><strong>R \\u2013 the language of ALA Labs<\\/strong><\\/p>\\n\\n\\n\\n<div class=\\"wp-block-image\\"><figure class=\\"alignright size-full is-resized\\"><img loading=\\"lazy\\" src=\\"https:\\/\\/www.ala.org.au\\/app\\/uploads\\/2021\\/07\\/21-00159_NCMI_BRAND_ALA-GalahHexSticker_FinalMockUp_210630.png\\" alt=\\"logo for galah, ALA's R package\\" class=\\"wp-image-44815\\" width=\\"220\\" height=\\"254\\" srcset=\\"https:\\/\\/www.ala.org.au\\/app\\/uploads\\/2021\\/07\\/21-00159_NCMI_BRAND_ALA-GalahHexSticker_FinalMockUp_210630.png 434w, https:\\/\\/www.ala.org.au\\/app\\/uploads\\/2021\\/07\\/21-00159_NCMI_BRAND_ALA-GalahHexSticker_FinalMockUp_210630-260x300.png 260w\\" sizes=\\"(max-width: 220px) 100vw, 220px\\" \\/><\\/figure><\\/div>\\n\\n\\n\\n<p>So far, posts on ALA Labs offer coding solutions for users of <a href=\\"https:\\/\\/www.r-project.org\\/\\">R<\\/a> \\u2013 a popular, free, open-access software for analysing and visualising data \\u2013 and {<a href=\\"https:\\/\\/atlasoflivingaustralia.github.io\\/galah\\/index.html\\">galah<\\/a>} \\u2013 the ALA\\u2019s R package to download information from the ALA. We intend to expand articles to explore other tools and coding languages in the near future.<\\/p>\\n\\n\\n\\n<p><\\/p>\\n\\n\\n\\n<p><strong>We want to hear from you<\\/strong><\\/p>\\n\\n\\n\\n<p>Is there a statistical analysis you are wondering how to do in R? Would you like to make a new visualisation or map? Are you looking to use the {galah} package to filter and download information?<\\/p>\\n\\n\\n\\n<p>We would love to hear from you. Please send any suggestions for ALA Labs posts to <a href=\\"mailto:support@ala.org.au\\">support@ala.org.au<\\/a>.<\\/p>\\n\\n\\n\\n<p>Back to <a href=\\"https:\\/\\/t.e2ma.net\\/webview\\/fbzw7j\\/5e28fa7d7a47a3c06f6266836eaa2b80\\">ALA Newsletter May 2022<\\/a><\\/p>\\n","protected":false},"excerpt":{"rendered":"<p>Do you conduct research using Australian biodiversity data? Do you analyse data or make data visualisations? ALA\\u2019s newest website, ALA Labs, is here to help. ALA Labs provides technical know-how and detailed solutions to specific scientific problems using data in the ALA. Learn how with ALA Labs posts The main showcase of ALA Labs is [&hellip;]<\\/p>\\n","protected":false},"author":33,"featured_media":46800,"comment_status":"closed","ping_status":"closed","sticky":false,"template":"","format":"standard","meta":[],"categories":[11],"tags":[],"acf":[],"_links":{"self":[{"href":"https:\\/\\/www.ala.org.au\\/wp-json\\/wp\\/v2\\/posts\\/46796"}],"collection":[{"href":"https:\\/\\/www.ala.org.au\\/wp-json\\/wp\\/v2\\/posts"}],"about":[{"href":"https:\\/\\/www.ala.org.au\\/wp-json\\/wp\\/v2\\/types\\/post"}],"author":[{"embeddable":true,"href":"https:\\/\\/www.ala.org.au\\/wp-json\\/wp\\/v2\\/users\\/33"}],"replies":[{"embeddable":true,"href":"https:\\/\\/www.ala.org.au\\/wp-json\\/wp\\/v2\\/comments?post=46796"}],"version-history":[{"count":2,"href":"https:\\/\\/www.ala.org.au\\/wp-json\\/wp\\/v2\\/posts\\/46796\\/revisions"}],"predecessor-version":[{"id":46879,"href":"https:\\/\\/www.ala.org.au\\/wp-json\\/wp\\/v2\\/posts\\/46796\\/revisions\\/46879"}],"wp:featuredmedia":[{"embeddable":true,"href":"https:\\/\\/www.ala.org.au\\/wp-json\\/wp\\/v2\\/media\\/46800"}],"wp:attachment":[{"href":"https:\\/\\/www.ala.org.au\\/wp-json\\/wp\\/v2\\/media?parent=46796"}],"wp:term":[{"taxonomy":"category","embeddable":true,"href":"https:\\/\\/www.ala.org.au\\/wp-json\\/wp\\/v2\\/categories?post=46796"},{"taxonomy":"post_tag","embeddable":true,"href":"https:\\/\\/www.ala.org.au\\/wp-json\\/wp\\/v2\\/tags?post=46796"}],"curies":[{"name":"wp","href":"https:\\/\\/api.w.org\\/{rel}","templated":true}]}}]"""

        //  previousResult is null the very first time, so run checkStatus to populate it
        service.executeQuery(blogs, hourly)
        QueryResult queryResult = service.getQueryResult(blogs, hourly)
        // manually change saved Json and saved property values to trigger email sending
        queryResult.lastResult = queryResult.compress(sampleJsonString)
        queryResult.propertyValues.first().currentValue = "1"
        queryResult.save(flush: true)
        when:
        //  due the the values set above, a change will be detected on checkQueryForFrequency, in turn triggering a diff email.
        def recipients = service.execQueryForFrequency(hourly, true)
        then: recipients.size() > 0
    }

    void "test sending email for news daily"() {
        given:
        // preconditions: 1. daily Queries exist. 2. User(s) subscribed to daily Blogs and News. 3. User is not locked.
        Frequency daily = Frequency.findByName("daily")
        Query blogs = Query.findByName(messageSource.getMessage("query.ala.blog.title", null,
                new Locale.Builder().setLanguageTag(Holders.config.siteDefaultLanguage as String).build()))

        Notification notification = new Notification()
        notification.query = blogs
        notification.user = User.findById(2)
        notification.save(flush: true)

        def users = Query.executeQuery(
                """select u.email, max(u.unsubscribeToken), max(n.unsubscribeToken)
                  from User u
                  inner join u.notifications n
                  where n.query = :query
                  and u.frequency = :frequency
                  and (u.locked is null or u.locked != 1)
                  group by u""", [query: blogs, frequency: daily])

        assert blogs != null
        assert daily != null
        assert notification != null
        assert users.size() > 0

        // sampleJsonString is a partial stringified sample of a standard JSON response as returned by the relevant query for this alert
        def sampleJsonString = """[{"id":46796000,"date":"2022-05-04T11:50:49","date_gmt":"2022-05-04T01:50:49","guid":{"rendered":"https:\\/\\/www.ala.org.au\\/?p=46796"},"modified":"2022-05-18T16:29:30","modified_gmt":"2022-05-18T06:29:30","slug":"ala-labs-new-site-offering-technical-solutions-to-scientific-problems","status":"publish","type":"post","link":"https:\\/\\/www.ala.org.au\\/blogs-news\\/ala-labs-new-site-offering-technical-solutions-to-scientific-problems\\/","title":{"rendered":"ALA Labs: new site offering technical solutions to scientific problems\\u00a0"},"content":{"rendered":"\\n<div class=\\"wp-block-image\\"><figure class=\\"alignright size-full is-resized\\"><img loading=\\"lazy\\" src=\\"https:\\/\\/www.ala.org.au\\/app\\/uploads\\/2022\\/05\\/chrome_2022-05-04_10-51-16.png\\" alt=\\"\\" class=\\"wp-image-46800\\" width=\\"386\\" height=\\"257\\" srcset=\\"https:\\/\\/www.ala.org.au\\/app\\/uploads\\/2022\\/05\\/chrome_2022-05-04_10-51-16.png 783w, https:\\/\\/www.ala.org.au\\/app\\/uploads\\/2022\\/05\\/chrome_2022-05-04_10-51-16-300x200.png 300w, https:\\/\\/www.ala.org.au\\/app\\/uploads\\/2022\\/05\\/chrome_2022-05-04_10-51-16-768x511.png 768w\\" sizes=\\"(max-width: 386px) 100vw, 386px\\" \\/><\\/figure><\\/div>\\n\\n\\n\\n<p>Do you conduct research using Australian biodiversity data? Do you analyse data or make data visualisations?<\\/p>\\n\\n\\n\\n<p>ALA\\u2019s newest website, <a href=\\"https:\\/\\/labs.ala.org.au\\/\\">ALA Labs<\\/a>, is here to help. ALA Labs provides technical know-how and detailed solutions to specific scientific problems using data in the ALA.<\\/p>\\n\\n\\n\\n<p><strong>Learn how with ALA Labs posts<\\/strong><\\/p>\\n\\n\\n\\n<p>The main showcase of ALA Labs is the <strong>Posts <\\/strong>section. Posts are \\u201chow-to\\u201d articles on how to conduct specific statistical analyses or how to create data visualisations. Each post provides step-by-step commentary on how to do a specific task along with code so you can reproduce the analysis.<\\/p>\\n\\n\\n\\n<p><\\/p>\\n\\n\\n\\n<p>ALA Labs is managed by <a href=\\"https:\\/\\/labs.ala.org.au\\/about.html\\">our Science and Decision Support team<\\/a>. Team members will share solutions to common problems for users, including those that they have used in their own work.<\\/p>\\n\\n\\n\\n<p>Solutions described in ALA Labs posts are not intended to be the \\u201conly\\u201d or \\u201ccorrect\\u201d way to solve a problem. Rather, each post offers one <em>possible<\\/em> way to do or make something. The goal is to provide ALA users with working examples to implement in their own work, hopefully making everyone\\u2019s analyses and visualisations better!<\\/p>\\n\\n\\n\\n<figure class=\\"wp-block-image size-large\\"><img loading=\\"lazy\\" width=\\"1024\\" height=\\"630\\" src=\\"https:\\/\\/www.ala.org.au\\/app\\/uploads\\/2022\\/05\\/ALA-labs-sunburst-plot-1024x630.png\\" alt=\\"\\" class=\\"wp-image-46797\\" srcset=\\"https:\\/\\/www.ala.org.au\\/app\\/uploads\\/2022\\/05\\/ALA-labs-sunburst-plot-1024x630.png 1024w, https:\\/\\/www.ala.org.au\\/app\\/uploads\\/2022\\/05\\/ALA-labs-sunburst-plot-300x185.png 300w, https:\\/\\/www.ala.org.au\\/app\\/uploads\\/2022\\/05\\/ALA-labs-sunburst-plot-768x473.png 768w, https:\\/\\/www.ala.org.au\\/app\\/uploads\\/2022\\/05\\/ALA-labs-sunburst-plot.png 1248w\\" sizes=\\"(max-width: 1024px) 100vw, 1024px\\" \\/><figcaption>Sunburst plots for taxonomic data.&nbsp;<br>This visualisation of ALA\\u2019s taxonomy was created by Dr Martin Westgate in R using {galah} and {ggplot2}. The <a rel=\\"noreferrer noopener\\" href=\\"https:\\/\\/labs.ala.org.au\\/posts\\/2022-02-17_sunburst-plots-for-taxonomic-data\\/\\" target=\\"_blank\\">full details of how to recreate this analysis and visualisation<\\/a> are available on labs.ala.org.au.&nbsp;<\\/figcaption><\\/figure>\\n\\n\\n\\n<p>\\u201cI always love hearing about the problems people are trying to solve using the biodiversity data in the ALA. We made ALA Labs to be a community space for people to share and learn about detailed solutions to data-related problems. I can\\u2019t wait to see how it grows,\\u201d said Dr Dax Kellie, evolutionary biologist, data analyst, and ALA Labs author.<\\/p>\\n\\n\\n\\n<p><strong>R \\u2013 the language of ALA Labs<\\/strong><\\/p>\\n\\n\\n\\n<div class=\\"wp-block-image\\"><figure class=\\"alignright size-full is-resized\\"><img loading=\\"lazy\\" src=\\"https:\\/\\/www.ala.org.au\\/app\\/uploads\\/2021\\/07\\/21-00159_NCMI_BRAND_ALA-GalahHexSticker_FinalMockUp_210630.png\\" alt=\\"logo for galah, ALA's R package\\" class=\\"wp-image-44815\\" width=\\"220\\" height=\\"254\\" srcset=\\"https:\\/\\/www.ala.org.au\\/app\\/uploads\\/2021\\/07\\/21-00159_NCMI_BRAND_ALA-GalahHexSticker_FinalMockUp_210630.png 434w, https:\\/\\/www.ala.org.au\\/app\\/uploads\\/2021\\/07\\/21-00159_NCMI_BRAND_ALA-GalahHexSticker_FinalMockUp_210630-260x300.png 260w\\" sizes=\\"(max-width: 220px) 100vw, 220px\\" \\/><\\/figure><\\/div>\\n\\n\\n\\n<p>So far, posts on ALA Labs offer coding solutions for users of <a href=\\"https:\\/\\/www.r-project.org\\/\\">R<\\/a> \\u2013 a popular, free, open-access software for analysing and visualising data \\u2013 and {<a href=\\"https:\\/\\/atlasoflivingaustralia.github.io\\/galah\\/index.html\\">galah<\\/a>} \\u2013 the ALA\\u2019s R package to download information from the ALA. We intend to expand articles to explore other tools and coding languages in the near future.<\\/p>\\n\\n\\n\\n<p><\\/p>\\n\\n\\n\\n<p><strong>We want to hear from you<\\/strong><\\/p>\\n\\n\\n\\n<p>Is there a statistical analysis you are wondering how to do in R? Would you like to make a new visualisation or map? Are you looking to use the {galah} package to filter and download information?<\\/p>\\n\\n\\n\\n<p>We would love to hear from you. Please send any suggestions for ALA Labs posts to <a href=\\"mailto:support@ala.org.au\\">support@ala.org.au<\\/a>.<\\/p>\\n\\n\\n\\n<p>Back to <a href=\\"https:\\/\\/t.e2ma.net\\/webview\\/fbzw7j\\/5e28fa7d7a47a3c06f6266836eaa2b80\\">ALA Newsletter May 2022<\\/a><\\/p>\\n","protected":false},"excerpt":{"rendered":"<p>Do you conduct research using Australian biodiversity data? Do you analyse data or make data visualisations? ALA\\u2019s newest website, ALA Labs, is here to help. ALA Labs provides technical know-how and detailed solutions to specific scientific problems using data in the ALA. Learn how with ALA Labs posts The main showcase of ALA Labs is [&hellip;]<\\/p>\\n","protected":false},"author":33,"featured_media":46800,"comment_status":"closed","ping_status":"closed","sticky":false,"template":"","format":"standard","meta":[],"categories":[11],"tags":[],"acf":[],"_links":{"self":[{"href":"https:\\/\\/www.ala.org.au\\/wp-json\\/wp\\/v2\\/posts\\/46796"}],"collection":[{"href":"https:\\/\\/www.ala.org.au\\/wp-json\\/wp\\/v2\\/posts"}],"about":[{"href":"https:\\/\\/www.ala.org.au\\/wp-json\\/wp\\/v2\\/types\\/post"}],"author":[{"embeddable":true,"href":"https:\\/\\/www.ala.org.au\\/wp-json\\/wp\\/v2\\/users\\/33"}],"replies":[{"embeddable":true,"href":"https:\\/\\/www.ala.org.au\\/wp-json\\/wp\\/v2\\/comments?post=46796"}],"version-history":[{"count":2,"href":"https:\\/\\/www.ala.org.au\\/wp-json\\/wp\\/v2\\/posts\\/46796\\/revisions"}],"predecessor-version":[{"id":46879,"href":"https:\\/\\/www.ala.org.au\\/wp-json\\/wp\\/v2\\/posts\\/46796\\/revisions\\/46879"}],"wp:featuredmedia":[{"embeddable":true,"href":"https:\\/\\/www.ala.org.au\\/wp-json\\/wp\\/v2\\/media\\/46800"}],"wp:attachment":[{"href":"https:\\/\\/www.ala.org.au\\/wp-json\\/wp\\/v2\\/media?parent=46796"}],"wp:term":[{"taxonomy":"category","embeddable":true,"href":"https:\\/\\/www.ala.org.au\\/wp-json\\/wp\\/v2\\/categories?post=46796"},{"taxonomy":"post_tag","embeddable":true,"href":"https:\\/\\/www.ala.org.au\\/wp-json\\/wp\\/v2\\/tags?post=46796"}],"curies":[{"name":"wp","href":"https:\\/\\/api.w.org\\/{rel}","templated":true}]}}]"""

        //  previousResult is null the very first time, so run checkStatus to populate it
        service.executeQuery(blogs, daily)
        QueryResult queryResult = service.getQueryResult(blogs, daily)
        // manually change saved Json and saved property values to trigger email sending
        queryResult.lastResult = queryResult.compress(sampleJsonString)
        queryResult.propertyValues.first().currentValue = "1"
        queryResult.save(flush: true)
        when:
        //  due the the values set above, a change will be detected on checkQueryForFrequency, in turn triggering a diff email.
        def recipients = service.execQueryForFrequency(daily, true)
        then: recipients.size() > 0
    }
}
