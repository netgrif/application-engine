package com.netgrif.workflow.oauth.service;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.domain.UserState;
import com.netgrif.workflow.oauth.domain.OAuthUser;
import com.netgrif.workflow.oauth.service.interfaces.IOAuthUserService;
import com.netgrif.workflow.oauth.service.interfaces.IOauthUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Map;

public class OAuthUserMapper implements IOauthUserMapper {

    @Autowired
    protected IOAuthUserService userService;

    @Override
    public LoggedUser transform(Authentication auth) {
        JwtAuthenticationToken oAuth2 = (JwtAuthenticationToken) auth;
        Map<String, Object> details = ((Jwt) oAuth2.getPrincipal()).getClaims();
        OAuthUser user = (OAuthUser) userService.findByOAuthId(getId(details));
        if (user == null) {
            user = new OAuthUser();
            user.setOauthId(getId(details));
            user.setState(UserState.ACTIVE);
            user = (OAuthUser) userService.saveNew(user);
        }
        user.setName(getProperty("given_name", details));
        user.setSurname(getProperty("family_name", details));
        user.setEmail(getProperty("email", details));
        return user.transformToLoggedUser();
    }

    protected String getProperty(String key, Map<String, Object> claims) {
        return claims.get(key).toString();
    }

    protected String getId(Map<String, Object> claims) {
        return getProperty("sub", claims);
    }
}
