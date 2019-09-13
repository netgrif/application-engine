package com.netgrif.workflow.workflow.service;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.elastic.domain.ElasticCase;
import com.netgrif.workflow.elastic.service.ElasticCaseService;
import com.netgrif.workflow.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.workflow.event.events.usecase.CreateCaseEvent;
import com.netgrif.workflow.event.events.usecase.DeleteCaseEvent;
import com.netgrif.workflow.event.events.usecase.UpdateMarkingEvent;
import com.netgrif.workflow.importer.service.FieldFactory;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.dataset.CaseField;
import com.netgrif.workflow.petrinet.domain.dataset.Field;
import com.netgrif.workflow.petrinet.domain.repositories.PetriNetRepository;
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.workflow.security.service.EncryptionService;
import com.netgrif.workflow.utils.FullPageRequest;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.DataField;
import com.netgrif.workflow.workflow.domain.Task;
import com.netgrif.workflow.workflow.domain.repositories.CaseRepository;
import com.netgrif.workflow.workflow.service.interfaces.ITaskService;
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService;
import com.querydsl.core.types.Predicate;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
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

@Service
public class WorkflowService implements IWorkflowService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowService.class);

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

    @Autowired
    @Lazy
    private IElasticCaseService elasticCaseService;

    @Override
    public Case save(Case useCase) {
        encryptDataSet(useCase);
        useCase = repository.save(useCase);
        if (useCase.getPetriNet() == null) {
            setPetriNet(useCase);
        }
        try {
            setImmediateDataFields(useCase);
            elasticCaseService.indexNow(new ElasticCase(useCase));
        } catch (Exception e) {
            log.error("Indexing failed ["+useCase.getStringId()+"]", e);
        }
        return useCase;
    }

    @Override
    public Case findOne(String caseId) {
        Optional<Case> caseOptional = repository.findById(caseId);
        if (!caseOptional.isPresent())
            throw new IllegalArgumentException("Could not find Case with id ["+caseId+"]");
        Case useCase = caseOptional.get();
        setPetriNet(useCase);
        decryptDataSet(useCase);
        return useCase;
    }

    @Override
    public List<Case> findAllById(List<String> ids) {
        List<Case> page = new LinkedList<>();
        ids.forEach(id -> {
            Optional<Case> useCase = repository.findById(id);
            useCase.ifPresent(page::add);
        });
        if (page.size() > 0) {
            page.forEach(c -> c.setPetriNet(petriNetService.get(c.getPetriNetObjectId())));
            decryptDataSets(page);
            page.forEach(this::setImmediateDataFieldsReadOnly);
        }
        return page;
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
        Page<Case> page = repository.findAll(searchPredicate, pageable);
        page.getContent().forEach(this::setPetriNet);
        decryptDataSets(page.getContent());
        return setImmediateDataFields(page);
    }

    @Override
    public long count(Map<String, Object> request, LoggedUser user, Locale locale) {
        Predicate searchPredicate = searchService.buildQuery(request, user, locale);
        return repository.count(searchPredicate);
    }

    @Override
    public List<Case> getCaseFieldChoices(Pageable pageable, String caseId, String fieldId) {
        Optional<Case> caseOptional = repository.findById(caseId);
        if (!caseOptional.isPresent())
            throw new IllegalArgumentException("Could not find case with id ["+caseId+"]");
        Case useCase = caseOptional.get();

        CaseField field = (CaseField) useCase.getPetriNet().getDataSet().get(fieldId);

        List<Case> list = new LinkedList<>();
        field.getConstraintNetIds().forEach((netImportId, fieldImportIds) -> {
            Optional<PetriNet> netOptional = petriNetRepository.findById(netImportId);
            if (!netOptional.isPresent())
                throw new IllegalArgumentException("Could not find model with id ["+netImportId+"]");
            list.addAll(repository.findAllByProcessIdentifier(netOptional.get().getIdentifier()));
        });

        return list;
    }

    @Override
    public Case createCase(String netId, String title, String color, LoggedUser user) {
        PetriNet petriNet = petriNetService.clone(new ObjectId(netId));
        Case useCase = new Case(title, petriNet, petriNet.getActivePlaces());
        useCase.setProcessIdentifier(petriNet.getIdentifier());
        useCase.setColor(color);
        useCase.setAuthor(user.transformToAuthor());
        useCase.setIcon(petriNet.getIcon());
        useCase.setCreationDate(LocalDateTime.now());
        useCase = save(useCase);

        publisher.publishEvent(new CreateCaseEvent(useCase));
        log.info("["+useCase.getStringId()+"]: Case " + title + " created");

        useCase.getPetriNet().initializeVarArcs(useCase.getDataSet());
        taskService.reloadTasks(useCase);

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
        Optional<Case> caseOptional = repository.findById(caseId);
        if (!caseOptional.isPresent())
            throw new IllegalArgumentException("Could not find case with id ["+caseId+"]");
        Case useCase = caseOptional.get();
        log.info("["+caseId+"]: Deleting case " + useCase.getTitle());

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
        Optional<Case> caseOptional = repository.findById(caseId);
        if (!caseOptional.isPresent())
            throw new IllegalArgumentException("Could not find case with id ["+caseId+"]");
        Case useCase = caseOptional.get();
        return removeTasksFromCase(tasks, useCase);
    }

    @Override
    public boolean removeTasksFromCase(Iterable<? extends Task> tasks, Case useCase) {
        if (StreamSupport.stream(tasks.spliterator(), false).count() == 0) {
            return true;
        }
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

    @Override
    public Case searchOne(Predicate predicate) {
        Page<Case> page = search(predicate, new PageRequest(0,1));
        if (page.getContent().isEmpty())
            return null;
        return page.getContent().get(0);
    }

    public List<Field> getData(String caseId) {
        Optional<Case> optionalUseCase = repository.findById(caseId);
        if (!optionalUseCase.isPresent())
            throw new IllegalArgumentException("Could not find case with id ["+caseId+"]");
        Case useCase = optionalUseCase.get();
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

    private void setImmediateDataFieldsReadOnly(Case useCase) {
        List<Field> immediateData = new ArrayList<>();

        useCase.getImmediateDataFields().forEach(fieldId -> {
            try {
                Field field = fieldFactory.buildImmediateField(useCase, fieldId);
                Field clone = field.clone();
                clone.setValue(field.getValue());
                immediateData.add(clone);
            } catch (Exception e) {
                log.error("Could not built immediate field [" + fieldId + "]");
            }
        });
        LongStream.range(0L, immediateData.size()).forEach(index -> immediateData.get((int) index).setOrder(index));

        useCase.setImmediateData(immediateData);
    }

    private Page<Case> setImmediateDataFields(Page<Case> cases) {
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
        PetriNet model = petriNetService.clone(useCase.getPetriNetObjectId());
        model.initializeTokens(useCase.getActivePlaces());
        model.initializeVarArcs(useCase.getDataSet());
        useCase.setPetriNet(model);
    }
}