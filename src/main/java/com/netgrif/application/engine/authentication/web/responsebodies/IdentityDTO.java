package com.netgrif.application.engine.authentication.web.responsebodies;

import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.authentication.domain.LoggedIdentity;
import lombok.Data;

import java.util.Set;

@Data
public class IdentityDTO {

    private String id;

    private String username;

    private String firstname;

    private String lastname;

    private String fullName;

    private String activeActorId;

    // todo 2058 groups
    private Set<String> nextGroups;

    public IdentityDTO(Identity identity, String activeActorId) {
        this.id = identity.getStringId();
        this.username = identity.getUsername();
        this.firstname = identity.getFirstname();
        this.lastname = identity.getLastname();
        this.fullName = identity.getFullName();
        this.activeActorId = activeActorId;
    }

    public IdentityDTO(LoggedIdentity loggedIdentity) {
        this.id = loggedIdentity.getIdentityId();
        this.username = loggedIdentity.getUsername();
        this.fullName = loggedIdentity.getFullName();
        this.activeActorId = loggedIdentity.getActiveActorId();
    }
}