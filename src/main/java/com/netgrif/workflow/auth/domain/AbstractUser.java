package com.netgrif.workflow.auth.domain;

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
    protected Set<String> nextGroups;

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

    public void addGroup(String group) {
        this.nextGroups.add(group);
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
}
