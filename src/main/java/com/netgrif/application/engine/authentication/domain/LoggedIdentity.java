package com.netgrif.application.engine.authentication.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.userdetails.User;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * todo javadoc
 * */
@Getter
public class LoggedIdentity extends User {
    protected final String fullName;
    protected final String identityId;
    protected final Map<String, String> properties;

    @Setter
    protected String activeActorId;

    @Builder(builderMethodName = "with")
    public LoggedIdentity(String fullName, String identityId, String activeActorId, String username, String password,
                          Map<String, String> properties) {
        super(username, password, new HashSet<>());
        this.fullName = fullName;
        this.identityId = identityId;
        this.activeActorId = activeActorId;
        this.properties = properties == null ? new HashMap<>() : properties;
    }
}
