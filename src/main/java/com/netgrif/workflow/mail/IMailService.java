package com.netgrif.workflow.mail;

import com.netgrif.workflow.auth.domain.User;
import freemarker.template.TemplateException;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.io.IOException;

@Service
public interface IMailService {

    void sendRegistrationEmail(User user) throws MessagingException, IOException, TemplateException;

    void sendPasswordResetEmail(User user) throws MessagingException, IOException, TemplateException;

    void testConnection();
}