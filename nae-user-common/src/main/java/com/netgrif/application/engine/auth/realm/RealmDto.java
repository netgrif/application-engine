package com.netgrif.application.engine.auth.realm;

public record RealmDto(String name,
                       String description,
                       Boolean defaultRealm,
                       Boolean adminRealm,
                       Boolean enableBlocking,
                       Integer maxFailedAttempts,
                       Integer blockDurationMinutes,
                       Boolean enableLimitSessions,
                       Integer maxSessionsAllowed) {
}
