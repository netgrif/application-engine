package com.netgrif.application.engine.auth.domain;

import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole;
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

    @Getter
    @Setter
    protected Set<String> nextGroups;

    @Setter
    @Getter
    @Transient
    protected IUser impersonated;

    public AbstractUser() {
        authorities = new HashSet<>();
        nextGroups = new HashSet<>();
        processRoles = new HashSet<>();
    }

    public void addAuthority(Authority authority) {
        if (authorities.stream().anyMatch(it -> it.get_id().equals(authority.get_id())))
            return;
        authorities.add(authority);
    }

    public void addProcessRole(ProcessRole role) {
        if (processRoles.stream().anyMatch(it -> it.getStringId().equals(role.getStringId())))
            return;
        processRoles.add(role);
    }

    public void removeProcessRole(ProcessRole role) {
        processRoles.remove(role);
    }

    public void addGroup(String groupId) {
        this.nextGroups.add(groupId);
    }

    public void removeGroup(String groupId) {
        this.nextGroups.remove(groupId);
    }

    public boolean isActive() {
        return UserState.ACTIVE.equals(state) || UserState.BLOCKED.equals(state);
    }

    public Author transformToAuthor() {
        Author author = new Author();
        author.setId(this.getStringId());
        author.setEmail(this.getEmail());
        author.setFullName(this.getFullName());

        return author;
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
