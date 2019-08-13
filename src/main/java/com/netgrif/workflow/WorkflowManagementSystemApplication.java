package com.netgrif.workflow;

import com.netgrif.workflow.configuration.ApplicationContextProvider;
import com.netgrif.workflow.configuration.JsonRootRelProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.hateoas.RelProvider;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

@EnableCaching
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableAspectJAutoProxy
@SpringBootApplication
@EnableMongoAuditing
public class WorkflowManagementSystemApplication {

    @Bean
    RelProvider relProvider() {
        return new JsonRootRelProvider();
    }

    @Bean
    ApplicationContextProvider applicationContextProvider() {
        return new ApplicationContextProvider();
    }

    public static void main(String[] args) {
        SpringApplication.run(WorkflowManagementSystemApplication.class, args);
    }
}