package au.org.ala.alerts.quartz

import org.quartz.spi.TriggerFiredBundle
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.scheduling.quartz.SpringBeanJobFactory

class AutowiringSpringBeanJobFactory extends SpringBeanJobFactory implements ApplicationContextAware {

    private transient AutowireCapableBeanFactory beanFactory

    @Override
    void setApplicationContext(ApplicationContext context) {
        beanFactory = context.getAutowireCapableBeanFactory()
    }

    @Override
    protected Object createJobInstance(TriggerFiredBundle bundle) throws Exception {
        Object job = super.createJobInstance(bundle)
        // This line performs the @Autowired injection on the job instance
        beanFactory.autowireBean(job)
        return job
    }
}