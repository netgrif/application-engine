package com.netgrif.workflow.workflow.service.interfaces;

import com.netgrif.workflow.workflow.domain.Case;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IWorkflowService {
    void saveCase(Case useCase);

    Page<Case> getAll(Pageable pageable);

    Page<Case> searchCase(List<String> nets, Pageable pageable);

    void createCase(String netId, String title, String color);

//    DataSet getDataForTransition(String caseId, String transitionId);

//    void modifyData(String caseId, Map<String, String> newValues);
}
