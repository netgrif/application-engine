package com.netgrif.workflow.auth.domain;

import com.netgrif.workflow.orgstructure.domain.Group;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import java.util.*;


public class LoggedUser extends org.springframework.security.core.userdetails.User {

    public static final long serialVersionUID = 3031325636490953409L;

    @Getter @Setter
    private Long id;

    @Getter @Setter
    private String fullName;

    @Getter @Setter
    private Set<Long> groups;

    @Getter @Setter
    private Set<String> processRoles;

    public LoggedUser(Long id, String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.id = id;
        this.processRoles = new HashSet<>();
        this.groups = new HashSet<>();
    }

    public void parseGroups(Iterable<Group> groups){
        groups.forEach(org -> this.groups.add(org.getId()));
    }

    public void parseProcessRoles(Set<UserProcessRole> processRoles){
        processRoles.forEach(role -> this.processRoles.add(role.getRoleId()));
    }

    public boolean isAdmin(){
        return getAuthorities().contains(new Authority(Authority.admin));
    }

    public User transformToUser(){
        User user = new User(this.id);
        user.setEmail(getUsername());
        String[] names = this.fullName.split(" ");
        user.setName(names[0]);
        user.setSurname(names[1]);
        user.setPassword(getPassword());

        return user;
    }

    public Author transformToAuthor(){
        Author author = new Author();
        author.setId(this.id);
        author.setEmail(getUsername());
        author.setFullName(this.fullName);

        return author;
    }
}