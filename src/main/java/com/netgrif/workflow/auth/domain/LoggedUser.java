package com.netgrif.workflow.auth.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import java.util.*;


public class LoggedUser extends org.springframework.security.core.userdetails.User {

    @Getter @Setter
    private Long id;

    @Getter @Setter
    private String fullName;

    @Getter @Setter
    private Set<Long> organizations;

    @Getter @Setter
    private Set<String> processRoles;

    public LoggedUser(Long id, String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.id = id;
        this.processRoles = new HashSet<>();
        this.organizations = new HashSet<>();
    }

    public void parseOrganizations(Iterable<Organization> organizations){
        organizations.forEach(org -> this.organizations.add(org.getId()));
    }

    public void parseProcessRoles(Set<UserProcessRole> processRoles){
        processRoles.forEach(role -> this.processRoles.add(role.getRoleId()));
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
}