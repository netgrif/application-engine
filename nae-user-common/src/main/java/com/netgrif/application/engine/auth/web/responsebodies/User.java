package com.netgrif.application.engine.auth.web.responsebodies;

import com.netgrif.application.engine.adapter.spring.petrinet.web.responsebodies.ProcessRole;
import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.Attribute;
import com.netgrif.application.engine.objects.auth.domain.Authority;
import com.netgrif.application.engine.objects.auth.domain.enums.UserState;
import com.netgrif.application.engine.objects.auth.domain.enums.UserType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

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
    private UserType type;

    public User(AbstractUser user) {
        id = user.getStringId();
        username = user.getUsername();
        realmId = user.getRealmId();
        email = user.getEmail();
        avatar = user.getAvatar();
        firstName = user.getFirstName();
        lastName = user.getLastName();
        fullName = user.getName();
        attributes = user.getAttributes();
        if (user instanceof com.netgrif.application.engine.objects.auth.domain.User u) {
            createdAt = u.getCreatedAt();
            enabled = u.isActive();
            emailVerified = u.isEmailVerified();
            state = u.getState();
        }
    }

    public static User createUser(AbstractUser user) {
        User result = new User(user);
        result.setAuthorities(user.getAuthoritySet());
        result.setNextGroups(user.getGroupIds());
        return result;
    }
}
