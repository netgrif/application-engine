package com.netgrif.workflow.auth.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netgrif.workflow.orgstructure.domain.Group;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRole;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Document
public class User {

    public static final String UNKNOWN = "unknown";

    @Id
    @Getter
    private ObjectId _id;

    @NotNull
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
    @Getter
    @Setter
    private String name;

    @NotNull
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

    @Getter
    @Setter
    private Set<Authority> authorities;

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

    @Transient
    @Getter
    @Setter
    private Set<String> nextGroups;

    public User() {
        groups = new HashSet<>();
        authorities = new HashSet<>();
        nextGroups = new HashSet<>();
        userProcessRoles = new HashSet<>();
        processRoles = new HashSet<>();
    }

    public User(ObjectId id) {
        this();
        this._id = id;
        nextGroups = new HashSet<>();
    }

    public User(User user){
        this._id = user.get_id();
        this.email = user.getEmail();
        this.surname = user.getSurname();
        this.name = user.getName();
        this.state = user.getState();
    }

    public User(String email, String password, String name, String surname) {
        this();
        this.email = email;
        this.password = password;
        this.name = name;
        this.surname = surname;
        this.nextGroups = new HashSet<>();
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

    public void removeProcessRole(UserProcessRole role) {
        userProcessRoles.remove(role);
    }

    public String getFullName() {
        return name + " " + surname;
    }

    @JsonIgnore
    public String getStringId() {
        return _id.toString();
    }

    public void addGroup(Group group) {
        this.groups.add(group);
    }

    public LoggedUser transformToLoggedUser() {
        LoggedUser loggedUser = new LoggedUser(this.get_id().toString(), this.getEmail(), this.getPassword(), this.getAuthorities());
        loggedUser.setFullName(this.getFullName());
        loggedUser.setAnonymous(false);
        if (!this.getUserProcessRoles().isEmpty())
            loggedUser.parseProcessRoles(this.getUserProcessRoles());
        if (!this.getGroups().isEmpty())
            loggedUser.parseGroups(this.getGroups());

        return loggedUser;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + _id +
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
        author.setId(this.getStringId());
        author.setEmail(this.getEmail());
        author.setFullName(this.getFullName());

        return author;
    }

    public boolean isRegistered() {
        return UserState.ACTIVE.equals(state) || UserState.BLOCKED.equals(state);
    }
}