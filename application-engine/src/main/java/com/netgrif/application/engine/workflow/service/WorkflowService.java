package com.netgrif.application.engine.workflow.service;

import com.google.common.collect.Ordering;
import com.netgrif.application.engine.auth.service.GroupService;
import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.ActorTransformer;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.*;
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRolePermission;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.service.UserService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseMappingService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.application.engine.event.evaluators.Evaluator;
import com.netgrif.application.engine.objects.event.events.workflow.CaseEvent;
import com.netgrif.application.engine.objects.event.events.workflow.CreateCaseEvent;
import com.netgrif.application.engine.objects.event.events.workflow.DeleteCaseEvent;
import com.netgrif.application.engine.event.services.EvaluationService;
import com.netgrif.application.engine.importer.service.FieldFactory;
import com.netgrif.application.engine.objects.petrinet.domain.I18nString;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.FieldActionsRunner;
import com.netgrif.application.engine.objects.petrinet.domain.events.CaseEventType;
import com.netgrif.application.engine.objects.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.adapter.spring.petrinet.service.ProcessRoleService;
import com.netgrif.application.engine.security.service.EncryptionService;
import com.netgrif.application.engine.utils.FullPageRequest;
import com.netgrif.application.engine.objects.workflow.domain.*;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.EventOutcome;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.caseoutcomes.CreateCaseEventOutcome;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.caseoutcomes.DeleteCaseEventOutcome;
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository;
import com.netgrif.application.engine.workflow.service.interfaces.IEventService;
import com.netgrif.application.engine.objects.workflow.service.InitValueExpressionEvaluator;
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
    protected ProcessRoleService processRoleService;

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
    protected FieldActionsRunner actionsRunner;

    @Autowired
    protected UserService userService;

    @Autowired
    protected GroupService groupService;

    @Autowired
    protected InitValueExpressionEvaluator initValueExpressionEvaluator;

    @Autowired
    protected IElasticCaseMappingService caseMappingService;

    @Lazy
    @Autowired
    protected IEventService eventService;

    protected IElasticCaseService elasticCaseService;

    protected EvaluationService evaluationService;

    @Autowired
    public void setElasticCaseService(IElasticCaseService elasticCaseService) {
        this.elasticCaseService = elasticCaseService;
    }

    @Autowired
    public void setEvaluationService(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
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
        ObjectId objectId = extractObjectId(caseId);
        Optional<Case> caseOptional = repository.findByIdObjectId(objectId);
        if (caseOptional.isEmpty()) {
            throw new IllegalArgumentException("Could not find Case with id [" + caseId + "]");
        }
        return caseOptional.get();
    }

    @Override
    public List<Case> findAllById(List<String> ids) {
        List<ObjectId> objectIds = ids.stream()
                .map(id -> {
                    String[] parts = id.split(ProcessResourceId.ID_SEPARATOR);
                    if (parts.length < 2) {
                        throw new IllegalArgumentException("Invalid NetgrifId format: " + id);
                    }
                    return new ObjectId(parts[1]);
                })
                .collect(Collectors.toList());

        List<Case> cases = repository.findAllByObjectIdsIn(objectIds).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Map<String, Case> caseMap = cases.stream()
                .collect(Collectors.toMap(Case::getStringId, caze -> caze));

        return ids.stream()
                .map(caseMap::get)
                .filter(Objects::nonNull)
                .sorted(Ordering.explicit(ids).onResultOf(Case::getStringId))
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
    public Case resolveActorRef(Case useCase) {
        useCase.getActors().clear();
        useCase.getNegativeViewActors().clear();
        useCase.getActorRefs().forEach((actorFieldId, permission) -> {
            resolveActorRefPermissions(useCase, actorFieldId, permission);
        });
        useCase.resolveViewActors();
        taskService.resolveActorRef(useCase);
        return save(useCase);
    }

    private void resolveActorRefPermissions(Case useCase, String actorFieldId, Map<String, Boolean> permission) {
        List<String> actorIds = getExistingActors((ActorListFieldValue) useCase.getDataSet().get(actorFieldId).getValue());
        if (actorIds != null && !actorIds.isEmpty()) {
            useCase.addActors(new HashSet<>(actorIds), permission);
            if (permission.containsKey(ProcessRolePermission.VIEW.getValue()) && !permission.get(ProcessRolePermission.VIEW.getValue())) {
                useCase.getNegativeViewActors().addAll(actorIds);
            }
        }
    }

    private List<String> getExistingActors(ActorListFieldValue actorListFieldValue) {
        if (actorListFieldValue == null) {
            return null;
        }
        return actorListFieldValue.getActorValues().stream()
                .map(ActorFieldValue::getId)
                .filter(actorId -> {
                    AbstractUser user = userService.findById(actorId, null);
                    if (user != null) {
                        return true;
                    }
                    try {
                        groupService.findById(actorId);
                        return true;
                    } catch (IllegalArgumentException ignored) {
                        return false;
                    }
                })
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
        PetriNet net = petriNetService.getDefaultVersionByIdentifier(identifier);
        if (net == null) {
            throw new IllegalArgumentException("Petri net with identifier [" + identifier + "] does not have default version.");
        }
        return this.createCase(net.getStringId(), title != null && !title.equals("") ? title : net.getDefaultCaseName().getTranslation(locale), color, user, params);
    }

    @Override
    public CreateCaseEventOutcome createCaseByIdentifier(String identifier, String title, String color, LoggedUser user, Locale locale) {
        return this.createCaseByIdentifier(identifier, title, color, user, locale, new HashMap<>());
    }

    @Override
    public CreateCaseEventOutcome createCaseByIdentifier(String identifier, String title, String color, LoggedUser user, Map<String, String> params) {
        PetriNet net = petriNetService.getDefaultVersionByIdentifier(identifier);
        if (net == null) {
            throw new IllegalArgumentException("Petri net with identifier [" + identifier + "] does not exist.");
        }
        return this.createCase(net.getStringId(), title, color, user, params);
    }

    @Override
    public CreateCaseEventOutcome createCaseByIdentifier(String identifier, String title, String color, LoggedUser user) {
        PetriNet net = petriNetService.getDefaultVersionByIdentifier(identifier);
        if (net == null) {
            throw new IllegalArgumentException("Petri net with identifier [" + identifier + "] does not exist.");
        }
        return this.createCase(net.getStringId(), title, color, user);
    }

    public CreateCaseEventOutcome createCase(String netId, Function<Case, String> makeTitle, String color, LoggedUser user) {
        return this.createCase(netId, makeTitle, color, user, new HashMap<>());
    }

    public CreateCaseEventOutcome createCase(String netId, Function<Case, String> makeTitle, String color, LoggedUser user, Map<String, String> params) {
        // TODO: impersonation
//        LoggedUser loggedOrImpersonated = user.getSelfOrImpersonated();
        LoggedUser loggedOrImpersonated = user;
        PetriNet petriNet = new com.netgrif.application.engine.adapter.spring.petrinet.domain.PetriNet((com.netgrif.application.engine.adapter.spring.petrinet.domain.PetriNet) petriNetService.get(new ObjectId(netId)));
//        int rulesExecuted;
        Case useCase = new com.netgrif.application.engine.adapter.spring.workflow.domain.Case(petriNet);
        useCase.populateDataSet(initValueExpressionEvaluator, params);
        useCase.setColor(color);
        useCase.setAuthor(ActorTransformer.toActorRef(loggedOrImpersonated));
        useCase.setCreationDate(LocalDateTime.now());
        useCase.setTitle(makeTitle.apply(useCase));

        CreateCaseEventOutcome outcome = new CreateCaseEventOutcome();
        outcome.addOutcomes(eventService.runActions(petriNet.getPreCreateActions(), null, Optional.empty(), params));
        //evaluateRules(new CreateCaseEvent(new CreateCaseEventOutcome(null, outcome.getOutcomes()), EventPhase.PRE));

        publisher.publishEvent(new CreateCaseEvent(outcome, EventPhase.PRE));
        log.info("[" + useCase.getStringId() + "]: Case " + useCase.getTitle() + " created");

        useCase.getPetriNet().initializeArcs(useCase.getDataSet());
        taskService.reloadTasks(useCase);
        useCase = findOne(useCase.getStringId());
        resolveTaskRefs(useCase);

        useCase = findOne(useCase.getStringId());
        outcome.addOutcomes(eventService.runActions(petriNet.getPostCreateActions(), useCase, Optional.empty(), params));
        useCase = findOne(useCase.getStringId());
        useCase = evaluateRules(new CreateCaseEvent(new CreateCaseEventOutcome(useCase, outcome.getOutcomes()), EventPhase.POST));
//        rulesExecuted = ruleEngine.evaluateRules(useCase, new CaseCreatedFact(useCase.getStringId(), EventPhase.POST));
//        if (rulesExecuted > 0) {
//            useCase = save(useCase);
//        }
        outcome.setCase(setImmediateDataFields(useCase));
        addMessageToOutcome(petriNet, CaseEventType.CREATE, outcome);
        publisher.publishEvent(new CreateCaseEvent(outcome, EventPhase.POST));
        return outcome;
    }

    protected Function<Case, String> resolveDefaultCaseTitle(String netId, Locale locale, Map<String, String> params) {
        PetriNet petriNet = new com.netgrif.application.engine.adapter.spring.petrinet.domain.PetriNet((com.netgrif.application.engine.adapter.spring.petrinet.domain.PetriNet) petriNetService.get(new ObjectId(netId)));
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
        List<Case> cases = mongoTemplate.find(query, Case.class);
        decryptDataSets(cases);
        return setImmediateDataFields(new PageImpl<Case>(cases, pageable, mongoTemplate.count(new BasicQuery(queryString, "{_id:1}"), Case.class)));
    }

    @Override
    public DeleteCaseEventOutcome deleteCase(String caseId, Map<String, String> params) {
        Case useCase = findOne(caseId);
        return deleteCase(useCase, params);
    }

    @Override
    public DeleteCaseEventOutcome deleteCase(String caseId) {
        return deleteCase(caseId, new HashMap<>());
    }

    @Override
    public DeleteCaseEventOutcome deleteCase(Case useCase, Map<String, String> params) {
        return deleteCase(useCase, params, false);
    }

    @Override
    public DeleteCaseEventOutcome deleteCase(Case useCase, Map<String, String> params, boolean force) {
        DeleteCaseEventOutcome outcome = null;
        if (!force) {
            outcome = new DeleteCaseEventOutcome(useCase, eventService.runActions(useCase.getPetriNet().getPreDeleteActions(), useCase, Optional.empty(), params));
            publisher.publishEvent(new DeleteCaseEvent(outcome, EventPhase.PRE));
            useCase = ((Evaluator<DeleteCaseEvent, Case>) evaluationService.getEvaluator("default")).apply(new DeleteCaseEvent(outcome, EventPhase.PRE));
        }
        log.info("[" + useCase.getStringId() + "]: User [" + userService.getLoggedOrSystem().getStringId() + "] is deleting case " + useCase.getTitle());

        taskService.deleteTasksByCase(useCase.getStringId());
        repository.delete(useCase);
        if (!force) {
            outcome.addOutcomes(eventService.runActions(useCase.getPetriNet().getPostDeleteActions(), null, Optional.empty(), params));
            addMessageToOutcome(useCase.getPetriNet(), CaseEventType.DELETE, outcome);
            ((Evaluator<DeleteCaseEvent, Case>) evaluationService.getEvaluator("noContext")).apply(new DeleteCaseEvent(outcome, EventPhase.POST));
            publisher.publishEvent(new DeleteCaseEvent(outcome, EventPhase.POST));
        }
        return outcome;
    }

    @Override
    public DeleteCaseEventOutcome deleteCase(Case useCase) {
        return deleteCase(useCase, false);
    }

    @Override
    public DeleteCaseEventOutcome deleteCase(Case useCase, boolean force) {
        return deleteCase(useCase, new HashMap<>(), force);
    }

    @Override
    public void deleteInstancesOfPetriNet(PetriNet net) {
        deleteInstancesOfPetriNet(net, false);
    }

    @Override
    public void deleteInstancesOfPetriNet(PetriNet net, boolean force) {
        log.info("[" + net.getStringId() + "]: User " + userService.getLoggedOrSystem().getStringId() + " is deleting all cases and tasks of Petri net " + net.getIdentifier() + " version " + net.getVersion().toString());
        List<Case> cases = this.searchAll(QCase.case$.petriNetObjectId.eq(net.getObjectId())).getContent();
        if (!cases.isEmpty()) {
            cases.forEach(aCase -> deleteCase(aCase, new HashMap<>(), force));
        }
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
        ObjectId objectId = extractObjectId(caseId);
        Optional<Case> caseOptional = repository.findByIdObjectId(objectId);
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
    public Page<Case> searchAll(Predicate predicate) {
        return search(predicate, new FullPageRequest());
    }

    @Override
    public Case searchOne(Predicate predicate) {
        Page<Case> page = search(predicate, PageRequest.of(0, 1));
        if (page.getContent().isEmpty())
            return null;
        return page.getContent().getFirst();
    }

    @Override
    public Map<String, I18nString> listToMap(List<Case> cases) {
        Map<String, I18nString> options = new HashMap<>();
        cases.forEach(aCase -> options.put(aCase.getStringId(), new I18nString(aCase.getTitle())));
        return options;
    }

    @Override
    public void setPetriNet(Case useCase) {
        PetriNet model = useCase.getPetriNet();
        if (model == null) {
            model = new com.netgrif.application.engine.adapter.spring.petrinet.domain.PetriNet((com.netgrif.application.engine.adapter.spring.petrinet.domain.PetriNet) petriNetService.get(new ObjectId(useCase.getPetriNetId())));
            useCase.setPetriNet(model);
        }
        model.initializeTokens(useCase.getActivePlaces());
        model.initializeArcs(useCase.getDataSet());
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

//    @Deprecated
//    public List<Field> getData(String caseId) {
//        ObjectId objectId = extractObjectId(caseId);
//        Optional<Case> optionalUseCase = repository.findByIdObjectId(objectId);
//        if (!optionalUseCase.isPresent())
//            throw new IllegalArgumentException("Could not find case with id [" + caseId + "]");
//        Case useCase = optionalUseCase.get();
//        List<Field> fields = new ArrayList<>();
//        useCase.getDataSet().forEach((id, dataField) -> {
//            if (dataField.isDisplayable() || useCase.getPetriNet().isDisplayableInAnyTransition(id)) {
//                Field field = fieldFactory.buildFieldWithoutValidation(useCase, id, null);
//                field.setBehavior(dataField.applyOnlyVisibleBehavior());
//                fields.add(field);
//            }
//        });
//
//        LongStream.range(0L, fields.size()).forEach(l -> fields.get((int) l).setOrder(l));
//        return fields;
//    }

    private void setImmediateDataFieldsReadOnly(Case useCase) {
        List<Field<?>> immediateData = new ArrayList<>();

        useCase.getImmediateDataFields().forEach(fieldId -> {
            try {
                Field clone = fieldFactory.buildImmediateField(useCase, fieldId);
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
        List<Field<?>> immediateData = new ArrayList<>();

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

    private EventOutcome addMessageToOutcome(PetriNet net, CaseEventType type, EventOutcome outcome) {
        if (net.getCaseEvents().containsKey(type)) {
            outcome.setMessage(net.getCaseEvents().get(type).getMessage());
        }
        return outcome;
    }

    private ObjectId extractObjectId(String caseId) {
        String[] parts = caseId.split(ProcessResourceId.ID_SEPARATOR);
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid NetgrifId format: " + caseId);
        }
        String objectIdPart = parts[1];

        return new ObjectId(objectIdPart);
    }

    private Case evaluateRules(CaseEvent event) {
        publisher.publishEvent(event);
        return findOne(event.getCaseEventOutcome().getCase().getStringId());
    }
}
