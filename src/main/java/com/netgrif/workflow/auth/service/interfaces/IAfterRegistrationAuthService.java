package com.netgrif.workflow.auth.service.interfaces;

public interface IAfterRegistrationAuthService {
    void authenticateWithUsernameAndPassword(String username, String password);
    void logoutAfterRegistrationFinished();
}
