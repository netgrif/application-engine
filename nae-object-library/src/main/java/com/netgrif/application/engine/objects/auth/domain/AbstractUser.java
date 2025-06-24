package com.netgrif.application.engine.objects.auth.domain;

import com.netgrif.application.engine.objects.auth.domain.enums.UserState;
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.bson.codecs.pojo.annotations.BsonIgnore;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
public abstract class AbstractUser implements IUser, Serializable {

    @Serial
    private static final long serialVersionUID = 341922197277508726L;

    @NotNull
    protected UserState state;

    protected Set<Authority> authorities;

    protected Set<ProcessRole> processRoles;

    protected Set<ProcessRole> negativeProcessRoles;

    protected Set<String> groupIds;

    @BsonIgnore
    protected Set<Group> groups;

    @BsonIgnore
    protected IUser impersonated;

    public AbstractUser() {
        authorities = new HashSet<>();
        groupIds = new HashSet<>();
        groups = new HashSet<>();
        processRoles = new HashSet<>();
        negativeProcessRoles = new HashSet<>();
    }

    public void addAuthority(Authority authority) {
        if (authorities.stream().anyMatch(it -> it.get_id().equals(authority.get_id())))
            return;
        authorities.add(authority);
    }

    @Override
    public void removeAuthority(Authority authority) {
        authorities.remove(authority);
    }

    public void addProcessRole(ProcessRole role) {
        if (processRoles.stream().anyMatch(it -> it.getStringId().equals(role.getStringId())))
            return;
        processRoles.add(role);
    }

    public void removeProcessRole(ProcessRole role) {
        processRoles.remove(role);
    }

    public void addNegativeProcessRole(ProcessRole role) {
        if (negativeProcessRoles.stream().anyMatch(it -> it.getStringId().equals(role.getStringId())))
            return;
        negativeProcessRoles.add(role);
    }

    public void removeNegativeProcessRole(ProcessRole role) {
        negativeProcessRoles.remove(role);
    }

    public void addGroup(Group group) {
        this.groupIds.add(group.getStringId());
        this.groups.add(group);
    }

    @Override
    public void removeGroupId(String groupId) {
        this.groupIds.remove(groupId);
    }

    public void removeGroup(Group group) {
        this.groupIds.remove(group.getStringId());
        this.groups.remove(group);
    }

    public boolean isActive() {
        return UserState.ACTIVE.equals(state) || UserState.BLOCKED.equals(state);
    }

    @Override
    public boolean isImpersonating() {
        return this.impersonated != null;
    }

    @Override
    public IUser getSelfOrImpersonated() {
        return isImpersonating() ? this.impersonated : this;
    }
}
