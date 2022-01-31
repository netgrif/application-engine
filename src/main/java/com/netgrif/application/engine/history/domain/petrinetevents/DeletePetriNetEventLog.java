package com.netgrif.application.engine.history.domain.petrinetevents;

import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import org.bson.types.ObjectId;

public class DeletePetriNetEventLog extends PetriNetEventLog {

    public DeletePetriNetEventLog(ObjectId triggerId, EventPhase eventPhase, ObjectId netId) {
        super(triggerId, eventPhase, netId);
    }
}
