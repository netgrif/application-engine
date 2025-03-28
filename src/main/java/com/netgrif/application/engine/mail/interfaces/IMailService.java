package com.netgrif.application.engine.mail.interfaces;

import com.netgrif.core.auth.domain.RegisteredUser;
import com.netgrif.application.engine.mail.domain.MailDraft;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;

import java.io.IOException;

public interface IMailService {

    void sendRegistrationEmail(RegisteredUser user) throws IOException, TemplateException, MessagingException;

    void sendPasswordResetEmail(RegisteredUser user) throws IOException, TemplateException, MessagingException;

    void testConnection();

    void sendMail(MailDraft mailDraft) throws IOException, TemplateException, MessagingException;
}