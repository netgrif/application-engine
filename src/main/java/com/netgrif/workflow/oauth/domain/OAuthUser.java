package com.netgrif.workflow.oauth.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.netgrif.workflow.auth.domain.AbstractUser;
import com.netgrif.workflow.auth.domain.LoggedUser;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;


@Document(collection = "user")
public class OAuthUser extends AbstractUser {

    @Id
    @Getter
    protected ObjectId _id;

    @Getter
    @Setter
    @Indexed
    protected String oauthId;

    @Getter
    @Setter
    @Transient
    protected String name;

    @Getter
    @Setter
    @Transient
    protected String surname;

    @Getter
    @Setter
    @Transient
    protected String email;

    @Getter
    @Setter
    @Transient
    protected List<RemoteGroupResource> remoteGroups;

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
    public LoggedUser transformToLoggedUser() {
        OAuthLoggedUser loggedUser = new OAuthLoggedUser(this.getOauthId(), this.getDbId(), this.getEmail(), this.getAuthorities());
        loggedUser.setFullName(getFullName());
        loggedUser.setAnonymous(false);
        if (!this.getProcessRoles().isEmpty())
            loggedUser.parseProcessRoles(this.getProcessRoles());
        if (!this.getGroups().isEmpty())
            loggedUser.parseGroups(this.getGroups());
        return loggedUser;
    }
}
