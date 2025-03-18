package com.netgrif.application.engine.petrinet.service.interfaces;

import com.netgrif.core.auth.domain.LoggedUser;

public interface IPetriNetAuthorizationService {

    boolean canCallProcessDelete(LoggedUser loggedUser, String processId);

}
