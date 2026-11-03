package com.netgrif.application.engine.workflow.service.interfaces;

import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
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

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public interface IWorkflowService {

    Case save(Case useCase);

    Case findOne(String caseId);

    Case findOneNoNet(String caseId);

    List<Case> findAllById(List<String> ids);

    /**
     * Loads a PARTIAL, READ-ONLY case containing all static fields and only the requested {@code dataSet} entries
     * (plus automatically required ones: immediate data fields and fields referenced by arcs).
     * The result must never be saved; {@link #save(Case)} throws {@link IllegalStateException} for it.
     *
     * @param caseId                 composite case id
     * @param dataFieldIds           ids of the data fields to load
     * @param includeImmediateFields whether to automatically resolve and load immediate data fields
     * @param setPetriNet            whether to resolve and initialize the case's PetriNet model
     * @return the partially loaded case
     */
    Case findOneProjected(String caseId, Collection<String> dataFieldIds, boolean includeImmediateFields, boolean setPetriNet);

    /**
     * Loads a PARTIAL, READ-ONLY case containing all static fields and only the requested {@code dataSet} entries
     * (plus automatically required ones: immediate data fields and fields referenced by arcs).
     * The result must never be saved; {@link #save(Case)} throws {@link IllegalStateException} for it.
     *
     * @param caseId       composite case id
     * @param dataFieldIds ids of the data fields to load
     * @return the partially loaded case
     */
    Case findOneProjected(String caseId, Collection<String> dataFieldIds);

    /**
     * Loads a PARTIAL, READ-ONLY case; the requested data field ids are derived from the case's PetriNet by the
     * resolver (e.g. the data of a transition). Automatically required keys are added. Must never be saved with
     * {@link #save(Case)}; use {@link #savePartial(Case)}.
     *
     * @param caseId            composite case id
     * @param dataFieldResolver maps the case's PetriNet to the ids of the data fields to load
     * @return the partially loaded case
     */
    Case findOneProjected(String caseId, java.util.function.Function<PetriNet, Collection<String>> dataFieldResolver);

    /**
     * Same as {@link #findOneProjected(String, Collection, boolean, boolean)} for many ids.
     *
     * @param caseIds                composite case ids
     * @param dataFieldIds           ids of the data fields to load
     * @param includeImmediateFields whether to automatically resolve and load immediate data fields
     * @param setPetriNet            whether to resolve and initialize the case's PetriNet model
     * @return partially loaded cases
     */
    List<Case> findAllByIdProjected(List<String> caseIds, Collection<String> dataFieldIds, boolean includeImmediateFields, boolean setPetriNet);

    /**
     * Same as {@link #findOneProjected(String, Collection)} for many ids. The result is PARTIAL and READ-ONLY, preserves
     * the order of the input ids and skips unknown ids.
     *
     * @param caseIds      composite case ids
     * @param dataFieldIds ids of the data fields to load
     * @return partially loaded cases
     */
    List<Case> findAllByIdProjected(List<String> caseIds, Collection<String> dataFieldIds);

    /**
     * Loads a page of PARTIAL, READ-ONLY cases with only the requested {@code dataSet} entries.
     *
     * @param pageable               paging and sorting
     * @param dataFieldIds           ids of the data fields to load
     * @param includeImmediateFields whether to automatically resolve and load immediate data fields
     * @param setPetriNet            whether to resolve and initialize the case's PetriNet model
     * @return page of partially loaded cases
     */
    Page<Case> getAllProjected(Pageable pageable, Collection<String> dataFieldIds, boolean includeImmediateFields, boolean setPetriNet);

    /**
     * Loads a page of PARTIAL, READ-ONLY cases with only the requested {@code dataSet} entries (plus automatically
     * required ones). Paging and sorting are done by Mongo. Results must never be saved with {@link #save(Case)};
     * use {@link #savePartial(Case)}.
     *
     * @param pageable     paging and sorting
     * @param dataFieldIds ids of the data fields to load
     * @return page of partially loaded cases
     */
    Page<Case> getAllProjected(Pageable pageable, Collection<String> dataFieldIds);

    /**
     * Persists a PARTIALLY LOADED case with Mongo {@code $set} operations: only static fields and the loaded
     * {@code dataSet} entries are written, unloaded entries stay untouched. The case is re-indexed from a fresh
     * full read so that Elasticsearch never receives a partial document.
     *
     * @param partialCase a case obtained from a projected read
     * @return the same (partial) instance
     * @throws IllegalArgumentException if the case is not partially loaded or does not exist anymore
     */
    Case savePartial(Case partialCase);

    Page<Case> getAll(Pageable pageable);

    Case resolveActorRef(Case useCase, boolean canSaveUseCase);

    void updateCaseFromDb(Case useCase);

    CreateCaseEventOutcome createCase(CreateCaseParams createCaseParams);

    Page<Case> findAllByAuthor(String authorId, String petriNet, Pageable pageable);

    DeleteCaseEventOutcome deleteCase(DeleteCaseParams deleteCaseParams);

    DeleteCaseEventOutcome deleteSubtreeRootedAt(String caseId);

    void deleteInstancesOfPetriNet(PetriNet net);

    void deleteInstancesOfPetriNet(PetriNet net, boolean force);

    void updateMarking(Case useCase);

    Page<Case> searchAll(Predicate predicate);

    Case searchOne(Predicate predicate);

    /**
     * Loads a PARTIAL, READ-ONLY case matching the given predicate with only the requested {@code dataSet} entries
     * (plus automatically required ones). Must never be saved with {@code save}; use {@code savePartial}.
     *
     * @param predicate    Querydsl predicate
     * @param dataFieldIds ids of the data fields to load
     * @return the partially loaded case, or {@code null} if not found
     */
    Case searchOneProjected(Predicate predicate, Collection<String> dataFieldIds);

    /**
     * Loads a PARTIAL, READ-ONLY case matching the given predicate; data field ids are derived from the case's
     * PetriNet via resolver. Must never be saved with {@code save}; use {@code savePartial}.
     *
     * @param predicate         Querydsl predicate
     * @param dataFieldResolver maps the case's PetriNet to the ids of the data fields to load
     * @return the partially loaded case, or {@code null} if not found
     */
    Case searchOneProjected(Predicate predicate, java.util.function.Function<PetriNet, Collection<String>> dataFieldResolver);

    /**
     * Loads all PARTIAL, READ-ONLY cases matching the given predicate as a full page. Must never be saved with
     * {@code save}; use {@code savePartial}.
     *
     * @param predicate    Querydsl predicate
     * @param dataFieldIds ids of the data fields to load
     * @return full page of partially loaded cases
     */
    Page<Case> searchAllProjected(Predicate predicate, Collection<String> dataFieldIds);

    /**
     * Loads a page of PARTIAL, READ-ONLY cases matching the given predicate with only the requested {@code dataSet}
     * entries (plus automatically required ones). Paging and sorting are applied by Mongo. Must never be saved with
     * {@code save}; use {@code savePartial}.
     *
     * @param predicate    Querydsl predicate
     * @param pageable     paging and sorting
     * @param dataFieldIds ids of the data fields to load
     * @return page of partially loaded cases
     */
    Page<Case> searchProjected(Predicate predicate, Pageable pageable, Collection<String> dataFieldIds);

    Map<String, I18nString> listToMap(List<Case> cases);

    Page<Case> search(Map<String, Object> request, Pageable pageable, LoggedUser user, Locale locale);

    long count(Map<String, Object> request, LoggedUser user, Locale locale);

    boolean removeTasksFromCase(List<Task> tasks, String caseId);

    boolean removeTasksFromCase(List<Task> tasks, Case useCase);

    Page<Case> search(Predicate predicate, Pageable pageable);

    void setPetriNet(Case useCase);
}
