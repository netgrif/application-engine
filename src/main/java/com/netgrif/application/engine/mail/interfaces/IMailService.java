package com.netgrif.application.engine.mail.interfaces;

import com.netgrif.application.engine.auth.domain.RegisteredUser;
import com.netgrif.application.engine.mail.domain.MailDraft;
import freemarker.template.TemplateException;

import javax.mail.MessagingException;
import java.io.IOException;

public interface IMailService {

    void sendRegistrationEmail(RegisteredUser user) throws MessagingException, IOException, TemplateException;

    void sendPasswordResetEmail(RegisteredUser user) throws MessagingException, IOException, TemplateException;

    void testConnection();

    void sendMail(MailDraft mailDraft) throws MessagingException, IOException, TemplateException;
}