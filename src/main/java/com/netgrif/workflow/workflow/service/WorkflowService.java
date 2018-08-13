package com.netgrif.workflow.workflow.service;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.event.events.usecase.CreateCaseEvent;
import com.netgrif.workflow.event.events.usecase.DeleteCaseEvent;
import com.netgrif.workflow.event.events.usecase.UpdateMarkingEvent;
import com.netgrif.workflow.importer.service.FieldFactory;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.dataset.CaseField;
import com.netgrif.workflow.petrinet.domain.dataset.Field;
import com.netgrif.workflow.petrinet.domain.dataset.FieldType;
import com.netgrif.workflow.petrinet.domain.repositories.PetriNetRepository;
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.workflow.petrinet.web.responsebodies.PetriNetReference;
import com.netgrif.workflow.security.service.EncryptionService;
import com.netgrif.workflow.utils.FullPageRequest;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.DataField;
import com.netgrif.workflow.workflow.domain.QCase;
import com.netgrif.workflow.workflow.domain.Task;
import com.netgrif.workflow.workflow.domain.repositories.CaseRepository;
import com.netgrif.workflow.workflow.service.interfaces.ITaskService;
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.StreamSupport;

@Service
public class WorkflowService implements IWorkflowService {

    private static final Logger log = Logger.getLogger(WorkflowService.class);

    @Autowired
    private CaseRepository repository;

    @Autowired
    private PetriNetRepository petriNetRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private IPetriNetService petriNetService;

    @Autowired
    private ITaskService taskService;

    @Autowired
    private CaseSearchService searchService;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private EncryptionService encryptionService;

    @Autowired
    private FieldFactory fieldFactory;

    @Override
    public Case save(Case useCase) {
        encryptDataSet(useCase);
        return repository.save(useCase);
    }

    @Override
    public Case findOne(String caseId) {
        Case useCase = repository.findOne(caseId);
        if (useCase == null)
            return null;
        decryptDataSet(useCase);
        return useCase;
    }

    @Override
    public Page<Case> getAll(Pageable pageable) {
        //page.getContent().forEach(aCase -> aCase.getPetriNet().initializeArcs());
        Page<Case> page = repository.findAll(pageable);
        decryptDataSets(page.getContent());
        return setImmediateDataFields(page);
    }

    public Page<Case> searchCase(List<String> nets, Pageable pageable) {
        StringBuilder queryBuilder = new StringBuilder();
        nets.forEach(net -> {
            queryBuilder.append("{$ref:\"petriNet\",$id:{$oid:\"");
            queryBuilder.append(net);
            queryBuilder.append("\"}},");
        });
        if (queryBuilder.length() > 0)
            queryBuilder.deleteCharAt(queryBuilder.length() - 1);
        String queryString = nets.isEmpty() ? "{}" : "{petriNet:{$in:[" + queryBuilder.toString() + "]}}";
        BasicQuery query = new BasicQuery(queryString);
        query = (BasicQuery) query.with(pageable);
        List<Case> useCases = mongoTemplate.find(query, Case.class);
        decryptDataSets(useCases);
        return setImmediateDataFields(new PageImpl<Case>(useCases, pageable, mongoTemplate.count(new BasicQuery(queryString, "{_id:1}"), Case.class)));
    }

    public Page<Case> search(Map<String, Object> request, Pageable pageable, LoggedUser user, Locale locale) {
        String key = "petriNet";
        Map<String, List<String>> idMap = new HashMap<>();

        List<PetriNetReference> nets = petriNetService.getReferencesByUsersProcessRoles(user, locale);
        if (request.containsKey(key)) {
            Set<String> netIds = nets.stream().map(PetriNetReference::getStringId).collect(Collectors.toSet());
            if (request.get(key) instanceof String && !netIds.contains(request.get(key)))
                return new PageImpl<Case>(new ArrayList<>(), pageable, 0);
            else if (request.get(key) instanceof List) {
                idMap.put("id", ((List<String>) request.get(key)).stream().filter(netIds::contains).collect(Collectors.toList()));
                request.put(key, idMap);
            }
        } else if (!nets.isEmpty()) {
            idMap.put("id", nets.stream().map(PetriNetReference::getStringId).collect(Collectors.toList()));
            request.put(key, idMap);
        }
        Page<Case> page = searchService.search(request, pageable, Case.class);
        decryptDataSets(page.getContent());
        return setImmediateDataFields(page);
    }

    @Override
    public Page<Case> search(Predicate predicate, Pageable pageable) {
        Page<Case> page = repository.findAll(predicate, pageable);
        return setImmediateDataFields(page);
    }

    @Override
    public Page<Case> fullTextSearch(String processIdentifier, String searchPhrase, Pageable pageable) {
        PetriNet petriNet = petriNetService.getNewestVersionByIdentifier(processIdentifier);
        if (petriNet == null)
            throw new IllegalArgumentException("Process with identifier " + processIdentifier + " was not found");
        //TODO include createDate
        List<BooleanExpression> predicates = new ArrayList<>();
        predicates.add(QCase.case$.visualId.containsIgnoreCase(searchPhrase));
        predicates.add(QCase.case$.title.containsIgnoreCase(searchPhrase));
        predicates.add(QCase.case$.author.fullName.containsIgnoreCase(searchPhrase));
        predicates.add(QCase.case$.author.email.containsIgnoreCase(searchPhrase));

        try {
            LocalDateTime creation = FieldFactory.parseDateTime(searchPhrase);
            if (creation != null)
                predicates.add(QCase.case$.creationDate.eq(creation));
        } catch (Exception e) {
            //ignore
        }

        petriNet.getImmediateFields().forEach(field -> {
            try {
                if (field.getType() == FieldType.TEXT || field.getType() == FieldType.ENUMERATION) {
                    Path<?> path = QCase.case$.dataSet.get(field.getStringId()).value;
                    Expression<String> constant = Expressions.constant(searchPhrase);
                    predicates.add(Expressions.predicate(Ops.STRING_CONTAINS_IC, path, constant));
                } else if (field.getType() == FieldType.NUMBER) {
                    Double value = FieldFactory.parseDouble(searchPhrase);
                    if (value != null)
                        predicates.add(QCase.case$.dataSet.get(field.getStringId()).value.eq(value));
                } else if (field.getType() == FieldType.DATE) {
                    LocalDate value = FieldFactory.parseDate(searchPhrase);
                    if (value != null)
                        predicates.add(QCase.case$.dataSet.get(field.getStringId()).value.eq(value));
                } else if (field.getType() == FieldType.DATETIME) {
                    LocalDateTime value = FieldFactory.parseDateTime(searchPhrase);
                    if (value != null)
                        predicates.add(QCase.case$.dataSet.get(field.getStringId()).value.eq(value));
                }
            } catch (Exception e) {
                log.error(e.getMessage());
                //Skip this field in search
            }
        });

        Predicate casePredicate = QCase.case$.processIdentifier.eq(processIdentifier)
                .andAnyOf(predicates.toArray(new BooleanExpression[0]));
        return search(casePredicate, pageable);
    }

    @Override
    public List<Case> getCaseFieldChoices(Pageable pageable, String caseId, String fieldId) {
        Case useCase = repository.findOne(caseId);
        CaseField field = (CaseField) useCase.getPetriNet().getDataSet().get(fieldId);

        List<Case> list = new LinkedList<>();
        field.getConstraintNetIds().forEach((netImportId, fieldImportIds) -> {
            PetriNet net = petriNetRepository.findOne(netImportId);
            list.addAll(repository.findAllByProcessIdentifier(net.getIdentifier()));
        });

        return list;
    }

    @Override
    public Case createCase(String netId, String title, String color, LoggedUser user) {
        PetriNet petriNet = petriNetService.getPetriNet(netId);
        Case useCase = new Case(title, petriNet, petriNet.getActivePlaces());
        useCase.setProcessIdentifier(petriNet.getIdentifier());
        useCase.setColor(color);
        useCase.setAuthor(user.transformToAuthor());
        useCase.setIcon(petriNet.getIcon());
        useCase.setCreationDate(LocalDateTime.now());
        useCase = save(useCase);

        publisher.publishEvent(new CreateCaseEvent(useCase));
        log.info("Case " + title + " created");

        useCase.getPetriNet().initializeVarArcs(useCase.getDataSet());
//        taskService.createTasks(useCase);
        taskService.reloadTasks(useCase);
//        useCase = save(useCase);

        useCase = findOne(useCase.getStringId());
        return setImmediateDataFields(useCase);
    }

    @Override
    public Page<Case> findAllByAuthor(Long authorId, String petriNet, Pageable pageable) {
        String queryString = "{author.id:" + authorId + ", petriNet:{$ref:\"petriNet\",$id:{$oid:\"" + petriNet + "\"}}}";
        BasicQuery query = new BasicQuery(queryString);
        query = (BasicQuery) query.with(pageable);
        List<Case> cases = mongoTemplate.find(query, Case.class);
        decryptDataSets(cases);
        return setImmediateDataFields(new PageImpl<Case>(cases, pageable, mongoTemplate.count(new BasicQuery(queryString, "{_id:1}"), Case.class)));
    }

    @Override
    public void deleteCase(String caseId) {
        Case useCase = repository.findOne(caseId);
        taskService.deleteTasksByCase(caseId);
        repository.delete(useCase);

        publisher.publishEvent(new DeleteCaseEvent(useCase));
    }

    @Override
    public void updateMarking(Case useCase) {
        PetriNet net = useCase.getPetriNet();
        useCase.setActivePlaces(net.getActivePlaces());

        publisher.publishEvent(new UpdateMarkingEvent(useCase));
    }

    @Override
    public boolean removeTasksFromCase(Iterable<? extends Task> tasks, String caseId) {
        return removeTasksFromCase(tasks, repository.findOne(caseId));
    }

    @Override
    public boolean removeTasksFromCase(Iterable<? extends Task> tasks, Case useCase) {
        boolean deleteSuccess = useCase.removeTasks(StreamSupport.stream(tasks.spliterator(), false).collect(Collectors.toList()));
        useCase = repository.save(useCase);
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

    public List<Field> getData(String caseId) {
        Case useCase = repository.findOne(caseId);
        List<Field> fields = new ArrayList<>();
        useCase.getDataSet().forEach((id, dataField) -> {
            if (dataField.isDisplayable() || useCase.getPetriNet().isDisplayableInAnyTransition(id)) {
                Field field = fieldFactory.buildFieldWithoutValidation(useCase, id);
                field.setBehavior(dataField.applyOnlyVisibleBehavior());
                fields.add(field);
            }
        });

        LongStream.range(0L, fields.size()).forEach(l -> fields.get((int) l).setOrder(l));
        return fields;
    }

    private Page<Case> setImmediateDataFields(Page<Case> cases) {
        cases.getContent().forEach(this::setImmediateDataFields);
        return cases;
    }

    private Case setImmediateDataFields(Case useCase) {
        List<Field> immediateData = new ArrayList<>();

        useCase.getImmediateDataFields().forEach(fieldId ->
                immediateData.add(fieldFactory.buildFieldWithoutValidation(useCase, fieldId))
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
}