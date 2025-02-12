//package com.netgrif.application.engine.history.domain.petrinetevents;
//
//import com.netgrif.core.event.events.petrinet.ProcessDeleteEvent;
//import com.netgrif.core.petrinet.domain.events.EventPhase;
//import com.netgrif.core.workflow.domain.ProcessResourceId;
//import lombok.EqualsAndHashCode;
//import org.bson.types.ObjectId;
//import org.springframework.data.mongodb.core.mapping.Document;
//
//@EqualsAndHashCode(callSuper = true)
//public class DeletePetriNetEventLog extends PetriNetEventLog {
//
//    public DeletePetriNetEventLog(ProcessResourceId triggerId, EventPhase eventPhase, ObjectId netId) {
//        super(triggerId, eventPhase, netId);
//    }
//
//    public static DeletePetriNetEventLog fromEvent(ProcessDeleteEvent event) {
//        return new DeletePetriNetEventLog(null, event.getEventPhase(), event.getPetriNet().getObjectId());
//    }
//}
