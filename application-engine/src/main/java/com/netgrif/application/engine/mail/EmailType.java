package com.netgrif.application.engine.mail;


import lombok.Getter;

public enum EmailType {
    REGISTRATION("registration.html", "Registration invite"),
    PASSWORD_RESET("password-reset.html", "Reset password");

    @Getter
    String template;
    @Getter
    String subject;

    EmailType(String template, String subject) {
        this.template = template;
        this.subject = subject;
    }
}