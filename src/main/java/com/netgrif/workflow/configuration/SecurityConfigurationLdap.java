//package com.netgrif.workflow.configuration;
//
//import com.netgrif.workflow.auth.domain.Authority;
//import com.netgrif.workflow.auth.domain.User;
//import com.netgrif.workflow.auth.service.interfaces.IAuthorityService;
//import com.netgrif.workflow.auth.service.interfaces.IUserService;
//import com.netgrif.workflow.configuration.properties.NaeLdapProperties;
//import com.netgrif.workflow.configuration.properties.SecurityConfigProperties;
//import com.netgrif.workflow.configuration.security.PublicAuthenticationFilter;
//import com.netgrif.workflow.configuration.security.RestAuthenticationEntryPoint;
//import com.netgrif.workflow.configuration.security.jwt.IJwtService;
//import com.netgrif.workflow.ldap.domain.LdapUser;
//import com.netgrif.workflow.ldap.domain.LdapUserRef;
//import com.netgrif.workflow.ldap.domain.repository.LdapUserRefRepository;
//import com.netgrif.workflow.ldap.service.LdapUserService;
//import com.netgrif.workflow.ldap.service.interfaces.ILdapUserRefService;
//import com.netgrif.workflow.petrinet.service.interfaces.IProcessRoleService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
//import org.springframework.boot.autoconfigure.security.SecurityProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.annotation.Order;
//import org.springframework.core.env.Environment;
//import org.springframework.http.HttpStatus;
//import org.springframework.ldap.core.DirContextAdapter;
//import org.springframework.ldap.core.DirContextOperations;
//import org.springframework.ldap.core.support.LdapContextSource;
//import org.springframework.security.authentication.AnonymousAuthenticationProvider;
//import org.springframework.security.authentication.ProviderManager;
//import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
//import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;
//import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
//import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
//import org.springframework.security.web.header.writers.StaticHeadersWriter;
//import org.springframework.session.web.http.HeaderHttpSessionIdResolver;
//import org.springframework.session.web.http.HttpSessionIdResolver;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.CorsConfigurationSource;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//
//import java.util.Collection;
//import java.util.HashSet;
//
//import static org.springframework.http.HttpMethod.OPTIONS;
//
//@Slf4j
//@Configuration
//@Controller
//@EnableWebSecurity
//@Order(SecurityProperties.BASIC_AUTH_ORDER)
//@ConditionalOnExpression("${nae.ldap.enabled} && !${server.security.static.enabled}")
//public class SecurityConfigurationLdap extends AbstractSecurityConfiguration {
//
//    @Autowired
//    private Environment env;
//
//    @Autowired
//    private RestAuthenticationEntryPoint authenticationEntryPoint;
//
//    @Autowired
//    private IAuthorityService authorityService;
//
//    @Autowired
//    private IJwtService jwtService;
//
//    @Autowired
//    private IProcessRoleService roleService;
//
//    @Autowired
//    private IUserService userService;
//
//    @Autowired
//    private LdapUserService ldapUserService;
//
//    @Autowired
//    private ILdapUserRefService ldapUserRefService;
//
//    @Autowired
//    private LdapUserRefRepository ldapUserRefRepository;
//
//    @Autowired
//    private SecurityConfigProperties properties;
//
//    @Autowired
//    private UserDetailsService userDetailsService;
//
//    @Autowired
//    protected NaeLdapProperties ldapProperties;
//
//    @Autowired
//    private EncryptionConfiguration passwordEncoder;
//
//    @Value("${nae.security.server-patterns}")
//    private String[] serverPatterns;
//
//    @Value("${spring.ldap.urls}")
//    private String ldapUrl;
//
//    @Value("${spring.ldap.username}")
//    private String ldapUsername;
//
//    @Value("${spring.ldap.password}")
//    private String ldapPassword;
//
//    private static final String ANONYMOUS_USER = "anonymousUser";
//
//
//    @Bean
//    public HttpSessionIdResolver httpSessionIdResolver() {
//        return HeaderHttpSessionIdResolver.xAuthToken();
//    }
//
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration config = new CorsConfiguration().applyPermitDefaultValues();
//        config.addAllowedMethod("*");
//        config.addAllowedHeader("*");
//        config.addExposedHeader("X-Auth-Token");
//        config.addExposedHeader("X-Jwt-Token");
//        config.setAllowCredentials(true);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", config);
//
//        return source;
//    }
//
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        log.info("Configuration with frontend separated");
////        @formatter:off
//        http
//                .httpBasic()
//                .authenticationEntryPoint(authenticationEntryPoint)
//                .and()
//                .cors()
//                .and()
//                .addFilterBefore(createPublicAuthenticationFilter(), BasicAuthenticationFilter.class)
//                .authorizeRequests()
//                .antMatchers(getPatterns()).permitAll()
//                .antMatchers(OPTIONS).permitAll()
//                .anyRequest().authenticated()
//                .and()
//                .logout()
//                .logoutUrl("/api/auth/logout")
//                .invalidateHttpSession(true)
//                .logoutSuccessHandler((new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK)))
//                .and()
//                .headers()
//                .frameOptions().disable()
//                .httpStrictTransportSecurity().includeSubDomains(true).maxAgeInSeconds(31536000)
//                .and()
//                .addHeaderWriter(new StaticHeadersWriter("X-Content-Security-Policy", "frame-src: 'none'"));
////        @formatter:on
//        setCsrf(http);
//        configureSession(http);
//        configureFilters(http);
//    }
//
//    @Override
//    public void configure(AuthenticationManagerBuilder auth) throws Exception {
//        log.info("Configuring security with both LDAP and alt authentication");
//
//        DaoAuthenticationProvider daoAuth = new DaoAuthenticationProvider();
//        daoAuth.setUserDetailsService(userDetailsService);
//        daoAuth.setPasswordEncoder(passwordEncoder.bCryptPasswordEncoder());
//
//        auth
//                .ldapAuthentication()
//                .userSearchFilter(ldapProperties.getUserFilter())
//                .groupSearchBase(ldapProperties.getGroupSearchBase())
//                .contextSource(contextSource())
//                .userDetailsContextMapper(new UserDetailsContextMapperImpl())
//                .and()
//                .authenticationProvider(daoAuth);
//    }
//
//    @Override
//    protected ProviderManager authenticationManager() throws Exception {
//        return (ProviderManager) super.authenticationManager();
//    }
//
//
//    @Override
//    boolean isOpenRegistration() {
//        return this.serverAuthProperties.isOpenRegistration();
//    }
//
//    @Override
//    boolean isCsrfEnabled() {
//        return properties.isCsrf();
//    }
//
//    @Override
//    String[] getStaticPatterns() {
//        return new String[]{
//                "/**/favicon.ico", "/favicon.ico", "/**/manifest.json", "/manifest.json", "/configuration/**", "/swagger-resources/**", "/swagger-ui.html", "/webjars/**"
//        };
//    }
//
//    @Override
//    String[] getServerPatterns() {
//        return this.serverPatterns;
//    }
//
//    @Override
//    Environment getEnvironment() {
//        return env;
//    }
//
//    private PublicAuthenticationFilter createPublicAuthenticationFilter() throws Exception {
//        Authority authority = authorityService.getOrCreate(Authority.anonymous);
//        authority.setUsers(new HashSet<>());
//        return new PublicAuthenticationFilter(
//                authenticationManager(),
//                new AnonymousAuthenticationProvider(ANONYMOUS_USER),
//                authority,
//                this.serverPatterns,
//                this.jwtService,
//                this.userService
//        );
//    }
//
//    public LdapContextSource contextSource() {
//        LdapContextSource contextSource = new LdapContextSource();
//        contextSource.setUrl(ldapUrl);
//        contextSource.setBase(ldapProperties.getBase());
//        contextSource.setUserDn(ldapUsername);
//        contextSource.setPassword(ldapPassword);
//        contextSource.afterPropertiesSet();
//        return contextSource;
//    }
//
//    public class UserDetailsContextMapperImpl implements UserDetailsContextMapper {
//
//        @Override
//        public UserDetails mapUserFromContext(DirContextOperations dirContextOperations, String username, Collection<? extends GrantedAuthority> authorities) {
//            User user = ldapUserService.findByDn(dirContextOperations.getDn());
//            if (user == null) {
//                LdapUserRef ldapUserOptional = ldapUserRefService.findById(dirContextOperations.getDn());
//                if (ldapUserOptional == null) {
//                    log.warn("Unknown user [" + username + "] tried to log in");
//                    return null;
//                }
//                user = ldapUserRefService.createUser(ldapUserOptional);
//            } else if( user instanceof LdapUser ){
//                ldapUserRefService.updateById(dirContextOperations.getDn(), user);
//            }
//            assert user != null;
//            return user.transformToLoggedUser();
//        }
//
//        @Override
//        public void mapUserToContext(UserDetails userDetails, DirContextAdapter dirContextAdapter) {
//
//        }
//    }
//}