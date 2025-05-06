package com.netgrif.application.engine.adapter.spring.auth.domain.mapper;

import com.netgrif.application.engine.adapter.spring.auth.domain.LoggedUserImpl;
import com.netgrif.application.engine.adapter.spring.auth.domain.User;
import com.netgrif.application.engine.objects.auth.domain.Authority;
import com.netgrif.application.engine.objects.auth.domain.IUser;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import org.bson.types.ObjectId;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class LoggedUserMapper {

    public static IUser toUser(LoggedUser loggedUser) {
        if (loggedUser == null) return null;
        User user = new User(new ObjectId(loggedUser.getId()));
        user.setEmail(loggedUser.getEmail());
        user.setUsername(loggedUser.getUsername());
        user.setFirstName(loggedUser.getFirstName());
        user.setLastName(loggedUser.getLastName());
        user.setEmailVerified(loggedUser.isEmailVerified());
        user.setEnabled(loggedUser.isEnabled());
        Set<Authority> authorities = loggedUser.getAuthoritySet().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        user.setAuthorities(authorities);
        user.setGroups(loggedUser.getGroups());
        user.setProcessRoles(loggedUser.getProcessRoles());
        user.setAttributes(loggedUser.getAttributes());
        user.setAuthMethods(loggedUser.getMfaMethod());
        user.setWorkspaceId(loggedUser.getWorkspaceId());
        return user;
    }


    public static LoggedUser toLoggedUser(IUser user) {
        if (user == null) return null;
        LoggedUserImpl loggedUser = new LoggedUserImpl(
                user.getStringId(),
                user.getUsername(),
                Optional.ofNullable(user.getCredentialValue("password")).map(Object::toString).orElse("N/A"),
                user.getAuthorities(),
                user.getProcessRoles(),
                user.getNegativeProcessRoles()
        );
        loggedUser.setEmail(user.getEmail());
        loggedUser.setFirstName(user.getFirstName());
        loggedUser.setLastName(user.getLastName());
        loggedUser.setRealmId(user.getRealmId());
        loggedUser.setGroups(user.getGroups());
        loggedUser.setMfaMethod(user.getEnabledMFAMethods());
        loggedUser.setEnabled(true);
        loggedUser.setEmailVerified(false);

        if (user.getImpersonated() != null) {
            loggedUser.setImpersonated(toLoggedUser(user.getImpersonated()));
        }
        loggedUser.setWorkspaceId(user.getWorkspaceId());
        return loggedUser;
    }

}
