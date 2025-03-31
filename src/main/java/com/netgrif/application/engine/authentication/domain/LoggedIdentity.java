package com.netgrif.application.engine.authentication.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.userdetails.User;

import java.util.HashSet;

/**
 * todo javadoc
 * */
@Getter
public class LoggedIdentity extends org.springframework.security.core.userdetails.User {
    protected final String fullName;
    protected final String identityId;

    @Setter
    protected String activeActorId;

    @Builder(builderMethodName = "with")
    public LoggedIdentity(String fullName, String identityId, String activeActorId, String username, String password) {
        super(username, password, new HashSet<>());
        this.fullName = fullName;
        this.identityId = identityId;
        this.activeActorId = activeActorId;
    }
}
