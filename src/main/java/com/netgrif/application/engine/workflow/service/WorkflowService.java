package com.netgrif.application.engine.workflow.service;

import com.google.common.collect.Ordering;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.authorization.service.interfaces.IRoleService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.history.domain.caseevents.CreateCaseEventLog;
import com.netgrif.application.engine.history.domain.caseevents.DeleteCaseEventLog;
import com.netgrif.application.engine.history.service.IHistoryService;
import com.netgrif.application.engine.importer.model.CaseEventType;
import com.netgrif.application.engine.importer.service.FieldFactory;
import com.netgrif.application.engine.petrinet.domain.I18nExpression;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.Process;
import com.netgrif.application.engine.petrinet.domain.arcs.Arc;
import com.netgrif.application.engine.petrinet.domain.arcs.ReferenceType;
import com.netgrif.application.engine.petrinet.domain.dataset.*;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ActionRunner;
import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.authorization.domain.permissions.CasePermission;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.rules.domain.facts.CaseCreatedFact;
import com.netgrif.application.engine.rules.service.interfaces.IRuleEngine;
import com.netgrif.application.engine.security.service.EncryptionService;
import com.netgrif.application.engine.utils.FullPageRequest;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.DataFieldValue;
import com.netgrif.application.engine.workflow.domain.QCase;
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
import org.springframework.context.annotation.Lazy;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    protected IPetriNetService petriNetService;

    @Autowired
    protected IRoleService roleService;

    @Autowired
    protected ITaskService taskService;

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
        if (useCase.getProcess() == null) {
            setPetriNet(useCase);
        }
        encryptDataSet(useCase);
        useCase = repository.save(useCase);
        try {
            useCase.resolveImmediateDataFields();
            elasticCaseService.indexNow(useCase);
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

    private void resolveUserRefPermissions(Case useCase, String userListId, Map<CasePermission, Boolean> permission) {
        List<String> userIds = getExistingUsers((UserListFieldValue) useCase.getDataSet().get(userListId).getRawValue());
        if (userIds != null && !userIds.isEmpty()) {
            if (permission.containsKey(CasePermission.VIEW) && !permission.get(CasePermission.VIEW)) {
//                TODO: release/8.0.0
//                useCase.getNegativeViewUsers().addAll(userIds);
            } else {
//                useCase.addUsers(new HashSet<>(userIds), permission);
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
            title = resolveDefaultCaseTitle(netId, locale, params);
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
        Process net = petriNetService.getNewestVersionByIdentifier(identifier);
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
        Process net = petriNetService.getNewestVersionByIdentifier(identifier);
        if (net == null) {
            throw new IllegalArgumentException("Petri net with identifier [" + identifier + "] does not exist.");
        }
        return this.createCase(net.getStringId(), title, color, user, params);
    }

    @Override
    public CreateCaseEventOutcome createCaseByIdentifier(String identifier, String title, String color, LoggedUser user) {
        Process net = petriNetService.getNewestVersionByIdentifier(identifier);
        if (net == null) {
            throw new IllegalArgumentException("Petri net with identifier [" + identifier + "] does not exist.");
        }
        return this.createCase(net.getStringId(), title, color, user);
    }

    public CreateCaseEventOutcome createCase(String netId, Function<Case, String> makeTitle, String color, LoggedUser user) {
        return this.createCase(netId, makeTitle, color, user, new HashMap<>());
    }

    // TODO: release/8.0.0 remove color
    public CreateCaseEventOutcome createCase(String netId, Function<Case, String> makeTitle, String color, LoggedUser user, Map<String, String> params) {
        LoggedUser loggedOrImpersonated = user.getSelfOrImpersonated();
        Process petriNet = petriNetService.clone(new ObjectId(netId));
        int rulesExecuted;
        Case useCase = new Case(petriNet);
        useCase.setAuthor(loggedOrImpersonated.transformToAuthor());
        useCase.setCreationDate(LocalDateTime.now());
        useCase.setTitle(makeTitle.apply(useCase));
        useCase = taskService.createTasks(useCase);
        dataSetInitializer.populateDataSet(useCase, params);
        roleService.resolveCaseRolesOnCase(useCase, useCase.getProcess().getCaseRolePermissions(), false);
        useCase = save(useCase);
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
//TODO: release/8.0.0
        resolveArcsWeight(useCase);
        taskService.reloadTasks(useCase);
        //TODO: release/8.0.0
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

    protected String resolveDefaultCaseTitle(String netId, Locale locale, Map<String, String> params) {
        Process petriNet = petriNetService.clone(new ObjectId(netId));
        I18nExpression caseTitle = petriNet.getDefaultCaseName();
        String title;
        if (caseTitle.isDynamic()) {
            title = initValueExpressionEvaluator.evaluateTitle(petriNet.getDefaultCaseName().getExpression(locale), params);
        } else {
            title = caseTitle.getTranslation(locale);
        }
        return title;
    }

    @Override
    public Page<Case> findAllByAuthor(String authorId, String petriNet, Pageable pageable) {
        Predicate query = QCase.case$.author.id.eq(authorId).and(QCase.case$.petriNetId.eq(petriNet));
        Page<Case> cases = repository.findAll(query, pageable);
        cases.forEach(this::initialize);
        return cases;
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
        DeleteCaseEventOutcome outcome = new DeleteCaseEventOutcome(useCase, eventService.runActions(useCase.getProcess().getPreDeleteActions(), useCase, Optional.empty(), params));
        historyService.save(new DeleteCaseEventLog(useCase, EventPhase.PRE));
        log.info("[{}]: User [{}] is deleting case {}", useCase.getStringId(), userService.getLoggedOrSystem().getStringId(), useCase.getTitle());

        roleService.removeAllByCase(useCase.getStringId());
        taskService.deleteTasksByCase(useCase.getStringId());
        repository.delete(useCase);

        outcome.addOutcomes(eventService.runActions(useCase.getProcess().getPostDeleteActions(), null, Optional.empty(), params));
        addMessageToOutcome(useCase.getProcess(), CaseEventType.DELETE, outcome);
        historyService.save(new DeleteCaseEventLog(useCase, EventPhase.POST));
        return outcome;
    }

    @Override
    public DeleteCaseEventOutcome deleteCase(Case useCase) {
        return deleteCase(useCase, new HashMap<>());
    }

    @Override
    public void deleteInstancesOfPetriNet(Process net) {
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
