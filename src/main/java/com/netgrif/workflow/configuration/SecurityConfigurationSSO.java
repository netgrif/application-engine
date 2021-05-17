package com.netgrif.workflow.configuration;

import com.netgrif.workflow.auth.domain.Authority;
import com.netgrif.workflow.auth.service.interfaces.IAuthorityService;
import com.netgrif.workflow.oauth.service.interfaces.IOauthUserMapper;
import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.configuration.properties.SecurityConfigProperties;
import com.netgrif.workflow.configuration.security.PublicAuthenticationFilter;
import com.netgrif.workflow.configuration.security.RestAuthenticationEntryPoint;
import com.netgrif.workflow.configuration.security.jwt.IJwtService;
import com.netgrif.workflow.petrinet.service.interfaces.IProcessRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.session.web.http.HeaderHttpSessionIdResolver;
import org.springframework.session.web.http.HttpSessionIdResolver;
import org.springframework.stereotype.Controller;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.HashSet;

import static org.springframework.http.HttpMethod.OPTIONS;

@Slf4j
@Configuration
@Controller
@EnableWebSecurity
@Order(SecurityProperties.DEFAULT_FILTER_ORDER)
@ConditionalOnExpression("${nae.oauth.enabled}")
@EnableOAuth2Sso
public class SecurityConfigurationSSO extends AbstractSecurityConfiguration {

    @Autowired
    private Environment env;

    @Autowired
    private RestAuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    private IAuthorityService authorityService;

    @Autowired
    private IJwtService jwtService;

    @Autowired
    private IProcessRoleService roleService;

    @Autowired
    private IUserService userService;

    @Autowired
    private IOauthUserMapper oauthUserMapper;

    @Autowired
    private SecurityConfigProperties properties;

    @Autowired
    private ResourceServerTokenServices tokenServices;

    @Value("${nae.security.server-patterns}")
    private String[] serverPatterns;

    private static final String ANONYMOUS_USER = "anonymousUser";

    @Bean
    public HttpSessionIdResolver httpSessionIdResolver() {
        return HeaderHttpSessionIdResolver.xAuthToken();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration().applyPermitDefaultValues();
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.addExposedHeader("X-Auth-Token");
        config.addExposedHeader("X-Jwt-Token");
        config.addAllowedOrigin("*");
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        log.info("Configuration with frontend separated");
//        @formatter:off
        http
//                .httpBasic()
//                .and()
                .cors()
                .and()
                .addFilterBefore(new ApiTokenAccessFilter(tokenServices), AbstractPreAuthenticatedProcessingFilter.class)
                .addFilterAfter(new OAuth2AuthenticationConvertingFilter(oauthUserMapper), ApiTokenAccessFilter.class)
//                .authenticationEntryPoint (authenticationEntryPoint)
                .addFilterBefore(createPublicAuthenticationFilter(), BasicAuthenticationFilter.class)
                .authorizeRequests()
                .antMatchers(getPatterns()).permitAll()
                .antMatchers(OPTIONS).permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .logout()
                .logoutUrl("/api/auth/logout")
                .invalidateHttpSession(true)
                .logoutSuccessHandler((new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK)))
                .and()
                .headers()
                .frameOptions().disable()
                .httpStrictTransportSecurity().includeSubDomains(true).maxAgeInSeconds(31536000)
                .and()
                .addHeaderWriter(new StaticHeadersWriter("X-Content-Security-Policy", "frame-src: 'none'"))
//                .and()
//                .oauth2Login()
//                .userInfoEndpoint()
//                .oidcUserService(new CustomOidcUserServiceImpl())
        ;
//        @formatter:on
        setCsrf(http);
    }


    @Override
    protected ProviderManager authenticationManager() throws Exception {
        return (ProviderManager) super.authenticationManager();
    }

    @Override
    boolean isOpenRegistration() {
        return this.serverAuthProperties.isOpenRegistration();
    }

    @Override
    boolean isCsrfEnabled() {
        return properties.isCsrf();
    }

    @Override
    String[] getStaticPatterns() {
        return new String[]{
                "/**/favicon.ico", "/favicon.ico", "/**/manifest.json", "/manifest.json", "/configuration/**", "/swagger-resources/**", "/swagger-ui.html", "/webjars/**"
        };
    }

    @Override
    String[] getServerPatterns() {
        return this.serverPatterns;
    }

    @Override
    Environment getEnvironment() {
        return env;
    }

//    @Bean
//    @ConfigurationProperties("security.oauth2.client")
//    public AuthorizationCodeResourceDetails oauth2Client() {
//        return new AuthorizationCodeResourceDetails();
//    }


//
//    @Bean
//    @ConfigurationProperties("security.oauth2.resource")
//    public ResourceServerProperties oauth2Resource() {
//        return new ResourceServerProperties();
//    }
//
//    @Bean
//    public FilterRegistrationBean<OAuth2ClientContextFilter> oauth2ClientFilterRegistration(
//            OAuth2ClientContextFilter filter) {
//        FilterRegistrationBean<OAuth2ClientContextFilter> registration = new FilterRegistrationBean<>();
//        registration.setFilter(filter);
//        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
//        return registration;
//    }
//
//
//    private Filter oauth2ClientFilter() {
//        OAuth2ClientAuthenticationProcessingFilter oauth2ClientFilter = new OAuth2ClientAuthenticationProcessingFilter("/");
//        OAuth2RestTemplate restTemplate = new OAuth2RestTemplate(oauth2Client(), oauth2ClientContext);
//        oauth2ClientFilter.setRestTemplate(restTemplate);
//        UserInfoTokenServices tokenServices = new UserInfoTokenServices(oauth2Resource().getUserInfoUri(),
//                oauth2Client().getClientId());
//        tokenServices.setRestTemplate(restTemplate);
//        oauth2ClientFilter.setTokenServices(tokenServices);
//        return oauth2ClientFilter;
//    }

    private PublicAuthenticationFilter createPublicAuthenticationFilter() throws Exception {
        Authority authority = authorityService.getOrCreate(Authority.anonymous);
        authority.setUsers(new HashSet<>());
        return new PublicAuthenticationFilter(
                authenticationManager(),
                new AnonymousAuthenticationProvider(ANONYMOUS_USER),
                authority,
                this.serverPatterns,
                this.jwtService,
                this.userService
        );
    }
}