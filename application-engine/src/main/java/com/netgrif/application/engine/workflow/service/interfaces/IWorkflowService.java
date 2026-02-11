package com.netgrif.application.engine.workflow.service.interfaces;

import com.netgrif.application.engine.objects.petrinet.domain.I18nString;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.objects.workflow.domain.Task;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.caseoutcomes.CreateCaseEventOutcome;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.caseoutcomes.DeleteCaseEventOutcome;
import com.netgrif.application.engine.workflow.params.CreateCaseParams;
import com.netgrif.application.engine.workflow.params.DeleteCaseParams;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public interface IWorkflowService {

    Case save(Case useCase);

    Case findOne(String caseId);

    Case findOneNoNet(String caseId);

    List<Case> findAllById(List<String> ids);

    Page<Case> getAll(Pageable pageable);

    Case resolveActorRef(Case useCase, boolean canSaveUseCase);

    CreateCaseEventOutcome createCase(CreateCaseParams createCaseParams);

    Page<Case> findAllByAuthor(String authorId, String petriNet, Pageable pageable);

    DeleteCaseEventOutcome deleteCase(DeleteCaseParams deleteCaseParams);

    DeleteCaseEventOutcome deleteSubtreeRootedAt(String caseId);

    void deleteInstancesOfPetriNet(PetriNet net);

    void deleteInstancesOfPetriNet(PetriNet net, boolean force);

    void updateMarking(Case useCase);

    Page<Case> searchAll(Predicate predicate);

    Case searchOne(Predicate predicate);

    Map<String, I18nString> listToMap(List<Case> cases);

    Page<Case> search(Map<String, Object> request, Pageable pageable, Locale locale);

    long count(Map<String, Object> request, Locale locale);

    boolean removeTasksFromCase(List<Task> tasks, String caseId);

    boolean removeTasksFromCase(List<Task> tasks, Case useCase);

    Page<Case> search(Predicate predicate, Pageable pageable);

    void setPetriNet(Case useCase);
}
