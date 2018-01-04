package com.netgrif.workflow.workflow.service.interfaces;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.petrinet.domain.dataset.Field;
import com.netgrif.workflow.workflow.domain.Case;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface IWorkflowService {
    Case save(Case useCase);

    Case findOne(String caseId);

    Page<Case> getAll(Pageable pageable);

    Page<Case> searchCase(List<String> nets, Pageable pageable);

    Case createCase(String netId, String title, String color, Long authorId);

    Page<Case> findAllByAuthor(Long authorId, String petriNet, Pageable pageable);

    void deleteCase(String caseId);

    void updateMarking(Case useCase);

    List<Field> getData(String caseId);

    Page<Case> search(Map<String, Object> request, Pageable pageable, LoggedUser user);

    List<Case> getCaseFieldChoices(Pageable pageable, String caseId, String fieldId);
}