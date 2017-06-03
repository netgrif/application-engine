package com.netgrif.workflow.mail;


public enum EmailType {
    REGISTRATION ("registration.html", "Registration");

    String template;
    String subject;

    EmailType(String template, String subject) {
        this.template = template;
        this.subject = subject;
    }
}