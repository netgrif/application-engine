package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.auth.service.interfaces.IAuthorityAuthorizationService;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.workflow.service.AbstractAuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthorityAuthorizationService extends AbstractAuthorizationService implements IAuthorityAuthorizationService {

    public AuthorityAuthorizationService(@Autowired IUserService userService) {
        super(userService);
    }

}
