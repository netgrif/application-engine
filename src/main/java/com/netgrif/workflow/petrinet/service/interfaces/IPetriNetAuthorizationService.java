package com.netgrif.workflow.petrinet.service.interfaces;

import com.netgrif.workflow.auth.domain.LoggedUser;

public interface IPetriNetAuthorizationService {

    boolean canCallProcessDelete(LoggedUser loggedUser, String processId);

}
