package com.netgrif.application.engine.orgstructure.services;

import com.netgrif.application.engine.auth.service.AbstractBaseAuthorizationService;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.orgstructure.services.interfaces.IGroupAuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GroupAuthorizationService extends AbstractBaseAuthorizationService implements IGroupAuthorizationService {

    public GroupAuthorizationService(@Autowired IUserService userService) {
        super(userService);
    }
}
