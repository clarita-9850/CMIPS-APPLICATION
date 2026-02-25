package com.ihss.scheduler.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.quartz.QuartzProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
public class QuartzConfig {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private QuartzProperties quartzProperties;

    /**
     * Custom job factory that enables Spring DI in Quartz jobs.
     * This allows @Autowired to work inside Job classes.
     */
    @Bean
    public SpringBeanJobFactory springBeanJobFactory() {
        AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        return jobFactory;
    }

    /**
     * Configure the Quartz SchedulerFactoryBean with our custom settings.
     */
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();

        factory.setDataSource(dataSource);
        factory.setJobFactory(springBeanJobFactory());
        factory.setQuartzProperties(quartzProperties());

        // Wait for jobs to complete on shutdown
        factory.setWaitForJobsToCompleteOnShutdown(true);

        // Overwrite existing jobs on startup
        factory.setOverwriteExistingJobs(true);

        // Auto-startup
        factory.setAutoStartup(true);

        // Delay startup by 10 seconds to allow app to fully initialize
        factory.setStartupDelay(10);

        return factory;
    }

    /**
     * Convert QuartzProperties to Properties object for SchedulerFactoryBean.
     */
    private Properties quartzProperties() {
        Properties properties = new Properties();
        properties.putAll(quartzProperties.getProperties());
        return properties;
    }

    /**
     * Custom job factory that supports Spring dependency injection.
     */
    public static class AutowiringSpringBeanJobFactory extends SpringBeanJobFactory {

        private ApplicationContext applicationContext;

        public void setApplicationContext(ApplicationContext applicationContext) {
            this.applicationContext = applicationContext;
        }

        @Override
        protected Object createJobInstance(org.quartz.spi.TriggerFiredBundle bundle) throws Exception {
            Object job = super.createJobInstance(bundle);
            applicationContext.getAutowireCapableBeanFactory().autowireBean(job);
            return job;
        }
    }
}
