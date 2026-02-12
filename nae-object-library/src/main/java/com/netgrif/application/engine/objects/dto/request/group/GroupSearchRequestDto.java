package com.netgrif.application.engine.objects.dto.request.group;

import com.netgrif.application.engine.objects.auth.domain.Group;

import java.io.Serializable;

/**
 * DTO for {@link Group} search request
 */
public record GroupSearchRequestDto(String fullText, String realmId) implements Serializable { }
