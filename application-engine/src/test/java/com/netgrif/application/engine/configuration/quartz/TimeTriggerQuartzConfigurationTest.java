package com.netgrif.application.engine.configuration.quartz;

import com.netgrif.application.engine.configuration.properties.DataConfigurationProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TimeTriggerQuartzConfigurationTest {

    private QuartzConfiguration configuration;

    @BeforeEach
    void setUp() {
        DataConfigurationProperties.MongoProperties mongoProperties = mock(DataConfigurationProperties.MongoProperties.class);
        when(mongoProperties.getHost()).thenReturn("localhost");
        when(mongoProperties.getDatabase()).thenReturn("test-db");

        configuration = new QuartzConfiguration();
        ReflectionTestUtils.setField(configuration, "applicationContext", mock(ApplicationContext.class));
        ReflectionTestUtils.setField(configuration, "jobFactory", mock(AutowiringSpringBeanJobFactory.class));
        ReflectionTestUtils.setField(configuration, "mongoProperties", mongoProperties);
    }

    @Test
    void setsSchedulerNameJobStorePrefixAndCheckInErrorHandler() throws Exception {
        Properties properties = quartzProperties();

        assertEquals("netgrif_onloadcode", properties.getProperty("org.quartz.scheduler.instanceName"));
        assertEquals("netgrif_quartz_", properties.getProperty("org.quartz.jobStore.collectionPrefix"));
        assertEquals(
                "com.netgrif.quartz.mongodb.cluster.NoOpErrorHandler",
                properties.getProperty("org.quartz.jobStore.checkInErrorHandler")
        );
    }

    @Test
    void storesJobDataAsPlainValuesInsteadOfBase64() throws Exception {
        assertEquals("false", quartzProperties().getProperty("org.quartz.jobStore.jobDataAsBase64"));
    }

    @Test
    void takesInstanceIdAndClusterTimingsFromQuartzProperties() throws Exception {
        Properties properties = quartzProperties();

        assertEquals("AUTO", properties.getProperty("org.quartz.scheduler.instanceId"));
        assertEquals("10000", properties.getProperty("org.quartz.jobStore.clusterCheckinInterval"));
        assertEquals("10000", properties.getProperty("org.quartz.jobStore.misfireThreshold"));
        assertEquals("1200000", properties.getProperty("org.quartz.jobStore.triggerTimeoutMillis"));
        assertEquals("1200000", properties.getProperty("org.quartz.jobStore.jobTimeoutMillis"));
    }

    @Test
    void readsCollectionPrefixAndWriteConcernTimeoutOnlyFromJobStoreProperties() throws Exception {
        Properties properties = quartzProperties();

        assertNull(properties.getProperty("org.quartz.scheduler.collectionPrefix"));
        assertNull(properties.getProperty("org.quartz.scheduler.mongoOptionWriteConcernTimeoutMillis"));
        assertEquals("10000", properties.getProperty("org.quartz.jobStore.mongoOptionWriteConcernTimeoutMillis"));
    }

    private Properties quartzProperties() throws Exception {
        SchedulerFactoryBean factory = configuration.schedulerFactoryBean();
        Properties properties = (Properties) ReflectionTestUtils.getField(factory, "quartzProperties");
        assertNotNull(properties);
        return properties;
    }
}
