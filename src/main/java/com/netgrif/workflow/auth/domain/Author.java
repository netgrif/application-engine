package com.netgrif.workflow.auth.domain;

import lombok.Data;

@Data
public class Author {

    private Long id;
    private String email;
    private String fullName;

    public Author(){}

    public Author(Long id, String email, String fullName) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
    }

    @Override
    public String toString() {
        return "Author{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                '}';
    }
}
