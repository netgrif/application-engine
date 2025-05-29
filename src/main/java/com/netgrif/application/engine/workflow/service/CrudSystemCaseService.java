package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseSearchService;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.manager.service.interfaces.ISessionManagerService;
import com.netgrif.application.engine.petrinet.domain.dataset.CaseField;
import com.netgrif.application.engine.petrinet.domain.dataset.TextField;
import com.netgrif.application.engine.transaction.NaeTransaction;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.CaseParams;
import com.netgrif.application.engine.workflow.domain.QCase;
import com.netgrif.application.engine.workflow.domain.SystemCase;
import com.netgrif.application.engine.workflow.domain.params.CreateCaseParams;
import com.netgrif.application.engine.workflow.domain.params.SetDataParams;
import com.netgrif.application.engine.workflow.service.interfaces.ICrudSystemCaseService;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import com.netgrif.application.engine.workflow.service.throwable.CaseAlreadyExistsException;
import groovy.lang.Closure;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public abstract class CrudSystemCaseService<T extends SystemCase> implements ICrudSystemCaseService<T> {

    protected final ISessionManagerService sessionManagerService;
    protected final IDataService dataService;
    protected final IWorkflowService workflowService;
    protected final SystemCaseFactoryRegistry systemCaseFactory;
    protected final IElasticCaseSearchService elasticCaseSearchService;
    protected final MongoTransactionManager transactionManager;
    /**
     * todo javadoc
     * */
    protected final Set<String> forbiddenKeywords;

    protected CrudSystemCaseService(ISessionManagerService sessionManagerService, IDataService dataService,
                                    IWorkflowService workflowService, SystemCaseFactoryRegistry systemCaseFactory,
                                    IElasticCaseSearchService elasticCaseSearchService, MongoTransactionManager transactionManager) {
        this.sessionManagerService = sessionManagerService;
        this.dataService = dataService;
        this.workflowService = workflowService;
        this.systemCaseFactory = systemCaseFactory;
        this.elasticCaseSearchService = elasticCaseSearchService;
        this.transactionManager = transactionManager;
        this.forbiddenKeywords = ConcurrentHashMap.newKeySet();
    }

    /**
     * Returns the unique process identifier for this system case service.
     *
     * @return Process identifier string
     */
    protected abstract String getProcessIdentifier();

    /**
     * Generates a search query to find existing cases with the same unique properties.
     *
     * @param params Parameters containing the unique properties to search for
     * @return Search query string
     */
    protected abstract String isUniqueQuery(@NotNull CaseParams params);

    /**
     * Validates and fixes parameters for case creation.
     *
     * @param params Parameters to validate
     * @throws IllegalArgumentException if parameters are invalid
     */
    protected abstract void validateAndFixCreateParams(@NotNull CaseParams params) throws IllegalArgumentException;

    /**
     * Validates and fixes parameters for case update.
     *
     * @param params Parameters to validate
     * @throws IllegalArgumentException if parameters are invalid
     */
    protected abstract void validateAndFixUpdateParams(@NotNull CaseParams params) throws IllegalArgumentException;

    /**
     * Performs any necessary actions after system case creation.
     *
     * @param systemCase The newly created system case
     */
    protected void postCreationActions(T systemCase) {}

    /**
     * Performs any necessary actions after system case update.
     *
     * @param systemCase The updated system case
     */
    protected void postUpdateActions(T systemCase) {}

    /**
     * todo javadoc
     * */
    @Override
    public boolean registerForbiddenKeywords(Set<String> keywords) {
        // todo: release/8.0.0 authorisation
        if (keywords == null) {
            return false;
        }
        boolean areRegistered = this.forbiddenKeywords.addAll(keywords);
        if (areRegistered) {
           log.debug("New forbidden keywords for process [{}] were registered: {}", getProcessIdentifier(), keywords);
        }
        return areRegistered;
    }

    /**
     * todo javadoc
     * */
    @Override
    public boolean removeForbiddenKeywords(Set<String> keywords) {
        // todo: release/8.0.0 authorisation
        if (keywords == null) {
            return false;
        }
        boolean areRemoved = this.forbiddenKeywords.removeAll(keywords);
        if (areRemoved) {
            log.debug("Some forbidden keywords for process [{}] were removed: {}", getProcessIdentifier(), keywords);
        }
        return areRemoved;
    }

    @Override
    public void clearForbiddenKeywords() {
        int countBefore = this.forbiddenKeywords.size();
        this.forbiddenKeywords.clear();
        if (countBefore > 0) {
            log.debug("All ({}) forbidden keywords for process [{}] were removed", countBefore, getProcessIdentifier());
        }
    }

    /**
     * todo javadoc
     * Creates identity based on params. Password is not encoded. User is not created.
     *
     * @param params Parameters, that are used to create the identity. At least username must be provided.
     *
     * @return Created identity. Cannot be null
     *
     * @throws IllegalArgumentException if the input parameters are invalid
     * */
    @Override
    @SuppressWarnings("unchecked")
    public T create(CaseParams params) throws IllegalArgumentException, IllegalStateException, CaseAlreadyExistsException {
        if (params == null) {
            throw new IllegalArgumentException("Please provide input parameters.");
        }
        if (!params.targetProcessIdentifier().equals(getProcessIdentifier())) {
            throw new IllegalArgumentException("Wrong type of parameters was provided.");
        }
        validateAndFixCreateParams(params);

        Optional<T> existingCaseOpt = findOneByQuery(isUniqueQuery(params));
        if (existingCaseOpt.isPresent()) {
            throw new CaseAlreadyExistsException(String.format("Such instance with id [%s] of process [%s] already exists.",
                    existingCaseOpt.get().getStringId(), getProcessIdentifier()));
        }

        final String activeActorId = sessionManagerService.getActiveActorId();

        T systemObject;
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            NaeTransaction transaction = NaeTransaction.builder()
                    .transactionManager(transactionManager)
                    .event(new Closure<T>(null) {
                        @Override
                        public T call() {
                            return doCreate(params, activeActorId);
                        }
                    })
                    .build();
            transaction.begin();
            systemObject = (T) transaction.getResultOfEvent();
        } else {
            systemObject = doCreate(params, activeActorId);
        }

        postCreationActions(systemObject);
        return systemObject;
    }

    @SuppressWarnings("unchecked")
    protected T doCreate(CaseParams params, String activeActorId) {
        CreateCaseParams createCaseParams = CreateCaseParams.with()
                .processIdentifier(getProcessIdentifier())
                .authorId(activeActorId)
                .build();
        Case systemCase = workflowService.createCase(createCaseParams).getCase();
        systemCase = dataService.setData(new SetDataParams(systemCase, params.toDataSet(),
                activeActorId)).getCase();
        if (params.getProperties() != null) {
            systemCase.setProperties(params.getProperties());
            systemCase = workflowService.save(systemCase);
        }
        T systemObject = (T) systemCaseFactory.fromCase(systemCase);

        if (systemObject == null) {
            throw new IllegalStateException(String.format("Unexpected: No wrapper class factory is registered for process [%s]",
                    getProcessIdentifier()));
        }
        log.debug("System case [{}][{}] was created by actor [{}].", systemObject.getCase(), getProcessIdentifier(),
                activeActorId);
        return systemObject;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T update(T systemObject, CaseParams params) throws IllegalArgumentException, IllegalStateException {
        if (systemObject == null) {
            throw new IllegalArgumentException("Please provide case to be updated");
        }
        if (params == null) {
            throw new IllegalArgumentException("Please provide input parameters.");
        }
        if (!params.targetProcessIdentifier().equals(getProcessIdentifier())) {
            throw new IllegalArgumentException("Wrong type of parameters was provided.");
        }

        validateAndFixUpdateParams(params);

        final String activeActorId = sessionManagerService.getActiveActorId();

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            T finalSystemObject = systemObject;
            NaeTransaction transaction = NaeTransaction.builder()
                    .transactionManager(transactionManager)
                    .event(new Closure<T>(null) {
                        @Override
                        public T call() {
                            return doUpdate(finalSystemObject, params, activeActorId);
                        }
                    })
                    .build();
            transaction.begin();
            systemObject = (T) transaction.getResultOfEvent();
        } else {
            systemObject = doUpdate(systemObject, params, activeActorId);
        }

        postUpdateActions(systemObject);

        return systemObject;
    }

    @SuppressWarnings("unchecked")
    protected T doUpdate(SystemCase systemObject, CaseParams params, String activeActorId) {
        Case systemCase = dataService.setData(new SetDataParams(systemObject.getCase(), params.toDataSet(),
                activeActorId)).getCase();

        if (params.getProperties() != null) {
            systemCase.setProperties(params.getProperties());
            systemCase = workflowService.save(systemCase);
        }

        systemObject = systemCaseFactory.fromCase(systemCase);

        if (systemObject == null) {
            throw new IllegalStateException(String.format("Unexpected: No wrapper class factory is registered for process [%s]",
                    getProcessIdentifier()));
        }
        log.debug("System case [{}][{}] was updated by actor [{}].", systemObject, getProcessIdentifier(), activeActorId);
        return (T) systemObject;
    }

    /**
     * todo javadoc
     * Finds identity by id.
     *
     * @param id id of the identity. If provided null, empty optional is returned
     *
     * @return If the identity exists, it's returned. If not, an empty optional is returned
     */
    @Override
    @SuppressWarnings("unchecked")
    public Optional<T> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        try {
            final Case systemCase = workflowService.findOne(id);
            if (!systemCase.getProcessIdentifier().equals(getProcessIdentifier())) {
                return Optional.empty();
            }
            return (Optional<T>) Optional.ofNullable(systemCaseFactory.fromCase(systemCase));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    @Override
    public boolean existsById(String id) {
        if (id == null) {
            return false;
        }
        // todo: release/8.0.0 edge case: can return true and findById will return empty optional
        return workflowService.count(QCase.case$.processIdentifier.eq(getProcessIdentifier())
                .and(QCase.case$.id.eq(new ObjectId(id)))) > 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> findAll() {
        List<Case> result = workflowService.searchAll(QCase.case$.processIdentifier.eq(getProcessIdentifier())).getContent();
        return (List<T>) result.stream()
                .map(systemCaseFactory::fromCase)
                .collect(Collectors.toList());
    }

    // todo: release/8.0.0 also removal method?

    protected boolean isForbidden(String keyword) {
        return this.forbiddenKeywords.contains(keyword);
    }

    protected boolean isTextFieldOrValueEmpty(TextField field) {
        return field == null || isTextFieldValueEmpty(field);
    }

    protected boolean isTextFieldValueEmpty(TextField field) {
        return field.getRawValue() == null || field.getRawValue().trim().isEmpty();
    }

    protected boolean isCaseFieldOrValueEmpty(CaseField field) {
        return field == null || isCaseFieldValueEmpty(field);
    }

    protected boolean isCaseFieldValueEmpty(CaseField field) {
        return field.getRawValue() == null || field.getRawValue().isEmpty();
    }

    protected List<? extends Case> findAllByQuery(String query) {
        Set<String> singletonQuerySet = query != null ? Set.of(query) : Set.of();
        CaseSearchRequest request = CaseSearchRequest.builder()
                .query(buildQuery(singletonQuerySet))
                .build();

        Page<Case> pageResult = elasticCaseSearchService.search(List.of(request), sessionManagerService.getActiveActorId(),
                Pageable.unpaged(), Locale.getDefault(), false, null);

        return pageResult.getContent();
    }

    @SuppressWarnings("unchecked")
    protected Optional<T> findOneByQuery(String query) {
        if (query == null) {
            return Optional.empty();
        }
        CaseSearchRequest request = CaseSearchRequest.builder()
                .query(buildQuery(Set.of(query)))
                .build();
        Page<Case> resultAsPage = elasticCaseSearchService.search(List.of(request), sessionManagerService.getActiveActorId(),
                PageRequest.of(0, 1), Locale.getDefault(), false, null);
        if (resultAsPage.hasContent()) {
            return (Optional<T>) Optional.ofNullable(systemCaseFactory.fromCase(resultAsPage.getContent().get(0)));
        }
        return Optional.empty();
    }

    protected long countByQuery(String query) {
        CaseSearchRequest request = CaseSearchRequest.builder()
                .query(buildQuery(Set.of(query)))
                .build();
        return elasticCaseSearchService.count(List.of(request), sessionManagerService.getActiveActorId(),
                Locale.getDefault(), false, null);
    }

    protected String buildQuery(Set<String> andQueries) {
        StringBuilder queryBuilder = new StringBuilder("processIdentifier:").append(getProcessIdentifier());
        for (String query : andQueries) {
            queryBuilder.append(" AND ");
            queryBuilder.append(query);
        }
        return queryBuilder.toString();
    }

    protected static String fulltextFieldQuery(String fieldId, String fieldValue) {
        return String.format("dataSet.%s.fulltextValue:\"%s\"", fieldId, fieldValue);
    }
}
