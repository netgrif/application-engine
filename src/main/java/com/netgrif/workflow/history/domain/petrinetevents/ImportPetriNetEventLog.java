package com.netgrif.workflow.history.domain.petrinetevents;

import com.netgrif.workflow.petrinet.domain.events.EventPhase;
import org.bson.types.ObjectId;

public class ImportPetriNetEventLog extends PetriNetEventLog {

    public ImportPetriNetEventLog(ObjectId triggerId, EventPhase eventPhase, ObjectId netId) {
        super(triggerId, eventPhase, netId);
    }
}
