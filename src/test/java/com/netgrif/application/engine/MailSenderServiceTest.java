package com.netgrif.application.engine;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.netgrif.application.engine.auth.domain.User;
import com.netgrif.application.engine.mail.EmailType;
import com.netgrif.application.engine.mail.domain.MailDraft;
import com.netgrif.application.engine.mail.interfaces.IMailService;
import freemarker.template.TemplateException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class MailSenderServiceTest {

    static final String FROM = "test@example.com";
    static final String RECIPIENT = "userTest@netgrif.com";
    static final String TOKEN = "čšňť";

    @Autowired
    private IMailService service;

    private GreenMail smtpServer;

    @BeforeEach
    public void before() {
        smtpServer = new GreenMail(new ServerSetup(2525, null, "smtp"));
        smtpServer.start();
    }

    @Test
    public void testSend() throws Exception {
        service.sendRegistrationEmail(new User(RECIPIENT, "", "", ""));

        MimeMessage[] messages = smtpServer.getReceivedMessages();

        assertMessageReceived(messages);
    }

    @Test
    public void testMailDraft() throws MessagingException, IOException, TemplateException {
        File file = new File("file.txt");
        file.createNewFile();

        MailDraft draft = MailDraft.builder(FROM, Collections.singletonList(RECIPIENT))
                .cc(Collections.singletonList("cc@netgrif.com"))
                .bcc(Collections.singletonList("bcc@netgrif.com"))
                .subject("Subject draft")
                .body("This is body and this is value ${value}")
                .model(Collections.singletonMap("value", 125))
                .attachments(Collections.singletonMap("file", file)).build();
        service.sendMail(draft);

        MimeMessage[] messages = smtpServer.getReceivedMessages();
        assertMessageDraftReceived(messages);
    }

    @AfterEach
    public void after() {
        smtpServer.stop();
    }

    private void assertMessageReceived(MimeMessage[] messages) throws MessagingException {
        assert messages.length > 0;

        MimeMessage message = messages[0];

        assert "test@example.com".equalsIgnoreCase(message.getFrom()[0].toString());
        assert EmailType.REGISTRATION.getSubject().equalsIgnoreCase(message.getSubject());
    }

    private void assertMessageDraftReceived(MimeMessage[] messages) throws MessagingException, IOException {
        assert messages.length > 0;
        MimeMessage message = messages[0];
        assert "test@example.com".equalsIgnoreCase(message.getFrom()[0].toString());
        assert "Subject draft".equalsIgnoreCase(message.getSubject());
    }
}