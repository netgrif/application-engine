package com.netgrif.workflow.mail;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.Setter;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
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

    @Setter
    private JavaMailSender mailSender;

    @Setter
    private Configuration configuration;

    @Override
    public void sendRegistrationEmail(String recipient, String token) throws MessagingException, IOException, TemplateException {
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
    @Async
    public void sendDraftEmail(String recipient, File pdf, Map<String, Object> model) throws MessagingException, IOException, TemplateException {
        List<String> recipients = new LinkedList<>();
        recipients.add(recipient);
        Map<String, File> attachments = new HashMap<>();

        topLevelDomain = topLevelDomain == null ? "com" : (topLevelDomain.isEmpty() ? "com" : topLevelDomain);
        model.put("serverName", "http://" + (subdomain != null && !subdomain.isEmpty() ? (subdomain + ".") : "") + InetAddress.getLocalHost().getHostName().toLowerCase() + "." + topLevelDomain + (port != null && !port.isEmpty() ?  (":" + port) : ""));
        attachments.put(pdf.getName(), pdf);
        MimeMessage email = buildEmail(EmailType.DRAFT, recipients, model, attachments);
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

    private MimeMessage buildEmail(EmailType type, List<String> recipients, Map<String, Object> model, Map<String, File> attachments) throws MessagingException, IOException, TemplateException {
        MimeMessage message = mailSender.createMimeMessage();
        message.setSubject(type.subject);
        MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
        helper.setFrom(mailFrom);
        helper.setTo(recipients.toArray(new String[recipients.size()]));
        Template template = configuration.getTemplate(type.template);
        helper.setText(FreeMarkerTemplateUtils.processTemplateIntoString(template, model), true);
        attachments.forEach((s, inputStream) -> {
            try {
                helper.addAttachment(s, inputStream);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        });
        return message;
    }
}