package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.configuration.locale.LocaleInterceptor;
import com.netgrif.application.engine.configuration.properties.LocaleConfigurationProperties;
import com.netgrif.application.engine.security.service.ISecurityContextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

/**
 * Configuration for LocaleInterceptor to extract time zone offset from request headers
 * and managing default time zone in system.
 * */
@Configuration
public class LocaleConfiguration implements WebMvcConfigurer {

    private final LocaleConfigurationProperties properties;

    public LocaleConfiguration(@Autowired LocaleConfigurationProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    void init() {
        TimeZone.setDefault(TimeZone.getTimeZone(properties.getDefaultTimeZone()));
    }

    /**
     * Adds new interceptor that extracts time zone from request header.
     * @param registry the interceptor registry of Spring MVC
     * */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry
                .addInterceptor(new LocaleInterceptor())
                .addPathPatterns(properties.getDefaultPathPatterns());
    }
}
