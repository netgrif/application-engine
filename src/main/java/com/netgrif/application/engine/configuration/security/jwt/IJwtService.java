package com.netgrif.application.engine.configuration.security.jwt;

import com.netgrif.application.engine.auth.domain.Authority;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;

public interface IJwtService {
    String tokenFrom(Map<String, Object> header, String subject, Map<String, Object> claims);

    Boolean isTokenExpired(String token);

    LoggedUser getLoggedUser(String token, String authority);
}
