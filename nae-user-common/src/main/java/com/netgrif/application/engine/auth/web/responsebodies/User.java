package com.netgrif.application.engine.auth.web.responsebodies;

import com.netgrif.application.engine.adapter.spring.petrinet.web.responsebodies.ProcessRole;
import com.netgrif.application.engine.objects.auth.domain.Attribute;
import com.netgrif.application.engine.objects.auth.domain.Authority;
import com.netgrif.application.engine.objects.auth.domain.Group;
import com.netgrif.application.engine.objects.auth.domain.IUser;
import com.netgrif.application.engine.objects.auth.domain.enums.UserState;
import lombok.Data;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class User {

    private String id;
    private String username;
    private String realmId;
    private String email;
    private String avatar;
    private String firstName;
    private String lastName;
    private String fullName;
    private Set<Authority> authorities;
    private Set<ProcessRole> processRoles;
    private Set<ProcessRole> negativeProcessRoles;
    private Set<String> nextGroups;
    private User impersonated;
    private LocalDateTime createdAt;
    private Map<String, Attribute<?>> attributes;
    private boolean enabled;
    private boolean emailVerified;
    protected UserState state;

    public User(IUser user) {
        id = user.getStringId();
        username = user.getUsername();
        realmId = user.getRealmId();
        email = user.getEmail();
        avatar = user.getAvatar();
        firstName = user.getFirstName();
        lastName = user.getLastName();
        fullName = user.getFullName();
        createdAt = user.getCreatedAt();
        attributes = user.getAttributes();
        enabled = user.isEnabled();
        emailVerified = user.isEmailVerified();
        state = user.getState();
    }

    public static User createUser(IUser user) {
        User result = new User(user);
        result.setAuthorities(user.getAuthorities());
        result.setNextGroups(user.getGroups().stream().map(Group::getStringId).collect(Collectors.toSet()));
        return result;
    }
}
