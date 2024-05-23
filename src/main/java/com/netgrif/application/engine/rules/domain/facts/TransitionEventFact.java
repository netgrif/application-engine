package com.netgrif.application.engine.rules.domain.facts;

import com.netgrif.application.engine.importer.model.EventType;
import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.workflow.domain.Task;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

//TODO: release/8.0.0 lombok needs default constructor?
@Data
@Document
@EqualsAndHashCode(callSuper = true)
public class TransitionEventFact extends CaseFact {

    private String transitionId;

    private EventType type;

    private EventPhase phase;

    public TransitionEventFact(String caseId, String transitionId, EventType type, EventPhase phase) {
        super(caseId);
        this.transitionId = transitionId;
        this.type = type;
        this.phase = phase;
    }

    public static TransitionEventFact assign(Task task, EventPhase phase) {
        return of(task, EventType.ASSIGN, phase);
    }

    public static TransitionEventFact cancel(Task task, EventPhase phase) {
        return of(task, EventType.CANCEL, phase);
    }

    public static TransitionEventFact finish(Task task, EventPhase phase) {
        return of(task, EventType.FINISH, phase);
    }

    public static TransitionEventFact of(Task task, EventType type, EventPhase phase) {
        return new TransitionEventFact(task.getCaseId(), task.getTransitionId(), type, phase);
    }

    public static TransitionEventFact of(String caseId, String transitionId, EventType type, EventPhase phase) {
        return new TransitionEventFact(caseId, transitionId, type, phase);
    }
}

