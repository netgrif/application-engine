package com.netgrif.application.engine.petrinet.service;

import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetAuthorizationService;
import org.springframework.stereotype.Service;

@Service
public class PetriNetAuthorizationService implements IPetriNetAuthorizationService {
    @Override
    public boolean canCallProcessDelete(LoggedUser loggedUser, String processId) {
        return loggedUser.isAdmin();
    }
}
