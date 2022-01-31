package com.netgrif.application.engine.auth.service.interfaces;

import org.springframework.stereotype.Service;

public interface ILoginAttemptService {

    void loginSucceeded(String key);

    void loginFailed(String key);

    boolean isBlocked(String key);
}
