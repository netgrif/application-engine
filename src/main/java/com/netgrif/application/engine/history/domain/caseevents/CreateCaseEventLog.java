package com.netgrif.application.engine.history.domain.caseevents;

import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.web.responsebodies.DataSet;
import lombok.Data;
import lombok.Getter;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Map;

@Document(collection = "eventLogs")
@EqualsAndHashCode(callSuper = true)
public class CreateCaseEventLog extends CaseEventLog {

    @Getter
    @Field("activePlaces") // TODO: release/7.0.0
    private Map<String, Integer> activePlaces;

    @Getter
    @Field("dataSetValues")
    private DataSet dataSetValues;

    public CreateCaseEventLog() {
        super();
    }

    public CreateCaseEventLog(Case useCase, EventPhase eventPhase) {
        super(useCase, eventPhase);
        this.activePlaces = useCase.getActivePlaces();
        this.dataSetValues = useCase.getDataSet();
    }

    public Map<String, Integer> getActivePlaces() {
        return activePlaces;
    }

    public void setActivePlaces(Map<String, Integer> places) {
        this.activePlaces = places;
    }

    public DataSet getDataSetValues() {
        return dataSetValues;
    }

    public void setDataSetValues(DataSet values) {
        this.dataSetValues = values;
    }
}
