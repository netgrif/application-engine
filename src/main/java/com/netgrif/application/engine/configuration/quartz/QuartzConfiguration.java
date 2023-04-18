package com.netgrif.application.engine.configuration.quartz;

import com.netgrif.application.engine.configuration.properties.MongoProperties;
import com.netgrif.application.engine.configuration.properties.NaeQuartzProperties;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

@Slf4j
@Configuration
public class QuartzConfiguration {

    private final ApplicationContext applicationContext;

    private final AutowiringSpringBeanJobFactory jobFactory;

    private final MongoProperties mongoProperties;

    private final NaeQuartzProperties quartzProperties;

    public QuartzConfiguration(ApplicationContext applicationContext, AutowiringSpringBeanJobFactory jobFactory, MongoProperties mongoProperties, NaeQuartzProperties quartzProperties) {
        this.applicationContext = applicationContext;
        this.jobFactory = jobFactory;
        this.mongoProperties = mongoProperties;
        this.quartzProperties = quartzProperties;
    }

    @Bean
    public Properties quartzProperties() throws IOException {
        PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
        propertiesFactoryBean.setLocation(new ClassPathResource("quartz.properties"));
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        List<Resource> resourceList = new ArrayList<>();
        try {
            Resource[] resourcesClassApp = resolver.getResources("classpath*:/application.properties");
            Resource[] resourcesClass = resolver.getResources("classpath*:/quartz.properties");
            Resource[] resources = resolver.getResources("file:/*/quartz.properties");
            Collections.addAll(resourceList, resourcesClassApp);
            Collections.addAll(resourceList, resourcesClass);
            Collections.addAll(resourceList, resources);
        } catch (Exception e) {
            log.error("", e);
        }
        propertiesFactoryBean.setLocations(resourceList.toArray(new Resource[]{}));
        propertiesFactoryBean.afterPropertiesSet();
        return propertiesFactoryBean.getObject();
    }

    @Bean
    public Scheduler scheduler() throws Exception {
        return schedulerFactoryBean().getScheduler();
    }


    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() throws Exception {
        Properties properties = new Properties();
        if (mongoProperties.getHost() != null && !mongoProperties.getHost().equals("null"))
            properties.setProperty("org.quartz.jobStore.mongoUri", "mongodb://" + mongoProperties.getHost() + ":27017/");
        else if (mongoProperties.getUri() != null && !mongoProperties.getUri().equals("null"))
            properties.setProperty("org.quartz.jobStore.mongoUri", mongoProperties.getUri());
        properties.setProperty("org.quartz.jobStore.dbName", quartzProperties.getDbName());
        properties.setProperty("org.quartz.jobStore.class", "com.novemberain.quartz.mongodb.MongoDBJobStore");
        properties.setProperty("spring.quartz.properties.org.quartz.jobStore.isClustered", "false");
        properties.setProperty("org.quartz.jobStore.isClustered", "true");
        properties.setProperty("org.quartz.threadPool.threadCount", "1");
        properties.setProperty("org.quartz.scheduler.instanceName", "netgrif_onloadcode");
        SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
        schedulerFactory.setApplicationContext(applicationContext);
        schedulerFactory.setAutoStartup(false);
        schedulerFactory.setApplicationContextSchedulerContextKey("applicationContext");
        schedulerFactory.setQuartzProperties(quartzProperties());
        schedulerFactory.setQuartzProperties(properties);
        jobFactory.setApplicationContext(applicationContext);
        schedulerFactory.setJobFactory(jobFactory);
        schedulerFactory.setOverwriteExistingJobs(true);
        schedulerFactory.setSchedulerName("netgrif_quartz");
        return schedulerFactory;
    }

}
