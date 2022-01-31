package com.netgrif.application.engine.configuration.security.jwt;

import com.netgrif.application.engine.auth.domain.Authority;
import com.netgrif.application.engine.auth.domain.LoggedUser;

import java.util.Map;

public interface IJwtService {
    String tokenFrom(Map<String, Object> claims);

    void isExpired(String token);

    LoggedUser getLoggedUser(String token, Authority anonymousRole);
}
