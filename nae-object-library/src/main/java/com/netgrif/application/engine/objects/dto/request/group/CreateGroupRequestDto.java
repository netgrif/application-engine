package com.netgrif.application.engine.objects.dto.request.group;

import java.io.Serializable;

public record CreateGroupRequestDto(String displayName, String realmId, String identifier, String ownerId) implements Serializable {
}
