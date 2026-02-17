package com.netgrif.application.engine.objects.dto.request.group;

import com.netgrif.application.engine.objects.auth.domain.Group;

import java.io.Serializable;
import java.util.Set;

/**
 * DTO for {@link Group} search request
 */
public record GroupSearchRequestDto(Set<String> ids, String fullText, String realmId) implements Serializable { }
