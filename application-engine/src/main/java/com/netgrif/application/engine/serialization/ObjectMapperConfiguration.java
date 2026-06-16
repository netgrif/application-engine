package com.netgrif.application.engine.serialization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.netgrif.application.engine.workflow.web.responsebodies.eventoutcomes.base.LocalisedEventOutcome;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObjectMapperConfiguration {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer(FieldSelectorHolder holder) {
        return builder -> builder.serializationInclusion(JsonInclude.Include.NON_NULL)
                .serializerByType(LocalisedEventOutcome.class, new ChangeRecordSerializer(holder));
    }
}
