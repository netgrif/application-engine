package com.netgrif.workflow.auth.domain;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "token")
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @NotBlank
    @Getter @Setter
    private String email;

    @NotNull
    @NotBlank
    @Getter @Setter
    private String hashedToken;

    @NotNull
    @Getter @Setter
    private LocalDateTime expirationDate;

    public Token() {
        expirationDate = LocalDateTime.now().plusDays(3);
    }

    public Token(String email, String token) {
        this();
        this.email = email;
        this.hashedToken = token;
    }
}