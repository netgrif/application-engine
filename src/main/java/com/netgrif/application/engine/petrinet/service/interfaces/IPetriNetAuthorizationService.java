package com.netgrif.application.engine.petrinet.service.interfaces;

import com.netgrif.application.engine.authentication.domain.Identity;

public interface IPetriNetAuthorizationService {

    boolean canCallProcessDelete(Identity identity, String processId);

}
