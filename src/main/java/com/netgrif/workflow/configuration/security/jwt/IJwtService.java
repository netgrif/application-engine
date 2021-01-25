package com.netgrif.workflow.configuration.security.jwt;

import com.netgrif.workflow.auth.domain.Authority;
import com.netgrif.workflow.auth.domain.LoggedUser;

import java.util.Map;

public interface IJwtService {
    String tokenFrom(Map<String, Object> claims);

    boolean isExpired(String token);

    LoggedUser getLoggedUser(String token, Authority anonymousRole);
}
