package com.netgrif.application.engine.objects.event.events.user;

import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.event.RunPhase;
import lombok.Getter;

public class ImpersonationEvent extends UserEvent {

    @Getter
    private final LoggedUser impersonated;
    private final RunPhase runPhase;

    public ImpersonationEvent(LoggedUser user, LoggedUser impersonated, RunPhase runPhase) {
        super(user, null);
        this.impersonated = impersonated;
        this.runPhase = runPhase;
    }

    @Override
    public String getMessage() {
        return "User %s is impersonating: %s phase: %s".formatted(user.getUsername() == null ? MISSING_IDENTIFIER : user.getUsername(),
                impersonated.getUsername() == null ? MISSING_IDENTIFIER : impersonated.getUsername(), runPhase);
    }
}
