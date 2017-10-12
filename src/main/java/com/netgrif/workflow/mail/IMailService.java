package com.netgrif.workflow.mail;

import javax.mail.MessagingException;
import java.io.File;
import java.net.UnknownHostException;
import java.util.Map;

public interface IMailService {
    void sendRegistrationEmail(String recipient, String token) throws MessagingException, UnknownHostException;

    void sendDraftEmail(String recipient, File pdf, Map<String, Object> model) throws MessagingException, UnknownHostException;

    void testConnection();
}
