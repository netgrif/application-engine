package com.netgrif.application.engine.petrinet.service;

import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetAuthorizationService;
import org.springframework.stereotype.Service;

@Service
public class PetriNetAuthorizationService implements IPetriNetAuthorizationService {
    @Override
    public boolean canCallProcessDelete(Identity identity, String processId) {
        return identity.isAdmin();
    }
}
