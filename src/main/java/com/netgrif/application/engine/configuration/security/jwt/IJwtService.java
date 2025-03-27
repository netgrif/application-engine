package com.netgrif.application.engine.configuration.security.jwt;

import com.netgrif.application.engine.authentication.domain.LoggedIdentity;

import java.util.Map;

public interface IJwtService {
    String tokenFrom(Map<String, Object> claims);

    void isExpired(String token);

    LoggedIdentity getLoggedIdentity(String token);
}
