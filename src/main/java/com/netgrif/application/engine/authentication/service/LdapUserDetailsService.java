package com.netgrif.application.engine.authentication.service;

import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.authentication.service.interfaces.ILoginAttemptService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.servlet.http.HttpServletRequest;

@ConditionalOnExpression("${nae.ldap.enabled:false}")
public class LdapUserDetailsService /*extends UserDetailsServiceImpl*/ {

    public LdapUserDetailsService(ApplicationEventPublisher publisher, ILoginAttemptService loginAttemptService,
                                  HttpServletRequest request) {
//        super(publisher, loginAttemptService, request);
    }

//    @Override
    protected Identity getLoggedUser(String email) throws UsernameNotFoundException {
        return null;
//        IUser user = userRepository.findByEmail(email);
//        if (user == null) {
//            throw new UsernameNotFoundException("No user was found for login: " + email);
//        } else if (user.getState() != IdentityState.ACTIVE) {
//            throw new UsernameNotFoundException("User with login " + email + " cannot be logged in!");
//        } else if (user instanceof LdapUser) {
//            throw new UsernameNotFoundException("Ldap has not verified the user " + email + "!");
//        }
//
//        return user.transformToLoggedUser();
    }
}
