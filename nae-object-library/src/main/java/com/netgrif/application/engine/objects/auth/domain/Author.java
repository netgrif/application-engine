package com.netgrif.application.engine.objects.auth.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class Author implements Serializable {

    @Serial
    private static final long serialVersionUID = 5167778985918909834L;

    private String id;
    private String username;
    private String email;
    private String fullName;
    private String realmId;

    public Author() {
    }

    public Author(String id, String email, String fullName) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
    }

    public Author(String id, String username, String email, String fullName, String realmId) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.realmId = realmId;
    }

    @Override
    public String toString() {
        return "Author{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                '}';
    }

    @Override
    public Author clone() {
        return new Author(this.id, this.email, this.fullName);
    }
}
