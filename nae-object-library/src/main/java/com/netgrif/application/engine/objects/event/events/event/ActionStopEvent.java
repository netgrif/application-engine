package com.netgrif.application.engine.objects.event.events.event;

import com.netgrif.application.engine.objects.event.RunPhase;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.logic.action.Action;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
public class ActionStopEvent extends ActionStartEvent {

    private final boolean success;
    private final long totalDuration;

    public ActionStopEvent(Action action, ActionStartEvent startEvent, boolean success, String workspaceId) {
        super(action,startEvent.getActor(), workspaceId);
        setPhase(RunPhase.STOP);
        this.success = success;
        this.totalDuration = calculateExecutionDuration(startEvent.getTime());
    }

    @Override
    public String getMessage() {
        return String.format("ActionStopEvent: trigger %s id [%s] phase: %s", getTrigger(), getId(), getPhase());
    }

    private long calculateExecutionDuration(LocalDateTime start) {
        Duration duration = Duration.between(start, this.getTime());
        return duration.toMillis();
    }

}
