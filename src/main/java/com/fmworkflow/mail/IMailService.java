package com.fmworkflow.mail;

import javax.mail.MessagingException;

public interface IMailService {
    void sendRegistrationEmail(String recipient, String token) throws MessagingException;
}
