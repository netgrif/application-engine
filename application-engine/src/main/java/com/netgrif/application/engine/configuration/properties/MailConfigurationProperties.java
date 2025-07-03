//package com.netgrif.application.engine.configuration.properties;
//
//import lombok.Data;
//import lombok.EqualsAndHashCode;
//import org.springframework.boot.autoconfigure.mail.MailProperties;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//
//@Data
//@EqualsAndHashCode(callSuper = true)
//@ConfigurationProperties(prefix = "netgrif.engine.mail")
//public class MailConfigurationProperties extends MailProperties {
//    private String mailFrom = "test@example.com";
//    private RedirectToProperties redirectTo = new RedirectToProperties();
//
//    @Data
//    public static class RedirectToProperties {
//        private String domain = "localhost";
//        private String port = "4200";
//        private boolean ssl = false;
//    }
//}
