package com.netgrif.workflow.mail.interfaces;

public interface IMailAttemptService {

    void mailAttempt(String key);

    boolean isBlocked(String key);
}
