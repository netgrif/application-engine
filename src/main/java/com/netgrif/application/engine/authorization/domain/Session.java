package com.netgrif.application.engine.authorization.domain;

import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;

@Data
public class Session {
    private LocalDateTime from;
    private LocalDateTime to;

    /**
     * todo javadoc
     * */
    public static Session forever() {
        return new Session();
    }

    /**
     * todo javadoc
     * */
    public static Session withDuration(Duration duration) {
        LocalDateTime now = LocalDateTime.now();
        return new Session(now, now.plus(duration));
    }

    public Session() {
        this(LocalDateTime.now(), null);
    }

    public Session(LocalDateTime from, LocalDateTime to) {
        this.from = from;
        this.to = to;
    }
}
