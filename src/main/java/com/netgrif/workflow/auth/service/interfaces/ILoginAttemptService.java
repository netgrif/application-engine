package com.netgrif.workflow.auth.service.interfaces;

import org.springframework.stereotype.Service;

@Service
public interface ILoginAttemptService {

    void loginSucceeded(String key);

    void loginFailed(String key);

    boolean isBlocked(String key);
}
