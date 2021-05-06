package com.netgrif.workflow;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.mail.EmailType;
import com.netgrif.workflow.mail.domain.MailDraft;
import com.netgrif.workflow.mail.interfaces.IMailService;
import freemarker.template.TemplateException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;

@SpringBootTest
@ActiveProfiles({"test"})
@RunWith(SpringRunner.class)
public class MailSenderServiceTest {

    static final String FROM = "noreply@netgrif.com";
    static final String RECIPIENT = "valdyreinn@gmail.com";
    static final String TOKEN = "čšňť";

    @Qualifier("mailService")
    @Autowired
    private IMailService service;

    private GreenMail smtpServer;

    @Before
    public void before() {
        smtpServer = new GreenMail(new ServerSetup(2525, null, "smtp"));
        smtpServer.start();
    }

    @Test
    public void testSend() throws Exception {
        service.sendRegistrationEmail(new User(RECIPIENT,"","",""));

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

    @After
    public void after() {
        smtpServer.stop();
    }

    private void assertMessageReceived(MimeMessage[] messages) throws MessagingException {
        assert messages.length > 0;

        MimeMessage message = messages[0];

        assert "noreply@netgrif.com".equalsIgnoreCase(message.getFrom()[0].toString());
        assert EmailType.REGISTRATION.getSubject().equalsIgnoreCase(message.getSubject());
    }

    private void assertMessageDraftReceived(MimeMessage[] messages) throws MessagingException, IOException {
        assert messages.length > 0;
        MimeMessage message = messages[0];
        assert "noreply@netgrif.com".equalsIgnoreCase(message.getFrom()[0].toString());
        assert "Subject draft".equalsIgnoreCase(message.getSubject());
    }
}