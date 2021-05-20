package com.netgrif.workflow.oauth.service;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.domain.UserState;
import com.netgrif.workflow.configuration.properties.NaeOAuthProperties;
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
    protected NaeOAuthProperties oAuthProperties;

    @Autowired
    protected IOAuthUserService userService;

    @Override
    public LoggedUser transform(Authentication auth) {
        JwtAuthenticationToken oAuth2 = (JwtAuthenticationToken) auth;
        Map<String, Object> details = ((Jwt) oAuth2.getPrincipal()).getClaims();
        OAuthUser user = (OAuthUser) userService.findByOAuthId(getProperty(oAuthProperties.getMapper().getId(), details));
        if (user == null) {
            user = new OAuthUser();
            user.setOauthId(getProperty(oAuthProperties.getMapper().getId(), details));
            user.setState(UserState.ACTIVE);
            user = (OAuthUser) userService.saveNew(user);
        }
        user.setName(getProperty(oAuthProperties.getMapper().getName(), details));
        user.setSurname(getProperty(oAuthProperties.getMapper().getSurname(), details));
        user.setEmail(getProperty(oAuthProperties.getMapper().getEmail(), details));
        return user.transformToLoggedUser();
    }

    protected String getProperty(String key, Map<String, Object> claims) {
        return claims.get(key).toString();
    }

}
