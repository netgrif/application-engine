package com.netgrif.workflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

@EnableCaching
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
@EnableGlobalMethodSecurity(prePostEnabled = true)
@SpringBootApplication
@EnableAspectJAutoProxy
public class WorkflowManagementSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(WorkflowManagementSystemApplication.class, args);
    }
}