package com.fmworkflow.workflow.service.interfaces;

import com.fmworkflow.workflow.domain.Case;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IWorkflowService {
    void saveCase(Case useCase);

    List<Case> getAll(Pageable pageable);

    List<Case> searchCase(List<String> nets);

    void createCase(String netId, String title, String color);

//    DataSet getDataForTransition(String caseId, String transitionId);

//    void modifyData(String caseId, Map<String, String> newValues);
}
