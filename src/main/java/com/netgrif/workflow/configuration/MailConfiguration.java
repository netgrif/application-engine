package com.netgrif.workflow.configuration;

import com.netgrif.workflow.mail.IMailService;
import com.netgrif.workflow.mail.MailService;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfiguration {
    @Bean
    public JavaMailSenderImpl mailSender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setPort(465);
        sender.setHost("smtp.websupport.sk");
        sender.setUsername("noreply@netgrif.com");
        sender.setPassword("Superstar38");
        Properties mailProperties = new Properties();
        mailProperties.put("mail.smtp.auth", true);
        mailProperties.put("mail.smtp.starttls.enable", true);
        mailProperties.put("mail.smtp.starttls.required", true);
        sender.setJavaMailProperties(mailProperties);
        return sender;
    }

    @Bean
    public VelocityEngine velocityEngine() {
        VelocityEngine engine = new VelocityEngine();
        engine.setProperty("resource.loader", "class");
        engine.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        return engine;
    }

    @Bean
    public IMailService mailService(VelocityEngine velocityEngine, JavaMailSender mailSender) {
        MailService mailService = new MailService();
        mailService.setVelocityEngine(velocityEngine);
        mailService.setMailSender(mailSender);
        return mailService;
    }
}
