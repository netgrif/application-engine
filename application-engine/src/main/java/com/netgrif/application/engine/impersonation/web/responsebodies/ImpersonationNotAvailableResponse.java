package com.netgrif.application.engine.impersonation.web.responsebodies;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImpersonationNotAvailableResponse {

    private boolean alreadyImpersonated;


}
