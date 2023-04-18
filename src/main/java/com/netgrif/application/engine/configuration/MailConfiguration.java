package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.configuration.properties.JndiMailProperties;
import com.netgrif.application.engine.configuration.properties.MailAdditionalProperties;
import com.netgrif.application.engine.configuration.properties.MailTlsProperties;
import com.netgrif.application.engine.configuration.properties.SpringMailProperties;
import com.netgrif.application.engine.mail.MailService;
import com.netgrif.application.engine.mail.interfaces.IMailService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfiguration {

    private final freemarker.template.Configuration configuration;

    private final SpringMailProperties springMailProperties;

    private final JndiMailProperties jndiMailProperties;

    private final MailAdditionalProperties mailAdditionalProperties;

    private final MailTlsProperties mailTlsProperties;

    public MailConfiguration(freemarker.template.Configuration configuration, SpringMailProperties springMailProperties, JndiMailProperties jndiMailProperties, MailAdditionalProperties mailAdditionalProperties, MailTlsProperties mailTlsProperties) {
        this.configuration = configuration;
        this.springMailProperties = springMailProperties;
        this.jndiMailProperties = jndiMailProperties;
        this.mailAdditionalProperties = mailAdditionalProperties;
        this.mailTlsProperties = mailTlsProperties;
    }

    @Bean
    public JavaMailSenderImpl mailSender() {
        Properties mailProperties = new Properties();
        mailProperties.put("mail.smtp.starttls.enable", mailTlsProperties.isEnable());
        mailProperties.put("mail.smtp.starttls.required", mailTlsProperties.isRequired());
        mailProperties.put("mail.debug", mailAdditionalProperties.isDebug());
        mailProperties.put("mail.smtp.debug", mailAdditionalProperties.getSmtp().isDebug());
        mailProperties.put("mail.smtp.auth", mailAdditionalProperties.getSmtp().isAuth());
        mailProperties.put("mail.smtp.starttls", mailAdditionalProperties.getSmtp().isStarttls());
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setPort(springMailProperties.getPort());
        sender.setHost(springMailProperties.getHost());
        sender.setUsername(jndiMailProperties.getUsername());
        sender.setPassword(jndiMailProperties.getPassword());
        sender.setProtocol(springMailProperties.getProtocol());
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