package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.configuration.locale.CustomLocaleResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

@Configuration
public class LocaleResolverConfiguration {


    @Bean
    LocaleResolver localeResolver() {
        SessionLocaleResolver l = new SessionLocaleResolver();
        return new CustomLocaleResolver(l);
    }
}
