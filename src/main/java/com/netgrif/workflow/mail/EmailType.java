package com.netgrif.workflow.mail;


import lombok.Getter;

public enum EmailType {
    REGISTRATION ("registration.html", "Registration to Netgrif WMS"),
    DRAFT("draft.html", "Návrh na uzavretie poistnej zmluvy");

    @Getter
    String template;
    @Getter
    String subject;

    EmailType(String template, String subject) {
        this.template = template;
        this.subject = subject;
    }
}