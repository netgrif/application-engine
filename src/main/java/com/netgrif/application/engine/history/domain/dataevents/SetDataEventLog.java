//package com.netgrif.application.engine.history.domain.dataevents;
//
//import com.netgrif.core.auth.domain.IUser;
//import com.netgrif.core.event.events.data.SetDataEvent;
//import com.netgrif.core.history.domain.taskevents.TaskEventLog;
//import com.netgrif.core.petrinet.domain.dataset.logic.ChangedField;
//import com.netgrif.core.petrinet.domain.events.EventPhase;
//import com.netgrif.core.workflow.domain.Case;
//import com.netgrif.core.workflow.domain.Task;
//import com.querydsl.core.annotations.QueryExclude;
//import lombok.EqualsAndHashCode;
//import lombok.Getter;
//
//import java.util.Map;
//
//@QueryExclude
////TODO
//@EqualsAndHashCode(callSuper = true)
//public class SetDataEventLog extends TaskEventLog {
//
//    @Getter
//    private Map<String, ChangedField> changedFields;
//
//    public SetDataEventLog() {
//        super();
//    }
//
//    public SetDataEventLog(Task task, Case useCase, EventPhase eventPhase, Map<String, ChangedField> changedFields, IUser user) {
//        super(task, useCase, eventPhase, user.getStringId(), user.isImpersonating() ? user.getImpersonated().getStringId() : null);
//        this.changedFields = changedFields;
//    }
//
//    public static SetDataEventLog fromEvent(SetDataEvent event) {
//        return new SetDataEventLog(event.getEventOutcome().getTask(), event.getEventOutcome().getCase(), event.getEventPhase(), event.getEventOutcome().getChangedFields(), event.getUser());
//    }
//}
