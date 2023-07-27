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
import com.netgrif.application.engine.importer.service.FieldFactory;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.UriContentType;
import com.netgrif.application.engine.petrinet.domain.UriNode;
import com.netgrif.application.engine.petrinet.domain.dataset.*;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.FieldActionsRunner;
import com.netgrif.application.engine.petrinet.domain.events.CaseEventType;
import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.petrinet.service.interfaces.IProcessRoleService;
import com.netgrif.application.engine.petrinet.service.interfaces.IUriService;
import com.netgrif.application.engine.rules.domain.facts.CaseCreatedFact;
import com.netgrif.application.engine.rules.service.interfaces.IRuleEngine;
import com.netgrif.application.engine.security.service.EncryptionService;
import com.netgrif.application.engine.utils.FullPageRequest;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.DataField;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.domain.TaskPair;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.EventOutcome;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.caseoutcomes.CreateCaseEventOutcome;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.caseoutcomes.DeleteCaseEventOutcome;
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository;
import com.netgrif.application.engine.workflow.service.interfaces.IEventService;
import com.netgrif.application.engine.workflow.service.interfaces.IInitValueExpressionEvaluator;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import com.querydsl.core.types.Predicate;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@Service
public class WorkflowService implements IWorkflowService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowService.class);

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
    protected CaseSearchService searchService;

    @Autowired
    protected ApplicationEventPublisher publisher;

    @Autowired
    protected EncryptionService encryptionService;

    @Autowired
    protected FieldFactory fieldFactory;

    @Autowired
    protected IRuleEngine ruleEngine;

    @Autowired
    protected FieldActionsRunner actionsRunner;

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

    @Autowired
    private IUriService uriService;

    protected IElasticCaseService elasticCaseService;

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
            setImmediateDataFields(useCase);
            elasticCaseService.indexNow(this.caseMappingService.transform(useCase));
        } catch (Exception e) {
            log.error("Indexing failed [" + useCase.getStringId() + "]", e);
        }
        return useCase;
    }

    @Override
    public Case findOne(String caseId) {
        Case useCase = findOneNoNet(caseId);
        setPetriNet(useCase);
        decryptDataSet(useCase);
        this.setImmediateDataFieldsReadOnly(useCase);
        return useCase;
    }

    @Override
    public Case findOneNoNet(String caseId) {
        Optional<Case> caseOptional = repository.findById(caseId);
        if (caseOptional.isEmpty()) {
            throw new IllegalArgumentException("Could not find Case with id [" + caseId + "]");
        }
        Case useCase = caseOptional.get();
        return useCase;
    }

    @Override
    public List<Case> findAllById(List<String> ids) {
        return repository.findAllBy_idIn(ids).stream()
                .filter(Objects::nonNull)
                .sorted(Ordering.explicit(ids)
                .onResultOf(Case::getStringId))
                .map(caze -> {
                    caze.setPetriNet(petriNetService.get(caze.getPetriNetObjectId()));
                    decryptDataSet(caze);
                    setImmediateDataFieldsReadOnly(caze);
                    return caze;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Page<Case> getAll(Pageable pageable) {
        Page<Case> page = repository.findAll(pageable);
        page.getContent().forEach(this::setPetriNet);
        decryptDataSets(page.getContent());
        return setImmediateDataFields(page);
    }

    @Override
    public Page<Case> findAllByUri(String uri, Pageable pageable) {
        Page<Case> page = repository.findAllByUriNodeId(uri, pageable);
        page.getContent().forEach(this::setPetriNet);
        decryptDataSets(page.getContent());
        return setImmediateDataFields(page);
    }

    @Override
    public Page<Case> search(Predicate predicate, Pageable pageable) {
        Page<Case> page = repository.findAll(predicate, pageable);
        page.getContent().forEach(this::setPetriNet);
        return setImmediateDataFields(page);
    }

    @Override
    public Page<Case> search(Map<String, Object> request, Pageable pageable, LoggedUser user, Locale locale) {
        Predicate searchPredicate = searchService.buildQuery(request, user, locale);
        Page<Case> page;
        if (searchPredicate != null) {
            page = repository.findAll(searchPredicate, pageable);
        } else {
            page = Page.empty();
        }
        page.getContent().forEach(this::setPetriNet);
        decryptDataSets(page.getContent());
        return setImmediateDataFields(page);
    }

    @Override
    public long count(Map<String, Object> request, LoggedUser user, Locale locale) {
        Predicate searchPredicate = searchService.buildQuery(request, user, locale);
        if (searchPredicate != null) {
            return repository.count(searchPredicate);
        } else {
            return 0;
        }
    }

    @Override
    public Case resolveUserRef(Case useCase) {
        useCase.getUsers().clear();
        useCase.getNegativeViewUsers().clear();
        useCase.getUserRefs().forEach((id, permission) -> {
            resolveUserRefPermissions(useCase, id, permission);
        });
        useCase.resolveViewUsers();
        taskService.resolveUserRef(useCase);
        return save(useCase);
    }

    private void resolveUserRefPermissions(Case useCase, String userListId, Map<String, Boolean> permission) {
        List<String> userIds = getExistingUsers((UserListFieldValue) useCase.getDataSet().get(userListId).getValue());
        if (userIds != null && userIds.size() != 0) {
            if (permission.containsKey("view") && !permission.get("view")) {
                useCase.getNegativeViewUsers().addAll(userIds);
            } else {
                useCase.addUsers(new HashSet<>(userIds), permission);
            }
        }
    }

    private List<String> getExistingUsers(UserListFieldValue userListValue) {
        if (userListValue == null)
            return null;
        return userListValue.getUserValues().stream().map(UserFieldValue::getId)
                .filter(id -> userService.resolveById(id, false) != null)
                .collect(Collectors.toList());
    }

    @Override
    public CreateCaseEventOutcome createCase(String netId, String title, String color, LoggedUser user, Locale locale) {
        if (locale == null) {
            locale = LocaleContextHolder.getLocale();
        }
        if (title == null) {
            return this.createCase(netId, resolveDefaultCaseTitle(netId, locale), color, user);
        }
        return this.createCase(netId, title, color, user);
    }

    @Override
    public CreateCaseEventOutcome createCase(String netId, String title, String color, LoggedUser user) {
        return createCase(netId, (u) -> title, color, user);
    }

    @Override
    public CreateCaseEventOutcome createCaseByIdentifier(String identifier, String title, String color, LoggedUser user, Locale locale) {
        PetriNet net = petriNetService.getNewestVersionByIdentifier(identifier);
        if (net == null) {
            throw new IllegalArgumentException("Petri net with identifier [" + identifier + "] does not exist.");
        }
        return this.createCase(net.getStringId(), title != null && !title.equals("") ? title : net.getDefaultCaseName().getTranslation(locale), color, user);
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
        LoggedUser loggedOrImpersonated = user.getSelfOrImpersonated();
        PetriNet petriNet = petriNetService.clone(new ObjectId(netId));
        int rulesExecuted;
        Case useCase = new Case(petriNet);
        useCase.populateDataSet(initValueExpressionEvaluator);
        useCase.setColor(color);
        useCase.setAuthor(loggedOrImpersonated.transformToAuthor());
        useCase.setCreationDate(LocalDateTime.now());
        useCase.setTitle(makeTitle.apply(useCase));
        UriNode uriNode = uriService.getOrCreate(petriNet, UriContentType.CASE);
        useCase.setUriNodeId(uriNode.getId());

        CreateCaseEventOutcome outcome = new CreateCaseEventOutcome();
        outcome.addOutcomes(eventService.runActions(petriNet.getPreCreateActions(), null, Optional.empty()));
        rulesExecuted = ruleEngine.evaluateRules(useCase, new CaseCreatedFact(useCase.getStringId(), EventPhase.PRE));
        if (rulesExecuted > 0) {
            useCase = save(useCase);
        }

        historyService.save(new CreateCaseEventLog(useCase, EventPhase.PRE));
        log.info("[" + useCase.getStringId() + "]: Case " + useCase.getTitle() + " created");

        useCase.getPetriNet().initializeArcs(useCase.getDataSet());
        taskService.reloadTasks(useCase);
        useCase = findOne(useCase.getStringId());
        resolveTaskRefs(useCase);

        useCase = findOne(useCase.getStringId());
        outcome.addOutcomes(eventService.runActions(petriNet.getPostCreateActions(), useCase, Optional.empty()));
        useCase = findOne(useCase.getStringId());
        rulesExecuted = ruleEngine.evaluateRules(useCase, new CaseCreatedFact(useCase.getStringId(), EventPhase.POST));
        if (rulesExecuted > 0) {
            useCase = save(useCase);
        }

        historyService.save(new CreateCaseEventLog(useCase, EventPhase.POST));
        outcome.setCase(setImmediateDataFields(useCase));
        addMessageToOutcome(petriNet, CaseEventType.CREATE, outcome);
        return outcome;
    }

    protected Function<Case, String> resolveDefaultCaseTitle(String netId, Locale locale) {
        PetriNet petriNet = petriNetService.clone(new ObjectId(netId));
        Function<Case, String> makeTitle;
        if (petriNet.hasDynamicCaseName()) {
            makeTitle = (u) -> initValueExpressionEvaluator.evaluateCaseName(u, petriNet.getDefaultCaseNameExpression()).getTranslation(locale);
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
        List<Case> cases = mongoTemplate.find(query, Case.class);
        decryptDataSets(cases);
        return setImmediateDataFields(new PageImpl<Case>(cases, pageable, mongoTemplate.count(new BasicQuery(queryString, "{_id:1}"), Case.class)));
    }

    @Override
    public DeleteCaseEventOutcome deleteCase(String caseId) {
        Case useCase = findOne(caseId);
        return deleteCase(useCase);
    }

    @Override
    public DeleteCaseEventOutcome deleteCase(Case useCase) {

        DeleteCaseEventOutcome outcome = new DeleteCaseEventOutcome(useCase, eventService.runActions(useCase.getPetriNet().getPreDeleteActions(), useCase, Optional.empty()));
        historyService.save(new DeleteCaseEventLog(useCase, EventPhase.PRE));
        log.info("[" + useCase.getStringId() + "]: User [" + userService.getLoggedOrSystem().getStringId() + "] is deleting case " + useCase.getTitle());

        taskService.deleteTasksByCase(useCase.getStringId());
        repository.delete(useCase);

        outcome.addOutcomes(eventService.runActions(useCase.getPetriNet().getPostDeleteActions(), null, Optional.empty()));
        addMessageToOutcome(useCase.getPetriNet(), CaseEventType.DELETE, outcome);
        historyService.save(new DeleteCaseEventLog(useCase, EventPhase.POST));
        return outcome;
    }

    @Override
    public void deleteInstancesOfPetriNet(PetriNet net) {
        log.info("[" + net.getStringId() + "]: User " + userService.getLoggedOrSystem().getStringId() + " is deleting all cases and tasks of Petri net " + net.getIdentifier() + " version " + net.getVersion().toString());

        taskService.deleteTasksByPetriNetId(net.getStringId());
        CaseSearchRequest request = new CaseSearchRequest();
        CaseSearchRequest.PetriNet netRequest = new CaseSearchRequest.PetriNet();
        netRequest.processId = net.getStringId();
        request.process = Collections.singletonList(netRequest);
        long countCases = elasticCaseService.count(Collections.singletonList(request), userService.getLoggedOrSystem().transformToLoggedUser(), Locale.getDefault(), false);
        log.info("[" + net.getStringId() + "]: User " + userService.getLoggedOrSystem().getStringId() + " is deleting " + countCases + " cases of Petri net " + net.getIdentifier() + " version " + net.getVersion().toString());
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
    public boolean removeTasksFromCase(List<Task> tasks, String caseId) {
        if (tasks.isEmpty()) {
            return true;
        }
        Optional<Case> caseOptional = repository.findById(caseId);
        if (caseOptional.isEmpty()) {
            throw new IllegalArgumentException("Could not find case with id [" + caseId + "]");
        }
        Case useCase = caseOptional.get();
        return removeTasksFromCase(tasks, useCase);
    }

    @Override
    public boolean removeTasksFromCase(List<Task> tasks, Case useCase) {
        if (tasks.isEmpty()) {
            return true;
        }
        return useCase.removeTasks(tasks);
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
            if (field.getDefaultValue() != null && !field.getDefaultValue().isEmpty() && useCase.getDataField(field.getStringId()).getValue() != null &&
                    useCase.getDataField(field.getStringId()).getValue().equals(field.getDefaultValue())) {
                useCase.getDataField(field.getStringId()).setValue(new ArrayList<>());
                List<TaskPair> taskPairList = useCase.getTasks().stream().filter(t ->
                        (field.getDefaultValue().contains(t.getTransition()))).collect(Collectors.toList());
                if (!taskPairList.isEmpty()) {
                    taskPairList.forEach(pair -> ((List<String>) useCase.getDataField(field.getStringId()).getValue()).add(pair.getTask()));
                }
            }
        });
        save(useCase);
    }

    @Deprecated
    public List<Field> getData(String caseId) {
        Optional<Case> optionalUseCase = repository.findById(caseId);
        if (!optionalUseCase.isPresent())
            throw new IllegalArgumentException("Could not find case with id [" + caseId + "]");
        Case useCase = optionalUseCase.get();
        List<Field> fields = new ArrayList<>();
        useCase.getDataSet().forEach((id, dataField) -> {
            if (dataField.isDisplayable() || useCase.getPetriNet().isDisplayableInAnyTransition(id)) {
                Field field = fieldFactory.buildFieldWithoutValidation(useCase, id, null);
                field.setBehavior(dataField.applyOnlyVisibleBehavior());
                fields.add(field);
            }
        });

        LongStream.range(0L, fields.size()).forEach(l -> fields.get((int) l).setOrder(l));
        return fields;
    }

    private void setImmediateDataFieldsReadOnly(Case useCase) {
        List<Field> immediateData = new ArrayList<>();

        useCase.getImmediateDataFields().forEach(fieldId -> {
            try {
                Field field = fieldFactory.buildImmediateField(useCase, fieldId);
                Field clone = field.clone();
                if (field.getValue() != null) {
                    if (field.getType() == FieldType.TEXT) {
                        clone.setValue(field.getValue().toString());
                    } else {
                        clone.setValue(field.getValue());
                    }
                } else {
                    clone.setValue(null);
                }
                immediateData.add(clone);
            } catch (Exception e) {
                log.error("Could not built immediate field [" + fieldId + "]");
            }
        });
        LongStream.range(0L, immediateData.size()).forEach(index -> immediateData.get((int) index).setOrder(index));

        useCase.setImmediateData(immediateData);
    }

    protected Page<Case> setImmediateDataFields(Page<Case> cases) {
        cases.getContent().forEach(this::setImmediateDataFields);
        return cases;
    }

    protected Case setImmediateDataFields(Case useCase) {
        List<Field> immediateData = new ArrayList<>();

        useCase.getImmediateDataFields().forEach(fieldId ->
                immediateData.add(fieldFactory.buildImmediateField(useCase, fieldId))
        );
        LongStream.range(0L, immediateData.size()).forEach(index -> immediateData.get((int) index).setOrder(index));

        useCase.setImmediateData(immediateData);
        return useCase;
    }

    private void encryptDataSet(Case useCase) {
        applyCryptoMethodOnDataSet(useCase, entry -> encryptionService.encrypt(entry.getFirst(), entry.getSecond()));
    }

    private void decryptDataSet(Case useCase) {
        applyCryptoMethodOnDataSet(useCase, entry -> encryptionService.decrypt(entry.getFirst(), entry.getSecond()));
    }

    private void decryptDataSets(Collection<Case> cases) {
        for (Case aCase : cases) {
            decryptDataSet(aCase);
        }
    }

    private void applyCryptoMethodOnDataSet(Case useCase, Function<Pair<String, String>, String> method) {
        Map<DataField, String> dataFields = getEncryptedDataSet(useCase);

        for (Map.Entry<DataField, String> entry : dataFields.entrySet()) {
            DataField dataField = entry.getKey();
            String value = (String) dataField.getValue();
            String encryption = entry.getValue();

            if (value == null)
                continue;

            dataField.setValue(method.apply(Pair.of(value, encryption)));
        }
    }

    private Map<DataField, String> getEncryptedDataSet(Case useCase) {
        PetriNet net = useCase.getPetriNet();
        Map<DataField, String> encryptedDataSet = new HashMap<>();

        for (Map.Entry<String, Field> entry : net.getDataSet().entrySet()) {
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
