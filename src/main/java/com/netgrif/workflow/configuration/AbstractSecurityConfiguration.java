package com.netgrif.workflow.configuration;

import com.netgrif.workflow.configuration.properties.ServerAuthProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    protected ServerAuthProperties serverAuthProperties;

    void setCsrf(HttpSecurity http) throws Exception {
        if (isCsrfEnabled()) {
            http.csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
        } else {
            http.csrf().disable();
        }
    }

    String[] getPatterns() {
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

    abstract boolean isOpenRegistration();

    abstract boolean isCsrfEnabled();

    abstract String[] getStaticPatterns();

    abstract String[] getServerPatterns();

    abstract Environment getEnvironment();
}