package com.netgrif.workflow.mail.interfaces;

import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.mail.EmailType;
import freemarker.template.TemplateException;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public interface IMailService {

    void sendRegistrationEmail(User user) throws MessagingException, IOException, TemplateException;

    void sendPasswordResetEmail(User user) throws MessagingException, IOException, TemplateException;

    void testConnection();

    void sendMail(List<String> recipients, EmailType type, Map<String, Object> model, Map<String, File> attachments) throws MessagingException, IOException, TemplateException;

    void sendMail(List<String> recipients, String subject, String text, boolean isHtml, Map<String, File> attachments) throws MessagingException, IOException, TemplateException;
}