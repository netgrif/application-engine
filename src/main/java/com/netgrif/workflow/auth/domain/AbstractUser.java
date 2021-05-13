package com.netgrif.workflow.auth.domain;

import com.netgrif.workflow.orgstructure.domain.Group;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRole;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Transient;

import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractUser implements IUser {

    @NotNull
    @Getter
    @Setter
    protected UserState state;

    @Getter
    @Setter
    protected Set<Authority> authorities;

    @Getter
    @Setter
    protected Set<ProcessRole> processRoles;

    @Transient
    @Getter
    @Setter
    protected Set<Group> groups;

    @Transient
    @Getter
    @Setter
    protected Set<String> nextGroups;

    public AbstractUser() {
        groups = new HashSet<>();
        authorities = new HashSet<>();
        nextGroups = new HashSet<>();
        processRoles = new HashSet<>();
    }

    public void addAuthority(Authority authority) {
        authorities.add(authority);
    }

    public void addProcessRole(ProcessRole role) {
        processRoles.add(role);
    }

    public void removeProcessRole(ProcessRole role) {
        processRoles.remove(role);
    }

    public void addGroup(Group group) {
        this.groups.add(group);
    }

    public boolean isActive() {
        return UserState.ACTIVE.equals(state) || UserState.BLOCKED.equals(state);
    }
}
