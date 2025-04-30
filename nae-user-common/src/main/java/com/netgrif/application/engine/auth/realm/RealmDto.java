package com.netgrif.application.engine.auth.realm;

public record RealmDto(String id,
                       String name,
                       String description,
                       Boolean defaultRealm,
                       Boolean adminRealm,
                       Boolean enableBlocking,
                       Integer maxFailedAttempts,
                       Integer blockDurationMinutes) {
}
