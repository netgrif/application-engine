package com.netgrif.application.engine.history.service.listener;

import com.netgrif.application.engine.event.events.authentication.IdentityLoginEvent;
import com.netgrif.application.engine.event.events.authentication.IdentityLogoutEvent;
import com.netgrif.application.engine.event.events.authentication.IdentityRegistrationEvent;
import com.netgrif.application.engine.history.domain.baseevent.repository.EventLogRepository;
import com.netgrif.application.engine.history.domain.identityevents.IdentityEventLog;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IdentityEventListener {

    private final EventLogRepository repository;

    @EventListener
    public void onIdentityLoginEvent(IdentityLoginEvent event) {
        // todo: release/8.0.0
    }

    @EventListener
    public void onIdentityLogoutEvent(IdentityLogoutEvent event) {
        // todo: release/8.0.0
    }

    @EventListener
    public void onIdentityRegistrationEvent(IdentityRegistrationEvent event) {
        repository.save(new IdentityEventLog(event.getIdentity().getUsername()));
    }
}
