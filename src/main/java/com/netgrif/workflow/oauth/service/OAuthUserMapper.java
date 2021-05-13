package com.netgrif.workflow.oauth.service;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.domain.UserState;
import com.netgrif.workflow.oauth.domain.OAuthUser;
import com.netgrif.workflow.oauth.service.interfaces.IOAuthUserService;
import com.netgrif.workflow.oauth.service.interfaces.IOauthUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@ConditionalOnExpression("${nae.oauth.enabled}")
public class OAuthUserMapper implements IOauthUserMapper {

    @Autowired
    protected IOAuthUserService userService;

    @Override
    public LoggedUser transform(OAuth2Authentication auth) {
        Map<String, String> details = (Map<String, String>) auth.getUserAuthentication().getDetails();
        OAuthUser user = userService.findByOAuthId(getId(details));
        if (user == null) {
            user = new OAuthUser();
            user.setOauthId(getId(details));
            user.setState(UserState.ACTIVE);
            user = userService.saveNewOAuth(user);
        }
        user.setName(details.get("given_name"));
        user.setSurname(details.get("family_name"));
        return user.transformToLoggedUser();
    }

    protected String getId(Map<String, String> details) {
        return details.get("sub");
    }
}
