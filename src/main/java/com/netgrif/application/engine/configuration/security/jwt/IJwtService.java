package com.netgrif.application.engine.configuration.security.jwt;

import com.netgrif.application.engine.authentication.domain.Authority;
import com.netgrif.application.engine.authentication.domain.Identity;

import java.util.Map;

public interface IJwtService {
    String tokenFrom(Map<String, Object> claims);

    void isExpired(String token);

    Identity getLoggedUser(String token, Authority anonymousRole);
}
