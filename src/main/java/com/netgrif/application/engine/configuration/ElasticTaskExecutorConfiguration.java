package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.configuration.properties.ElasticTaskExecutorProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ElasticTaskExecutorConfiguration {

    protected final ElasticTaskExecutorProperties elasticTaskExecutorProperties;

    public ElasticTaskExecutorConfiguration(ElasticTaskExecutorProperties elasticTaskExecutorProperties) {
        this.elasticTaskExecutorProperties = elasticTaskExecutorProperties;
    }

    @Bean("elasticTaskExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(elasticTaskExecutorProperties.getSize());
        executor.setMaxPoolSize(elasticTaskExecutorProperties.getMaxPoolSize());
        executor.setAllowCoreThreadTimeOut(elasticTaskExecutorProperties.isAllowCoreThreadTimeOut());
        executor.setKeepAliveSeconds(elasticTaskExecutorProperties.getKeepAliveSeconds());
        executor.setThreadNamePrefix(elasticTaskExecutorProperties.getThreadNamePrefix());
        executor.initialize();
        return executor;
    }
}
