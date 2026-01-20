import grails.util.Holders
import org.springframework.scheduling.quartz.SchedulerFactoryBean
import org.springframework.web.servlet.i18n.SessionLocaleResolver
import au.org.ala.alerts.quartz.AutowiringSpringBeanJobFactory

beans = {
    localeResolver(SessionLocaleResolver) {
        defaultLocale= new java.util.Locale(Holders.config.siteDefaultLanguage as String)
    }

    // This tells Spring to find all @Component, @Service, etc. in your package
    // Assure the Quartz schedulers are loaded
    xmlns context: "http://www.springframework.org/schema/context"
    context.'component-scan'('base-package': "au.org.ala.alerts.quartz")


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
