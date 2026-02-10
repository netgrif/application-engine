package com.netgrif.application.engine.objects.auth.dto;

import lombok.Data;

@Data
public class GroupDto {

    private String id;

    private String identifier;

    private String displayName;

    private String ownerId;

    private String ownerUsername;
}
