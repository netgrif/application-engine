package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.configuration.locale.LocaleResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

@Configuration
public class LocaleResolverConfiguration {


    @Bean
    org.springframework.web.servlet.LocaleResolver localeResolver() {
        SessionLocaleResolver l = new SessionLocaleResolver();
        return new LocaleResolver(l);
    }
}
