package com.netgrif.application.engine.workflow.service.interfaces;

import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.caseoutcomes.CreateCaseEventOutcome;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.caseoutcomes.DeleteCaseEventOutcome;
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

    Case resolveUserRef(Case useCase);

    CreateCaseEventOutcome createCase(String netId, String title, String color, LoggedUser user, Locale locale);

    CreateCaseEventOutcome createCase(String netId, String title, String color, LoggedUser user);

    CreateCaseEventOutcome createCaseByIdentifier(String identifier, String title, String color, LoggedUser user);

    CreateCaseEventOutcome createCaseByIdentifier(String identifier, String title, String color, LoggedUser user, Locale locale);

    Page<Case> findAllByAuthor(String authorId, String petriNet, Pageable pageable);

    DeleteCaseEventOutcome deleteCase(String caseId);

    DeleteCaseEventOutcome deleteSubtreeRootedAt(String caseId);

    DeleteCaseEventOutcome deleteCase(Case useCase);

    void deleteInstancesOfPetriNet(PetriNet net);

    void updateMarking(Case useCase);

    Page<Case> searchAll(Predicate predicate);

    Case searchOne(Predicate predicate);

    Map<String, I18nString> listToMap(List<Case> cases);

    @Deprecated
    List<Field> getData(String caseId);

    Page<Case> search(Map<String, Object> request, Pageable pageable, LoggedUser user, Locale locale);

    long count(Map<String, Object> request, LoggedUser user, Locale locale);

//    List<Case> getCaseFieldChoices(Pageable pageable, String caseId, String fieldId);

    boolean removeTasksFromCase(List<Task> tasks, String caseId);

    boolean removeTasksFromCase(List<Task> tasks, Case useCase);

    Case decrypt(Case useCase);

    Page<Case> findAllByUri(String uri, Pageable pageable);

    Page<Case> search(Predicate predicate, Pageable pageable);
}