package com.netgrif.workflow.configuration;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.stream.Stream;

@Configuration
@Controller
@EnableWebSecurity
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfiguration.class);

    private final String[] PERMIT_ALL_STATIC_PATTERNS = {
            "/bower_components/**","/scripts/**","/assets/**","/styles/**","/views/**","/**/favicon.ico"
    };
    private final String[] PERMIT_ALL_SERVER_PATTERNS = {
            "/index.html", "/", "/login", "/signup/{token}", "/signup", "/signup/token"
    };

    @Autowired
    private Environment env;

    @Autowired
    private UserDetailsService userDetailsService;

    @RequestMapping(value = "{path:[^res][^\\.]*$}")
    public String redirect() {
        log.info("Forwarding to root");
        return "forward:/";
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder());
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @RequestMapping("/user")
    @ResponseBody
    public Principal user(Principal user) {
        return user;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .httpBasic().and()
            .authorizeRequests()
                .antMatchers(getPatterns()).permitAll()
                .anyRequest().authenticated()
            .and()
            .formLogin()
                .loginPage("/login")
            .and()
            .csrf()//.disable();
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
    }

    private String[] getPatterns() {
        String[] patterns = (String[]) ArrayUtils.addAll(PERMIT_ALL_STATIC_PATTERNS, PERMIT_ALL_SERVER_PATTERNS);
        if (Stream.of(env.getActiveProfiles()).anyMatch(it -> it.equals("dev")))
            patterns = (String[]) ArrayUtils.add(patterns, "/dev/**");
        return patterns;
    }
}
