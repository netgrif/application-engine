package com.netgrif.workflow.mail;

import freemarker.template.TemplateException;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public interface IMailService {
    void sendRegistrationEmail(String recipient, String token) throws MessagingException, IOException, TemplateException;

    void sendDraftEmail(String recipient, File pdf, Map<String, Object> model) throws MessagingException, IOException, TemplateException;

    void testConnection();
}
