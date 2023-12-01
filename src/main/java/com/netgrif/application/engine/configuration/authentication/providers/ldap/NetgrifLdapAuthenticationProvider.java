package com.netgrif.application.engine.configuration.authentication.providers.ldap;


import com.netgrif.application.engine.configuration.authentication.providers.NetgrifAuthenticationProvider;
import com.netgrif.application.engine.configuration.properties.NaeLdapProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.ldap.NamingException;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.ldap.authentication.*;
import org.springframework.security.ldap.ppolicy.PasswordPolicyException;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Collection;


@Slf4j
@Component
@ConditionalOnExpression("${nae.ldap.enabled:false}")
public class NetgrifLdapAuthenticationProvider extends NetgrifAuthenticationProvider {

    protected NaeLdapProperties ldapProperties;

    protected PasswordEncoder passwordEncoder;

    protected String passwordAttribute;

    protected LdapAuthenticator authenticator;

    protected LdapAuthoritiesPopulator authoritiesPopulator;

    protected UserDetailsContextMapper userDetailsContextMapper;

    protected boolean useAuthenticationRequestCredentials = true;

    protected boolean hideUserNotFoundExceptions = true;


    public BaseLdapPathContextSource contextSource() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(ldapProperties.getUrl());
        contextSource.setBase(ldapProperties.getBase());
        contextSource.setUserDn(ldapProperties.getUsername());
        contextSource.setPassword(ldapProperties.getPassword());
        contextSource.afterPropertiesSet();
        return contextSource;
    }


    protected BindAuthenticator createBindAuthenticator(
            BaseLdapPathContextSource contextSource) {
        return new BindAuthenticator(contextSource);
    }

    protected PasswordComparisonAuthenticator createPasswordCompareAuthenticator(
            BaseLdapPathContextSource contextSource) {
        PasswordComparisonAuthenticator ldapAuthenticator = new PasswordComparisonAuthenticator(
                contextSource);
        if (passwordAttribute != null) {
            ldapAuthenticator.setPasswordAttributeName(passwordAttribute);
        }
        ldapAuthenticator.setPasswordEncoder(passwordEncoder);
        return ldapAuthenticator;
    }

    protected LdapAuthenticator createLdapAuthenticator(BaseLdapPathContextSource contextSource) {
        AbstractLdapAuthenticator ldapAuthenticator = passwordEncoder == null ? createBindAuthenticator(contextSource) : createPasswordCompareAuthenticator(contextSource);

        LdapUserSearch userSearch = new FilterBasedLdapUserSearch(ldapProperties.getPeopleSearchBase(), ldapProperties.getUserFilter(), contextSource);
        if (userSearch != null) {
            ldapAuthenticator.setUserSearch(userSearch);
        }
        return ldapAuthenticator;
    }

    public NetgrifLdapAuthenticationProvider(NaeLdapProperties properties) {
        this.ldapProperties = properties;
        BaseLdapPathContextSource contextSource = contextSource();
        LdapAuthenticator ldapAuthenticator = createLdapAuthenticator(contextSource);
        this.setAuthenticator(ldapAuthenticator);
        this.setAuthoritiesPopulator(new NullLdapAuthoritiesPopulator());
    }


    protected void setAuthenticator(LdapAuthenticator authenticator) {
        Assert.notNull(authenticator, "LdapAuthenticator must be supplied");
        this.authenticator = authenticator;
    }

    protected LdapAuthenticator getAuthenticator() {
        return authenticator;
    }

    protected void setAuthoritiesPopulator(LdapAuthoritiesPopulator authoritiesPopulator) {
        Assert.notNull(authoritiesPopulator, "LdapAuthoritiesPopulator must be supplied");
        this.authoritiesPopulator = authoritiesPopulator;
    }

    protected LdapAuthoritiesPopulator getAuthoritiesPopulator() {
        return authoritiesPopulator;
    }


    public void setUserDetailsContextMapper(UserDetailsContextMapper userDetailsContextMapper) {
        Assert.notNull(userDetailsContextMapper, "UserDetailsContextMapper must not be null");
        this.userDetailsContextMapper = userDetailsContextMapper;
    }


    public void setUseAuthenticationRequestCredentials(boolean useAuthenticationRequestCredentials) {
        this.useAuthenticationRequestCredentials = useAuthenticationRequestCredentials;
    }


    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        WebAuthenticationDetails details = (WebAuthenticationDetails) authentication.getDetails();
        String key = details.getRemoteAddress();
        if (key == null) {
            throw new BadCredentialsException("Bad credentials");
        }
        final UsernamePasswordAuthenticationToken userToken = (UsernamePasswordAuthenticationToken) authentication;

        String username = userToken.getName();
        String password = (String) authentication.getCredentials();

        if (log.isDebugEnabled()) {
            log.debug("Processing authentication request for user: " + username);
        }

        if (!StringUtils.hasLength(username)) {
            log.error("Empty Username");
            loginAttemptService.loginFailed(key);
            throw new BadCredentialsException("Empty Username");
        }

        Assert.notNull(password, "Null password was supplied in authentication token");

        try {
            DirContextOperations userData = getAuthenticator().authenticate(authentication);

            Collection<GrantedAuthority> extraAuthorities = loadUserAuthorities(userData, username, password);

            UserDetails user = userDetailsContextMapper.mapUserFromContext(userData, username, extraAuthorities);
            loginAttemptService.loginSucceeded(key);
            return createSuccessfulAuthentication(userToken, user);
        } catch (PasswordPolicyException ppe) {
            log.error(ppe.getStatus().getErrorCode() + ": " + ppe.getStatus().getDefaultMessage());
            loginAttemptService.loginFailed(key);
            throw new LockedException(ppe.getStatus().getErrorCode() + ": " + ppe.getStatus().getDefaultMessage());
        } catch (UsernameNotFoundException notFound) {
            if (hideUserNotFoundExceptions) {
                log.error("Bad credentials");
                loginAttemptService.loginFailed(key);
                throw new BadCredentialsException("Bad credentials");
            } else {
                loginAttemptService.loginFailed(key);
                throw notFound;
            }
        } catch (NamingException ldapAccessFailure) {
            loginAttemptService.loginFailed(key);
            throw new AuthenticationServiceException(ldapAccessFailure.getMessage(), ldapAccessFailure);
        }
    }

    protected Collection<GrantedAuthority> loadUserAuthorities(DirContextOperations userData, String username, String password) {
        return (Collection<GrantedAuthority>) getAuthoritiesPopulator().getGrantedAuthorities(userData, username);
    }


    protected Authentication createSuccessfulAuthentication(UsernamePasswordAuthenticationToken authentication,
                                                            UserDetails user) {
        Object password = useAuthenticationRequestCredentials ? authentication.getCredentials() : user.getPassword();

        UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken(user, password, user.getAuthorities());
        result.setDetails(authentication.getDetails());

        return result;
    }

    @Override
    public boolean supports(Class<? extends Object> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }


}
