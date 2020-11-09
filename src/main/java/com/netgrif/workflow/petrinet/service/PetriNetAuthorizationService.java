package com.netgrif.workflow.petrinet.service;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetAuthorizationService;
import org.springframework.stereotype.Service;

@Service
public class PetriNetAuthorizationService implements IPetriNetAuthorizationService {
    @Override
    public boolean canCallProcessDelete(LoggedUser loggedUser, String processId) {
        return loggedUser.isAdmin();
    }
}
