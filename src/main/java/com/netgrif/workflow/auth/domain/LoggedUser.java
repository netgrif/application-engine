package com.netgrif.workflow.auth.domain;

import com.netgrif.workflow.orgstructure.domain.Group;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRole;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


public class LoggedUser extends org.springframework.security.core.userdetails.User {

    public static final long serialVersionUID = 3031325636490953409L;

    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private String fullName;

    @Getter
    @Setter
    private Set<Long> groups;

    @Getter
    @Setter
    private Set<String> processRoles;

    @Getter
    @Setter
    private boolean anonymous;

    public LoggedUser(Long id, String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.id = id;
        this.processRoles = new HashSet<>();
        this.groups = new HashSet<>();
    }

    public void parseGroups(Iterable<Group> groups) {
        groups.forEach(org -> this.groups.add(org.getId()));
    }

    public void parseProcessRoles(Set<UserProcessRole> processRoles) {
        processRoles.forEach(role -> this.processRoles.add(role.getRoleId()));
    }

    public boolean isAdmin() {
        return getAuthorities().contains(new Authority(Authority.admin));
    }

    public String getEmail() {
        return getUsername();
    }

    public User transformToUser() {
        User user = new User(this.id);
        user.setEmail(getUsername());
        String[] names = this.fullName.split(" ");
        user.setName(names[0]);
        user.setSurname(names[1]);
        user.setPassword(getPassword());
        user.setState(UserState.ACTIVE);
        user.setAuthorities(getAuthorities().stream().map(a -> (Authority) a).collect(Collectors.toSet()));
        user.setGroups(groups.stream().map(Group::new).collect(Collectors.toSet()));
        user.setProcessRoles(processRoles.stream().map(roleId -> {
            ProcessRole role = new ProcessRole();
            role.set_id(roleId);
            return role;
        }).collect(Collectors.toSet()));

        return user;
    }


    @Override
    public String toString() {
        return "LoggedUser{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", groups=" + groups +
                ", processRoles=" + processRoles +
                '}';
    }

    public Author transformToAuthor() {
        Author author = new Author();
        author.setId(this.id);
        author.setEmail(getUsername());
        author.setFullName(this.fullName);

        return author;
    }

    public User transformToAnonymousUser() {
        User user = new User(this.id);
        user.setEmail(getUsername());
        user.setPassword(getPassword());
        user.setState(UserState.ACTIVE);
        user.setAuthorities(getAuthorities().stream().map(a -> (Authority) a).collect(Collectors.toSet()));
        user.setGroups(groups.stream().map(Group::new).collect(Collectors.toSet()));
        user.setProcessRoles(processRoles.stream().map(roleId -> {
            ProcessRole role = new ProcessRole();
            role.set_id(roleId);
            return role;
        }).collect(Collectors.toSet()));
        return user;
    }
}