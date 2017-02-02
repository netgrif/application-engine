package com.fmworkflow;

import com.fmworkflow.mail.IMailService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class MailSenderServiceTest {

    @Autowired
    private IMailService service;

    @Test
    public void testSend() throws Exception {
        service.sendRegistrationEmail("valdyreinn@gmail.com", "token");
    }
}