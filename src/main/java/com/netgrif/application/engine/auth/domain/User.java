package com.netgrif.application.engine.auth.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.HashSet;

@Getter
@Document
public class User extends AbstractUser implements RegisteredUser {

    public static final String UNKNOWN = "unknown";

    @Id
    protected ObjectId id;

    @NotNull
    @Setter
    @Indexed(unique = true)
    protected String email;

    @Setter
    protected String telNumber;

    @Setter
    protected String avatar;

    @JsonIgnore
    @Setter
    protected String password;

    @NotNull
    @Setter
    @Indexed
    protected String name;

    @NotNull
    @Setter
    @Indexed
    protected String surname;

    @Setter
    protected String token;

    @Setter
    protected LocalDateTime expirationDate;

    public User() {
        super();
    }

    public User(ObjectId id) {
        this();
        this.id = id;
        nextGroups = new HashSet<>();
    }

    public User(User user) {
        this.id = user.getId();
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

    public String getFullName() {
        return name + " " + surname;
    }

    @JsonIgnore
    public String getStringId() {
        return id.toString();
    }

    public LoggedUser transformToLoggedUser() {
        LoggedUser loggedUser = new LoggedUser(this.getId().toString(), this.getEmail(), this.getPassword(), this.getAuthorities());
        loggedUser.setFullName(this.getFullName());
        loggedUser.setAnonymous(false);
        if (!this.getRoles().isEmpty()) {
            loggedUser.parseRoles(this.getRoles());
        }
        loggedUser.setGroups(this.getNextGroups());
        if (this.isImpersonating()) {
            loggedUser.impersonate(this.getImpersonated().transformToLoggedUser());
        }
        return loggedUser;
    }

    @Override
    @JsonIgnore
    public IUser getSelfOrImpersonated() {
        return super.getSelfOrImpersonated();
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
                ", roles=" + roles +
                ", groups=" + nextGroups +
                '}';
    }
}
