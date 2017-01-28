package com.fmworkflow;

import org.apache.velocity.app.VelocityEngine;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Component;
import org.springframework.ui.velocity.VelocityEngineUtils;

import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Map;

@Component
public class MailSenderService {
    private JavaMailSender mailSender;
    private VelocityEngine velocityEngine;

    public void send() {
        MimeMessagePreparator preparator = mimeMessage -> {
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage);
            message.setTo("valdyreinn@gmail.com");// TODO: 27. 1. 2017
            message.setFrom("noreply@fmworkflow.com"); // TODO: 27. 1. 2017
            Map model = new HashMap();
            //model.put("user", ); // TODO: 27. 1. 2017
//            String text = VelocityEngineUtils.mergeTemplateIntoString(
//                    velocityEngine, "com/dns/registration-confirmation.vm", model);
            message.setText("text", true);
        };
        this.mailSender.send(preparator);
    }

    public JavaMailSender getMailSender() {
        return mailSender;
    }

    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public VelocityEngine getVelocityEngine() {
        return velocityEngine;
    }

    public void setVelocityEngine(VelocityEngine velocityEngine) {
        this.velocityEngine = velocityEngine;
    }
}