package com.netgrif.workflow.mail.interfaces;

import com.netgrif.workflow.auth.domain.IUser;
import com.netgrif.workflow.auth.domain.RegisteredUser;
import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.mail.domain.MailDraft;
import freemarker.template.TemplateException;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.io.IOException;

@Service
public interface IMailService {

    void sendRegistrationEmail(RegisteredUser user) throws MessagingException, IOException, TemplateException;

    void sendPasswordResetEmail(RegisteredUser user) throws MessagingException, IOException, TemplateException;

    void testConnection();

    void sendMail(MailDraft mailDraft) throws MessagingException, IOException, TemplateException;
}