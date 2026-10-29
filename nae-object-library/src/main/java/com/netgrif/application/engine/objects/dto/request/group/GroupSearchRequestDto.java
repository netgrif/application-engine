package com.netgrif.application.engine.objects.dto.request.group;

import com.netgrif.application.engine.objects.auth.domain.Group;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

/**
 * DTO for {@link Group} search request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupSearchRequestDto {
    private Set<String> ids;
    private String realmId;
    private String fullText;
}