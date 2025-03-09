package com.netgrif.application.engine.configuration.plugin;

import com.netgrif.application.engine.integration.plugins.properties.PluginRegistrationConfigProperties;
import com.netgrif.application.engine.integration.plugins.repository.PluginRepository;
import com.netgrif.application.engine.integration.plugins.service.IPluginService;
import com.netgrif.application.engine.integration.plugins.service.PluginService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
@ConditionalOnProperty(value="nae.plugin.default-service",
        havingValue="true",
        matchIfMissing=true)
public class PluginServiceConfiguration {

    @Bean
    public IPluginService pluginService(PluginRegistrationConfigProperties properties, PluginRepository pluginRepository) throws IOException {
        return new PluginService(properties, pluginRepository);
    }
}
