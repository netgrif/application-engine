package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.configuration.properties.MailConfigurationProperties;
import com.netgrif.application.engine.mail.MailService;
import com.netgrif.application.engine.mail.interfaces.IMailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfiguration {

    @Autowired
    private freemarker.template.Configuration configuration;

    @Autowired
    private MailConfigurationProperties mailConfigurationProperties;

    @Bean
    public JavaMailSenderImpl mailSender() {
        Properties mailProperties = new Properties();
        mailProperties.put("mail.smtp.starttls.enable", mailConfigurationProperties.getProperties().get("mail.smtp.starttls.enable"));
        mailProperties.put("mail.smtp.starttls.required", mailConfigurationProperties.getProperties().get("mail.smtp.starttls.required"));
        mailProperties.put("mail.debug", mailConfigurationProperties.getProperties().get("mail.debug"));
        mailProperties.put("mail.smtp.debug", mailConfigurationProperties.getProperties().get("mail.smtp.debug"));
        mailProperties.put("mail.smtp.auth", mailConfigurationProperties.getProperties().get("mail.smtp.auth"));
        mailProperties.put("mail.smtp.starttls", mailConfigurationProperties.getProperties().get("mail.smtp.starttls"));
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setPort(mailConfigurationProperties.getPort());
        sender.setHost(mailConfigurationProperties.getHost());
        sender.setUsername(mailConfigurationProperties.getUsername());
        sender.setPassword(mailConfigurationProperties.getPassword());
        sender.setProtocol(mailConfigurationProperties.getProtocol());
        sender.setJavaMailProperties(mailProperties);
        return sender;
    }

    @Bean
    public IMailService mailService(JavaMailSender mailSender) {
        MailService mailService = new MailService();
        mailService.setMailSender(mailSender);

        configuration.setClassForTemplateLoading(this.getClass(), "/templates");
        mailService.setConfiguration(configuration);
        return mailService;
    }
}