package com.netgrif.application.engine.auth.domain;

public enum AuthorityDomainEnum {
    ADMIN("*"),
    PROCESS("PROCESS"),
    FILTER("FILTER"),
    USER("USER"),
    GROUP("GROUP"),
    ROLE("ROLE"),
    AUTHORITY("AUTHORITY");
    
    AuthorityDomainEnum(String name) {
    }
}
