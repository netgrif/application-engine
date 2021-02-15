package com.netgrif.workflow;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.mail.EmailType;
import com.netgrif.workflow.mail.interfaces.IMailService;
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

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class MailSenderServiceTest {

    static final String RECIPIENT = "valdyreinn@gmail.com";
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

    @AfterEach
    public void after() {
        smtpServer.stop();
    }

    private void assertMessageReceived(MimeMessage[] messages) throws MessagingException {
        assert messages.length > 0;

        MimeMessage message = messages[0];

        assert "noreply@netgrif.com".equalsIgnoreCase(message.getFrom()[0].toString());
        assert EmailType.REGISTRATION.getSubject().equalsIgnoreCase(message.getSubject());
    }
}