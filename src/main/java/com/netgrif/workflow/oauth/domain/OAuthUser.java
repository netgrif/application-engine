package com.netgrif.workflow.oauth.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.netgrif.workflow.auth.domain.*;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;


@Document
public class OAuthUser extends AbstractUser {

    @Id
    @Getter
    protected ObjectId _id;

    @Getter
    @Setter
    @Indexed
    protected String oauthId;

    @Transient
    protected String name;

    @Transient
    protected String surname;

    @Transient
    protected String email;

    public OAuthUser(ObjectId id) {
        this();
        this._id = id;
    }

    public OAuthUser() {
    }

    public String getDbId() {
        return _id.toString();
    }

    @JsonIgnore
    public String getStringId() {
        return oauthId;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSurname() {
        return surname;
    }

    @Override
    public String getFullName() {
        return name + " " + surname;
    }

    @Override
    public String getAvatar() {
        return null;
    }

    @Override
    public String getTelNumber() {
        return null;
    }

    @Override
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setSurname(String surname) {
        this.surname = surname;
    }

    @Override
    public LoggedUser transformToLoggedUser() {
        OAuthLoggedUser loggedUser = new OAuthLoggedUser(this.getOauthId(), this.getDbId(), this.getEmail(), this.getAuthorities());
        loggedUser.setFullName(getFullName());
        return loggedUser;
    }
}
