package com.netgrif.workflow.auth.service;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.domain.OauthUser;
import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.auth.domain.UserState;
import com.netgrif.workflow.auth.service.interfaces.IOauthUserMapper;
import com.netgrif.workflow.auth.service.interfaces.IOauthUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@ConditionalOnExpression("${nae.oauth.enabled}")
public class OauthUserMapper implements IOauthUserMapper {

    @Autowired
    protected IOauthUserService userService;

    @Override
    public LoggedUser transform(OAuth2Authentication auth) {
        Map<String, String> details = (Map<String, String>) auth.getUserAuthentication().getDetails();
        User user = userService.findByEmail(getEmail(details), false);
        if (user == null) {
            OauthUser userNew = new OauthUser();
            userNew.setOauthId(details.get("sub"));
            userNew.setEmail(details.get("preferred_username"));
            userNew.setName(details.get("given_name"));
            userNew.setSurname(details.get("family_name"));
            userNew.setPassword("");
            userNew.setState(UserState.ACTIVE);
            user = userService.saveNewOAuth(userNew);
        }
        return user.transformToLoggedUser();
    }

    protected String getEmail(Map<String, String> details) {
        return details.get("preferred_username");
    }
}
