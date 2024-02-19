package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.auth.domain.Authority;
import com.netgrif.application.engine.auth.service.interfaces.IAuthorityService;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.configuration.authentication.providers.NaeAuthProperties;
import com.netgrif.application.engine.configuration.properties.NaeLdapProperties;
import com.netgrif.application.engine.configuration.properties.SecurityConfigProperties;
import com.netgrif.application.engine.configuration.security.ImpersonationRequestFilter;
import com.netgrif.application.engine.configuration.security.PublicAuthenticationFilter;
import com.netgrif.application.engine.configuration.security.RestAuthenticationEntryPoint;
import com.netgrif.application.engine.configuration.security.SecurityContextFilter;
import com.netgrif.application.engine.configuration.security.filter.HostValidationRequestFilter;
import com.netgrif.application.engine.configuration.security.jwt.IJwtService;
import com.netgrif.application.engine.impersonation.service.interfaces.IImpersonationService;
import com.netgrif.application.engine.security.service.ISecurityContextService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationProvider;
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

import java.util.HashSet;
import java.util.List;

import static org.springframework.http.HttpMethod.OPTIONS;


@Slf4j
@Controller
@Configuration
@EnableWebSecurity
@Order(SecurityProperties.DEFAULT_FILTER_ORDER)
public class NaeSecurityConfiguration extends AbstractSecurityConfiguration {

    @Autowired
    private Environment env;

    @Autowired
    private RestAuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    private IAuthorityService authorityService;

    @Autowired
    private IJwtService jwtService;

    @Autowired
    private IUserService userService;

    @Autowired
    private NaeAuthProperties naeAuthProperties;

    @Autowired
    private SecurityConfigProperties properties;

    @Autowired
    private ISecurityContextService securityContextService;

    @Autowired
    protected NaeLdapProperties ldapProperties;

    @Autowired
    protected IImpersonationService impersonationService;

    private static final String ANONYMOUS_USER = "anonymousUser";

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
        Authority authority = authorityService.getOrCreate(Authority.anonymous);
        authority.setUsers(new HashSet<>());
        return new PublicAuthenticationFilter(
                authenticationManager(),
                new AnonymousAuthenticationProvider(ANONYMOUS_USER),
                authority,
                this.naeAuthProperties.getServerPatterns(),
                this.naeAuthProperties.getAnonymousExceptions(),
                this.jwtService,
                this.userService
        );
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
