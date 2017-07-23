package com.netgrif.workflow.mail;

import javax.mail.MessagingException;
import java.net.UnknownHostException;

public interface IMailService {
    void sendRegistrationEmail(String recipient, String token) throws MessagingException, UnknownHostException;
    void testConnection();
}
