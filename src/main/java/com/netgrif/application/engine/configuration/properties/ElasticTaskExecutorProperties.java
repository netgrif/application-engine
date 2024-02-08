package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "spring.data.elasticsearch.task.executors")
public class ElasticTaskExecutorProperties {

    private int size = 50;
    private int maxPoolSize = size*10;
    private boolean allowCoreThreadTimeOut = true;
    private int keepAliveSeconds = 30;
    private String threadNamePrefix = "ElasticTaskExecutor-";
}
