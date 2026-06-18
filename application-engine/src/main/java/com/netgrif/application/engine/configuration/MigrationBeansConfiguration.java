package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.migration.MigrationHelper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MigrationBeansConfiguration {

    @Bean
    @ConditionalOnMissingBean(MigrationHelper.class)
    public MigrationHelper migrationHelper() {
        return new MigrationHelper();
    }
}
