package com.netgrif.application.engine.history.domain.petrinetevents;

import com.netgrif.application.engine.event.events.petrinet.ProcessDeleteEvent;
import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.workflow.domain.ProcessResourceId;
import lombok.EqualsAndHashCode;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

@EqualsAndHashCode(callSuper = true)
public class DeletePetriNetEventLog extends PetriNetEventLog {

    public DeletePetriNetEventLog(ProcessResourceId triggerId, EventPhase eventPhase, ObjectId netId) {
        super(triggerId, eventPhase, netId);
    }

    public static DeletePetriNetEventLog fromEvent(ProcessDeleteEvent event) {
        return new DeletePetriNetEventLog(null, event.getEventPhase(), event.getPetriNet().getObjectId());
    }
}
