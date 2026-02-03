package com.netgrif.application.engine.petrinet.service.interfaces;

import com.netgrif.application.engine.objects.auth.domain.AbstractUser;

public interface IPetriNetAuthorizationService {

    boolean canCallProcessDelete(AbstractUser user, String processId);

}
