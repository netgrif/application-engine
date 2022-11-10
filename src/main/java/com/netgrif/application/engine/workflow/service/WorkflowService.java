package com.netgrif.application.engine.workflow.service;

import com.google.common.collect.Ordering;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseMappingService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.application.engine.history.domain.caseevents.CreateCaseEventLog;
import com.netgrif.application.engine.history.domain.caseevents.DeleteCaseEventLog;
import com.netgrif.application.engine.history.service.IHistoryService;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.service.FieldFactory;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.petrinet.domain.dataset.TaskField;
import com.netgrif.application.engine.petrinet.domain.dataset.TextField;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.FieldActionsRunner;
import com.netgrif.application.engine.petrinet.domain.events.CaseEventType;
import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.petrinet.service.interfaces.IProcessRoleService;
import com.netgrif.application.engine.rules.domain.facts.CaseCreatedFact;
import com.netgrif.application.engine.rules.service.interfaces.IRuleEngine;
import com.netgrif.application.engine.security.service.EncryptionService;
import com.netgrif.application.engine.utils.FullPageRequest;
import com.netgrif.application.engine.workflow.domain.Case;
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
import java.util.stream.StreamSupport;

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
        resolveUserRef(useCase);
        taskService.resolveUserRef(useCase);
        try {
//            TODO: NAE-1645 not needed?
//            setImmediateDataFields(useCase);
            elasticCaseService.indexNow(this.caseMappingService.transform(useCase));
        } catch (Exception e) {
            log.error("Indexing failed [" + useCase.getStringId() + "]", e);
        }
        return useCase;
    }

    @Override
    public Case findOne(String caseId) {
        Optional<Case> caseOptional = repository.findById(caseId);
        if (caseOptional.isEmpty())
            throw new IllegalArgumentException("Could not find Case with id [" + caseId + "]");
        Case useCase = caseOptional.get();
        setPetriNet(useCase);
        decryptDataSet(useCase);
//        TODO: NAE-1645
//        this.setImmediateDataFieldsReadOnly(useCase);
        return useCase;
    }

    @Override
    public List<Case> findAllById(List<String> ids) {
        return repository.findAllBy_idIn(ids).stream()
                .filter(Objects::nonNull)
                .sorted(Ordering.explicit(ids).onResultOf(Case::getStringId))
                .peek(caze -> {
                    caze.setPetriNet(petriNetService.get(caze.getPetriNetObjectId()));
                    decryptDataSet(caze);
//                    TODO: NAE-1645
//                    setImmediateDataFieldsReadOnly(caze);
                })
                .collect(Collectors.toList());
    }

    @Override
    public Page<Case> getAll(Pageable pageable) {
        Page<Case> page = repository.findAll(pageable);
        page.getContent().forEach(this::setPetriNet);
        decryptDataSets(page.getContent());
//        TODO: NAE-1645
//        return setImmediateDataFields(page);
        return page;
    }

    @Override
    public Page<Case> search(Predicate predicate, Pageable pageable) {
        Page<Case> page = repository.findAll(predicate, pageable);
        page.getContent().forEach(this::setPetriNet);
//        TODO: NAE-1645
//        return setImmediateDataFields(page);
        return page;
    }

    @Override
    public Case resolveUserRef(Case useCase) {
        useCase.getUsers().clear();
        useCase.getNegativeViewUsers().clear();
        useCase.getUserRefs().forEach((id, permission) -> {
            List<String> userIds = getExistingUsers((List<String>) useCase.getDataSet().get(id).getValue());
            if (userIds != null && userIds.size() != 0 && permission.containsKey("view") && !permission.get("view")) {
                useCase.getNegativeViewUsers().addAll(userIds);
            } else if (userIds != null && userIds.size() != 0) {
                useCase.addUsers(new HashSet<>(userIds), permission);
            }
        });
        useCase.resolveViewUsers();
        return repository.save(useCase);
    }

    private List<String> getExistingUsers(List<String> userIds) {
        if (userIds == null)
            return null;
        return userIds.stream().filter(userId -> userService.resolveById(userId, false) != null).collect(Collectors.toList());
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
        PetriNet petriNet = petriNetService.clone(new ObjectId(netId));
        Case useCase = new Case(petriNet);
//        TODO: NAE-1645
//        useCase.populateDataSet(initValueExpressionEvaluator);
        useCase.setColor(color);
        useCase.setAuthor(user.transformToAuthor());
        useCase.setCreationDate(LocalDateTime.now());
        useCase.setTitle(makeTitle.apply(useCase));

        CreateCaseEventOutcome outcome = new CreateCaseEventOutcome();
        outcome.addOutcomes(eventService.runActions(petriNet.getPreCreateActions(), null, Optional.empty()));
        ruleEngine.evaluateRules(useCase, new CaseCreatedFact(useCase.getStringId(), EventPhase.PRE));
        useCase = save(useCase);

        historyService.save(new CreateCaseEventLog(useCase, EventPhase.PRE));
        log.info("[" + useCase.getStringId() + "]: Case " + useCase.getTitle() + " created");

        useCase.getPetriNet().initializeArcs(useCase.getDataSet());
        taskService.reloadTasks(useCase);
        useCase = findOne(useCase.getStringId());
        resolveTaskRefs(useCase);

        useCase = findOne(useCase.getStringId());
        outcome.addOutcomes(eventService.runActions(petriNet.getPostCreateActions(), useCase, Optional.empty()));
        useCase = findOne(useCase.getStringId());
        ruleEngine.evaluateRules(useCase, new CaseCreatedFact(useCase.getStringId(), EventPhase.POST));
        useCase = save(useCase);

        historyService.save(new CreateCaseEventLog(useCase, EventPhase.POST));
//        TODO: NAE-1645
//        outcome.setCase(setImmediateDataFields(useCase));
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
//        TODO: NAE-1645 remove mongoTemplates from project
        List<Case> cases = mongoTemplate.find(query, Case.class);
        decryptDataSets(cases);
//        TODO: NAE-1645
//        return setImmediateDataFields(new PageImpl<Case>(cases, pageable, mongoTemplate.count(new BasicQuery(queryString, "{_id:1}"), Case.class)));
        return new PageImpl<>(cases, pageable, mongoTemplate.count(new BasicQuery(queryString, "{_id:1}"), Case.class));
    }

    @Override
    public DeleteCaseEventOutcome deleteCase(String caseId) {
        Case useCase = findOne(caseId);

        DeleteCaseEventOutcome outcome = new DeleteCaseEventOutcome(useCase, eventService.runActions(useCase.getPetriNet().getPreDeleteActions(), useCase, Optional.empty()));
        historyService.save(new DeleteCaseEventLog(useCase, EventPhase.PRE));
        log.info("[" + caseId + "]: User [" + userService.getLoggedOrSystem().getStringId() + "] is deleting case " + useCase.getTitle());

        taskService.deleteTasksByCase(caseId);
        repository.delete(useCase);

        outcome.addOutcomes(eventService.runActions(useCase.getPetriNet().getPostDeleteActions(), null, Optional.empty()));
        addMessageToOutcome(petriNetService.clone(useCase.getPetriNetObjectId()), CaseEventType.DELETE, outcome);
        historyService.save(new DeleteCaseEventLog(useCase, EventPhase.POST));
        return outcome;
    }

    @Override
    public void deleteInstancesOfPetriNet(PetriNet net) {
        log.info("[" + net.getStringId() + "]: User " + userService.getLoggedOrSystem().getStringId() + " is deleting all cases and tasks of Petri net " + net.getIdentifier() + " version " + net.getVersion().toString());

        taskService.deleteTasksByPetriNetId(net.getStringId());
        repository.deleteAllByPetriNetObjectId(net.getObjectId());
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
    public boolean removeTasksFromCase(Iterable<? extends Task> tasks, String caseId) {
        Optional<Case> caseOptional = repository.findById(caseId);
        if (!caseOptional.isPresent())
            throw new IllegalArgumentException("Could not find case with id [" + caseId + "]");
        Case useCase = caseOptional.get();
        return removeTasksFromCase(tasks, useCase);
    }

    @Override
    public boolean removeTasksFromCase(Iterable<? extends Task> tasks, Case useCase) {
        if (StreamSupport.stream(tasks.spliterator(), false).count() == 0) {
            return true;
        }
        boolean deleteSuccess = useCase.removeTasks(StreamSupport.stream(tasks.spliterator(), false).collect(Collectors.toList()));
        save(useCase);
        return deleteSuccess;
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
        Map<Field, String> dataFields = getEncryptedDataSet(useCase);

        for (Map.Entry<Field, String> entry : dataFields.entrySet()) {
            Field dataField = entry.getKey();
            if (!(dataField instanceof TextField)) {
                continue;
            }
            String encryption = entry.getValue();
            String value = ((TextField) dataField).getValue().getValue();
            if (value == null) {
                continue;
            }
            dataField.setValue(method.apply(Pair.of(value, encryption)));
        }
    }

    private Map<Field, String> getEncryptedDataSet(Case useCase) {
        PetriNet net = useCase.getPetriNet();
        Map<Field, String> encryptedDataSet = new HashMap<>();

        for (Map.Entry<String, Field> entry : net.getDataSet().entrySet()) {
            String encryption = entry.getValue().getEncryption();
            if (encryption != null) {
                encryptedDataSet.put(useCase.getDataSet().get(entry.getKey()), encryption);
            }
        }

        return encryptedDataSet;
    }

    private void setPetriNet(Case useCase) {
        PetriNet model = petriNetService.clone(useCase.getPetriNetObjectId());
        model.initializeTokens(useCase.getActivePlaces());
        model.initializeArcs(useCase.getDataSet());
        useCase.setPetriNet(model);
    }

    private EventOutcome addMessageToOutcome(PetriNet net, CaseEventType type, EventOutcome outcome) {
        if (net.getCaseEvents().containsKey(type)) {
            outcome.setMessage(net.getCaseEvents().get(type).getMessage());
        }
        return outcome;
    }
}
