package com.netgrif.application.engine.serialization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.netgrif.application.engine.workflow.web.responsebodies.eventoutcomes.base.LocalisedEventOutcome;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.module.SimpleModule;

@Configuration
public class ObjectMapperConfiguration {

    @Bean
    public JsonMapperBuilderCustomizer jsonCustomizer(FieldSelectorHolder holder) {
        SimpleModule module = new SimpleModule();
        module.addSerializer(LocalisedEventOutcome.class, new LocalizedEventOutcomeSerializer(holder));
        return builder -> builder
                .changeDefaultPropertyInclusion(include -> include.withValueInclusion(JsonInclude.Include.NON_NULL))
                .findAndAddModules();
    }
}
