package com.netgrif.application.engine.objects.event.events.event;

import com.netgrif.application.engine.objects.auth.domain.ActorRef;
import com.netgrif.application.engine.objects.event.RunPhase;
import com.netgrif.application.engine.objects.event.events.Event;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.logic.action.Action;
import lombok.Getter;
import org.bson.types.ObjectId;

import java.util.Map;

@Getter
public abstract class ActionEvent extends Event {

    private final String importId;
    private final ObjectId id;
    private final Map<String, String> fieldIds;
    private final Map<String, String> transitionIds;
    private RunPhase phase;

    protected ActionEvent(Action action, ActorRef actorRef, String workspaceId) {
        super(action, actorRef, workspaceId);
        this.importId = action.getImportId();
        this.id = action.getId();
        this.fieldIds = action.getFieldIds();
        this.transitionIds = action.getTransitionIds();
    }

    protected void setPhase(RunPhase phase) {
        this.phase = phase;
    }

}