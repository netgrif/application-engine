package com.netgrif.workflow.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Configuration
@Controller
@EnableWebSecurity
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfiguration.class);

    private final String[] PERMIT_ALL_STATIC_PATTERNS = {
            "/bower_components/**", "/scripts/**", "/assets/**", "/styles/**", "/views/**", "/**/favicon.ico", "/favicon.ico", "/configuration/**", "/swagger-resources/**", "/swagger-ui.html", "/webjars/**"
    };
    private final String[] PERMIT_ALL_SERVER_PATTERNS = {
            "/index.html", "/", "/login", "/signup/**", "/recover/**", "/api/auth/signup", "/api/auth/token/verify", "/api/auth/reset", "/api/auth/recover", "/v2/api-docs", "/swagger-ui.html"
    };

    @Autowired
    private Environment env;

    @Value("${server.auth.open-registration}")
    private boolean openRegistration;

    @RequestMapping(value = "{path:^(?!\\/api\\S*)\\S*(?!\\.\\S*)\\S*$}")
    public String redirect(HttpServletRequest request) {
        log.info("Forwarding to root for request URI [" + request.getRequestURI() + "]");
        return "forward:/";
    }

    @RequestMapping(value = {"/signup/{token}", "/recover/{token}"})
    public String redirectWithToken(@PathVariable("token") String token, HttpServletRequest request) {
        log.info("Forwarding to root for URI [ " + request.getRequestURI() + " ] with token " + token);
        return "forward:/";
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
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
            .csrf()//.disable();
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            .and()
            .headers()
                .frameOptions().sameOrigin();
//        @formatter:on
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
