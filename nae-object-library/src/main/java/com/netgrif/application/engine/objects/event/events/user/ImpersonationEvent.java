package com.netgrif.application.engine.objects.event.events.user;

import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.event.RunPhase;
import lombok.Getter;

public class ImpersonationEvent extends UserEvent {

    @Getter
    private LoggedUser impersonated;

    private final RunPhase runPhase;

    public ImpersonationEvent(LoggedUser user, LoggedUser impersonated, RunPhase runPhase) {
        super(user);
        this.impersonated = impersonated;
        this.runPhase = runPhase;
    }

    @Override
    public String getMessage() {
        return "User " + user.getUsername() + " is impersonating: " + impersonated.getUsername() + " phase: " + runPhase;
    }
}
