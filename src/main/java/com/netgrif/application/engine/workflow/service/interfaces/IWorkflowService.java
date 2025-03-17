package com.netgrif.application.engine.workflow.service.interfaces;

import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.Process;
import com.netgrif.application.engine.workflow.domain.Case;
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

    CreateCaseEventOutcome createCase(String netId, String title, String color, Identity user, Locale locale, Map<String, String> params);

    CreateCaseEventOutcome createCase(String netId, String title, String color, Identity user, Locale locale);

    CreateCaseEventOutcome createCase(String netId, String title, String color, Identity user, Map<String, String> params);

    CreateCaseEventOutcome createCase(String netId, String title, String color, Identity user);

    CreateCaseEventOutcome createCaseByIdentifier(String identifier, String title, String color, Identity user, Map<String, String> params);

    CreateCaseEventOutcome createCaseByIdentifier(String identifier, String title, String color, Identity user);

    CreateCaseEventOutcome createCaseByIdentifier(String identifier, String title, String color, Identity user, Locale locale, Map<String, String> params);

    CreateCaseEventOutcome createCaseByIdentifier(String identifier, String title, String color, Identity user, Locale locale);

    Page<Case> findAllByAuthor(String authorId, String petriNet, Pageable pageable);

    DeleteCaseEventOutcome deleteCase(String caseId, Map<String, String> params);

    DeleteCaseEventOutcome deleteCase(String caseId);

    DeleteCaseEventOutcome deleteSubtreeRootedAt(String caseId);

    DeleteCaseEventOutcome deleteCase(Case useCase, Map<String, String> params);

    DeleteCaseEventOutcome deleteCase(Case useCase);

    void deleteInstancesOfPetriNet(Process net);

    void updateMarking(Case useCase);

    Page<Case> search(Predicate predicate, Pageable pageable);

    Page<Case> searchAll(Predicate predicate);

    long count(Predicate predicate);

    Case searchOne(Predicate predicate);

    Map<String, I18nString> listToMap(List<Case> cases);

//    TODO: release/8.0.0
//    void removeTasksFromCase(List<Task> tasks, String caseId);
//    void removeTasksFromCase(List<Task> tasks, Case useCase);

    Case decrypt(Case useCase);

    Page<Case> findAllByUri(String uri, Pageable pageable);
}