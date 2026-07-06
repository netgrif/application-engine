package com.netgrif.application.engine.auth.config;

import tools.jackson.databind.module.SimpleModule;
import com.netgrif.application.engine.auth.provider.AuthMethodConfigDeserializer;
import com.netgrif.application.engine.auth.provider.ProviderRegistry;
import com.netgrif.application.engine.objects.auth.provider.AuthMethodConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfiguration {

    @Bean
    public SimpleModule authConfigModule(ProviderRegistry providerRegistry) {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(AuthMethodConfig.class, new AuthMethodConfigDeserializer(providerRegistry));
        return module;
    }
}
