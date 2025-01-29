package com.netgrif.application.engine.history.domain.petrinetevents;

import com.netgrif.application.engine.event.events.petrinet.ProcessDeleteEvent;
import com.netgrif.application.engine.event.events.petrinet.ProcessDeployEvent;
import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.workflow.domain.ProcessResourceId;
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
