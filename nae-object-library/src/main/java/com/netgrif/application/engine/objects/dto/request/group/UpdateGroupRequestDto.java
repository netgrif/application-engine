package com.netgrif.application.engine.objects.dto.request.group;

import jakarta.validation.constraints.NotBlank;

public record UpdateGroupRequestDto(
        @NotBlank(message = "Group ID cannot be null") String id,
        String identifier,
        String displayName) {
}
