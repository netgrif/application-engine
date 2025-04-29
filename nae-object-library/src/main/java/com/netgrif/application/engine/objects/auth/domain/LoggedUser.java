package com.netgrif.application.engine.objects.auth.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import java.util.*;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class LoggedUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 3031325636490953409L;

    private String id;

    private String realmId;

    private String workspaceId;

    private String createMethod;

    private String username;

    private String email;

    private String password;

    private String firstName;

    private String lastName;

    private Set<Authority> authoritySet;

    private Set<Group> groups;

    private boolean enabled;

    private boolean emailVerified;

    private boolean accountNonExpired;

    private boolean accountNonLocked;

    private boolean credentialsNonExpired;

    private LoggedUser impersonated;

    private Set<ProcessRole> processRoles;

    private Set<String> mfaMethod;

    private transient Duration sessionTimeout = Duration.ofMinutes(30);

    private Map<String, Attribute<?>> attributes;

    public LoggedUser(String id, String username, String password, Collection<Authority> authorities, Collection<ProcessRole> processRoles) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.authoritySet = new HashSet<>(authorities);
        this.processRoles = new HashSet<>(processRoles);
        this.mfaMethod = new HashSet<>();
        this.groups = new HashSet<>();
        this.enabled = true;
        this.emailVerified = false;
        this.accountNonExpired = true;
        this.accountNonLocked = true;
        this.credentialsNonExpired = true;
    }

    public LoggedUser(String id, String realmId, String username, String password, Collection<? extends Authority> authorities, Set<Group> groups, Set<ProcessRole> processRoles, Map<String, Attribute<?>> attributes, Set<String> mfaMethod, String firstName, String lastName) {
        this.id = id;
        this.realmId = realmId;
        this.username = username;
        this.password = password;
        this.authoritySet = new HashSet<>(authorities);
        this.groups = new HashSet<>(groups);
        this.processRoles = new HashSet<>(processRoles);
        this.attributes = new HashMap<>(attributes);
        this.mfaMethod = new HashSet<>(mfaMethod);
        this.firstName = firstName;
        this.lastName = lastName;
        this.enabled = true;
        this.emailVerified = false;
        this.accountNonExpired = true;
        this.accountNonLocked = true;
        this.credentialsNonExpired = true;
    }

    public boolean isAdmin() {
        return authoritySet.stream().anyMatch(a -> a.getAuthority().equals(Authority.admin));
    }

    public void impersonate(LoggedUser toImpersonate) {
        this.impersonated = toImpersonate;
    }

    public void clearImpersonated() {
        this.impersonated = null;
    }

    public boolean isImpersonating() {
        return this.impersonated != null;
    }

    @JsonIgnore
    public LoggedUser getSelfOrImpersonated() {
        return this.isImpersonating() ? this.impersonated : this;
    }

    public String getFullName() {
        return String.join(" ", firstName, lastName);
    }

    @Override
    public String toString() {
        return "LoggedUser{" +
                "id=" + id +
                ", realmId='" + realmId + '\'' +
                ", username='" + username + '\'' +
                ", fullName='" + getFullName() + '\'' +
                ", groups=" + groups +
                ", impersonated=" + impersonated +
                '}';
    }

    public abstract IUser transformToUser();

    public abstract Author transformToAuthor();
}
