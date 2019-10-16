package com.netgrif.workflow.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.session.web.http.HeaderHttpSessionStrategy;
import org.springframework.session.web.http.HttpSessionStrategy;
import org.springframework.stereotype.Controller;

@Configuration
@Controller
@EnableWebSecurity
@Order(SecurityProperties.BASIC_AUTH_ORDER)
@ConditionalOnProperty(
        value="server.security.static.enabled",
        havingValue = "true"
)
public class SecurityConfigurationStaticEnabled extends AbstractSecurityConfiguration {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfigurationStaticEnabled.class);

    @Autowired
    private Environment env;

    @Value("${server.auth.open-registration}")
    private boolean openRegistration;

    @Value("${server.security.csrf}")
    private boolean csrf = true;

    @Bean
    public HttpSessionStrategy httpSessionStrategy() {
        return new HeaderHttpSessionStrategy();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        log.info("Configuration with frontend in static/");
//        @formatter:off
        http
            .httpBasic()
            .and()
            .authorizeRequests()
                .antMatchers(getPatterns()).permitAll()
                .anyRequest().authenticated()
            .and()
            .formLogin()
                .loginPage("/")
            .and()
            .logout()
                .logoutUrl("/api/auth/logout")
            .and()
            .headers()
                .frameOptions().disable()
                .addHeaderWriter(new StaticHeadersWriter("X-Content-Security-Policy","frame-src: 'none'"));
//        @formatter:on
        setCsrf(http);
    }

    @Override
    boolean isOpenRegistration() {
        return openRegistration;
    }

    @Override
    boolean isCsrfEnabled() {
        return csrf;
    }

    @Override
    String[] getStaticPatterns() {
        return new String[]{
                "/bower_components/**", "/scripts/**", "/assets/**", "/styles/**", "/views/**", "/**/favicon.ico", "/favicon.ico", "/**/manifest.json", "/manifest.json", "/configuration/**", "/swagger-resources/**", "/swagger-ui.html", "/webjars/**"
        };
    }

    @Override
    String[] getServerPatterns() {
        return new String[] {
                "/index.html", "/", "/login", "/signup/**", "/recover/**", "/api/auth/signup", "/api/auth/token/verify", "/api/auth/reset", "/api/auth/recover", "/v2/api-docs", "/swagger-ui.html"
        };
    }

    @Override
    Environment getEnvironment() {
        return env;
    }
}