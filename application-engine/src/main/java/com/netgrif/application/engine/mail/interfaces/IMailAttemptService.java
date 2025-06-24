package com.netgrif.application.engine.mail.interfaces;

public interface IMailAttemptService {

    void mailAttempt(String key);

    boolean isBlocked(String key);
}
