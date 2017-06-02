package com.fmworkflow.auth.web;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ActiveProfiles({"test"})
@SpringBootTest
public class SignUpControllerTest {
    @Autowired
    SignUpController controller;

    @Test
    public void inviteTest() {
//        String response = controller.invite("email=valdyreinn@gmail.com");
//
//        assertSuccess(response);
    }

    private void assertSuccess(String response) {
        assert response.contains("success") && response.contains("Mail sent");
    }
}