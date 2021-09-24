package com.netgrif.workflow.history.domain.petrinetevents;

import com.netgrif.workflow.history.domain.baseevent.EventLog;
import com.netgrif.workflow.petrinet.domain.events.EventPhase;
import lombok.Getter;
import org.bson.types.ObjectId;

import java.util.List;

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
