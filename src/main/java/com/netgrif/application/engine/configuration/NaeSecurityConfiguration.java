package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.authentication.domain.PublicStrategy;
import com.netgrif.application.engine.configuration.authentication.providers.NaeAuthProperties;
import com.netgrif.application.engine.configuration.properties.NaeLdapProperties;
import com.netgrif.application.engine.configuration.properties.SecurityConfigProperties;
import com.netgrif.application.engine.configuration.security.*;
import com.netgrif.application.engine.configuration.security.factory.PublicAuthenticationFilterFactory;
import com.netgrif.application.engine.configuration.security.filter.HostValidationRequestFilter;
import com.netgrif.application.engine.impersonation.service.interfaces.IImpersonationService;
import com.netgrif.application.engine.security.service.ISecurityContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.request.async.WebAsyncManagerIntegrationFilter;
import org.springframework.session.web.http.HeaderHttpSessionIdResolver;
import org.springframework.session.web.http.HttpSessionIdResolver;
import org.springframework.stereotype.Controller;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.ForwardedHeaderFilter;

import java.util.List;

import static org.springframework.http.HttpMethod.OPTIONS;


@Slf4j
@Controller
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Order(SecurityProperties.DEFAULT_FILTER_ORDER)
public class NaeSecurityConfiguration extends AbstractSecurityConfiguration {

    private final Environment env;

    private final RestAuthenticationEntryPoint authenticationEntryPoint;

    private final ApplicationContext applicationContext;

    private final NaeAuthProperties naeAuthProperties;

    private final SecurityConfigProperties properties;

    private final PublicViewProperties publicProperties;

    private final ISecurityContextService securityContextService;

    protected final NaeLdapProperties ldapProperties;

    protected final IImpersonationService impersonationService;

    @Bean
    public HttpSessionIdResolver httpSessionIdResolver() {
        return HeaderHttpSessionIdResolver.xAuthToken();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        List<String> allowedOrigins = properties.getAllowedOrigins();

        CorsConfiguration config = new CorsConfiguration().applyPermitDefaultValues();
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.addExposedHeader("X-Auth-Token");
        config.addExposedHeader("X-Jwt-Token");
        config.setAllowCredentials(true);
        if (allowedOrigins == null || allowedOrigins.isEmpty()) {
            config.addAllowedOriginPattern("*");
        } else {
            config.setAllowedOrigins(allowedOrigins);
        }

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        log.info("Configuration with frontend separated");
        http
                .httpBasic()
                .authenticationEntryPoint(authenticationEntryPoint)
                .and()
                .addFilterBefore(new ForwardedHeaderFilter(), WebAsyncManagerIntegrationFilter.class)
                .addFilterBefore(createPublicAuthenticationFilter(), BasicAuthenticationFilter.class)
                .addFilterAfter(createSecurityContextFilter(), BasicAuthenticationFilter.class)
                .addFilterAfter(impersonationRequestFilter(), BasicAuthenticationFilter.class)
                .addFilterAfter(hostValidationRequestFilter(), BasicAuthenticationFilter.class)
                .authorizeRequests()
                .antMatchers(getPatterns()).permitAll()
                .antMatchers(OPTIONS).permitAll()
                .anyRequest().authenticated()
                .and()
                .logout()
                .logoutUrl("/api/auth/logout")
                .invalidateHttpSession(true)
                .logoutSuccessHandler((new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK)));
        configureFilters(http);
        configureSession(http);
        setHeaders(http);
        setCsrf(http);
        corsEnable(http);
    }

    @Override
    protected ProviderManager authenticationManager() throws Exception {
        return (ProviderManager) super.authenticationManager();
    }

    @Override
    protected boolean isOpenRegistration() {
        return this.serverAuthProperties.isOpenRegistration();
    }

    @Override
    protected boolean isCsrfEnabled() {
        return properties.isCsrf();
    }

    @Override
    protected boolean isCorsEnabled() {
        return properties.isCors();
    }

    @Override
    protected String[] getStaticPatterns() {
        return this.naeAuthProperties.getStaticPatterns();
    }

    @Override
    protected String[] getServerPatterns() {
        return this.naeAuthProperties.getServerPatterns();
    }

    @Override
    protected Environment getEnvironment() {
        return env;
    }

    @Override
    protected SecurityConfigProperties getSecurityConfigProperties() {
        return properties;
    }

    protected PublicAuthenticationFilter createPublicAuthenticationFilter() throws Exception {
        PublicStrategy strategy = publicProperties.getStrategy();
        PublicAuthenticationFilterFactory filterFactory = (PublicAuthenticationFilterFactory) applicationContext.getBean(strategy.factoryClass);
        return filterFactory.createFilter(authenticationManager());
    }

    private SecurityContextFilter createSecurityContextFilter() {
        return new SecurityContextFilter(securityContextService);
    }

    private HostValidationRequestFilter hostValidationRequestFilter() {
        return new HostValidationRequestFilter(properties);
    }

    private ImpersonationRequestFilter impersonationRequestFilter() {
        return new ImpersonationRequestFilter(impersonationService);
    }
}
