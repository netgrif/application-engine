package com.fmworkflow.mail;

import javax.mail.MessagingException;

public interface IMailService {
    void sendRegistrationEmail(String recipient) throws MessagingException;
}
