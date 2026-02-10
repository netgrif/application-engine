package com.netgrif.application.engine.objects.auth.dto;

import lombok.Data;

@Data
public class SearchGroupDto {
    private String fullText;
    private String realmId;
}
