package com.netgrif.workflow.auth.domain;

import org.springframework.security.core.GrantedAuthority;

import java.util.*;


public class LoggedUser extends org.springframework.security.core.userdetails.User {

    private Long id;
    private String fullName;
    private Set<Long> organizations;
    private Set<String> processRoles;

    public LoggedUser(Long id, String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.id = id;
        this.processRoles = new HashSet<>();
        this.organizations = new HashSet<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Set<Long> getOrganizations() {
        return organizations;
    }

    public void setOrganizations(Set<Long> organizations) {
        this.organizations = organizations;
    }

    public void parseOrganizations(Iterable<Organization> organizations){
        organizations.forEach(org -> this.organizations.add(org.getId()));
    }

    public Set<String> getProcessRoles() {
        return processRoles;
    }

    public void setProcessRoles(Set<String> processRoles) {
        this.processRoles = processRoles;
    }

    public void parseProcessRoles(Set<UserProcessRole> processRoles){
        processRoles.forEach(role -> this.processRoles.add(role.getRoleId()));
    }

    public User transformToUser(){
        User user = new User();
        user.setId(this.id);
        user.setEmail(getUsername());
        String[] names = this.fullName.split(" ");
        user.setName(names[0]);
        user.setSurname(names[1]);
        user.setPassword(getPassword());

        return user;
    }
}
