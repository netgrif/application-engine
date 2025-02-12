//package com.netgrif.application.engine.history.domain.caseevents;
//
//import com.netgrif.core.event.events.workflow.CreateCaseEvent;
//import com.netgrif.core.petrinet.domain.events.EventPhase;
//import com.netgrif.core.workflow.domain.Case;
//import com.netgrif.core.workflow.domain.DataField;
//import lombok.EqualsAndHashCode;
//
//import java.io.Serial;
//import java.util.Map;
//
////todo
//@EqualsAndHashCode(callSuper = true)
//public class CreateCaseEventLog extends CaseEventLog {
//
//
//    @Serial
//    private static final long serialVersionUID = -7202365131803028246L;
//
//    private Map<String, DataField> dataSetValues;
//
//    public CreateCaseEventLog() {
//        super();
//    }
//
//    public CreateCaseEventLog(Case useCase, EventPhase eventPhase) {
//        super(useCase, eventPhase);
//        this.dataSetValues = useCase.getDataSet();
//    }
//
//    public Map<String, DataField> getDataSetValues() {
//        return dataSetValues;
//    }
//
//    public void setDataSetValues(Map<String, DataField> values) {
//        this.dataSetValues = values;
//    }
//
//    public static CreateCaseEventLog fromEvent(CreateCaseEvent event) {
//        return new CreateCaseEventLog(event.getCaseEventOutcome().getCase(), event.getEventPhase());
//    }
//}
