package com.netgrif.application.engine.history.domain.petrinetevents;

import com.netgrif.application.engine.history.domain.baseevent.EventLog;
import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bson.types.ObjectId;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
public abstract class PetriNetEventLog extends EventLog {

    @Getter
    protected ObjectId netId;

    protected PetriNetEventLog() {
        super();
    }

    protected PetriNetEventLog(ObjectId triggerId, EventPhase eventPhase, ObjectId netId) {
        super(triggerId, eventPhase);
        this.netId = netId;
    }

    protected PetriNetEventLog(ObjectId triggerId, EventPhase eventPhase, List<ObjectId> triggeredEvents, ObjectId netId) {
        super(triggerId, eventPhase, triggeredEvents);
        this.netId = netId;
    }
}
