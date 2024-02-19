package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.configuration.locale.LocaleResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

@Configuration
public class LocaleResolverConfiguration {

    @Primary
    @Bean(name = DispatcherServlet.LOCALE_RESOLVER_BEAN_NAME)
    org.springframework.web.servlet.LocaleResolver localeResolver() {
        SessionLocaleResolver l = new SessionLocaleResolver();
        return new LocaleResolver(l);
    }
}
