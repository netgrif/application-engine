package com.netgrif.workflow.auth.service;

import com.netgrif.workflow.auth.domain.OauthUser;
import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.auth.service.interfaces.IOauthUserService;
import com.netgrif.workflow.event.events.user.UserRegistrationEvent;

public class OauthUserService extends UserService implements IOauthUserService {

    @Override
    public OauthUser saveNewOAuth(OauthUser user) {
        addDefaultRole(user);
        addDefaultAuthorities(user);
        return userRepository.save(user);
    }

}
