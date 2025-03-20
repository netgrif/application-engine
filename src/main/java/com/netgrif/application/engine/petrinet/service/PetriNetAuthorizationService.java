package com.netgrif.application.engine.petrinet.service;

import com.netgrif.application.engine.authentication.domain.LoggedIdentity;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetAuthorizationService;
import org.springframework.stereotype.Service;

@Service
public class PetriNetAuthorizationService implements IPetriNetAuthorizationService {
    @Override
    public boolean canCallProcessDelete(LoggedIdentity identity, String processId) {
        // todo 2058
        return identity.isAdmin();
    }
}
