package com.netgrif.application.engine.objects.dto.request.impersonation;

import java.io.Serializable;

public record ImpersonationSearchRequestDto(String query) implements Serializable {
}
