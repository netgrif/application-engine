package com.fmworkflow.workflow.service;

import com.fmworkflow.workflow.domain.Case;
import com.fmworkflow.workflow.domain.dataset.DataSet;

import java.util.List;

public interface IWorkflowService {
    void saveCase(Case useCase);

    List<Case> getAll();

    void createCase(String netId, String title);

    DataSet getDataForTransition(String caseId, String transitionId);
}
