package com.netgrif.application.engine.workflow.service;

import com.google.common.collect.Ordering;
import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.history.domain.caseevents.CreateCaseEventLog;
import com.netgrif.application.engine.history.domain.caseevents.DeleteCaseEventLog;
import com.netgrif.application.engine.history.service.IHistoryService;
import com.netgrif.application.engine.importer.model.CaseEventType;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.dataset.*;
import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRolePermission;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.rules.domain.facts.CaseCreatedFact;
import com.netgrif.application.engine.rules.service.interfaces.IRuleEngine;
import com.netgrif.application.engine.security.service.EncryptionService;
import com.netgrif.application.engine.utils.FullPageRequest;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.DataFieldValue;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.domain.outcomes.eventoutcomes.EventOutcome;
import com.netgrif.application.engine.workflow.domain.outcomes.eventoutcomes.caseoutcomes.CreateCaseEventOutcome;
import com.netgrif.application.engine.workflow.domain.outcomes.eventoutcomes.caseoutcomes.DeleteCaseEventOutcome;
import com.netgrif.application.engine.workflow.domain.outcomes.CreateTasksOutcome;
import com.netgrif.application.engine.workflow.domain.params.DeleteCaseParams;
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository;
import com.netgrif.application.engine.workflow.service.initializer.DataSetInitializer;
import com.netgrif.application.engine.workflow.service.interfaces.IEventService;
import com.netgrif.application.engine.workflow.service.interfaces.IInitValueExpressionEvaluator;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import com.netgrif.application.engine.workflow.domain.params.CreateCaseParams;
import com.querydsl.core.types.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Slf4j
@Service
public class WorkflowService implements IWorkflowService {

    @Autowired
    private CaseRepository repository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private IPetriNetService petriNetService;

    @Autowired
    private ITaskService taskService;

    @Autowired
    private EncryptionService encryptionService;

    @Autowired
    private IRuleEngine ruleEngine;

    @Autowired
    private IUserService userService;

    @Autowired
    private IInitValueExpressionEvaluator initValueExpressionEvaluator;

    @Lazy
    @Autowired
    private IEventService eventService;

    @Autowired
    private IHistoryService historyService;

    private IElasticCaseService elasticCaseService;

    @Autowired
    private DataSetInitializer dataSetInitializer;

    @Autowired
    public void setElasticCaseService(IElasticCaseService elasticCaseService) {
        this.elasticCaseService = elasticCaseService;
    }

    @Override
    public Case save(Case useCase) {
        if (useCase.getPetriNet() == null) {
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

    @Override
    public Case resolveUserRef(Case useCase) {
        useCase.getUsers().clear();
        useCase.getNegativeViewUsers().clear();
        useCase.getUserRefs().forEach((id, permission) -> resolveUserRefPermissions(useCase, id, permission));
        useCase.resolveViewUsers();
        taskService.resolveUserRef(useCase);
        return save(useCase);
    }

    private void resolveUserRefPermissions(Case useCase, String userListId, Map<ProcessRolePermission, Boolean> permission) {
        List<String> userIds = getExistingUsers((UserListFieldValue) useCase.getDataSet().get(userListId).getRawValue());
        if (userIds != null && !userIds.isEmpty()) {
            if (permission.containsKey(ProcessRolePermission.VIEW) && !permission.get(ProcessRolePermission.VIEW)) {
                useCase.getNegativeViewUsers().addAll(userIds);
            } else {
                useCase.addUsers(new HashSet<>(userIds), permission);
            }
        }
    }

    private List<String> getExistingUsers(UserListFieldValue userListValue) {
        if (userListValue == null) {
            return null;
        }
        // TODO: release/8.0.0 fix null set as user value
        // TODO: release/8.0.0  .filter(id -> userService.resolveById(id, false) != null)
        return userListValue.getUserValues().stream()
                .filter(Objects::nonNull)
                .map(UserFieldValue::getId)
                .filter(id -> id != null && userService.existsById(id))
                .collect(Collectors.toList());
    }

    /**
     * todo javadoc
     * */
    @Override
    @Transactional
    public CreateCaseEventOutcome createCase(CreateCaseParams createCaseParams) {
        fillMissingAttributes(createCaseParams);

        Case useCase = createCaseObject(createCaseParams);
        CreateTasksOutcome createTasksOutcome = taskService.createAndSetTasksInCase(useCase);
        save(useCase); // must be after tasks creation for effectivity reasons

        PetriNet petriNet = createCaseParams.getPetriNet();
        CreateCaseEventOutcome outcome = new CreateCaseEventOutcome();

        outcome.addOutcomes(eventService.runActions(petriNet.getPreCreateActions(), null, Optional.empty(),
                createCaseParams.getParams()));

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

        List<EventOutcome> eventPostOutcomes = eventService.runActions(petriNet.getPostCreateActions(), useCase, Optional.empty(),
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

        log.info("[{}]: Case {} created", useCase.getStringId(), useCase.getTitle());

        outcome.setCase(useCase);
        addMessageToOutcome(petriNet, CaseEventType.CREATE, outcome);
        return outcome;
    }

    /**
     * todo javadoc
     * */
    private Case createCaseObject(CreateCaseParams createCaseParams) {
        LoggedUser loggedOrImpersonated = createCaseParams.getLoggedUser().getSelfOrImpersonated();

        Case useCase = new Case(createCaseParams.getPetriNet());
        dataSetInitializer.populateDataSet(useCase, createCaseParams.getParams());
        useCase.setColor(createCaseParams.getColor());
        useCase.setAuthor(loggedOrImpersonated.transformToAuthor());
        useCase.setCreationDate(LocalDateTime.now());
        useCase.setTitle(createCaseParams.getMakeTitle().apply(useCase));
        useCase.setUriNodeId(createCaseParams.getPetriNet().getUriNodeId());

        useCase.getPetriNet().initializeArcs(useCase.getDataSet());

        return useCase;
    }

    /**
     * todo javadoc
     * makeTitle, petriNet
     * */
    private void fillMissingAttributes(CreateCaseParams createCaseParams) throws IllegalArgumentException {
        if (createCaseParams.getLoggedUser() == null) {
            throw new IllegalArgumentException("Logged user cannot be null on Case creation.");
        }
        if (createCaseParams.getPetriNet() == null) {
            PetriNet petriNet;
            if (createCaseParams.getPetriNetId() != null) {
                petriNet = petriNetService.get(new ObjectId(createCaseParams.getPetriNetId())).clone();
            } else if (createCaseParams.getPetriNetIdentifier() != null) {
                petriNet = petriNetService.getNewestVersionByIdentifier(createCaseParams.getPetriNetIdentifier()).clone();
            } else {
                throw new IllegalArgumentException("Could not find the PetriNet for the Case from provided inputs on case creation.");
            }
            createCaseParams.setPetriNet(petriNet);
        }
        if (createCaseParams.getMakeTitle() == null && createCaseParams.getPetriNet() != null) {
            createCaseParams.setMakeTitle(resolveDefaultCaseTitle(createCaseParams));
        }
    }

    private Function<Case, String> resolveDefaultCaseTitle(CreateCaseParams createCaseParams) {
        Locale locale = createCaseParams.getLocale();
        PetriNet petriNet = createCaseParams.getPetriNet();
        Function<Case, String> makeTitle;
        if (petriNet.hasDynamicCaseName()) {
            makeTitle = (u) -> initValueExpressionEvaluator.evaluateCaseName(u, petriNet.getDefaultCaseNameExpression(),
                    createCaseParams.getParams()).getTranslation(locale);
        } else if (petriNet.hasDefaultCaseName()) {
            makeTitle = (u) -> petriNet.getDefaultCaseName().getTranslation(locale);
        } else {
            makeTitle = (u) -> null;
        }
        return makeTitle;
    }

    @Override
    public Page<Case> findAllByAuthor(String authorId, String petriNet, Pageable pageable) {
        String queryString = "{author.id:" + authorId + ", petriNet:{$ref:\"petriNet\",$id:{$oid:\"" + petriNet + "\"}}}";
        BasicQuery query = new BasicQuery(queryString);
        query = (BasicQuery) query.with(pageable);
//        TODO: release/8.0.0 remove mongoTemplates from project
        List<Case> cases = mongoTemplate.find(query, Case.class);
        cases.forEach(this::initialize);
        return new PageImpl<>(cases, pageable, mongoTemplate.count(new BasicQuery(queryString, "{id:1}"), Case.class));
    }

    /**
     * todo javadoc
     * */
    @Override
    @Transactional
    public DeleteCaseEventOutcome deleteCase(DeleteCaseParams deleteCaseParams) {
        fillMissingAttributes(deleteCaseParams);

        Case useCase = deleteCaseParams.getUseCase();

        List<EventOutcome> preEventOutcomes = eventService.runActions(useCase.getPetriNet().getPreDeleteActions(),
                useCase, Optional.empty(), deleteCaseParams.getParams());

        historyService.save(new DeleteCaseEventLog(useCase, EventPhase.PRE));

        log.info("[{}]: User [{}] is deleting case {}", useCase.getStringId(), userService.getLoggedOrSystem().getStringId(),
                useCase.getTitle());

        taskService.deleteTasksByCase(useCase.getStringId());
        repository.delete(useCase);

        DeleteCaseEventOutcome outcome = new DeleteCaseEventOutcome(useCase, preEventOutcomes);
        outcome.addOutcomes(eventService.runActions(useCase.getPetriNet().getPostDeleteActions(), null,
                Optional.empty(), deleteCaseParams.getParams()));
        addMessageToOutcome(useCase.getPetriNet(), CaseEventType.DELETE, outcome);

        historyService.save(new DeleteCaseEventLog(useCase, EventPhase.POST));

        return outcome;
    }

    /**
     * todo javadoc
     * */
    private void fillMissingAttributes(DeleteCaseParams deleteCaseParams) throws IllegalArgumentException {
        if (deleteCaseParams.getUseCase() == null) {
            if (deleteCaseParams.getUseCaseId() != null) {
                deleteCaseParams.setUseCase(findOne(deleteCaseParams.getUseCaseId()));
            } else {
                throw new IllegalArgumentException("At least case id must be provided on case removal.");
            }
        }
    }

    @Override
    @Transactional
    public void deleteInstancesOfPetriNet(PetriNet net) {
        final IUser user = userService.getLoggedOrSystem();
        final LoggedUser loggedUser = user.transformToLoggedUser();

        log.info("[{}]: User {} is deleting all cases and tasks of Petri net {} version {}", net.getStringId(),
                user.getStringId(), net.getIdentifier(), net.getVersion().toString());

        taskService.deleteTasksByPetriNetId(net.getStringId());

        CaseSearchRequest request = new CaseSearchRequest();
        CaseSearchRequest.PetriNet netRequest = new CaseSearchRequest.PetriNet();
        netRequest.processId = net.getStringId();
        request.process = Collections.singletonList(netRequest);
        long countCases = elasticCaseService.count(Collections.singletonList(request), loggedUser, Locale.getDefault(), false);

        log.info("[{}]: User {} is deleting {} cases of Petri net {} version {}", net.getStringId(), user.getStringId(),
                countCases, net.getIdentifier(), net.getVersion().toString());

        long pageCount = (countCases / 100) + 1;
        LongStream.range(0, pageCount)
                .forEach(i -> elasticCaseService.search(
                                Collections.singletonList(request),
                                loggedUser,
                                PageRequest.of((int) i, 100),
                                Locale.getDefault(),
                                false)
                        .getContent()
                        .forEach(useCase -> deleteCase(new DeleteCaseParams(useCase))));
    }

    @Override
    @Transactional
    public DeleteCaseEventOutcome deleteSubtreeRootedAt(String subtreeRootCaseId) {
        Case subtreeRoot = findOne(subtreeRootCaseId);
        if (subtreeRoot.getImmediateDataFields().contains("treeChildCases")) {
            ((List<String>) subtreeRoot.getDataSet().get("treeChildCases").getValue()).forEach(this::deleteSubtreeRootedAt);
        }

        return deleteCase(new DeleteCaseParams(subtreeRoot));
    }

    @Override
    public void updateMarking(Case useCase) {
        PetriNet net = useCase.getPetriNet();
        useCase.setActivePlaces(net.getActivePlaces());
    }

    @Override
    public void removeTasksFromCase(List<Task> tasks, String caseId) {
        Optional<Case> caseOptional = repository.findById(caseId);
        if (caseOptional.isEmpty()) {
            throw new IllegalArgumentException("Could not find case with id [" + caseId + "]");
        }
        Case useCase = caseOptional.get();
        removeTasksFromCase(tasks, useCase);
    }

    @Override
    public void removeTasksFromCase(List<Task> tasks, Case useCase) {
        if (tasks == null || tasks.isEmpty()) {
            return;
        }
        useCase.removeTasks(tasks);
        save(useCase);
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

    private void resolveTaskRefs(Case useCase) {
        useCase.getPetriNet().getDataSet().values().stream().filter(f -> f instanceof TaskField).map(TaskField.class::cast).forEach(field -> {
            if (field.getDefaultValue() != null && !field.getDefaultValue().isEmpty() && useCase.getDataSet().get(field.getStringId()).getValue() != null &&
                    useCase.getDataSet().get(field.getStringId()).getRawValue().equals(field.getDefaultValue())) {
                TaskField taskRef = (TaskField) useCase.getDataSet().get(field.getStringId());
                taskRef.setRawValue(new ArrayList<>());
                field.getDefaultValue().forEach(transitionId -> {
                    if (!useCase.getTasks().containsKey(transitionId)) {
                        return;
                    }
                    taskRef.getRawValue().add(useCase.getTasks().get(transitionId).getTaskStringId());
                });
            }
        });
        save(useCase);
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
        PetriNet net = useCase.getPetriNet();
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
        PetriNet model = useCase.getPetriNet();
        if (model == null) {
            model = petriNetService.clone(useCase.getPetriNetObjectId());
            useCase.setPetriNet(model);
        }
        model.initializeTokens(useCase.getActivePlaces());
        model.initializeArcs(useCase.getDataSet());
    }

    private EventOutcome addMessageToOutcome(PetriNet net, CaseEventType type, EventOutcome outcome) {
        if (net.getCaseEvents().containsKey(type)) {
            outcome.setMessage(net.getCaseEvents().get(type).getMessage());
        }
        return outcome;
    }
}
