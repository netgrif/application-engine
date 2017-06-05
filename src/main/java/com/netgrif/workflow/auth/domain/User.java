package com.netgrif.workflow.auth.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Email
    @Column(unique = true)
    private String email;

    private String telNumber;

    private String avatar;

    @JsonIgnore
    @NotNull
    @Length(min = 6)
    private String password;

    @NotNull
    @NotBlank
    private String name;

    @NotNull
    @NotBlank
    private String surname;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_organizations", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "organization_id"))
    private Set<Organization> organizations;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_authorities", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "authority_id"))
    private Set<Authority> authorities;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_process_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "user_process_role_id"))
    private Set<UserProcessRole> userProcessRoles;

    public User() {
        organizations = new HashSet<>();
        authorities = new HashSet<>();
        userProcessRoles = new HashSet<>();
    }

    public User(String email, String password, String name, String surname) {
        this();
        this.email = email;
        this.password = password;
        this.name = name;
        this.surname = surname;
    }

    public User(ObjectNode json){
        this(json.get("email").asText(),null,json.get("name").asText(),json.get("surname").asText());
        ((ArrayNode)json.get("userProcessRoles"))
                .forEach(node -> userProcessRoles.add(new UserProcessRole( node.get("roleId").asText())));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<Authority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Set<Authority> authorities) {
        this.authorities = authorities;
    }

    public void addAuthority(Authority authority) {
        authorities.add(authority);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public Set<UserProcessRole> getUserProcessRoles() {
        return userProcessRoles;
    }

    public void setUserProcessRoles(Set<UserProcessRole> userProcessRoles) {
        this.userProcessRoles = userProcessRoles;
    }

    public void addProcessRole(UserProcessRole role) {
        userProcessRoles.add(role);
    }

    public String getFullName() {
        return name + " " + surname;
    }

    public String getTelNumber() {
        return telNumber;
    }

    public void setTelNumber(String telNumber) {
        this.telNumber = telNumber;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Set<Organization> getOrganizations() {
        return organizations;
    }

    public void setOrganizations(Set<Organization> organizations) {
        this.organizations = organizations;
    }

    public void addOrganization(Organization org){
        this.organizations.add(org);
    }
}