package com.fmworkflow.mail;

import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.ui.velocity.VelocityEngineUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Component
public class MailService implements IMailService {

    @Value("${mail.from}")
    String mailFrom;

    private JavaMailSender mailSender;

    private VelocityEngine velocityEngine;

    public void sendRegistrationEmail(String recipient, String token) throws MessagingException {
        List<String> recipients = new LinkedList<>();
        recipients.add(recipient);
        Map<String, Object> model = new HashMap<>();
        model.put("token", token);
        model.put("date", LocalDate.now().plusDays(3).toString());
        MimeMessage email = buildEmail(EmailType.REGISTRATION, recipients, model);
        mailSender.send(email);
    }

    private MimeMessage buildEmail(EmailType type, List<String> recipients, Map<String, Object> model) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        message.setSubject(type.subject);
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(mailFrom);
        helper.setTo(recipients.toArray(new String[recipients.size()]));
        helper.setText(VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, type.template,"UTF-8", model), true);
        return message;
    }

    public void setVelocityEngine(VelocityEngine velocityEngine) {
        this.velocityEngine = velocityEngine;
    }

    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
}