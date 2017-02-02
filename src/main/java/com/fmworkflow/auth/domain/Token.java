package com.fmworkflow.auth.domain;

import org.hibernate.validator.constraints.NotBlank;
import org.joda.time.DateTime;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "token")
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @NotBlank
    private String email;

    @NotNull
    @NotBlank
    private String hashedToken;

    @NotNull
    private Date expirationDate;

    public Token() {
        expirationDate = DateTime.now().plusDays(3).toDate();
    }

    public Token(String email, String token) {
        this();
        this.email = email;
        this.hashedToken = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getHashedToken() {
        return hashedToken;
    }

    public void setHashedToken(String hashedToken) {
        this.hashedToken = hashedToken;
    }

    public DateTime getExpirationDate() {
        return new DateTime(expirationDate);
    }

    public void setExpirationDate(DateTime expirationDate) {
        this.expirationDate = expirationDate.toDate();
    }
}
