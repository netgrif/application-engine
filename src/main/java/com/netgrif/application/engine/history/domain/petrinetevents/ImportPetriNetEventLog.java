package com.netgrif.application.engine.history.domain.petrinetevents;

import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import lombok.EqualsAndHashCode;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "eventLogs")
@EqualsAndHashCode(callSuper = true)
public class ImportPetriNetEventLog extends PetriNetEventLog {

    public ImportPetriNetEventLog(ObjectId triggerId, EventPhase eventPhase, ObjectId netId) {
        super(triggerId, eventPhase, netId);
    }
}
