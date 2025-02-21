package com.netgrif.application.engine.authorization.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Session {
    private LocalDateTime from;
    private LocalDateTime to;

    public static Session forever() {
        return new Session();
    }

    public Session() {
        this.from = LocalDateTime.now();
    }
}
