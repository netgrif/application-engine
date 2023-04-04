package com.netgrif.application.engine.history.domain.caseevents;

import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.web.responsebodies.DataSet;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Map;

public class CreateCaseEventLog extends CaseEventLog {

    @Getter
    @Field("activePlaces") // TODO: release/7.0.0
    private final Map<String, Integer> activePlaces;

    @Getter
    @Field("dataSetValues")
    private final DataSet dataSetValues;

    public CreateCaseEventLog(Case useCase, EventPhase eventPhase) {
        super(useCase, eventPhase);
        this.activePlaces = useCase.getActivePlaces();
        this.dataSetValues = useCase.getDataSet();
    }
}