//package com.netgrif.application.engine.event.events.user;
//
//import com.netgrif.application.engine.auth.domain.LoggedUser;
//import lombok.Getter;
//
//public class ImpersonationEvent extends UserEvent {
//
//    @Getter
//    private LoggedUser impersonated;
//
//    private final ImpersonationPhase impersonationPhase;
//
//    public ImpersonationEvent(LoggedUser user, LoggedUser impersonated, ImpersonationPhase impersonationPhase) {
//        super(user);
//        this.impersonated = impersonated;
//        this.impersonationPhase = impersonationPhase;
//    }
//
//    @Override
//    public String getMessage() {
//        return "User " + user.getUsername() + " is impersonating: " + impersonated.getUsername() + " phase: " + impersonationPhase;
//    }
//}
