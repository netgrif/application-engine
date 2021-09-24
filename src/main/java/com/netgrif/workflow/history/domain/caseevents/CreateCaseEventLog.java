package com.netgrif.workflow.history.domain.caseevents;

import com.netgrif.workflow.petrinet.domain.events.EventPhase;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.DataField;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Map;

public class CreateCaseEventLog extends CaseEventLog {

    @Field("activePlaces")
    private Map<String, Integer> activePlaces;

    @Field("dataSetValues")
    private Map<String, DataField> dataSetValues;

    public CreateCaseEventLog(Case useCase, EventPhase eventPhase) {
        super(useCase, eventPhase);
        this.activePlaces = useCase.getActivePlaces();
        this.dataSetValues = useCase.getDataSet();
    }

    public void setActivePlaces(Map<String, Integer> places) {
        this.activePlaces = places;
    }

    public void setDataSetValues(Map<String, DataField> values) {
        this.dataSetValues = values;
    }

    public Map<String, Integer> getActivePlaces() {
        return activePlaces;
    }

    public Map<String, DataField> getDataSetValues() {
        return dataSetValues;
    }
}