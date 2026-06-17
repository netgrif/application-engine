package com.netgrif.application.engine.objects.auth.domain;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

@Setter
@Getter
public class AuthConfig {

    private ObjectId id;

    @NotNull
    private String realmId;

    private boolean basicAuthEnabled;

    private boolean ldapAuthEnabled;


    public AuthConfig() {
    }

    public AuthConfig(String realmId) {
        this.realmId = realmId;
    }

    @Override
    public String toString() {
        return "AuthConfig{" +
                "id=" + id +
                ", realmId='" + realmId + '\'' +
                ", basicAuthEnabled=" + basicAuthEnabled +
                ", ldapAuthEnabled=" + ldapAuthEnabled +
                '}';
    }
}
