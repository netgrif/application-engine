package com.netgrif.workflow.oauth.service.interfaces;

import com.netgrif.workflow.auth.domain.LoggedUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Map;

public interface IOauthUserMapper {

    LoggedUser transform(Authentication principal);

    default Map<String, Object> getDetails(Authentication auth) {
        JwtAuthenticationToken oAuth2 = (JwtAuthenticationToken) auth;
        return  ((Jwt) oAuth2.getPrincipal()).getClaims();
    }

}
