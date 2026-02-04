package com.netgrif.application.engine.mail;

import com.netgrif.application.engine.configuration.properties.MailConfigurationProperties;
import com.netgrif.application.engine.configuration.properties.SecurityConfigurationProperties;
import com.netgrif.application.engine.auth.service.interfaces.IRegistrationService;
import com.netgrif.application.engine.mail.domain.MailDraft;
import com.netgrif.application.engine.mail.interfaces.IMailService;
import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.User;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
public class MailService implements IMailService {

    public static final String TOKEN = "token";
    public static final String VALIDITY = "validity";
    public static final String EXPIRATION = "expiration";
    public static final String SERVER = "server";
    public static final String NAME = "name";

    @Getter
    @Setter
    protected JavaMailSender mailSender;

    @Getter
    @Setter
    protected Configuration configuration;

    @Autowired
    private IRegistrationService registrationService;

    @Autowired
    private SecurityConfigurationProperties.AuthProperties serverAuthProperties;

    @Autowired
    private MailConfigurationProperties mailConfigurationProperties;

    @Override
    public void sendRegistrationEmail(AbstractUser user) throws IOException, TemplateException, MessagingException {
        List<String> recipients = new LinkedList<>();
        Map<String, Object> model = new HashMap<>();

        recipients.add(user.getEmail());
        if (user instanceof User u) {
            model.put(TOKEN, registrationService.encodeToken(user.getEmail(), u.getToken()));
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        model.put(VALIDITY, "" + serverAuthProperties.getTokenValidityPeriod());
        model.put(EXPIRATION, registrationService.generateExpirationDate().format(formatter));
        model.put(SERVER, getServerURL());

        MailDraft mailDraft = MailDraft.builder(mailConfigurationProperties.getMailFrom(), recipients).subject(EmailType.REGISTRATION.getSubject())
                .body(configuration.getTemplate(EmailType.REGISTRATION.template).toString()).model(model).build();
        MimeMessage email = buildEmail(mailDraft);
        mailSender.send(email);

        log.info("Registration email sent to [{}] with a token expiring on [{}]", user.getEmail(), model.get(EXPIRATION));
    }

    @Override
    public void sendPasswordResetEmail(AbstractUser user) throws IOException, TemplateException, MessagingException {
        Map<String, Object> model = new HashMap<>();

        model.put(NAME, user.getFirstName());
        if (user instanceof User u) {
            model.put(TOKEN, registrationService.encodeToken(user.getEmail(), u.getToken()));
        }
        model.put(VALIDITY, "" + serverAuthProperties.getTokenValidityPeriod());
        model.put(EXPIRATION, registrationService.generateExpirationDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        model.put(SERVER, getServerURL());

        MailDraft mailDraft = MailDraft.builder(mailConfigurationProperties.getMailFrom(), Collections.singletonList(user.getEmail())).subject(EmailType.PASSWORD_RESET.getSubject())
                .body(configuration.getTemplate(EmailType.PASSWORD_RESET.template).toString()).model(model).build();
        MimeMessage email = buildEmail(mailDraft);
        mailSender.send(email);

        log.info("Reset email sent to [{}] with a token expiring on [{}]", user.getEmail(), model.get(EXPIRATION));
    }

    @Override
    public void testConnection() {
        //TODO: remove?
//        try {
//            ((JavaMailSenderImpl) mailSender).testConnection();
//            log.info("MAIL: Connection to mail server is stable");
//        } catch (MessagingException e) {
//            log.error("MAIL: Connection failed!", e);
//        }
    }


    @Override
    public void sendMail(MailDraft mailDraft) throws IOException, TemplateException, MessagingException {
        MimeMessage email = buildEmail(mailDraft);
        mailSender.send(email);

        String formattedRecipients = StringUtils.join(mailDraft.getTo(), ", ");
        log.info("Email sent to [{}]", formattedRecipients);
    }

    protected MimeMessage buildEmail(MailDraft draft) throws MessagingException, IOException, TemplateException {
        MimeMessage message = mailSender.createMimeMessage();
        message.setSubject(draft.getSubject());

        MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
        helper.setFrom(draft.getFrom());
        helper.setTo(draft.getTo().toArray(new String[draft.getTo().size()]));
        helper.setCc(draft.getCc().toArray(new String[draft.getCc().size()]));
        helper.setBcc(draft.getBcc().toArray(new String[draft.getBcc().size()]));
        helper.setSubject(draft.getSubject());

        if (!draft.getModel().isEmpty()) {
            Template template = new Template("mailTemplate", new StringReader(draft.getBody()), configuration);
            helper.setText(FreeMarkerTemplateUtils.processTemplateIntoString(template, draft.getModel()), true);
        } else {
            helper.setText(draft.getBody(), draft.isHtml());
        }

        draft.getAttachments().forEach((s, inputStream) -> {
            try {
                helper.addAttachment(s, inputStream);
            } catch (MessagingException e) {
                log.error("Building email failed: ", e);
            }
        });
        return message;
    }


    protected String getServerURL() {
        String encryptedHttp = mailConfigurationProperties.getRedirectTo().isSsl() ? "https://" : "http://";
        String usedPort = mailConfigurationProperties.getRedirectTo().getPort() != null && !mailConfigurationProperties.getRedirectTo().getPort().isEmpty() ? (":" + mailConfigurationProperties.getRedirectTo().getPort()) : "";
        return encryptedHttp + mailConfigurationProperties.getRedirectTo().getDomain() + usedPort;
    }
}
