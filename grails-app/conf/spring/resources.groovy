import grails.util.Holders
import liquibase.integration.spring.SpringLiquibase
import org.springframework.scheduling.quartz.SchedulerFactoryBean
import org.springframework.web.servlet.i18n.SessionLocaleResolver
import au.org.ala.alerts.quartz.AutowiringSpringBeanJobFactory

beans = {
    localeResolver(SessionLocaleResolver) {
        defaultLocale= new java.util.Locale(Holders.config.siteDefaultLanguage as String)
    }

    // This tells Spring to find all @Component, @Service, etc. in your package
    xmlns context: "http://www.springframework.org/schema/context"
    context.'component-scan'('base-package': "au.org.ala.alerts.quartz")

    // 1. Manually define the Liquibase bean
    liquibase(SpringLiquibase) {
        dataSource = ref('dataSource')
        changeLog = "classpath:db/changelog/db.changelog-master.xml"
        // This ensures the properties from your YAML are still applied
        shouldRun = true
    }

    // 1. Define the custom Quartz JobFactory
    quartzJobFactory(AutowiringSpringBeanJobFactory)

    // 2. Link it to the Scheduler
    quartzScheduler(SchedulerFactoryBean) {
        dependsOn('liquibase')
        dataSource = ref('dataSource')
        transactionManager = ref('transactionManager')
        jobFactory = ref('quartzJobFactory') // CRITICAL: This enables @Autowired in jobs

        quartzProperties = [
                'org.quartz.scheduler.instanceName': 'quartzScheduler',
                'org.quartz.jobStore.class': 'org.springframework.scheduling.quartz.LocalDataSourceJobStore',
                'org.quartz.jobStore.driverDelegateClass': 'org.quartz.impl.jdbcjobstore.StdJDBCDelegate',
                'org.quartz.jobStore.tablePrefix': 'QRTZ_'
        ] as Properties
    }
}
