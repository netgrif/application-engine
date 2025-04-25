package com.netgrif.application.engine.impersonation.domain;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "#{@impersonatorRedisHash}")
public class Impersonator implements Serializable {

    @Id
    protected String impersonatorId;

    @Indexed
    protected String impersonatedId;

    @Indexed
    protected List<String> configIds = new ArrayList<>();

    protected LocalDateTime impersonatingSince;
    protected LocalDateTime impersonatingUntil;
}
