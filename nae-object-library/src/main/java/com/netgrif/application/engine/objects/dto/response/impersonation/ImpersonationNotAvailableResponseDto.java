package com.netgrif.application.engine.objects.dto.response.impersonation;

import java.io.Serializable;

public record ImpersonationNotAvailableResponseDto(boolean alreadyImpersonated) implements Serializable {
}
