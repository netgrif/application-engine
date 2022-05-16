package com.netgrif.application.engine.configuration;


import com.netgrif.application.engine.auth.domain.Authority;
import com.netgrif.application.engine.auth.domain.AuthorizingObject;
import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.UserProperties;
import com.netgrif.application.engine.ldap.domain.LdapUser;
import com.netgrif.application.engine.auth.service.AfterRegistrationAuthService;
import com.netgrif.application.engine.auth.service.interfaces.IAfterRegistrationAuthService;
import com.netgrif.application.engine.auth.service.interfaces.IAuthorityService;
import com.netgrif.application.engine.auth.service.interfaces.ILdapUserRefService;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.configuration.properties.NaeLdapProperties;
import com.netgrif.application.engine.configuration.properties.SecurityConfigProperties;
import com.netgrif.application.engine.configuration.security.PublicAuthenticationFilter;
import com.netgrif.application.engine.configuration.security.RestAuthenticationEntryPoint;
import com.netgrif.application.engine.configuration.security.jwt.IJwtService;

import com.netgrif.application.engine.ldap.domain.LdapUserRef;
import com.netgrif.application.engine.ldap.service.LdapUserService;
import com.netgrif.application.engine.petrinet.service.interfaces.IProcessRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.AnonymousAuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.session.web.http.HeaderHttpSessionIdResolver;
import org.springframework.session.web.http.HttpSessionIdResolver;
import org.springframework.stereotype.Controller;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static org.springframework.http.HttpMethod.OPTIONS;

@Slf4j
@Configuration
@Controller
@EnableWebSecurity
@Order(SecurityProperties.BASIC_AUTH_ORDER)
@ConditionalOnExpression("${nae.ldap.enabled} && !${nae.server.security.static.enabled}")
public class SecurityConfigurationLdap extends AbstractSecurityConfiguration {

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
    private LdapUserService ldapUserService;

    @Autowired
    private ILdapUserRefService ldapUserRefService;

    @Autowired
    private SecurityConfigProperties properties;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    protected NaeLdapProperties ldapProperties;

    @Autowired
    private EncryptionConfiguration passwordEncoder;

    @Value("${nae.security.server-patterns}")
    private String[] serverPatterns;

    @Value("${nae.security.anonymous-exceptions}")
    private String[] anonymousExceptions;

    @Value("${spring.ldap.urls}")
    private String ldapUrl;

    @Value("${spring.ldap.username}")
    private String ldapUsername;

    @Value("${spring.ldap.password}")
    private String ldapPassword;

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
        config.addAllowedOriginPattern("*");
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
                .httpBasic()
                .authenticationEntryPoint(authenticationEntryPoint)
                .and()
                .cors()
                .and()
                .addFilterBefore(createPublicAuthenticationFilter(), BasicAuthenticationFilter.class)
                .authorizeRequests()
                .antMatchers(getPatterns()).permitAll()
                .antMatchers(OPTIONS).permitAll()
                .anyRequest().authenticated()
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
                .addHeaderWriter(new StaticHeadersWriter("X-Content-Security-Policy", "frame-src: 'none'"));
//        @formatter:on
        setCsrf(http);
        configureSession(http);
        configureFilters(http);
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        log.info("Configuring security with both LDAP and alt authentication");

        DaoAuthenticationProvider daoAuth = new DaoAuthenticationProvider();
        daoAuth.setUserDetailsService(userDetailsService);
        daoAuth.setPasswordEncoder(passwordEncoder.bCryptPasswordEncoder());

        auth
                .ldapAuthentication()
                .userSearchFilter(ldapProperties.getUserFilter())
                .groupSearchBase(ldapProperties.getGroupSearchBase())
                .contextSource(contextSource())
                .userDetailsContextMapper(new UserDetailsContextMapperImpl())
                .and()
                .authenticationProvider(daoAuth);
    }

    @Override
    protected ProviderManager authenticationManager() throws Exception {
        return (ProviderManager) super.authenticationManager();
    }

    @Bean
    protected IAfterRegistrationAuthService authenticationService() throws Exception {
        return new AfterRegistrationAuthService(authenticationManager());
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
    protected String[] getStaticPatterns() {
        return new String[]{
                "/**/favicon.ico", "/favicon.ico", "/**/manifest.json", "/manifest.json", "/configuration/**", "/swagger-resources/**", "/swagger-ui.html", "/webjars/**"
        };
    }

    @Override
    protected String[] getServerPatterns() {
        return this.serverPatterns;
    }

    @Override
    protected Environment getEnvironment() {
        return env;
    }

    private PublicAuthenticationFilter createPublicAuthenticationFilter() throws Exception {
        List<Authority> authorities = authorityService.getOrCreate(Authority.defaultAnonymousAuthorities);
        authorities.forEach(a -> a.setUsers(new HashSet<>()));
        return new PublicAuthenticationFilter(
                authenticationManager(),
                new AnonymousAuthenticationProvider(UserProperties.ANONYMOUS_AUTH_KEY),
                authorities,
                this.serverPatterns,
                this.anonymousExceptions,
                this.jwtService,
                this.userService
        );
    }

    public LdapContextSource contextSource() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(ldapUrl);
        contextSource.setBase(ldapProperties.getBase());
        contextSource.setUserDn(ldapUsername);
        contextSource.setPassword(ldapPassword);
        contextSource.afterPropertiesSet();
        return contextSource;
    }

    public class UserDetailsContextMapperImpl implements UserDetailsContextMapper {

        @Override
        public UserDetails mapUserFromContext(DirContextOperations dirContextOperations, String username, Collection<? extends GrantedAuthority> authorities) {
            dirContextOperations.setAttributeValues("objectClass", ldapProperties.getPeopleClass());
            IUser user = ldapUserService.findByDn(dirContextOperations.getDn());
            if (user == null) {
                LdapUserRef ldapUserOptional = ldapUserRefService.findById(dirContextOperations.getDn());
                if (ldapUserOptional == null) {
                    log.warn("Unknown user [" + username + "] tried to log in");
                    return null;
                }
                user = ldapUserRefService.createUser(ldapUserOptional);
            } else if( user instanceof LdapUser){
                ldapUserRefService.updateById(dirContextOperations.getDn(), user);
            }
            assert user != null;
            return user.transformToLoggedUser();
        }

        @Override
        public void mapUserToContext(UserDetails userDetails, DirContextAdapter dirContextAdapter) {

        }
    }
}