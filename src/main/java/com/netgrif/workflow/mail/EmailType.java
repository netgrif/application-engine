package com.netgrif.workflow.mail;


public enum EmailType {
    REGISTRATION ("registration.html", "Registration to Netgrif WMS");

    String template;
    String subject;

    EmailType(String template, String subject) {
        this.template = template;
        this.subject = subject;
    }
}