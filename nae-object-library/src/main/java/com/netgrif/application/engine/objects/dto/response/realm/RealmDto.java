package com.netgrif.application.engine.objects.dto.response.realm;

import com.netgrif.application.engine.objects.auth.domain.Realm;

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

    public static RealmDto fromRealm(Realm realm) {
        return new RealmDto(realm.getName(), realm.getDescription(), realm.isDefaultRealm(), realm.isAdminRealm(),
                realm.isEnableBlocking(), realm.getMaxFailedAttempts(), realm.getBlockDurationMinutes(),
                realm.isEnableLimitSessions(), realm.getMaxSessionsAllowed());
    }
}
