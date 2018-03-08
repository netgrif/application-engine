package com.netgrif.workflow.configuration;

import com.netgrif.workflow.importer.service.Importer;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class ImporterConfiguration {
    @Bean("importer")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public Importer importer() {
        return new Importer();
    }
}