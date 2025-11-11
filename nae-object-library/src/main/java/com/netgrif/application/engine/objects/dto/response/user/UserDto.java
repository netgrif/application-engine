package com.netgrif.application.engine.objects.dto.response.user;

import com.netgrif.application.engine.objects.auth.domain.User;
import com.netgrif.application.engine.objects.dto.response.petrinet.ProcessRoleDto;
import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.Attribute;
import com.netgrif.application.engine.objects.auth.domain.Credential;
import com.netgrif.application.engine.objects.auth.domain.enums.UserState;
import com.netgrif.application.engine.objects.dto.response.authority.AuthorityDto;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public record UserDto(String id, String username, String realmId, String email, String avatar, String firstName,
                      String lastName, String fullName, Set<AuthorityDto> authorities,
                      Set<ProcessRoleDto> processRoles, Set<String> nextGroups, UserDto impersonated, LocalDateTime createdAt,
                      Map<String, Attribute<?>> attributes, boolean enabled, boolean emailVerified, UserState state) implements Serializable {
    public static final String ATTR_ENABLED_CREDENTIALS = "enabledCredentials";

    public static UserDto fromAbstractUser(AbstractUser user) {
        return fromAbstractUser(user, null);
    }

    public static UserDto fromAbstractUser(AbstractUser user, Set<ProcessRoleDto> processRoles) {
        return fromAbstractUser(user, null, processRoles);
    }

    public static UserDto fromAbstractUser(AbstractUser user, UserDto impersonated, Set<ProcessRoleDto> processRoles) {
        LocalDateTime createdAt = null;
        boolean enabled = false;
        boolean emailVerified = false;
        UserState state = null;
        Attribute<Set<String>> enabledCredentialsAttribute = new Attribute<>();
        Map<String, Attribute<?>> attributes = new HashMap<>();
        if (user instanceof User domainUser) {
            createdAt = domainUser.getCreatedAt();
            enabled = domainUser.isActive();
            emailVerified = domainUser.isEmailVerified();
            state = domainUser.getState();
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
            if (enabledCredentialsAttribute.getValue() != null && !enabledCredentialsAttribute.getValue().isEmpty()) {
                attributes.put(ATTR_ENABLED_CREDENTIALS, enabledCredentialsAttribute);
            }
        }

        return new UserDto(
                user.getStringId(), user.getUsername(), user.getRealmId(), user.getEmail(), user.getAvatar(),
                user.getFirstName(), user.getLastName(), user.getFullName(),
                user.getAuthoritySet().stream().map(AuthorityDto::fromAuthority).collect(Collectors.toSet()),
                processRoles, user.getGroupIds(), impersonated,
                createdAt, attributes, enabled, emailVerified, state
        );
    }
}
