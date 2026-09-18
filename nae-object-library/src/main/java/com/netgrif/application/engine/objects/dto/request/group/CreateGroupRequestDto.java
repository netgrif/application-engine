package com.netgrif.application.engine.objects.dto.request.group;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.io.Serializable;

public record CreateGroupRequestDto(
        @NotBlank(message = "Title is mandatory") String displayName,
        @NotBlank(message = "Realm ID is mandatory") String realmId,
        @NotBlank(message = "Identifier is mandatory") String identifier,
        @Pattern(regexp = "^[a-fA-F0-9]{24}$") String ownerId) implements Serializable {

}
