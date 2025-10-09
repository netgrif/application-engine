package com.netgrif.application.engine.objects.auth.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class AuthPrincipalDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 6725518942728316525L;

    private String username;
    private String realmId;
    private String sessionId;
}
