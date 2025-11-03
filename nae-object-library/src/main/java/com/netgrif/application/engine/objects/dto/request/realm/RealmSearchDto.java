package com.netgrif.application.engine.objects.dto.request.realm;

import java.io.Serializable;

public record RealmSearchDto(String id,
                             String name,
                             String description,
                             Boolean defaultRealm,
                             Boolean adminRealm,
                             Boolean enableBlocking,
                             Integer maxFailedAttempts,
                             Integer blockDurationMinutes,
                             Boolean enableLimitSessions,
                             Integer maxSessionsAllowed) implements Serializable {
}
