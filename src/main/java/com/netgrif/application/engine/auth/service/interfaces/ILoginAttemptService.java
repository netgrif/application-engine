package com.netgrif.application.engine.auth.service.interfaces;

public interface ILoginAttemptService {

    void loginSucceeded(String key);

    void loginFailed(String key);

    boolean isBlocked(String key);
}
