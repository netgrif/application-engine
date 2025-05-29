package com.netgrif.application.engine.workflow.service;

import com.google.common.collect.Ordering;
import com.netgrif.application.engine.authorization.service.interfaces.IRoleService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.history.domain.caseevents.CreateCaseEventLog;
import com.netgrif.application.engine.history.domain.caseevents.DeleteCaseEventLog;
import com.netgrif.application.engine.history.service.IHistoryService;
import com.netgrif.application.engine.importer.model.CaseEventType;
import com.netgrif.application.engine.manager.service.interfaces.ISessionManagerService;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.Process;
import com.netgrif.application.engine.petrinet.domain.Transition;
import com.netgrif.application.engine.petrinet.domain.arcs.Arc;
import com.netgrif.application.engine.petrinet.domain.arcs.ReferenceType;
import com.netgrif.application.engine.petrinet.domain.dataset.*;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ActionRunner;
import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.rules.domain.facts.CaseCreatedFact;
import com.netgrif.application.engine.rules.service.interfaces.IRuleEngine;
import com.netgrif.application.engine.security.service.EncryptionService;
import com.netgrif.application.engine.transaction.NaeTransaction;
import com.netgrif.application.engine.transaction.configuration.NaeTransactionProperties;
import com.netgrif.application.engine.utils.FullPageRequest;
import com.netgrif.application.engine.workflow.domain.*;
import com.netgrif.application.engine.workflow.domain.outcomes.CreateTasksOutcome;
import com.netgrif.application.engine.workflow.domain.outcomes.eventoutcomes.EventOutcome;
import com.netgrif.application.engine.workflow.domain.outcomes.eventoutcomes.caseoutcomes.CreateCaseEventOutcome;
import com.netgrif.application.engine.workflow.domain.outcomes.eventoutcomes.caseoutcomes.DeleteCaseEventOutcome;
import com.netgrif.application.engine.workflow.domain.params.CreateCaseParams;
import com.netgrif.application.engine.workflow.domain.params.DeleteCaseParams;
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository;
import com.netgrif.application.engine.workflow.service.initializer.DataSetInitializer;
import com.netgrif.application.engine.workflow.service.interfaces.IEventService;
import com.netgrif.application.engine.workflow.service.interfaces.IInitValueExpressionEvaluator;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import com.querydsl.core.types.Predicate;
import groovy.lang.Closure;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Slf4j
@Service
public class WorkflowService implements IWorkflowService {

    @Autowired
    protected CaseRepository repository;

    @Autowired
    protected IPetriNetService petriNetService;

    @Autowired
    protected IRoleService roleService;

    @Autowired
    protected ITaskService taskService;

    @Autowired
    protected EncryptionService encryptionService;

    @Autowired
    protected IRuleEngine ruleEngine;

    @Autowired
    protected ActionRunner actionsRunner;

    @Autowired
    protected IInitValueExpressionEvaluator initValueExpressionEvaluator;

    @Lazy
    @Autowired
    private IEventService eventService;

    @Autowired
    private IHistoryService historyService;

    protected IElasticCaseService elasticCaseService;

    @Autowired
    private DataSetInitializer dataSetInitializer;

    @Autowired
    private ISessionManagerService sessionManagerService;

    @Autowired
    private MongoTransactionManager transactionManager;

    @Autowired
    private NaeTransactionProperties transactionProperties;

    @Autowired
    public void setElasticCaseService(IElasticCaseService elasticCaseService) {
        this.elasticCaseService = elasticCaseService;
    }

    @Override
    public Case save(Case useCase) {
        if (useCase.getProcess() == null) {
            setPetriNet(useCase);
        }
        encryptDataSet(useCase);
        useCase = repository.save(useCase);

        return useCase;
    }

    @Override
    public Case findOne(String caseId) {
        Case useCase = findOneNoNet(caseId);
        initialize(useCase);
        return useCase;
    }

    @Override
    public Case findOneNoNet(String caseId) {
        Optional<Case> caseOptional = repository.findById(caseId);
        return caseOptional.orElseThrow(() -> new IllegalArgumentException("Could not find Case with id [" + caseId + "]"));
    }

    private void initialize(Case useCase) {
        setPetriNet(useCase);
        decryptDataSet(useCase);
        useCase.resolveImmediateDataFields();
    }

    @Override
    public List<Case> findAllById(List<String> ids) {
        // TODO: release/8.0.0 check if repository method works, expects ObjectId
        // TODO: check merge
        // setImmediateDataFieldsReadOnly(caze) ?
        return repository.findAllByIdIn(ids).stream()
                .filter(Objects::nonNull)
                .sorted(Ordering.explicit(ids).onResultOf(Case::getStringId))
                .peek(this::initialize)
                .collect(Collectors.toList());
    }

    @Override
    public Page<Case> getAll(Pageable pageable) {
        Page<Case> page = repository.findAll(pageable);
        page.getContent().forEach(this::initialize);
        return page;
    }

    @Override
    public Page<Case> findAllByUri(String uri, Pageable pageable) {
        Page<Case> page = repository.findAllByUriNodeId(uri, pageable);
        page.getContent().forEach(this::initialize);
        return page;
    }

    @Override
    public Page<Case> search(Predicate predicate, Pageable pageable) {
        Page<Case> page = repository.findAll(predicate, pageable);
        // TODO: release/8.0.0: decrypt data set was not called before
        page.getContent().forEach(this::initialize);
        return page;
    }

    /**
     * Create {@link Case} object as {@link CreateCaseEventOutcome} by provided parameters as {@link CreateCaseParams}.
     * Created object is saved into database along with the tasks. Any {@link Task}, that should be executed at the
     * object creation is executed (auto-trigger tasks).
     *
     * @param createCaseParams parameters for {@link Case} creation
     * <br>
     * <b>Required parameters:</b>
     * <ul>
     *     <li>petriNet or petriNetIdentifier or petriNetId</li>
     *     <li>loggedUser</li>
     * </ul>
     *
     * @return outcome with up to date {@link} Case object containing sub-outcomes as result of triggered events
     * */
    @Override
    public CreateCaseEventOutcome createCase(CreateCaseParams createCaseParams) {
        fillMissingAttributes(createCaseParams);

        if (createCaseParams.getIsTransactional() && !TransactionSynchronizationManager.isSynchronizationActive()) {
            NaeTransaction transaction = NaeTransaction.builder()
                    .transactionManager(transactionManager)
                    .event(new Closure<CreateCaseEventOutcome>(null) {
                        @Override
                        public CreateCaseEventOutcome call() {
                            return doCreateCase(createCaseParams);
                        }
                    })
                    .build();
            transaction.begin();
            return (CreateCaseEventOutcome) transaction.getResultOfEvent();
        } else {
            return doCreateCase(createCaseParams);
        }
    }

    private CreateCaseEventOutcome doCreateCase(CreateCaseParams createCaseParams) {
        Case useCase = createCaseObject(createCaseParams);
        CreateTasksOutcome createTasksOutcome = taskService.createAndSetTasksInCase(useCase);
        dataSetInitializer.populateDataSet(useCase, createCaseParams.getParams());

        Process process = createCaseParams.getProcess();
        roleService.resolveCaseRolesOnCase(useCase, useCase.getProcess().getCaseRolePermissions(), false);
        for (Task task : createTasksOutcome.getTasks()) {
            Transition transition = process.getTransition(task.getTransitionId());
            roleService.resolveCaseRolesOnTask(useCase, task, transition.getCaseRolePermissions(), false, true);
        }
        save(useCase); // must be after tasks creation for effectivity reasons

        CreateCaseEventOutcome outcome = new CreateCaseEventOutcome();

        // todo: release/8.0.0 pre actions should be run before the actual case creation? At this moment the case already exists in DB
        outcome.addOutcomes(eventService.runActions(process.getPreCreateActions(), null, Optional.empty(),
                createCaseParams.getParams()));

        // todo: release/8.0.0 should ruleEngine have useCase at this stage of execution?
        int rulesExecuted = ruleEngine.evaluateRules(useCase, new CaseCreatedFact(useCase.getStringId(), EventPhase.PRE));
        if (rulesExecuted > 0) {
            useCase = save(useCase);
        }

        historyService.save(new CreateCaseEventLog(useCase, EventPhase.PRE));

        if (createTasksOutcome.getAutoTriggerTask() != null) {
            taskService.executeTask(createTasksOutcome.getAutoTriggerTask(), useCase);
            useCase = findOne(useCase.getStringId());
        }

        // TODO release/8.0.0 resolving init of taskRefs is going to be done differently
        // resolveTaskRefs(useCase);

        List<EventOutcome> eventPostOutcomes = eventService.runActions(process.getPostCreateActions(), useCase, Optional.empty(),
                createCaseParams.getParams());
        if (!eventPostOutcomes.isEmpty()) {
            outcome.addOutcomes(eventPostOutcomes);
            useCase = findOne(useCase.getStringId());
        }

        rulesExecuted = ruleEngine.evaluateRules(useCase, new CaseCreatedFact(useCase.getStringId(), EventPhase.POST));
        if (rulesExecuted > 0) {
            useCase = save(useCase);
        }

        historyService.save(new CreateCaseEventLog(useCase, EventPhase.POST));

        log.info("[{}]: Case {} was created by actor {}", useCase.getStringId(), useCase.getTitle(), createCaseParams.getAuthorId());

        outcome.setCase(useCase);
        addMessageToOutcome(process, CaseEventType.CREATE, outcome);
        return outcome;
    }

    /**
     * Creates pure {@link Case} object without any {@link Task} object initialized.
     *
     * @param createCaseParams parameters for object creation
     *
     * @return created {@link Case} object
     * */
    private Case createCaseObject(CreateCaseParams createCaseParams) {
        Case useCase = new Case(createCaseParams.getProcess());
        useCase.setAuthorId(createCaseParams.getAuthorId());
        useCase.setCreationDate(LocalDateTime.now());
        useCase.setTitle(createCaseParams.getMakeTitle().apply(useCase));
        useCase.setUriNodeId(createCaseParams.getProcess().getUriNodeId());

        useCase.getProcess().initializeArcs();

        return useCase;
    }

    private void fillMissingAttributes(CreateCaseParams createCaseParams) throws IllegalArgumentException {
        if (createCaseParams.getIsTransactional() == null) {
            createCaseParams.setIsTransactional(transactionProperties.isCreateCaseTransactional());
        }
        if (createCaseParams.getProcess() == null) {
            Process process;
            if (createCaseParams.getProcessId() != null) {
                process = petriNetService.get(new ObjectId(createCaseParams.getProcessId())).clone();
            } else if (createCaseParams.getProcessIdentifier() != null) {
                Process originalProcess = petriNetService.getNewestVersionByIdentifier(createCaseParams.getProcessIdentifier());
                if (originalProcess == null) {
                    throw new IllegalArgumentException(String.format("Could not find the Process [%s] for the Case creation.",
                            createCaseParams.getProcessIdentifier()));
                }
                process = petriNetService.getNewestVersionByIdentifier(createCaseParams.getProcessIdentifier()).clone();
            } else {
                throw new IllegalArgumentException("Could not find the Process for the Case from provided inputs on case creation.");
            }
            createCaseParams.setProcess(process);
        }
        if (createCaseParams.getMakeTitle() == null && createCaseParams.getProcess() != null) {
            createCaseParams.setMakeTitle(resolveDefaultCaseTitle(createCaseParams));
        }
    }

    private Function<Case, String> resolveDefaultCaseTitle(CreateCaseParams createCaseParams) {
        Locale locale = createCaseParams.getLocale();
        Process process = createCaseParams.getProcess();
        Function<Case, String> makeTitle;
        if (process.getDefaultCaseName().isDynamic()) {
            makeTitle = (u) -> initValueExpressionEvaluator.evaluate(u, process.getDefaultCaseName().getExpression(locale),
                    createCaseParams.getParams());
        } else if (process.hasDefaultCaseName()) {
            makeTitle = (u) -> process.getDefaultCaseName().getTranslation(locale);
        } else {
            makeTitle = (u) -> null;
        }
        return makeTitle;
    }

    @Override
    public Page<Case> findAllByAuthor(String authorId, String petriNet, Pageable pageable) {
        Predicate query = QCase.case$.authorId.eq(authorId).and(QCase.case$.petriNetId.eq(petriNet));
        Page<Case> cases = repository.findAll(query, pageable);
        cases.forEach(this::initialize);
        return cases;
    }

    /**
     * Deletes the {@link Case} object from database by provided parameters as {@link DeleteCaseParams}
     *
     * @param deleteCaseParams parameters to determine the object to be deleted
     * <br>
     * <b>Required parameters</b>
     * <ul>
     *      <li>useCaseId or useCase</li>
     * </ul>
     *
     * @return outcome with the removed {@link Case} object and sub-outcomes as result of triggered events
     * */
    @Override
    public DeleteCaseEventOutcome deleteCase(DeleteCaseParams deleteCaseParams) {
        fillMissingAttributes(deleteCaseParams);

        if (deleteCaseParams.getIsTransactional() && !TransactionSynchronizationManager.isSynchronizationActive()) {
            NaeTransaction transaction = NaeTransaction.builder()
                    .transactionManager(transactionManager)
                    .event(new Closure<DeleteCaseEventOutcome>(null) {
                        @Override
                        public DeleteCaseEventOutcome call() {
                            return doDeleteCase(deleteCaseParams);
                        }
                    })
                    .build();
            transaction.begin();
            return (DeleteCaseEventOutcome) transaction.getResultOfEvent();
        } else {
            return doDeleteCase(deleteCaseParams);
        }
    }

    private DeleteCaseEventOutcome doDeleteCase(DeleteCaseParams deleteCaseParams) {
        Case useCase = deleteCaseParams.getUseCase();

        List<EventOutcome> preEventOutcomes = eventService.runActions(useCase.getProcess().getPreDeleteActions(),
                useCase, Optional.empty(), deleteCaseParams.getParams());

        historyService.save(new DeleteCaseEventLog(useCase, EventPhase.PRE));

        log.info("[{}]: Actor [{}] is deleting case {}", useCase.getStringId(), sessionManagerService.getActiveActorId(),
                useCase.getTitle());

        roleService.removeAllByCase(useCase.getStringId());
        taskService.deleteTasksByCase(useCase.getStringId());
        repository.delete(useCase);

        DeleteCaseEventOutcome outcome = new DeleteCaseEventOutcome(useCase, preEventOutcomes);
        outcome.addOutcomes(eventService.runActions(useCase.getProcess().getPostDeleteActions(), null,
                Optional.empty(), deleteCaseParams.getParams()));
        addMessageToOutcome(useCase.getProcess(), CaseEventType.DELETE, outcome);

        historyService.save(new DeleteCaseEventLog(useCase, EventPhase.POST));

        return outcome;
    }

    private void fillMissingAttributes(DeleteCaseParams deleteCaseParams) throws IllegalArgumentException {
        if (deleteCaseParams.getUseCase() == null) {
            if (deleteCaseParams.getUseCaseId() != null) {
                deleteCaseParams.setUseCase(findOne(deleteCaseParams.getUseCaseId()));
            } else {
                throw new IllegalArgumentException("At least case id must be provided on case removal.");
            }
        }
        if (deleteCaseParams.getIsTransactional() == null) {
            deleteCaseParams.setIsTransactional(transactionProperties.isDeleteCaseTransactional());
        }
    }

    @Override
    public void deleteInstancesOfPetriNet(Process net) {
        log.info("[{}]: Actor {} is deleting all cases and tasks of Petri net {} version {}", net.getStringId(),
                sessionManagerService.getActiveActorId(), net.getIdentifier(), net.getVersion().toString());

        taskService.deleteTasksByPetriNetId(net.getStringId());
        CaseSearchRequest request = new CaseSearchRequest();
        CaseSearchRequest.PetriNet netRequest = new CaseSearchRequest.PetriNet();
        netRequest.processId = net.getStringId();
        request.process = Collections.singletonList(netRequest);
        long countCases = elasticCaseService.count(Collections.singletonList(request), sessionManagerService.getActiveActorId(),
                Locale.getDefault(), false);
        log.info("[{}]: Actor [{}] is deleting {} cases of Petri net {} version {}", net.getStringId(),
                sessionManagerService.getActiveActorId(), countCases, net.getIdentifier(),
                net.getVersion().toString());
        // todo: release/8.0.0 page.unpaged?
        long pageCount = (countCases / 100) + 1;
        LongStream.range(0, pageCount)
                .forEach(i -> elasticCaseService.search(
                                Collections.singletonList(request),
                                sessionManagerService.getActiveActorId(),
                                PageRequest.of((int) i, 100),
                                Locale.getDefault(),
                                false)
                        .getContent()
                        .forEach(useCase -> deleteCase(new DeleteCaseParams(useCase))));
    }

    @Override
    public DeleteCaseEventOutcome deleteSubtreeRootedAt(String subtreeRootCaseId) {
        Case subtreeRoot = findOne(subtreeRootCaseId);
        if (subtreeRoot.getImmediateDataFields().contains("treeChildCases")) {
            ((List<String>) subtreeRoot.getDataSet().get("treeChildCases").getValue()).forEach(this::deleteSubtreeRootedAt);
        }
        return deleteCase(new DeleteCaseParams(subtreeRoot));
    }

    @Override
    public void updateMarking(Case useCase) {
        Process net = useCase.getProcess();
        useCase.setActivePlaces(net.getActivePlaces());
    }

    @Override
    public Case decrypt(Case useCase) {
        decryptDataSet(useCase);
        return useCase;
    }

    @Override
    public Page<Case> searchAll(Predicate predicate) {
        return search(predicate, new FullPageRequest());
    }

    @Override
    public long count(Predicate predicate) {
        return repository.count(predicate);
    }

    @Override
    public Case searchOne(Predicate predicate) {
        Page<Case> page = search(predicate, PageRequest.of(0, 1));
        if (page.getContent().isEmpty())
            return null;
        return page.getContent().get(0);
    }

    @Override
    public Map<String, I18nString> listToMap(List<Case> cases) {
        Map<String, I18nString> options = new HashMap<>();
        cases.forEach(aCase -> options.put(aCase.getStringId(), new I18nString(aCase.getTitle())));
        return options;
    }

    private Case resolveTaskRefs(Case useCase, Map<String, String> params) {
        // TODO: release/8.0.0
        Case finalUseCase = findOne(useCase.getStringId());
        useCase.getDataSet().getFields().values().stream()
                .filter(field -> field instanceof TaskField && field.getDefaultValue() != null)
                .forEach(field -> {
                    if (field.getDefaultValue().isDynamic()) {
                        dataSetInitializer.initializeValue(finalUseCase, field, params);
                    } else {
                        List<String> defaultValue = ((TaskField) field).getDefaultValue().getDefaultValue();
                        ((TaskField) field).setRawValue(
                                defaultValue.stream()
                                        .map(useCase::getTaskStringId)
                                        .collect(Collectors.toList())
                        );
                    }
                });
        return save(useCase);
    }

    // TODO: release/8.0.0 getData?

    private void encryptDataSet(Case useCase) {
        applyCryptoMethodOnDataSet(useCase, entry -> encryptionService.encrypt(entry.getFirst(), entry.getSecond()));
    }

    private void decryptDataSet(Case useCase) {
        applyCryptoMethodOnDataSet(useCase, entry -> encryptionService.decrypt(entry.getFirst(), entry.getSecond()));
    }

    private void applyCryptoMethodOnDataSet(Case useCase, Function<Pair<String, String>, String> method) {
        Map<Field<?>, String> dataFields = getEncryptedDataSet(useCase);

        for (Map.Entry<Field<?>, String> entry : dataFields.entrySet()) {
            if (!(entry.getKey() instanceof TextField)) {
                continue;
            }
            TextField dataField = (TextField) entry.getKey();
            String encryption = entry.getValue();
            String value = dataField.getRawValue();
            if (value == null) {
                continue;
            }
            dataField.setValue(new DataFieldValue<>(method.apply(Pair.of(value, encryption))));
        }
    }

    private Map<Field<?>, String> getEncryptedDataSet(Case useCase) {
        Process net = useCase.getProcess();
        Map<Field<?>, String> encryptedDataSet = new HashMap<>();

        for (Map.Entry<String, Field<?>> entry : net.getDataSet().entrySet()) {
            String encryption = entry.getValue().getEncryption();
            if (encryption != null) {
                encryptedDataSet.put(useCase.getDataSet().get(entry.getKey()), encryption);
            }
        }

        return encryptedDataSet;
    }

    private void setPetriNet(Case useCase) {
        Process model = useCase.getProcess();
        if (model == null) {
            model = petriNetService.clone(useCase.getPetriNetObjectId());
            useCase.setProcess(model);
        }
        model.initializeTokens(useCase.getActivePlaces());
        resolveArcsWeight(useCase);
    }

    private void resolveArcsWeight(Case useCase) {
        useCase.getProcess().getArcs().values().forEach(arcCollection -> {
            arcCollection.getInput().forEach(arc -> this.resolveWeight(arc, useCase));
            arcCollection.getOutput().forEach(arc -> this.resolveWeight(arc, useCase));
        });
    }

    private void resolveWeight(Arc<?, ?> arc, Case useCase) {
        int weight;
        if (arc.getMultiplicityExpression().isDynamic()) {
            String definition = arc.getMultiplicityExpression().getDefinition();
            ReferenceType referenceType = arc.getMultiplicityExpression().getReferenceType();
            if (referenceType == ReferenceType.PLACE) {
                weight = useCase.getProcess().getPlace(definition).getTokens();
            } else if (referenceType == ReferenceType.DATA_VARIABLE) {
                weight = ((Number) useCase.getDataSet().get(definition).getRawValue()).intValue();
            } else {
                // TODO: release/8.0.0 evaluate expression
                weight = 2;
            }
        } else {
            weight = arc.getMultiplicityExpression().getDefaultValue();
        }
        arc.setMultiplicity(weight);
    }

    private EventOutcome addMessageToOutcome(Process net, CaseEventType type, EventOutcome outcome) {
        if (net.getCaseEvents().containsKey(type)) {
            outcome.setMessage(net.getCaseEvents().get(type).getMessage());
        }
        return outcome;
    }
}
