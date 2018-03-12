package com.netgrif.workflow.mail;

import freemarker.template.TemplateException;

import javax.mail.MessagingException;
import java.io.IOException;

public interface IMailService {
    void sendRegistrationEmail(String recipient, String token) throws MessagingException, IOException, TemplateException;

    void testConnection();
}