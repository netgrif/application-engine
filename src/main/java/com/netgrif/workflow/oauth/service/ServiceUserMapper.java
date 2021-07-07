package com.netgrif.workflow.oauth.service;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.configuration.properties.NaeOAuthProperties;
import com.netgrif.workflow.oauth.service.interfaces.IOauthUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;

import java.util.Map;

public class ServiceUserMapper implements IOauthUserMapper {

    @Autowired
    protected IUserService userService;

    @Autowired
    protected NaeOAuthProperties oAuthProperties;

    @Override
    public LoggedUser transform(Authentication auth) {
        Map<String, Object> details = getDetails(auth);
        return userService.findById(details.get(oAuthProperties.getMapper().getId()).toString(), false).transformToLoggedUser();
    }
}
