package com.netgrif.workflow.importer;

import com.netgrif.workflow.importer.model.*;
import com.netgrif.workflow.importer.model.DataLogic;
import com.netgrif.workflow.petrinet.domain.*;
import com.netgrif.workflow.petrinet.domain.dataset.Field;
import com.netgrif.workflow.petrinet.domain.dataset.logic.FieldBehavior;
import com.netgrif.workflow.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.workflow.petrinet.domain.repositories.PetriNetRepository;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRole;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRoleRepository;
import com.netgrif.workflow.petrinet.service.ArcFactory;
import com.netgrif.workflow.workflow.domain.triggers.Trigger;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.*;
import java.util.stream.Stream;

@Component
public class Importer {

    private static final Logger log = Logger.getLogger(Importer.class.getName());

    public static final String FIELD_KEYWORD = "f";
    public static final String TRANSITION_KEYWORD = "t";

    private Document document;
    private PetriNet net;
    private Map<Long, ProcessRole> roles;
    private Map<Long, Field> fields;
    private Map<Long, Transition> transitions;
    private Map<Long, Place> places;
    private Map<Long, Transaction> transactions;

    private ImportFieldFactory fieldFactory;
    private ImportTriggerFactory triggerFactory;

    @Autowired
    private PetriNetRepository repository;

    @Autowired
    private ProcessRoleRepository roleRepository;

    public Importer() {
        this.roles = new HashMap<>();
        this.transitions = new HashMap<>();
        this.places = new HashMap<>();
        this.fields = new HashMap<>();
        this.fieldFactory = new ImportFieldFactory(this);
        this.triggerFactory = new ImportTriggerFactory(this);
        this.transactions = new HashMap<>();
    }

    @Transactional
    public PetriNet importPetriNet(File xml, String title, String initials) {
        try {
            unmarshallXml(xml);
            return createPetriNet(title, initials);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Transactional
    protected void unmarshallXml(File xml) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(Document.class);

        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        document = (Document) jaxbUnmarshaller.unmarshal(xml);
    }

    @Transactional
    protected PetriNet createPetriNet(String title, String initials) {
        net = new PetriNet();
        net.setImportId(document.getId());
        net.setTitle(title);
        net.setInitials(initials);

        Arrays.stream(document.getImportRoles()).forEach(this::createRole);
        Arrays.stream(document.getImportData()).forEach(this::createDataSet);
        Arrays.stream(document.getImportTransactions()).forEach(this::createTransaction);
        Arrays.stream(document.getImportPlaces()).forEach(this::createPlace);
        Arrays.stream(document.getImportTransitions()).forEach(this::createTransition);
        Arrays.stream(document.getImportArc()).forEach(this::createArc);

        //Resolve actions after everything has object id
        Arrays.stream(document.getImportTransitions()).forEach(trans -> {
            if (trans.getDataRef() != null) {
                Arrays.stream(trans.getDataRef()).forEach(ref -> {
                    if (ref.getLogic().getAction() != null) {
                        String fieldId = fields.get(ref.getId()).getObjectId();
                        transitions.get(trans.getId()).addActions(fieldId, buildActions(ref.getLogic().getAction(),
                                fieldId,
                                transitions.get(trans.getId()).getStringId()));
                    }
                });
            }
        });
        Arrays.stream(document.getImportData()).forEach(data -> {
            if (data.getAction() != null) {
                fields.get(data.getId()).setActions(buildActions(data.getAction(), fields.get(data.getId()).getObjectId(), null));
            }
        });
        return repository.save(net);
    }

    @Transactional
    protected void createArc(ImportArc importArc) {
        Arc arc = ArcFactory.getArc(importArc.getType());
        arc.setMultiplicity(importArc.getMultiplicity());
        arc.setSource(getNode(importArc.getSourceId()));
        arc.setDestination(getNode(importArc.getDestinationId()));

        net.addArc(arc);
    }

    @Transactional
    protected void createDataSet(ImportData importData) {
        Field field = fieldFactory.getField(importData);

        net.addDataSetField(field);
        fields.put(importData.getId(), field);
    }

    @Transactional
    protected void createTransition(ImportTransition importTransition) {
        Transition transition = new Transition();
        transition.setTitle(importTransition.getLabel());
        transition.setPosition(importTransition.getX(), importTransition.getY());
        transition.setPriority(importTransition.getPriority());

        if (importTransition.getRoleRef() != null) {
            Arrays.stream(importTransition.getRoleRef()).forEach(roleRef ->
                    addRoleLogic(transition, roleRef)
            );
        }
        if (importTransition.getDataRef() != null) {
            Arrays.stream(importTransition.getDataRef()).forEach(dataRef ->
                    addDataLogic(transition, dataRef)
            );
        }
        if (importTransition.getTrigger() != null) {
            Arrays.stream(importTransition.getTrigger()).forEach(trigger ->
                    addTrigger(transition, trigger)
            );
        }
        if (importTransition.getTransactionRef() != null) {
            addToTransaction(transition, importTransition.getTransactionRef());
        }
        if (importTransition.getDataGroup() != null) {
            addDataGroups(transition, importTransition.getDataGroup());
        }

        net.addTransition(transition);
        transitions.put(importTransition.getId(), transition);
    }

    @Transactional
    protected void addDataGroups(Transition transition, ImportDataGroup[] dataGroups) {
        Stream.of(dataGroups).forEach(importDataGroup -> {
            DataGroup dataGroup = new DataGroup(importDataGroup.getTitle(), importDataGroup.getAlignment(), importDataGroup.getStretch());
            Stream.of(importDataGroup.getDataRef()).forEach(dataRef -> dataGroup.addData(fields.get(dataRef.getId()).getObjectId()));
            transition.addDataGroup(dataGroup);
        });
    }

    @Transactional
    protected void addToTransaction(Transition transition, TransactionRef transactionRef) {
        Transaction transaction = transactions.get(transactionRef.getId());
        if (transaction == null)
            throw new IllegalArgumentException("Referenced transaction [" + transactionRef.getId() + "] in transition [" + transition.getTitle() + "] doesn't exist.");
        transaction.addTransition(transition);
    }

    @Transactional
    protected void addRoleLogic(Transition transition, RoleRef roleRef) {
        RoleLogic logic = roleRef.getLogic();
        String roleId = roles.get(roleRef.getId()).getObjectId();

        if (logic == null || roleId == null)
            return;

        transition.addRole(roleId, ImportRoleFactory.getPermissions(logic));
    }

    @Transactional
    protected void addDataLogic(Transition transition, DataRef dataRef) {
        DataLogic logic = dataRef.getLogic();
        try {
            String fieldId = fields.get(dataRef.getId()).getObjectId();
            if (logic == null || fieldId == null)
                return;

            Set<FieldBehavior> behavior = new HashSet<>();
            if (logic.getBehavior() != null)
                Arrays.stream(logic.getBehavior()).forEach(b -> behavior.add(FieldBehavior.fromString(b)));

            transition.addDataSet(fieldId, behavior, null);
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Wrong dataRef id [" + dataRef.getId() + "] on transition [" + transition.getTitle() + "]", e);
        }
    }

    @Transactional
    protected LinkedHashSet<Action> buildActions(ImportAction[] imported, String fieldId, String transitionId) {
        final LinkedHashSet<Action> actions = new LinkedHashSet<>();
        Arrays.stream(imported).forEach(action -> {
            if (action.getTrigger() == null)
                throw new IllegalArgumentException("Action [" + action.getDefinition() + "] doesn't have trigger");

            String definition = action.getDefinition();
            try {
                definition = parseObjectIds(definition, fieldId, FIELD_KEYWORD);
                definition = parseObjectIds(definition, transitionId, TRANSITION_KEYWORD);
            } catch (NumberFormatException e) {
//                todo: message
                throw new IllegalArgumentException("Error parsing ids of action [" + action.getDefinition() + "]", e);
            }
            actions.add(new Action(definition, action.getTrigger()));
        });
        return actions;
    }

    @Transactional
    protected String parseObjectIds(String action, String currentId, String processedObject) {
        action = action.replace("\n", "").replace("  ", "");
        int last = 0;
        try {
            while (true) {
                int start = action.indexOf(processedObject + ".", last);
                if (start == -1) break;
                int coma = action.indexOf(',', start);
                int semicolon = action.indexOf(';', start);
                int delimeter = coma < semicolon && coma != -1 ? coma : semicolon;

                String id = action.substring(start + 2, delimeter);
                String objectId = id.equalsIgnoreCase("this") ? currentId : getObjectId(processedObject, Long.parseLong(id));

                action = action.replace(processedObject + "." + id, processedObject + "." + objectId);

                if (delimeter == semicolon) break;
                else last = coma + (objectId.length() - id.length());
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            log.error("Failed to parse action: " + action, e);
        }
        return action;
    }

    private String getObjectId(String processedObject, Long xmlId) {
        try {
            if (processedObject.equalsIgnoreCase(FIELD_KEYWORD)) return fields.get(xmlId).getObjectId();
            if (processedObject.equalsIgnoreCase(TRANSITION_KEYWORD)) return transitions.get(xmlId).getStringId();
        } catch (Exception e) {
            throw new IllegalArgumentException("Object " + processedObject + "." + xmlId + " does not exists");
        }
        return "";
    }

    @Transactional
    protected void addTrigger(Transition transition, ImportTrigger importTrigger) {
        Trigger trigger = triggerFactory.buildTrigger(importTrigger);

        transition.addTrigger(trigger);
    }

    @Transactional
    protected void createPlace(ImportPlace importPlace) {
        Place place = new Place();
        place.setIsStatic(importPlace.getIsStatic());
        place.setTokens(importPlace.getTokens());
        place.setPosition(importPlace.getX(), importPlace.getY());
        place.setTitle(importPlace.getLabel());

        net.addPlace(place);
        places.put(importPlace.getId(), place);
    }

    @Transactional
    protected void createRole(ImportRole importRole) {
        ProcessRole role = new ProcessRole();
        role.setName(importRole.getName());
        role = roleRepository.save(role);

        net.addRole(role);
        roles.put(importRole.getId(), role);
    }

    @Transactional
    protected void createTransaction(ImportTransaction importTransaction) {
        Transaction transaction = new Transaction();
        transaction.setTitle(importTransaction.getTitle());

        net.addTransaction(transaction);
        transactions.put(importTransaction.getId(), transaction);
    }

    @Transactional
    protected Node getNode(Long id) {
        // TODO: 18/02/2017 maybe throw exception if transitions doesn't contain id
        if (places.containsKey(id))
            return places.get(id);
        else
            return transitions.get(id);
    }

    PetriNet getNetByImportId(Long id) {
        return repository.findByImportId(id);
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public PetriNet getNet() {
        return net;
    }

    public void setNet(PetriNet net) {
        this.net = net;
    }

    public Map<Long, ProcessRole> getRoles() {
        return roles;
    }

    public void setRoles(Map<Long, ProcessRole> roles) {
        this.roles = roles;
    }

    public Map<Long, Field> getFields() {
        return fields;
    }

    public void setFields(Map<Long, Field> fields) {
        this.fields = fields;
    }

    public Map<Long, Transition> getTransitions() {
        return transitions;
    }

    public void setTransitions(Map<Long, Transition> transitions) {
        this.transitions = transitions;
    }

    public Map<Long, Place> getPlaces() {
        return places;
    }

    public void setPlaces(Map<Long, Place> places) {
        this.places = places;
    }

    public PetriNetRepository getRepository() {
        return repository;
    }

    public void setRepository(PetriNetRepository repository) {
        this.repository = repository;
    }

    public ProcessRoleRepository getRoleRepository() {
        return roleRepository;
    }

    public void setRoleRepository(ProcessRoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }
}