package com.netgrif.workflow;

import com.netgrif.workflow.configuration.ApplicationContextProvider;
import com.netgrif.workflow.configuration.JsonRootRelProvider;
import com.netgrif.workflow.petrinet.domain.version.StringToVersionConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.hateoas.RelProvider;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

import java.util.ArrayList;
import java.util.List;

@EnableCaching
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableAspectJAutoProxy
@SpringBootApplication
@EnableMongoAuditing
public class WorkflowManagementSystemApplication {

    @Bean
    public MongoCustomConversions customConversions() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(new StringToVersionConverter());
        return new MongoCustomConversions(converters);
    }

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