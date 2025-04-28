package com.netgrif.application.engine.history.service.listener;

import com.netgrif.application.engine.event.events.authorization.ActorAssignRoleEvent;
import com.netgrif.application.engine.event.events.authorization.ActorRemoveRoleEvent;
import com.netgrif.application.engine.history.domain.baseevent.repository.EventLogRepository;
import com.netgrif.application.engine.history.domain.actorevents.ActorRemoveRoleEventLog;
import com.netgrif.application.engine.history.domain.actorevents.ActorAssignRoleEventLog;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ActorEventListener {

    private final EventLogRepository repository;

    public ActorEventListener(EventLogRepository repository) {
        this.repository = repository;
    }

    @EventListener
    public void onActorRoleChangeEvent(ActorAssignRoleEvent event) {
        repository.save(new ActorAssignRoleEventLog(event.getActor().getEmail(), event.getRoles()));
    }

    @EventListener
    public void onActorRoleChangeEvent(ActorRemoveRoleEvent event) {
        repository.save(new ActorRemoveRoleEventLog(event.getActor().getEmail(), event.getRoles()));
    }
}
