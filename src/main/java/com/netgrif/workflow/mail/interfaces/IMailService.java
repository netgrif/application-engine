package com.netgrif.workflow.mail.interfaces;

import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.mail.EmailType;
import com.netgrif.workflow.mail.domain.SimpleMailDraft;
import com.netgrif.workflow.mail.domain.TypedMailDraft;
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

    void sendMail(TypedMailDraft mailDraft) throws MessagingException, IOException, TemplateException;

    void sendMail(SimpleMailDraft mailDraft) throws MessagingException, IOException, TemplateException;
}