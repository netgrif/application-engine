package com.netgrif.application.engine.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.scheduling.DelegatingSecurityContextTaskScheduler;

@EnableScheduling
@Configuration
public class SchedulingConfiguration {

    @Bean
    public TaskScheduler taskScheduler() {
        return new DelegatingSecurityContextTaskScheduler(new ThreadPoolTaskScheduler());
    }
}