package com.netgrif.application.engine.authentication.service.interfaces;

public interface ILoginAttemptService {

    void loginSucceeded(String key);

    void loginFailed(String key);

    boolean isBlocked(String key);
}
