package com.netgrif.application.engine.authorization.service.interfaces;

public interface ICaseAuthorizationService {
    boolean canCallCreate(String processId);
    boolean canCallDelete(String caseId);
    boolean canView(String caseId);
}
