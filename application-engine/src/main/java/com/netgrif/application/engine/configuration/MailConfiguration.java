package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.mail.MailService;
import com.netgrif.application.engine.mail.interfaces.IMailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfiguration {

    @Autowired
    private freemarker.template.Configuration configuration;

    @Value("${spring.mail.default-encoding}")
    private String encoding;
    @Value("${spring.mail.host}")
    private String host;
    @Value("${spring.mail.jndi-name.spring.mail.username}")
    private String username;
    @Value("${spring.mail.jndi-name.spring.mail.password}")
    private String password;
    @Value("${spring.mail.port}")
    private int port;
    @Value("${spring.mail.properties.mail.debug}")
    private boolean debug;
    @Value("${spring.mail.properties.mail.smtp.debug}")
    private boolean smtpDebug;
    @Value("${spring.mail.properties.mail.smtp.auth}")
    private boolean smtpAuth;
    @Value("${spring.mail.properties.mail.smtp.starttls}")
    private boolean smtpStartTls;
    @Value("${spring.mail.protocol}")
    private String protocol;
    @Value("${spring.mail.test-connection}")
    private boolean testConnection;
    @Value("${spring.mail.smtp.starttls.enable}")
    private boolean smtpStartTlsEnable;
    @Value("${spring.mail.smtp.starttls.required}")
    private boolean smtpStartTlsRequired;

    @Bean
    public JavaMailSenderImpl mailSender() {
        Properties mailProperties = new Properties();
        mailProperties.put("mail.smtp.starttls.enable", smtpStartTlsEnable);
        mailProperties.put("mail.smtp.starttls.required", smtpStartTlsRequired);
        mailProperties.put("mail.debug", debug);
        mailProperties.put("mail.smtp.debug", smtpDebug);
        mailProperties.put("mail.smtp.auth", smtpAuth);
        mailProperties.put("mail.smtp.starttls", smtpStartTls);
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setPort(port);
        sender.setHost(host);
        sender.setUsername(username);
        sender.setPassword(password);
        sender.setProtocol(protocol);
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