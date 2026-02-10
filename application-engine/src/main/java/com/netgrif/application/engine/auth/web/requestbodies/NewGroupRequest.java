package com.netgrif.application.engine.auth.web.requestbodies;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NewGroupRequest {

    @Size(min = 3, max = 255)
    private String identifier;

    @Size(min = 3, max = 255)
    private String title;

    @Size(min = 24, max = 24)
    private String ownerId;
}
