package com.netgrif.workflow.mail;


import lombok.Getter;

public enum EmailType {
    REGISTRATION ("registration.html", "Registration invite"),
    DRAFT("draft.html", "Návrh na uzavretie poistnej zmluvy"),
    INSURANCE("insurance.html", "Poistka k poistnej zmluve"),
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