package com.fmworkflow.workflow.service.interfaces;

import com.fmworkflow.workflow.domain.Case;

import java.util.List;

public interface IWorkflowService {
    void saveCase(Case useCase);

    List<Case> getAll();

    List<Case> searchCase(List<String> nets);

    void createCase(String netId, String title, String color);

//    DataSet getDataForTransition(String caseId, String transitionId);

//    void modifyData(String caseId, Map<String, String> newValues);
}
