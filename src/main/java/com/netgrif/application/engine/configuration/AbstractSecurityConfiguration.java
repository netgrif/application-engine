package com.netgrif.application.engine.configuration;

import com.google.common.collect.Ordering;
import com.netgrif.application.engine.configuration.authentication.providers.NaeAuthProperties;
import com.netgrif.application.engine.configuration.authentication.providers.NetgrifAuthenticationProvider;
import com.netgrif.application.engine.configuration.properties.ServerAuthProperties;
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

import java.util.*;
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

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        context.getBeansOfType(NetgrifAuthenticationProvider.class)
                .entrySet().stream()
                .filter(it -> Arrays.stream(naeAuthProperties.getProviders()).map(String::toLowerCase).collect(Collectors.toSet()).contains(it.getKey().toLowerCase()))
                .sorted(Ordering.explicit(Arrays.stream(naeAuthProperties.getProviders()).map(String::toLowerCase).collect(Collectors.toList())).onResultOf(entry -> entry.getKey().toLowerCase()))
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

    protected abstract boolean isOpenRegistration();

    protected abstract boolean isCsrfEnabled();

    protected abstract String[] getStaticPatterns();

    protected abstract String[] getServerPatterns();

    protected abstract Environment getEnvironment();
}