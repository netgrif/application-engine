package com.netgrif.application.engine.auth.realm.request;

public record RealmSearch(String id,
                          String name,
                          String description,
                          Boolean defaultRealm,
                          Boolean adminRealm,
                          Boolean enableBlocking,
                          Integer maxFailedAttempts,
                          Integer blockDurationMinutes) {
}
