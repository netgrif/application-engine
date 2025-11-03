package com.netgrif.application.engine.objects.dto.response.realm;

import java.io.Serializable;

public record RealmDto(String name,
                       String description,
                       Boolean defaultRealm,
                       Boolean adminRealm,
                       Boolean enableBlocking,
                       Integer maxFailedAttempts,
                       Integer blockDurationMinutes,
                       Boolean enableLimitSessions,
                       Integer maxSessionsAllowed) implements Serializable {
}
