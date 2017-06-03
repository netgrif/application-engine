package com.netgrif.workflow.mail;

import javax.mail.MessagingException;

public interface IMailService {
    void sendRegistrationEmail(String recipient, String token) throws MessagingException;
}
