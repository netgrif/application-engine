package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BaseAuthorizationService extends AbstractBaseAuthorizationService {

    public BaseAuthorizationService(@Autowired IUserService userService) {
        super(userService);
    }
}
