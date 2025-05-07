package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseSearchService;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.manager.service.interfaces.ISessionManagerService;
import com.netgrif.application.engine.petrinet.domain.dataset.CaseField;
import com.netgrif.application.engine.petrinet.domain.dataset.TextField;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.CaseParams;
import com.netgrif.application.engine.workflow.domain.QCase;
import com.netgrif.application.engine.workflow.domain.SystemCase;
import com.netgrif.application.engine.workflow.service.interfaces.ICrudSystemCaseService;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Slf4j
public abstract class CrudSystemCaseService<T extends SystemCase> implements ICrudSystemCaseService<T> {

    protected final ISessionManagerService sessionManagerService;
    protected final IDataService dataService;
    protected final IWorkflowService workflowService;
    protected final SystemCaseFactoryRegistry systemCaseFactory;
    protected final IElasticCaseSearchService elasticCaseSearchService;
    /**
     * todo javadoc
     * */
    protected final Set<String> forbiddenKeywords;

    protected CrudSystemCaseService(ISessionManagerService sessionManagerService, IDataService dataService,
                                    IWorkflowService workflowService, SystemCaseFactoryRegistry systemCaseFactory,
                                    IElasticCaseSearchService elasticCaseSearchService) {
        this.sessionManagerService = sessionManagerService;
        this.dataService = dataService;
        this.workflowService = workflowService;
        this.systemCaseFactory = systemCaseFactory;
        this.elasticCaseSearchService = elasticCaseSearchService;
        this.forbiddenKeywords = ConcurrentHashMap.newKeySet();
    }

    // todo javadoc on abstract methods
    protected abstract String getProcessIdentifier();
    protected abstract String isUniqueQuery(CaseParams params);
    protected abstract void validateAndFixCreateParams(CaseParams params) throws IllegalArgumentException;
    protected abstract void validateAndFixUpdateParams(CaseParams params) throws IllegalArgumentException;

    protected void postCreationActions(T systemCase) {}
    protected void postUpdateActions(T systemCase) {}

    /**
     * todo javadoc
     * */
    @Override
    public void registerForbiddenKeywords(Set<String> keywords) {
        // todo 2058 authorisation
        if (keywords == null) {
            return;
        }
        this.forbiddenKeywords.addAll(keywords);
    }

    /**
     * todo javadoc
     * */
    @Override
    public void removeFromForbiddenKeywords(Set<String> keywords) {
        // todo 2058 authorisation
        if (keywords == null) {
            return;
        }
        this.forbiddenKeywords.removeAll(keywords);
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
    @Transactional
    public T create(CaseParams params) throws IllegalArgumentException, IllegalStateException {
        validateAndFixCreateParams(params);

        Optional<T> existingCaseOpt = findOneByQuery(isUniqueQuery(params));
        if (existingCaseOpt.isPresent()) {
            log.warn("New instance of process [{}] wasn't created. Such instance with id [{}] already exists.",
                    getProcessIdentifier(), existingCaseOpt.get().getStringId());
            return existingCaseOpt.get();
        }

        final String activeActorId = sessionManagerService.getActiveActorId();
        T systemObject = doCreate(params, activeActorId);

        postCreationActions(systemObject);

        return systemObject;
    }

    @SuppressWarnings("unchecked")
    protected T doCreate(CaseParams params, String activeActorId) {
        Case systemCase = workflowService.createCaseByIdentifier(getProcessIdentifier(), null, "",
                activeActorId).getCase();
        T systemObject = (T) systemCaseFactory.fromCase(dataService.setData(systemCase, params.toDataSet(),
                activeActorId).getCase());

        if (systemObject == null) {
            throw new IllegalStateException(String.format("Unexpected: No wrapper class factory is registered for process [%s]",
                    getProcessIdentifier()));
        }
        log.debug("System case [{}][{}] was created by actor [{}].", systemObject.getCase(), getProcessIdentifier(),
                activeActorId);
        return systemObject;
    }

    @Override
    @Transactional
    public T update(T systemObject, CaseParams params) throws IllegalArgumentException, IllegalStateException {
        if (systemObject == null) {
            throw new IllegalArgumentException("Please provide case to be updated");
        }

        validateAndFixUpdateParams(params);

        final String activeActorId = sessionManagerService.getActiveActorId();
        systemObject = doUpdate(systemObject, params, activeActorId);

        postUpdateActions(systemObject);

        return systemObject;
    }

    @SuppressWarnings("unchecked")
    protected T doUpdate(SystemCase systemObject, CaseParams params, String activeActorId) {
        systemObject = systemCaseFactory.fromCase(dataService.setData(systemObject.getCase(), params.toDataSet(),
                activeActorId).getCase());

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

        List<Case> result = new ArrayList<>();

        long identityCount = elasticCaseSearchService.count(List.of(request),sessionManagerService.getLoggedIdentity(),
                Locale.getDefault(), false, null);
        long pageCount = (identityCount / 100) + 1;
        LongStream.range(0, pageCount).forEach(pageIdx -> {
            Page<Case> pageResult = elasticCaseSearchService.search(List.of(request), sessionManagerService.getLoggedIdentity(),
                    PageRequest.of((int) pageIdx, 100), Locale.getDefault(), false, null);
            result.addAll(pageResult.getContent());
        });

        return result;
    }

    @SuppressWarnings("unchecked")
    protected Optional<T> findOneByQuery(String query) {
        if (query == null) {
            return Optional.empty();
        }
        CaseSearchRequest request = CaseSearchRequest.builder()
                .query(buildQuery(Set.of(query)))
                .build();
        Page<Case> resultAsPage = elasticCaseSearchService.search(List.of(request), sessionManagerService.getLoggedIdentity(),
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
        return elasticCaseSearchService.count(List.of(request), sessionManagerService.getLoggedIdentity(), Locale.getDefault(),
                false, null);
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
