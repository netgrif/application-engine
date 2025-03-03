package com.netgrif.application.engine.petrinet.service.interfaces;

import com.netgrif.application.engine.authentication.domain.LoggedUser;

public interface IPetriNetAuthorizationService {

    boolean canCallProcessDelete(LoggedUser loggedUser, String processId);

}
