package com.netgrif.workflow.configuration;

import com.netgrif.workflow.configuration.AbstractSecurityConfiguration;
import com.netgrif.workflow.configuration.security.jwt.JwtAuthenticationProvider;
import groovy.util.logging.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@Configuration
@EnableWebSecurity
@Order(SecurityProperties.BASIC_AUTH_ORDER + 1)
@ConditionalOnProperty(
        value = "server.security.static.enabled",
        havingValue = "false"
)
public class AnonymousSecurityConfiguration extends AbstractSecurityConfiguration {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfiguration.class);

    @Value("${server.auth.open-registration}")
    private boolean openRegistration;

    @Value("${server.security.csrf}")
    private boolean csrf = true;

    @Autowired
    private Environment env;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        log.info("Configuring security for anonymous user.");
        http
                .authenticationProvider(new JwtAuthenticationProvider("anonymous"))
                .antMatcher("/api/public/**")
                .authorizeRequests()
                .anyRequest().permitAll();
    }

    boolean isOpenRegistration() {
        return openRegistration;
    }

    @Override
    boolean isCsrfEnabled() {
        return csrf;
    }

    @Override
    String[] getStaticPatterns() {
        return new String[0];
    }

    @Override
    String[] getServerPatterns() {
        return new String[0];
    }

    @Override
    Environment getEnvironment() {
        return env;
    }


}
