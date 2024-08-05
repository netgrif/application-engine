package com.netgrif.application.engine.workflow.service.interfaces;

import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.domain.outcomes.eventoutcomes.caseoutcomes.CreateCaseEventOutcome;
import com.netgrif.application.engine.workflow.domain.outcomes.eventoutcomes.caseoutcomes.DeleteCaseEventOutcome;
import com.netgrif.application.engine.workflow.domain.params.CreateCaseParams;
import com.netgrif.application.engine.workflow.domain.params.DeleteCaseParams;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface IWorkflowService {

    Case save(Case useCase);

    Case findOne(String caseId);

    Case findOneNoNet(String caseId);

    List<Case> findAllById(List<String> ids);

    Page<Case> getAll(Pageable pageable);

    Page<Case> findAllByAuthor(String authorId, String petriNet, Pageable pageable);

    Case resolveUserRef(Case useCase);

    CreateCaseEventOutcome createCase(CreateCaseParams createCaseParams);

    DeleteCaseEventOutcome deleteCase(DeleteCaseParams deleteCaseParams);

    DeleteCaseEventOutcome deleteSubtreeRootedAt(String caseId);

    void deleteInstancesOfPetriNet(PetriNet net);

    void updateMarking(Case useCase);

    Page<Case> search(Predicate predicate, Pageable pageable);

    Page<Case> searchAll(Predicate predicate);

    long count(Predicate predicate);

    Case searchOne(Predicate predicate);

    Map<String, I18nString> listToMap(List<Case> cases);

    void removeTasksFromCase(List<Task> tasks, String caseId);

    void removeTasksFromCase(List<Task> tasks, Case useCase);

    Case decrypt(Case useCase);

    Page<Case> findAllByUri(String uri, Pageable pageable);
}