package com.netgrif.application.engine.history.domain.petrinetevents;

import com.netgrif.core.event.events.petrinet.ProcessDeleteEvent;
import com.netgrif.core.event.events.petrinet.ProcessDeployEvent;
import com.netgrif.core.petrinet.domain.events.EventPhase;
import com.netgrif.core.workflow.domain.ProcessResourceId;
import lombok.EqualsAndHashCode;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "eventLogs")
@EqualsAndHashCode(callSuper = true)
public class ImportPetriNetEventLog extends PetriNetEventLog {

    public ImportPetriNetEventLog(ProcessResourceId triggerId, EventPhase eventPhase, ObjectId netId) {
        super(triggerId, eventPhase, netId);
    }


    public static ImportPetriNetEventLog fromEvent(ProcessDeployEvent event) {
        return new ImportPetriNetEventLog(null, event.getEventPhase(), event.getEventOutcome().getNet().getObjectId());
    }
}
