package com.netgrif.workflow.mail;

import org.apache.log4j.Logger;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.ui.velocity.VelocityEngineUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Component
public class MailService implements IMailService {

    static final Logger log = Logger.getLogger(MailService.class.getName());

    @Value("${mail.server.port}")
    String port;
    @Value("${mail.server.host.subdomain}")
    String subdomain;
    @Value("${mail.server.host.toplevel}")
    String topLevelDomain;
    @Value("${mail.from}")
    String mailFrom;

    private JavaMailSender mailSender;

    private VelocityEngine velocityEngine;

    @Override
    public void sendRegistrationEmail(String recipient, String token) throws MessagingException, UnknownHostException {
        List<String> recipients = new LinkedList<>();
        recipients.add(recipient);
        Map<String, Object> model = new HashMap<>();
        model.put("token", token);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        model.put("date", LocalDate.now().plusDays(3).format(formatter));

        topLevelDomain = topLevelDomain == null ? "com" : (topLevelDomain.isEmpty() ? "com" : topLevelDomain);
        model.put("serverName", "http://" + (subdomain != null && !subdomain.isEmpty() ? (subdomain + ".") : "") + InetAddress.getLocalHost().getHostName().toLowerCase() + "." + topLevelDomain + (port != null && !port.isEmpty() ?  (":" + port) : ""));
        MimeMessage email = buildEmail(EmailType.REGISTRATION, recipients, model, new HashMap<>());
        mailSender.send(email);
    }

    @Override
    public void sendDraftEmail(String recipient, File pdf) throws MessagingException, UnknownHostException {
        List<String> recipients = new LinkedList<>();
        recipients.add(recipient);
        Map<String, Object> model = new HashMap<>();
        Map<String, File> attachments = new HashMap<>();

        topLevelDomain = topLevelDomain == null ? "com" : (topLevelDomain.isEmpty() ? "com" : topLevelDomain);
        model.put("serverName", "http://" + (subdomain != null && !subdomain.isEmpty() ? (subdomain + ".") : "") + InetAddress.getLocalHost().getHostName().toLowerCase() + "." + topLevelDomain + (port != null && !port.isEmpty() ?  (":" + port) : ""));
        attachments.put(pdf.getName(), pdf);
        MimeMessage email = buildEmail(EmailType.REGISTRATION, recipients, model, attachments);
        mailSender.send(email);

        log.info("Mail with draft pdf sent to " + recipient);
    }

    @Override
    public void testConnection() {
//        try {
//            ((JavaMailSenderImpl) mailSender).testConnection();
//            log.info("MAIL: Connection to mail server is stable");
//        } catch (MessagingException e) {
//            e.printStackTrace();
//            log.error("MAIL: Connection failed!");
//        }
    }

    private MimeMessage buildEmail(EmailType type, List<String> recipients, Map<String, Object> model, Map<String, File> attachments) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        message.setSubject(type.subject);
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(mailFrom);
        helper.setTo(recipients.toArray(new String[recipients.size()]));
        helper.setText(VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, type.template, "UTF-8", model), true);
        attachments.forEach((s, inputStream) -> {
            try {
                helper.addAttachment(s, inputStream);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        });
        return message;
    }

    public void setVelocityEngine(VelocityEngine velocityEngine) {
        this.velocityEngine = velocityEngine;
    }

    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
}