package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.domain.UserState;
import com.netgrif.application.engine.ldap.domain.LdapUser;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ConditionalOnExpression("${nae.ldap.enabled:false}")
public class LdapUserDetailsService extends UserDetailsServiceImpl {

    @Override
    protected LoggedUser getLoggedUser(String email) throws UsernameNotFoundException {
        IUser user = userRepository.findByEmail(email);
        if(user == null) {
            throw new UsernameNotFoundException("No user was found for login: " + email);
        } else if (user.getState() != UserState.ACTIVE) {
            throw new UsernameNotFoundException("User with login "+email+" cannot be logged in!");
        } else if (user instanceof LdapUser) {
            throw new UsernameNotFoundException("Ldap has not verified the user " + email + "!");
        }

        return user.transformToLoggedUser();
    }
}
