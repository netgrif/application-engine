package com.netgrif.application.engine.auth.web.responsebodies;

import com.netgrif.application.engine.adapter.spring.petrinet.web.responsebodies.ProcessRole;
import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.Attribute;
import com.netgrif.application.engine.objects.auth.domain.Authority;
import com.netgrif.application.engine.objects.auth.domain.Credential;
import com.netgrif.application.engine.objects.auth.domain.enums.UserState;
import com.netgrif.application.engine.objects.dto.response.group.GroupDto;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class UserDto {
    public static final String ATTR_ENABLED_CREDENTIALS = "enabledCredentials";

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
    private Set<String> groupIds;
    private Set<GroupDto> groups;
    private UserDto impersonated;
    private LocalDateTime createdAt;
    private Map<String, Attribute<?>> attributes;
    private boolean enabled;
    private boolean emailVerified;
    protected UserState state;

    public UserDto(AbstractUser user) {
        Attribute<Set<String>> enabledCredentialsAttribute = new Attribute<>();
        if (user instanceof com.netgrif.application.engine.objects.auth.domain.User domainUser) {
            Map<String, Credential<?>> credentials = domainUser.getCredentials();
            enabledCredentialsAttribute.setValue(
                    (credentials == null ? java.util.Map.<String, Credential<?>>of() : credentials)
                            .values().stream()
                            .filter(java.util.Objects::nonNull)
                            .filter(Credential::isEnabled)
                            .map(Credential::getType)
                            .filter(java.util.Objects::nonNull)
                            .collect(Collectors.toSet()));
            enabledCredentialsAttribute.setRequired(true);
        }

        id = user.getStringId();
        username = user.getUsername();
        realmId = user.getRealmId();
        email = user.getEmail();
        avatar = user.getAvatar();
        firstName = user.getFirstName();
        lastName = user.getLastName();
        fullName = user.getFullName();
        attributes = user.getAttributes() != null
                ? new java.util.HashMap<>(user.getAttributes())
                : new java.util.HashMap<>();
        if (enabledCredentialsAttribute.getValue() != null && !enabledCredentialsAttribute.getValue().isEmpty()) {
            attributes.put(ATTR_ENABLED_CREDENTIALS, enabledCredentialsAttribute);
        }
        if (user instanceof com.netgrif.application.engine.objects.auth.domain.User u) {
            createdAt = u.getCreatedAt();
            enabled = u.isActive();
            emailVerified = u.isEmailVerified();
            state = u.getState();
        }
    }

    public static UserDto createUser(AbstractUser user) {
        UserDto result = new UserDto(user);
        result.setAuthorities(user.getAuthoritySet());
        result.setGroupIds(user.getGroupIds());
        return result;
    }

    public static UserDto createUser(AbstractUser user, List<GroupDto> groups) {
        UserDto result = createUser(user);
        result.setGroups(new HashSet<>(groups));
        return result;
    }
}
