package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.adapter.spring.configuration.filters.NetgrifHttpRequestTransformFilter;
import com.netgrif.application.engine.configuration.properties.SecurityConfigurationProperties;
import com.netgrif.application.engine.configuration.security.ImpersonationRequestFilter;
import com.netgrif.application.engine.configuration.security.PublicAuthenticationFilter;
import com.netgrif.application.engine.configuration.security.RestAuthenticationEntryPoint;
import com.netgrif.application.engine.configuration.security.SecurityContextFilter;
import com.netgrif.application.engine.configuration.security.filter.HostValidationRequestFilter;
import com.netgrif.application.engine.configuration.security.RealmFilter;
import com.netgrif.application.engine.impersonation.service.interfaces.IImpersonationService;
import com.netgrif.application.engine.security.service.ISecurityContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.request.async.WebAsyncManagerIntegrationFilter;
import org.springframework.stereotype.Controller;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.ForwardedHeaderFilter;

import java.util.List;

import static org.springframework.http.HttpMethod.OPTIONS;


@Slf4j
@Configuration
@EnableWebSecurity
@Order(SecurityProperties.DEFAULT_FILTER_ORDER)
public class NaeSecurityConfiguration extends AbstractSecurityConfiguration {

    @Autowired
    private Environment env;

    @Autowired
    private RestAuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    private SecurityConfigurationProperties securityConfigurationProperties;

    @Autowired
    private ISecurityContextService securityContextService;

    @Autowired
    protected IImpersonationService impersonationService;

    @Autowired
    private List<AuthenticationProvider> authenticationProviders;

    @Autowired
    private PublicAuthenticationFilter publicAuthenticationFilter;

    @Autowired
    private NetgrifHttpRequestTransformFilter netgrifHttpRequestTransformFilter;

    @Autowired
    private RealmFilter realmFilter;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        List<String> allowedOrigins = securityConfigurationProperties.getAllowedOrigins();

        CorsConfiguration config = new CorsConfiguration().applyPermitDefaultValues();
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.addExposedHeader("X-Auth-Token");
        config.addExposedHeader("X-Anonymous-Token");
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

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        log.info("Configuration with frontend separated");
        http
                .httpBasic(httpSecurityHttpBasicConfigurer ->
                        httpSecurityHttpBasicConfigurer.authenticationEntryPoint(authenticationEntryPoint))
                .addFilterBefore(new ForwardedHeaderFilter(), WebAsyncManagerIntegrationFilter.class)
                .addFilterAfter(createSecurityContextFilter(), BasicAuthenticationFilter.class)
                .addFilterAfter(impersonationRequestFilter(), BasicAuthenticationFilter.class)
                .addFilterAfter(hostValidationRequestFilter(), BasicAuthenticationFilter.class)
                .addFilterBefore(publicAuthenticationFilter, SecurityContextFilter.class)
                .addFilterBefore(netgrifHttpRequestTransformFilter, BasicAuthenticationFilter.class)
                .addFilterBefore(realmFilter, PublicAuthenticationFilter.class)
                .authorizeHttpRequests(requestMatcherRegistry ->
                        requestMatcherRegistry
                                .requestMatchers(getPatterns()).permitAll()
                                .requestMatchers(OPTIONS).permitAll()
                                .anyRequest().authenticated())
                .logout(httpSecurityLogoutConfigurer ->
                        httpSecurityLogoutConfigurer
                                .logoutUrl("/api/auth/logout")
                                .invalidateHttpSession(true)
                                .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK)));
        http.authenticationProvider(authenticationProviders.getFirst());
        http.sessionManagement(httpSecuritySessionManagementConfigurer -> httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.ALWAYS));
        configureFilters(http);
        configureSession(http);
        setHeaders(http);
        setCsrf(http);
        corsEnable(http);
        return http.build();
    }

    @Override
    protected boolean isOpenRegistration() {
        return this.serverAuthProperties.isOpenRegistration();
    }

    @Override
    protected boolean isCsrfEnabled() {
        return securityConfigurationProperties.isCsrf();
    }

    @Override
    protected boolean isCorsEnabled() {
        return securityConfigurationProperties.isCors();
    }

    @Override
    protected String[] getStaticPatterns() {
        return this.securityConfigurationProperties.getStaticPatterns();
    }

    @Override
    protected String[] getServerPatterns() {
        return this.securityConfigurationProperties.getServerPatterns();
    }

    @Override
    protected Environment getEnvironment() {
        return env;
    }

    @Override
    protected SecurityConfigurationProperties getSecurityConfigProperties() {
        return securityConfigurationProperties;
    }


    private SecurityContextFilter createSecurityContextFilter() {
        return new SecurityContextFilter(securityContextService);
    }

    private HostValidationRequestFilter hostValidationRequestFilter() {
        return new HostValidationRequestFilter(securityConfigurationProperties);
    }

    private ImpersonationRequestFilter impersonationRequestFilter() {
        return new ImpersonationRequestFilter(impersonationService);
    }
}
