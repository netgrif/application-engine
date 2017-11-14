package com.netgrif.workflow.history.domain;

import com.netgrif.workflow.workflow.domain.DataField;

import java.util.Map;

public interface ICaseEventLog {

    void setCaseId(String caseId);

    void setCaseTitle(String caseTitle);

    void setActivePlaces(Map<String, Integer> places);

    void setDataSetValues(Map<String, DataField> values);

    String getCaseId();

    String getCaseTitle();

    Map<String, Integer> getActivePlaces();

    Map<String, DataField> getDataSetValues();
}