package com.netgrif.application.engine.objects.event.events.task;

import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.objects.petrinet.domain.events.EventType;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.taskoutcomes.DelegateTaskEventOutcome;
import lombok.Getter;

public class DelegateTaskEvent extends TaskEvent {

    @Getter
    private String delegate;

    public DelegateTaskEvent(DelegateTaskEventOutcome eventOutcome, EventPhase eventPhase) {
        super(eventOutcome, eventPhase);
    }

    public DelegateTaskEvent(DelegateTaskEventOutcome eventOutcome, EventPhase eventPhase, String delegate) {
        this(eventOutcome, eventPhase, null, delegate);
    }

    public DelegateTaskEvent(DelegateTaskEventOutcome eventOutcome, AbstractUser user, String delegate) {
        this(eventOutcome, null, user, delegate);
    }

    public DelegateTaskEvent(DelegateTaskEventOutcome eventOutcome, EventPhase eventPhase, AbstractUser user, String delegate) {
        super(eventOutcome,eventPhase, user);
        this.delegate = delegate;
    }

    @Override
    public String getMessage() {
        return "DelegateTaskEvent: Task [%s] delegated"
                .formatted(taskEventOutcome.getTask() == null ? MISSING_IDENTIFIER : taskEventOutcome.getTask().getStringId());
    }
    @Override
    public EventType getEventType() {
        return EventType.DELEGATE;
    }
}
