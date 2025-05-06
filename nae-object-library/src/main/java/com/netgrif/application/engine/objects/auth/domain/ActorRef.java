package com.netgrif.application.engine.objects.auth.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActorRef implements Serializable {

    private String id;

    private String realmId;

    private String identifier;

    private String displayName;

    public String getUsername() {
        return identifier;
    }

    public String getFullName() {
        return displayName;
    }

}
