package com.netgrif.application.engine.configuration;

import com.google.common.collect.Ordering;
import com.netgrif.application.engine.configuration.authentication.providers.NaeAuthProperties;
import com.netgrif.application.engine.configuration.authentication.providers.NetgrifAuthenticationProvider;
import com.netgrif.application.engine.configuration.properties.SecurityConfigProperties;
import com.netgrif.application.engine.configuration.properties.ServerAuthProperties;
import com.netgrif.application.engine.configuration.properties.enumeration.HSTS;
import com.netgrif.application.engine.configuration.properties.enumeration.XFrameOptionsMode;
import com.netgrif.application.engine.configuration.properties.enumeration.XXSSProtection;
import com.netgrif.application.engine.configuration.security.SessionUtilsProperties;
import com.netgrif.application.engine.ldap.filters.LoginAttemptsFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.header.writers.StaticHeadersWriter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class AbstractSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    protected ServerAuthProperties serverAuthProperties;

    @Autowired
    protected SessionUtilsProperties sessionUtilsProperties;

    @Autowired
    private NaeAuthProperties naeAuthProperties;

    @Autowired
    private ApplicationContext context;

    protected void setHeaders(HttpSecurity http) throws Exception {
        setStrictTransportSecurity(http);
        setContentSecurityPolicy(http);
        setXFrameOptions(http);
        setXXSSProtection(http);

    }

    protected void setStrictTransportSecurity(HttpSecurity http) throws Exception {
        if (existConfigurationHeaders()
                && getSecurityConfigProperties().getHeaders().getHsts() != null
                && getSecurityConfigProperties().getHeaders().getHsts().isEnable()
                && getSecurityConfigProperties().getHeaders().getHsts().getMaxAge() >= 0) {
            HSTS headers = getSecurityConfigProperties().getHeaders().getHsts();
            if (Objects.nonNull(headers.isIncludeSubDomains())
                    && Objects.nonNull(headers.isPreload())) {
                http
                        .headers()
                        .httpStrictTransportSecurity()
                        .maxAgeInSeconds(headers.getMaxAge())
                        .includeSubDomains(headers.isIncludeSubDomains())
                        .preload(headers.isPreload());

            } else if (Objects.nonNull(headers.isIncludeSubDomains())
                    && Objects.isNull(headers.isPreload())) {
                http
                        .headers()
                        .httpStrictTransportSecurity()
                        .maxAgeInSeconds(headers.getMaxAge())
                        .includeSubDomains(headers.isIncludeSubDomains());
            } else if (Objects.isNull(headers.isIncludeSubDomains())
                    && Objects.nonNull(headers.isPreload())) {
                http
                        .headers()
                        .httpStrictTransportSecurity()
                        .maxAgeInSeconds(headers.getMaxAge())
                        .preload(headers.isPreload());
            } else {
                http
                        .headers()
                        .httpStrictTransportSecurity()
                        .maxAgeInSeconds(headers.getMaxAge());
            }
        } else {
            http
                    .headers()
                    .httpStrictTransportSecurity().disable();
        }
    }

    protected void setXXSSProtection(HttpSecurity http) throws Exception {
        XXSSProtection mode;
        if (!existConfigurationHeaders()
                || getSecurityConfigProperties().getHeaders().getXXssProtection() == null) {
            mode = XXSSProtection.ENABLE;
        } else {
            mode = getSecurityConfigProperties().getHeaders().getXXssProtection();
        }
        switch (mode) {
            case DISABLE:
                http
                        .headers()
                        .xssProtection().disable();
                break;
            case DISABLE_XSS:
                http
                        .headers()
                        .xssProtection();
                break;
            case ENABLE:
                http
                        .headers()
                        .xssProtection().xssProtectionEnabled(false);
                break;
            case ENABLE_MODE:
                http
                        .headers()
                        .xssProtection().xssProtectionEnabled(true);
                break;
        }
    }

    protected void setContentSecurityPolicy(HttpSecurity http) throws Exception {
        if (!existConfigurationHeaders()
                || getSecurityConfigProperties().getHeaders().getContentSecurityPolicy() == null
                || getSecurityConfigProperties().getHeaders().getContentSecurityPolicy().isEmpty()) {
            http
                    .headers()
                    .addHeaderWriter(new StaticHeadersWriter("X-Content-Security-Policy", "frame-src: 'none'"));
        } else {
            http
                    .headers()
                    .contentSecurityPolicy(getSecurityConfigProperties().getHeaders().getContentSecurityPolicy());
        }
    }

    protected void setXFrameOptions(HttpSecurity http) throws Exception {
        XFrameOptionsMode mode;
        if (!existConfigurationHeaders() || getSecurityConfigProperties().getHeaders().getXFrameOptions() == null) {
            mode = XFrameOptionsMode.DISABLE;
        } else {
            mode = getSecurityConfigProperties().getHeaders().getXFrameOptions();
        }
        switch (mode) {
            case SAMEORIGIN:
                http
                        .headers()
                        .frameOptions()
                        .sameOrigin();
                break;
            case DENY:
                http
                        .headers()
                        .frameOptions()
                        .deny();
                break;
            case DISABLE:
            default:
                http
                        .headers()
                        .frameOptions()
                        .disable();
                break;
        }
    }

    protected void setCsrf(HttpSecurity http) throws Exception {
        if (isCsrfEnabled()) {
            http
                    .csrf()
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
        } else {
            http
                    .csrf()
                    .disable();
        }
    }

    protected void corsEnable(HttpSecurity http) throws Exception {
        if (isCorsEnabled()) {
            http
                    .cors();
        }
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        List<String> properties = Arrays.stream(naeAuthProperties.getProviders()).map(String::toLowerCase).collect(Collectors.toList());
        context.getBeansOfType(NetgrifAuthenticationProvider.class)
                .entrySet().stream()
                .filter(it -> properties.contains(it.getKey().toLowerCase()))
                .sorted(Ordering.explicit(properties).onResultOf(entry -> entry.getKey().toLowerCase()))
                .forEach(it -> auth.authenticationProvider(it.getValue()));
    }

    protected String[] getPatterns() {
        List<String> patterns = new ArrayList<>(Arrays.asList(getStaticPatterns()));
        patterns.addAll(Arrays.asList(getServerPatterns()));
        patterns.addAll(Arrays.asList(serverAuthProperties.getNoAuthenticationPatterns()));
        if (isOpenRegistration()) {
            patterns.add("/api/auth/invite");
        }
        if (Arrays.asList(getEnvironment().getActiveProfiles()).contains("dev")) {
            patterns.add("/dev/**");
        }
        return patterns.toArray(new String[0]);
    }

    protected void configureSession(HttpSecurity http) throws Exception {
        if (sessionUtilsProperties.isEnabledLimitSession()) {
            http.sessionManagement()
                    .maximumSessions(sessionUtilsProperties.getMaxSession())
                    .and()
                    .sessionFixation().newSession();
        }
    }

    protected void configureFilters(HttpSecurity http) {
        if (sessionUtilsProperties.isEnabledFilter()) {
            http.addFilterBefore(new LoginAttemptsFilter(), ChannelProcessingFilter.class);
        }
    }

    protected boolean existConfigurationHeaders() {
        return getSecurityConfigProperties() != null && getSecurityConfigProperties().getHeaders() != null;
    }

    protected abstract boolean isOpenRegistration();

    protected abstract boolean isCsrfEnabled();

    protected abstract boolean isCorsEnabled();

    protected abstract String[] getStaticPatterns();

    protected abstract String[] getServerPatterns();

    protected abstract Environment getEnvironment();

    protected abstract SecurityConfigProperties getSecurityConfigProperties();

}