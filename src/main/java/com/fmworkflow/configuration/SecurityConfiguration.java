package com.fmworkflow.configuration;

import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.Collections;
import java.util.Map;

@Configuration
@Controller
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @RequestMapping(value = "/{path:[^\\.]*}")
    public String redirect() {
        return "forward:/";
    }

    @Bean
    CsrfTokenRepository httpSessionCsrfTokenRepository() {
        HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
        repository.setHeaderName("X-XSRF-TOKEN");
        return repository;
    }

    @RequestMapping("/user")
    @ResponseBody
    public Principal user(Principal user) {
        return user;
    }

//    @RequestMapping("/token")
//    public Map<String,String> token(HttpSession session) {
//        return Collections.singletonMap("token", session.getId());
//    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .httpBasic().and()
            .authorizeRequests()
                .antMatchers("/index.html", "/", "/login").permitAll()
                .anyRequest().authenticated()
                .and()
            .csrf()
                .csrfTokenRepository(httpSessionCsrfTokenRepository());
    }
}
