package com.netgrif.application.engine.objects.event.events.event;

import com.netgrif.application.engine.objects.event.RunPhase;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.application.engine.objects.petrinet.domain.events.DataEventType;
import lombok.Getter;

@Getter
public class ActionStartEvent  extends ActionEvent {

    private final DataEventType trigger;

    public ActionStartEvent(Action action, String workspaceId) {
        super(action, workspaceId);
        this.trigger = action.getTrigger();
        setPhase(RunPhase.START);
    }

    @Override
    public String getMessage() {
        return "ActionStartEvent: trigger %s id [%s] phase: %s".formatted(trigger, getId(), getPhase());
    }
}
