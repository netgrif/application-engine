package com.netgrif.application.engine.objects.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public record AuthPrincipalDto(String username,
                               String realmId,
                               @JsonIgnore
                               String sessionId) implements Serializable {
    @Serial
    private static final long serialVersionUID = 6725518942728316525L;

    @Override
    public String toString() {
        return "AuthPrincipalDto{" +
                "username='" + username + '\'' +
                ", realmId='" + realmId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthPrincipalDto that = (AuthPrincipalDto) o;
        return Objects.equals(username, that.username) && Objects.equals(realmId, that.realmId);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(username, realmId);
    }
}
