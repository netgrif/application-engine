package com.netgrif.workflow.history.domain;

import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.DataField;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Map;

@Document
@NoArgsConstructor
public class CaseEventLog extends EventLog implements ICaseEventLog {

    private String caseId;

    private String caseTitle;

    @Field("activePlaces")
    private Map<String, Integer> activePlaces;

    @Field("dataSetValues")
    private Map<String, DataField> dataSetValues;

    public CaseEventLog(Case useCase) {
        this.caseId = useCase.getStringId();
        this.caseTitle = useCase.getTitle();
        this.activePlaces = useCase.getActivePlaces();
    }

    @Override
    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    @Override
    public void setCaseTitle(String caseTitle) {
        this.caseTitle = caseTitle;
    }

    @Override
    public void setActivePlaces(Map<String, Integer> places) {
        this.activePlaces = places;
    }

    @Override
    public void setDataSetValues(Map<String, DataField> values) {
        this.dataSetValues = values;
    }

    @Override
    public String getCaseId() {
        return caseId;
    }

    @Override
    public String getCaseTitle() {
        return caseTitle;
    }

    @Override
    public Map<String, Integer> getActivePlaces() {
        return activePlaces;
    }

    @Override
    public Map<String, DataField> getDataSetValues() {
        return dataSetValues;
    }
}