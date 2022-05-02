package com.netgrif.application.engine.petrinet.service;

import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.service.AbstractBaseAuthorizationService;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetAuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PetriNetAuthorizationService extends AbstractBaseAuthorizationService implements IPetriNetAuthorizationService {

    public PetriNetAuthorizationService(@Autowired IUserService userService) {
        super(userService);
    }

    @Override
    public boolean canCallImport(LoggedUser loggedUser) {
        return loggedUser.isAdmin();
    }

    @Override
    public boolean canCallProcessDelete(LoggedUser loggedUser, String processId) {
        return loggedUser.isAdmin();
    }
}
