package com.fmworkflow;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class MailSenderServiceTest {

    @MockBean
    private
    MailSenderService service;

    @Test
    public void testSend() throws Exception {
        service.send();
    }
}