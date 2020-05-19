package com.netgrif.workflow.rules.domain.facts;

import com.netgrif.workflow.petrinet.domain.EventType;
import com.netgrif.workflow.workflow.domain.Task;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
@EqualsAndHashCode(callSuper = true)
public class TransitionEvent extends CaseFact {

    private String transitionId;

    private EventType type;

    public TransitionEvent(String caseId, String transitionId, EventType type) {
        super(caseId);
        this.transitionId = transitionId;
        this.type = type;
    }

    public static TransitionEvent assign(Task task) {
        return of(task, EventType.ASSIGN);
    }

    public static TransitionEvent cancel(Task task) {
        return of(task, EventType.CANCEL);
    }

    public static TransitionEvent finish(Task task) {
        return of(task, EventType.FINISH);
    }

    public static TransitionEvent of(Task task, EventType type) {
        return new TransitionEvent(task.getCaseId(), task.getTransitionId(), type);
    }

    public static TransitionEvent of(String caseId, String transitionId, EventType type) {
        return new TransitionEvent(caseId, transitionId, type);
    }
}

