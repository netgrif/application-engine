package com.netgrif.application.engine.petrinet.service;

import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetAuthorizationService;
import org.springframework.stereotype.Service;

@Service
public class PetriNetAuthorizationService implements IPetriNetAuthorizationService {
    @Override
    public boolean canCallProcessDelete(AbstractUser user, String processId) {
        return user.isAdmin();
    }
}
