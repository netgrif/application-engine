package com.netgrif.application.engine.authorization.service.interfaces;

public interface ICaseAuthorizationService {
    boolean canCallCreate(String caseId);
    boolean canCallDelete(String caseId);
}
