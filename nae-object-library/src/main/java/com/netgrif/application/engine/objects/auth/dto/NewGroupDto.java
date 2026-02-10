package com.netgrif.application.engine.objects.auth.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NewGroupDto {

    @Size(min = 3, max = 255)
    private String identifier;

    @Size(min = 3, max = 255)
    private String title;

    @Size(min = 24, max = 24)
    private String ownerId;
}
