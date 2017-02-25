package com.fmworkflow.configuration;

import com.fmworkflow.importer.Importer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class ImporterConfiguration {
    @Bean
    @Scope("prototype")
    public Importer importer() {
        return new Importer();
    }
}