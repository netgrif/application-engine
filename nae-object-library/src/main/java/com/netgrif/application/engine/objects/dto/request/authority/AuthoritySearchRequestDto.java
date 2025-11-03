package com.netgrif.application.engine.objects.dto.request.authority;

import java.io.Serializable;

/**
 * DTO for {@link com.netgrif.application.engine.objects.auth.domain.Authority} search request
 */
public record AuthoritySearchRequestDto(String fullText) implements Serializable {
}
