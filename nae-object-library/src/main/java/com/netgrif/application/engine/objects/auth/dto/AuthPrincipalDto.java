package com.netgrif.application.engine.objects.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

public record AuthPrincipalDto(String username,
                               String realmId,
                               @ToString.Exclude
                               @EqualsAndHashCode.Exclude
                               @JsonIgnore
                               String sessionId) implements Serializable {
    @Serial
    private static final long serialVersionUID = 6725518942728316525L;
}
