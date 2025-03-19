package com.netgrif.application.engine.authentication.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Transient;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Getter
public abstract class AbstractUser implements IUser, Serializable {

    private static final long serialVersionUID = 341922197277508726L;

    @NotNull
    @Setter
    protected IdentityState state;

    @Setter
    protected Set<Authority> authorities;

    @Setter
    protected Set<String> nextGroups;

    @Setter
    @Transient
    protected IUser impersonated;

    public AbstractUser() {
        authorities = new HashSet<>();
        nextGroups = new HashSet<>();
    }

    public void addAuthority(Authority authority) {
        // TODO: release/8.0.0 is this needed?
        if (authorities.stream().anyMatch(it -> it.getId().equals(authority.getId())))
            return;
        authorities.add(authority);
    }

    public void addGroup(String groupId) {
        this.nextGroups.add(groupId);
    }

    public void removeGroup(String groupId) {
        this.nextGroups.remove(groupId);
    }

    public boolean isActive() {
        return IdentityState.ACTIVE.equals(state) || IdentityState.BLOCKED.equals(state);
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
