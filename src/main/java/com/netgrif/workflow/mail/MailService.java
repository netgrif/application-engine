package com.netgrif.workflow.mail;

import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.auth.service.interfaces.IRegistrationService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class MailService implements IMailService {

    static final Logger log = Logger.getLogger(MailService.class.getName());
    public static final String TOKEN = "token";
    public static final String VALIDITY = "validity";
    public static final String EXPIRATION = "expiration";
    public static final String SERVER = "server";
    public static final String NAME = "name";

    @Autowired
    private IRegistrationService registrationService;

    @Getter
    @Value("${mail.server.port}")
    protected String port;

    @Getter
    @Value("${mail.server.host.domain}")
    protected String domain;

    @Getter
    @Value("${mail.server.host.ssl}")
    protected boolean ssl;

    @Getter
    @Value("${mail.from}")
    protected String mailFrom;

    @Getter
    @Value("${server.auth.token-validity-period}")
    protected String validityPeriod;

    @Getter
    @Setter
    protected JavaMailSender mailSender;

    @Getter
    @Setter
    protected Configuration configuration;

    @Override
    public void sendRegistrationEmail(User user) throws MessagingException, IOException, TemplateException {
        List<String> recipients = new LinkedList<>();
        Map<String, Object> model = new HashMap<>();

        recipients.add(user.getEmail());
        model.put(TOKEN, registrationService.encodeToken(user.getEmail(), user.getToken()));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        model.put(VALIDITY, validityPeriod);
        model.put(EXPIRATION, registrationService.generateExpirationDate().format(formatter));
        model.put(SERVER, getServerURL());

        MimeMessage email = buildEmail(EmailType.REGISTRATION, recipients, model, new HashMap<>());
        mailSender.send(email);

        log.info("Registration email sent to [" + user.getEmail() + "] with token [" + model.get(TOKEN) + "], expiring on [" + model.get(EXPIRATION) + "]");
    }

    @Override
    public void sendPasswordResetEmail(User user) throws MessagingException, IOException, TemplateException {
        Map<String, Object> model = new HashMap<>();

        model.put(NAME, user.getName());
        model.put(TOKEN, registrationService.encodeToken(user.getEmail(), user.getToken()));
        model.put(VALIDITY, validityPeriod);
        model.put(EXPIRATION, registrationService.generateExpirationDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        model.put(SERVER, getServerURL());

        MimeMessage email = buildEmail(EmailType.PASSWORD_RESET, Collections.singletonList(user.getEmail()), model, new HashMap<>());
        mailSender.send(email);

        log.info("Reset email sent to [" + user.getEmail() + "] with token [" + model.get(TOKEN) + "], expiring on [" + model.get(EXPIRATION) + "]");
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

    protected MimeMessage buildEmail(EmailType type, List<String> recipients, Map<String, Object> model, Map<String, File> attachments) throws MessagingException, IOException, TemplateException {
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

    protected String getServerURL() {
        String encryptedHttp = ssl ? "https://" : "http://";
        String usedPort = port != null && !port.isEmpty() ? (":" + port) : "";
        return encryptedHttp + domain + usedPort;
    }
}