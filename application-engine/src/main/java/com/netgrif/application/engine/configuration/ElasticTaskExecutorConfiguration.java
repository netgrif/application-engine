package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.auth.context.ExecutionContext;
import com.netgrif.application.engine.auth.context.Origin;
import com.netgrif.application.engine.configuration.properties.DataConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
public class ElasticTaskExecutorConfiguration {

    protected final DataConfigurationProperties.ElasticsearchProperties elasticsearchProperties;

    public ElasticTaskExecutorConfiguration(DataConfigurationProperties.ElasticsearchProperties elasticsearchProperties) {
        this.elasticsearchProperties = elasticsearchProperties;
    }

    @Bean("elasticTaskExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(elasticsearchProperties.getExecutors().getSize());
        executor.setMaxPoolSize(elasticsearchProperties.getExecutors().getMaxPoolSize());
        executor.setAllowCoreThreadTimeOut(elasticsearchProperties.getExecutors().isAllowCoreThreadTimeOut());
        executor.setKeepAliveSeconds(elasticsearchProperties.getExecutors().getKeepAliveSeconds());
        executor.setThreadNamePrefix(elasticsearchProperties.getExecutors().getThreadNamePrefix());
        executor.setTaskDecorator(ExecutionContext.getTaskDecorator());
        executor.initialize();
        return executor;
    }
}
