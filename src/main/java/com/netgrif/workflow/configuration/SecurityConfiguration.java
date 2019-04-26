package com.netgrif.workflow.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Configuration
@Controller
@EnableWebSecurity
@Order(SecurityProperties.BASIC_AUTH_ORDER)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfiguration.class);

    private final String[] PERMIT_ALL_STATIC_PATTERNS = {
            "/**/favicon.ico", "/favicon.ico", "/**/manifest.json", "/manifest.json", "/configuration/**", "/swagger-resources/**", "/swagger-ui.html", "/webjars/**"
    };
    private final String[] PERMIT_ALL_SERVER_PATTERNS = {
            "/api/auth/signup", "/api/auth/token/verify", "/api/auth/reset", "/api/auth/recover", "/v2/api-docs", "/swagger-ui.html"
    };

    @Autowired
    private Environment env;

    @Value("${server.auth.open-registration}")
    private boolean openRegistration;

    @Value("${server.security.csrf}")
    private boolean csrf = true;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration().applyPermitDefaultValues();
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.addExposedHeader("x-auth-token");
        config.addAllowedOrigin("*");
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
//        @formatter:off
        http
            .httpBasic()
            .and()
                .cors()
                .and()
            .authorizeRequests()
                .antMatchers(getPatterns()).permitAll()
                .antMatchers(HttpMethod.OPTIONS).permitAll()
                .anyRequest().authenticated()
            .and()
            .formLogin()
                .loginPage("/")
            .and()
            .logout()
                .logoutUrl("/api/auth/logout")
            .and()
            .headers()
                .frameOptions().sameOrigin();
//        @formatter:on
        setCsrf(http);
    }

    private void setCsrf(HttpSecurity http) throws Exception {
        if (csrf) {
            http.csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
        } else {
            http.csrf().disable();
        }
    }


    private String[] getPatterns() {
        List<String> patterns = new ArrayList<>(Arrays.asList(PERMIT_ALL_STATIC_PATTERNS));
        patterns.addAll(Arrays.asList(PERMIT_ALL_SERVER_PATTERNS));
        if (openRegistration)
            patterns.add("/api/auth/invite");
        if (Stream.of(env.getActiveProfiles()).anyMatch(it -> it.equals("dev")))
            patterns.add("/dev/**");
        return patterns.toArray(new String[0]);
    }
}
