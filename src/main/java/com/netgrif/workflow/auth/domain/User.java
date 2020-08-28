package com.netgrif.workflow.auth.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netgrif.workflow.orgstructure.domain.Group;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRole;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Entity
@Table(name = "user")
public class User {

    public static final String UNKNOWN = "unknown";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Getter
    private Long id;

    @NotNull
    @Email
    @Column(unique = true)
    @Getter
    @Setter
    private String email;

    @Getter
    @Setter
    private String telNumber;

    @Getter
    @Setter
    private String avatar;

    @JsonIgnore
    @Getter
    @Setter
    private String password;

    @NotNull
    @NotBlank
    @Getter
    @Setter
    private String name;

    @NotNull
    @NotBlank
    @Getter
    @Setter
    private String surname;

    @NotNull
    @Getter
    @Setter
    private UserState state;

    @Getter
    @Setter
    private String token;

    @Getter
    @Setter
    private LocalDateTime expirationDate;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_authorities", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "authority_id"))
    @Getter
    @Setter
    private Set<Authority> authorities;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_process_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "user_process_role_id"))
    @Getter
    @Setter
    private Set<UserProcessRole> userProcessRoles;

    @Transient
    @Getter
    @Setter
    private Set<ProcessRole> processRoles;

    @Transient
    @Getter
    @Setter
    private Set<Group> groups;

    public User() {
        groups = new HashSet<>();
        authorities = new HashSet<>();
        userProcessRoles = new HashSet<>();
        processRoles = new HashSet<>();
    }

    public User(Long id) {
        this();
        this.id = id;
    }

    public User(String email, String password, String name, String surname) {
        this();
        this.email = email;
        this.password = password;
        this.name = name;
        this.surname = surname;
    }

    public User(ObjectNode json) {
        this(json.get("email").asText(), null, json.get("name").asText(), json.get("surname").asText());
        ((ArrayNode) json.get("userProcessRoles"))
                .forEach(node -> userProcessRoles.add(new UserProcessRole(node.get("roleId").asText())));
    }

    public void addAuthority(Authority authority) {
        authorities.add(authority);
    }

    public void addProcessRole(UserProcessRole role) {
        userProcessRoles.add(role);
    }

    public String getFullName() {
        return name + " " + surname;
    }

    public void addGroup(Group group) {
        this.groups.add(group);
    }

    public LoggedUser transformToLoggedUser() {
        LoggedUser loggedUser = new LoggedUser(this.getId(), this.getEmail(), this.getPassword(), this.getAuthorities());
        loggedUser.setFullName(this.getFullName());
        if (!this.getUserProcessRoles().isEmpty())
            loggedUser.parseProcessRoles(this.getUserProcessRoles());
        if (!this.getGroups().isEmpty())
            loggedUser.parseGroups(this.getGroups());

        return loggedUser;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", telNumber='" + telNumber + '\'' +
                ", avatar='" + avatar + '\'' +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", state=" + state +
                ", token='" + token + '\'' +
                ", expirationDate=" + expirationDate +
                ", authorities=" + authorities +
                ", userProcessRoles=" + userProcessRoles +
                ", processRoles=" + processRoles +
                ", groups=" + groups +
                '}';
    }

    public Author transformToAuthor() {
        Author author = new Author();
        author.setId(this.getId());
        author.setEmail(this.getEmail());
        author.setFullName(this.getFullName());

        return author;
    }

    public boolean isRegistered() {
        return UserState.ACTIVE.equals(state) || UserState.BLOCKED.equals(state);
    }
}