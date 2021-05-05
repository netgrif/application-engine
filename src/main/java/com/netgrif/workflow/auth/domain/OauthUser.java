package com.netgrif.workflow.auth.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Table(name = "oauth_user")
public class OauthUser extends User {

    @NotNull
    @Column(unique = true)
    private String oauthId;

    public OauthUser() {
    }

    @Override
    public LoggedUser transformToLoggedUser() {
        OauthLoggedUser loggedUser = new OauthLoggedUser(this.getId(), this.getOauthId(), this.getEmail(), this.getPassword(), this.getAuthorities());
        loggedUser.setFullName(this.getFullName());
        loggedUser.setAnonymous(false);
        if (!this.getUserProcessRoles().isEmpty())
            loggedUser.parseProcessRoles(this.getUserProcessRoles());
        if (!this.getGroups().isEmpty())
            loggedUser.parseGroups(this.getGroups());

        return loggedUser;
    }
}
