//package com.netgrif.application.engine.workflow.domain.eventoutcomes.taskoutcomes;
//
//import com.netgrif.core.workflow.domain.Case;
//import com.netgrif.core.workflow.domain.Task;
//import com.netgrif.core.workflow.domain.eventoutcomes.EventOutcome;
//import lombok.Data;
//
//import java.util.List;
//
//@Data
//public class CancelTaskEventOutcome extends TaskEventOutcome {
//
//    public CancelTaskEventOutcome() {
//        super();
//    }
//
//    public CancelTaskEventOutcome(Case useCase, Task task) {
//        super(useCase, task);
//    }
//
//    public CancelTaskEventOutcome(Case useCase, Task task, List<EventOutcome> outcomes) {
//        this(useCase, task);
//        setOutcomes(outcomes);
//    }
//}
