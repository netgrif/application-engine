//package com.netgrif.application.engine.configuration.authenticationProviders;
//
//
//import com.netgrif.application.engine.auth.domain.IUser;
//import com.netgrif.application.engine.auth.domain.LoggedUser;
//import com.netgrif.application.engine.auth.domain.UserState;
//import com.netgrif.application.engine.auth.service.interfaces.ILdapUserRefService;
//import com.netgrif.application.engine.configuration.SecurityConfigurationLdap;
//import com.netgrif.application.engine.configuration.properties.NaeLdapProperties;
//import com.netgrif.application.engine.event.events.user.UserLoginEvent;
//import com.netgrif.application.engine.ldap.domain.LdapUser;
//import com.netgrif.application.engine.ldap.domain.LdapUserRef;
//import com.netgrif.application.engine.ldap.service.LdapUserService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Primary;
//import org.springframework.context.support.MessageSourceAccessor;
//import org.springframework.ldap.core.DirContextAdapter;
//import org.springframework.ldap.core.DirContextOperations;
//import org.springframework.ldap.core.support.LdapContextSource;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
//import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
//import org.springframework.security.core.SpringSecurityMessageSource;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.security.crypto.factory.PasswordEncoderFactories;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.util.Assert;
//
//@Slf4j
//@Component
//public class LdapAuthenticationProvider extends NetgrifAuthenticationProvider {
//
//    @Autowired
//    protected NaeLdapProperties ldapProperties;
//
//    @Autowired
//    private LdapUserService ldapUserService;
//
//    @Autowired
//    private ILdapUserRefService ldapUserRefService;
//
//    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();
//
//    protected PasswordEncoder passwordEncoder;
//
//    @Value("${nae.security.server-patterns}")
//    private String[] serverPatterns;
//
//    @Value("${nae.security.anonymous-exceptions}")
//    private String[] anonymousExceptions;
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
//
//
////    @Override
////    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
////        String name = authentication.getName();
////        User user = findUser(name);
////        if (user == null) {
////            log.debug("User not found");
////            throw new BadCredentialsException(this.messages
////                    .getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
////        }
////        String presentedPassword = authentication.getCredentials().toString();
////        if (!this.passwordEncoder.matches(presentedPassword, user.getPassword())) {
////            log.debug("Failed to authenticate since password does not match stored value");
////            throw new BadCredentialsException(this.messages
////                    .getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
////        }
////        UserDetails userDetails = user.transformToLoggedUser();
////        return new UsernamePasswordAuthenticationToken(
////                userDetails, presentedPassword, new ArrayList<>());
////    }
//
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
//    @Override
//    public boolean supports(Class<?> authentication) {
//        return authentication.equals(UsernamePasswordAuthenticationToken.class);
//    }
//
//    protected UserDetails findUser(String email) {
//        DirContextOperations dirContextOperations = new DirContextAdapter();
////        Name dn = LdapNameBuilder.newInstance()
////                .add("ou=users")
////                .add("uid=bpatel")
////                .build();
////        dirContextOperations.setDn(dn);
//        dirContextOperations.setAttributeValues("objectClass", ldapProperties.getPeopleClass());
//        dirContextOperations.setAttributeValues("email", email);
//        IUser user = ldapUserService.findByDn(dirContextOperations.getDn());
//        if (user == null) {
//            LdapUserRef ldapUserOptional = ldapUserRefService.findById(dirContextOperations.getDn());
//            if (ldapUserOptional == null) {
//                log.warn("Unknown user [" + dirContextOperations.getDn() + "] tried to log in");
//                return null;
//            }
//            user = ldapUserRefService.createUser(ldapUserOptional);
//        } else if (user instanceof LdapUser) {
//            ldapUserRefService.updateById(dirContextOperations.getDn(), user);
//        }
//        assert user != null;
//        return user.transformToLoggedUser();
//    }
//
//
//    protected String getClientIP() {
//        String xfHeader = request.getHeader("X-Forwarded-For");
//        if (xfHeader == null) {
//            return request.getRemoteAddr();
//        }
//        return xfHeader.split(",")[0];
//    }
//
//
//
//    @Override
//    @Primary
//    @Transactional(readOnly = true)
//    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
//        String ip = getClientIP();
//        if (loginAttemptService.isBlocked(ip)) {
//            log.info("User " + email + " with IP Address " + ip + " is blocked.");
//            throw new RuntimeException("blocked");
//        }
//
//        LoggedUser loggedUser = getLoggedUser(email);
//        loggedUser.setFullName("JOZIKEEEEE");
//        publisher.publishEvent(new UserLoginEvent(loggedUser));
//
//        return loggedUser;
//    }
//
//
//    protected LoggedUser getLoggedUser(String email) throws UsernameNotFoundException {
//        LdapUser user = (LdapUser) ldapUserService.findByEmail(email, false);
//        if (user == null)
//            throw new UsernameNotFoundException("No user was found for login: " + email);
//        if (user.getPassword() == null || user.getState() != UserState.ACTIVE)
//            throw new UsernameNotFoundException("User with login " + email + " cannot be logged in!");
//
//        return user.transformToLoggedUser();
//    }
//
//
//    /**
//     * Sets the PasswordEncoder instance to be used to encode and validate passwords. If
//     * not set, the password will be compared using
//     * {@link PasswordEncoderFactories#createDelegatingPasswordEncoder()}
//     *
//     * @param passwordEncoder must be an instance of one of the {@code PasswordEncoder}
//     *                        types.
//     */
//    public void setLdapProperties(AuthenticationManagerBuilder auth) {
//          auth
//                .ldapAuthentication()
//                .userSearchFilter(ldapProperties.getUserFilter())
//                .groupSearchBase(ldapProperties.getGroupSearchBase())
//                .contextSource(contextSource())
//                .userDetailsContextMapper(new SecurityConfigurationLdap.UserDetailsContextMapperImpl());
//
//    }
//
//}
