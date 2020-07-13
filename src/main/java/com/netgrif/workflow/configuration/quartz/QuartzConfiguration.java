package com.netgrif.workflow.configuration.quartz;

import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.quartz.QuartzDataSource;
import org.springframework.boot.autoconfigure.quartz.QuartzProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
public class QuartzConfiguration {

    @Value("${quartz.jdbc-url}")
    private String jdbcUrl;

    @Value("${quartz.jdbc-user}")
    private String jdbcUser;

    @Value("${quartz.jdbc-password}")
    private String jdbcPass;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private QuartzProperties quartzProperties;

    @QuartzDataSource
    public DataSource quartzDataSource() {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.url(jdbcUrl);
        dataSourceBuilder.username(jdbcUser);
        dataSourceBuilder.password(jdbcPass);
        return dataSourceBuilder.build();
    }

    @Bean
    public Scheduler scheduler() {
        return schedulerFactoryBean().getScheduler();
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() {
        SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
        schedulerFactory.setApplicationContext(applicationContext);
        schedulerFactory.setAutoStartup(false);

        Properties properties = new Properties();
        properties.putAll(quartzProperties.getProperties());
        schedulerFactory.setQuartzProperties(properties);

        schedulerFactory.setJobFactory(new SpringBeanJobFactory());
        schedulerFactory.setDataSource(quartzDataSource());

        return schedulerFactory;
    }

}
