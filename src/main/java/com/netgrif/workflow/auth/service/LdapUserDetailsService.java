package com.netgrif.workflow.auth.service;

import com.netgrif.workflow.auth.domain.IUser;
import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.domain.UserState;
import com.netgrif.workflow.ldap.domain.LdapUser;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class LdapUserDetailsService extends UserDetailsServiceImpl {

    @Override
    protected LoggedUser getLoggedUser(String email) throws UsernameNotFoundException {
        IUser user = userRepository.findByEmail(email);
        if(user == null) {
            throw new UsernameNotFoundException("No user was found for login: " + email);
        } else if (user.getState() != UserState.ACTIVE) {
            throw new UsernameNotFoundException("User with login "+email+" cannot be logged in!");
        } else if (user instanceof LdapUser) {
            throw new UsernameNotFoundException("User " + email + " is an LDAP user");
        }

        return user.transformToLoggedUser();
    }
}
