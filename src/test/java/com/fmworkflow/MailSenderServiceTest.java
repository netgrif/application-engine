package com.fmworkflow;

import com.fmworkflow.mail.IMailService;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@ActiveProfiles({"test"})
@RunWith(SpringRunner.class)
public class MailSenderServiceTest {

    @Autowired
    private IMailService service;

    // TODO: 28/03/2017 allow when mail server
    @Test
    @Ignore
    public void testSend() throws Exception {
        Exception exception = null;
        try {
            service.sendRegistrationEmail("valdyreinn@gmail.com", "token");
        } catch (Exception e) {
            exception = e;
        }

        assert exception == null;
    }
}