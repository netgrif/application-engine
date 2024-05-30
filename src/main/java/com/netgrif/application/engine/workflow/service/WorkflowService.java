package com.netgrif.application.engine.workflow.service;

import com.google.common.collect.Ordering;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseMappingService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.history.domain.caseevents.CreateCaseEventLog;
import com.netgrif.application.engine.history.domain.caseevents.DeleteCaseEventLog;
import com.netgrif.application.engine.history.service.IHistoryService;
import com.netgrif.application.engine.importer.model.CaseEventType;
import com.netgrif.application.engine.importer.service.FieldFactory;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.dataset.*;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ActionRunner;
import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRolePermission;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.petrinet.service.interfaces.IProcessRoleService;
import com.netgrif.application.engine.rules.domain.facts.CaseCreatedFact;
import com.netgrif.application.engine.rules.service.interfaces.IRuleEngine;
import com.netgrif.application.engine.security.service.EncryptionService;
import com.netgrif.application.engine.utils.FullPageRequest;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.DataFieldValue;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.EventOutcome;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.caseoutcomes.CreateCaseEventOutcome;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.caseoutcomes.DeleteCaseEventOutcome;
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository;
import com.netgrif.application.engine.workflow.service.initializer.DataSetInitializer;
import com.netgrif.application.engine.workflow.service.interfaces.IEventService;
import com.netgrif.application.engine.workflow.service.interfaces.IInitValueExpressionEvaluator;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import com.querydsl.core.types.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

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
    protected MongoTemplate mongoTemplate;

    @Autowired
    protected IPetriNetService petriNetService;

    @Autowired
    protected IProcessRoleService processRoleService;

    @Autowired
    protected ITaskService taskService;

    @Autowired
    protected ApplicationEventPublisher publisher;

    @Autowired
    protected EncryptionService encryptionService;

    @Autowired
    protected FieldFactory fieldFactory;

    @Autowired
    protected IRuleEngine ruleEngine;

    @Autowired
    protected ActionRunner actionsRunner;

    @Autowired
    protected IUserService userService;

    @Autowired
    protected IInitValueExpressionEvaluator initValueExpressionEvaluator;

    @Autowired
    protected IElasticCaseMappingService caseMappingService;

    @Lazy
    @Autowired
    private IEventService eventService;

    @Autowired
    private IHistoryService historyService;

    protected IElasticCaseService elasticCaseService;

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
        try {
            useCase.resolveImmediateDataFields();
            elasticCaseService.indexNow(this.caseMappingService.transform(useCase));
        } catch (Exception e) {
            log.error("Indexing failed [{}]", useCase.getStringId(), e);
        }
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
        if (caseOptional.isEmpty()) {
            throw new IllegalArgumentException("Could not find Case with id [" + caseId + "]");
        }
        // TODO: release/8.0.0 get or throw?
        return caseOptional.get();
    }

    protected void initialize(Case useCase) {
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

    @Override
    public CreateCaseEventOutcome createCase(String netId, String title, String color, LoggedUser user, Locale locale, Map<String, String> params) {
        if (locale == null) {
            locale = LocaleContextHolder.getLocale();
        }
        if (title == null) {
            return this.createCase(netId, resolveDefaultCaseTitle(netId, locale, params), color, user, params);
        }
        return this.createCase(netId, title, color, user, params);
    }

    @Override
    public CreateCaseEventOutcome createCase(String netId, String title, String color, LoggedUser user, Locale locale) {
        return this.createCase(netId, title, color, user, locale, new HashMap<>());
    }

    @Override
    public CreateCaseEventOutcome createCase(String netId, String title, String color, LoggedUser user, Map<String, String> params) {
        return createCase(netId, (u) -> title, color, user, params);
    }

    @Override
    public CreateCaseEventOutcome createCase(String netId, String title, String color, LoggedUser user) {
        return this.createCase(netId, (u) -> title, color, user);
    }

    @Override
    public CreateCaseEventOutcome createCaseByIdentifier(String identifier, String title, String color, LoggedUser user, Locale locale, Map<String, String> params) {
        PetriNet net = petriNetService.getNewestVersionByIdentifier(identifier);
        if (net == null) {
            throw new IllegalArgumentException("Petri net with identifier [" + identifier + "] does not exist.");
        }
        return this.createCase(net.getStringId(), title != null && !title.equals("") ? title : net.getDefaultCaseName().getTranslation(locale), color, user, params);
    }

    @Override
    public CreateCaseEventOutcome createCaseByIdentifier(String identifier, String title, String color, LoggedUser user, Locale locale) {
        return this.createCaseByIdentifier(identifier, title, color, user, locale, new HashMap<>());
    }

    @Override
    public CreateCaseEventOutcome createCaseByIdentifier(String identifier, String title, String color, LoggedUser user, Map<String, String> params) {
        PetriNet net = petriNetService.getNewestVersionByIdentifier(identifier);
        if (net == null) {
            throw new IllegalArgumentException("Petri net with identifier [" + identifier + "] does not exist.");
        }
        return this.createCase(net.getStringId(), title, color, user, params);
    }

    @Override
    public CreateCaseEventOutcome createCaseByIdentifier(String identifier, String title, String color, LoggedUser user) {
        PetriNet net = petriNetService.getNewestVersionByIdentifier(identifier);
        if (net == null) {
            throw new IllegalArgumentException("Petri net with identifier [" + identifier + "] does not exist.");
        }
        return this.createCase(net.getStringId(), title, color, user);
    }

    public CreateCaseEventOutcome createCase(String netId, Function<Case, String> makeTitle, String color, LoggedUser user) {
        return this.createCase(netId, makeTitle, color, user, new HashMap<>());
    }

    public CreateCaseEventOutcome createCase(String netId, Function<Case, String> makeTitle, String color, LoggedUser user, Map<String, String> params) {
        LoggedUser loggedOrImpersonated = user.getSelfOrImpersonated();
        PetriNet petriNet = petriNetService.clone(new ObjectId(netId));
        int rulesExecuted;
        Case useCase = new Case(petriNet);
        dataSetInitializer.populateDataSet(useCase, params);
        useCase.setColor(color);
        useCase.setAuthor(loggedOrImpersonated.transformToAuthor());
        useCase.setCreationDate(LocalDateTime.now());
        useCase.setTitle(makeTitle.apply(useCase));
        // TODO: release/7.0.0 6.2.5
        // TODO: release/8.0.0 useCase.setUriNodeId(petriNet.getUriNodeId());
//        UriNode uriNode = uriService.getOrCreate(petriNet, UriContentType.CASE);
//        useCase.setUriNodeId(uriNode.getId());

        CreateCaseEventOutcome outcome = new CreateCaseEventOutcome();
        outcome.addOutcomes(eventService.runActions(petriNet.getPreCreateActions(), null, Optional.empty(), params));
        rulesExecuted = ruleEngine.evaluateRules(useCase, new CaseCreatedFact(useCase.getStringId(), EventPhase.PRE));
        if (rulesExecuted > 0) {
            useCase = save(useCase);
        }

        historyService.save(new CreateCaseEventLog(useCase, EventPhase.PRE));
        log.info("[{}]: Case {} created", useCase.getStringId(), useCase.getTitle());

        useCase.getPetriNet().initializeArcs(useCase.getDataSet());
        taskService.reloadTasks(useCase);
        useCase = findOne(useCase.getStringId());
        resolveTaskRefs(useCase);

        useCase = findOne(useCase.getStringId());
        outcome.addOutcomes(eventService.runActions(petriNet.getPostCreateActions(), useCase, Optional.empty(), params));
        useCase = findOne(useCase.getStringId());
        rulesExecuted = ruleEngine.evaluateRules(useCase, new CaseCreatedFact(useCase.getStringId(), EventPhase.POST));
        if (rulesExecuted > 0) {
            useCase = save(useCase);
        }

        historyService.save(new CreateCaseEventLog(useCase, EventPhase.POST));
        outcome.setCase(useCase);
        addMessageToOutcome(petriNet, CaseEventType.CREATE, outcome);
        return outcome;
    }

    protected Function<Case, String> resolveDefaultCaseTitle(String netId, Locale locale, Map<String, String> params) {
        PetriNet petriNet = petriNetService.clone(new ObjectId(netId));
        Function<Case, String> makeTitle;
        if (petriNet.hasDynamicCaseName()) {
            makeTitle = (u) -> initValueExpressionEvaluator.evaluateCaseName(u, petriNet.getDefaultCaseNameExpression(), params).getTranslation(locale);
        } else {
            makeTitle = (u) -> petriNet.getDefaultCaseName().getTranslation(locale);
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

    @Override
    public DeleteCaseEventOutcome deleteCase(String caseId) {
        return deleteCase(caseId, new HashMap<>());
    }

    @Override
    public DeleteCaseEventOutcome deleteCase(String caseId, Map<String, String> params) {
        Case useCase = findOne(caseId);
        return deleteCase(useCase, params);
    }

    @Override
    public DeleteCaseEventOutcome deleteCase(Case useCase, Map<String, String> params) {
        DeleteCaseEventOutcome outcome = new DeleteCaseEventOutcome(useCase, eventService.runActions(useCase.getPetriNet().getPreDeleteActions(), useCase, Optional.empty(), params));
        historyService.save(new DeleteCaseEventLog(useCase, EventPhase.PRE));
        log.info("[{}]: User [{}] is deleting case {}", useCase.getStringId(), userService.getLoggedOrSystem().getStringId(), useCase.getTitle());

        taskService.deleteTasksByCase(useCase.getStringId());
        repository.delete(useCase);

        outcome.addOutcomes(eventService.runActions(useCase.getPetriNet().getPostDeleteActions(), null, Optional.empty(), params));
        addMessageToOutcome(useCase.getPetriNet(), CaseEventType.DELETE, outcome);
        historyService.save(new DeleteCaseEventLog(useCase, EventPhase.POST));
        return outcome;
    }

    @Override
    public DeleteCaseEventOutcome deleteCase(Case useCase) {
        return deleteCase(useCase, new HashMap<>());
    }

    @Override
    public void deleteInstancesOfPetriNet(PetriNet net) {
        log.info("[{}]: User {} is deleting all cases and tasks of Petri net {} version {}", net.getStringId(), userService.getLoggedOrSystem().getStringId(), net.getIdentifier(), net.getVersion().toString());

        taskService.deleteTasksByPetriNetId(net.getStringId());
        CaseSearchRequest request = new CaseSearchRequest();
        CaseSearchRequest.PetriNet netRequest = new CaseSearchRequest.PetriNet();
        netRequest.processId = net.getStringId();
        request.process = Collections.singletonList(netRequest);
        long countCases = elasticCaseService.count(Collections.singletonList(request), userService.getLoggedOrSystem().transformToLoggedUser(), Locale.getDefault(), false);
        log.info("[{}]: User {} is deleting {} cases of Petri net {} version {}", net.getStringId(), userService.getLoggedOrSystem().getStringId(), countCases, net.getIdentifier(), net.getVersion().toString());
        long pageCount = (countCases / 100) + 1;
        LongStream.range(0, pageCount)
                .forEach(i -> elasticCaseService.search(
                                Collections.singletonList(request),
                                userService.getLoggedOrSystem().transformToLoggedUser(),
                                PageRequest.of((int) i, 100),
                                Locale.getDefault(),
                                false)
                        .getContent()
                        .forEach(this::deleteCase));
    }

    @Override
    public DeleteCaseEventOutcome deleteSubtreeRootedAt(String subtreeRootCaseId) {
        Case subtreeRoot = findOne(subtreeRootCaseId);
        if (subtreeRoot.getImmediateDataFields().contains("treeChildCases")) {
            ((List<String>) subtreeRoot.getDataSet().get("treeChildCases").getValue()).forEach(this::deleteSubtreeRootedAt);
        }
        return deleteCase(subtreeRootCaseId);
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
