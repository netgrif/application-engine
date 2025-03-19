package com.netgrif.application.engine.mail.interfaces;

import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.mail.domain.MailDraft;
import freemarker.template.TemplateException;

import javax.mail.MessagingException;
import java.io.IOException;

public interface IMailService {

    void sendRegistrationEmail(Identity identity) throws MessagingException, IOException, TemplateException;

    void sendPasswordResetEmail(Identity identity) throws MessagingException, IOException, TemplateException;

    void testConnection();

    void sendMail(MailDraft mailDraft) throws MessagingException, IOException, TemplateException;
}