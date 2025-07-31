package com.netgrif.application.engine.mail.interfaces;

import com.netgrif.application.engine.mail.domain.MailDraft;
import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;

import java.io.IOException;

public interface IMailService {

    void sendRegistrationEmail(AbstractUser user) throws IOException, TemplateException, MessagingException;

    void sendPasswordResetEmail(AbstractUser user) throws IOException, TemplateException, MessagingException;

    void testConnection();

    void sendMail(MailDraft mailDraft) throws IOException, TemplateException, MessagingException;
}
