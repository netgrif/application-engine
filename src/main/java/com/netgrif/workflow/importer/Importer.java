package com.netgrif.workflow.importer;

import com.netgrif.workflow.importer.model.*;
import com.netgrif.workflow.petrinet.domain.Arc;
import com.netgrif.workflow.petrinet.domain.DataGroup;
import com.netgrif.workflow.petrinet.domain.*;
import com.netgrif.workflow.petrinet.domain.Place;
import com.netgrif.workflow.petrinet.domain.Transaction;
import com.netgrif.workflow.petrinet.domain.Transition;
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Component
public class Importer {

    private static final Logger log = Logger.getLogger(Importer.class.getName());

    public static final String FIELD_KEYWORD = "f";
    public static final String TRANSITION_KEYWORD = "t";

    private Path importedXmlPath;
    private Document document;
    private PetriNet net;
    private ProcessRole defaultRole;
    private Map<Long, ProcessRole> roles;
    private Map<Long, Field> fields;
    private Map<Long, Transition> transitions;
    private Map<Long, Place> places;
    private Map<Long, Transaction> transactions;
    private Map<String, I18nString> i18n;

    @Autowired
    private FieldFactory fieldFactory;

    @Autowired
    private PetriNetRepository repository;

    @Autowired
    private ProcessRoleRepository roleRepository;

    @Autowired
    private ArcFactory arcFactory;

    @Autowired
    private RoleFactory roleFactory;

    @Autowired
    private TriggerFactory triggerFactory;

    @Transactional
    public Optional<PetriNet> importPetriNet(File xml, String title, String initials) {
        try {
            initialize();
            unmarshallXml(xml);
            return createPetriNet(title, initials);
        } catch (JAXBException | IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    private void initialize() {
        this.importedXmlPath = null;
        this.roles = new HashMap<>();
        this.transitions = new HashMap<>();
        this.places = new HashMap<>();
        this.fields = new HashMap<>();
        this.transactions = new HashMap<>();
        this.defaultRole = roleRepository.findByName_DefaultValue(ProcessRole.DEFAULT_ROLE);
        this.i18n = new HashMap<>();
    }

    @Transactional
    protected void unmarshallXml(File xml) throws JAXBException, IOException {
        JAXBContext jaxbContext = JAXBContext.newInstance(Document.class);

        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        document = (Document) jaxbUnmarshaller.unmarshal(xml);
        importedXmlPath = Files.copy(xml.toPath(), (new File("storage/" + xml.getName())).toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    @Transactional
    protected Optional<PetriNet> createPetriNet(String title, String initials) {
        net = new PetriNet();
        net.setImportXmlPath(importedXmlPath.toString());
        net.setImportId(document.getId());
        net.setIcon(document.getIcon());
        net.setTitle(title);
        net.setInitials(initials);

        document.getI18N().forEach(this::addI18N);
        document.getRole().forEach(this::createRole);
        document.getData().forEach(this::createDataSet);
        document.getTransaction().forEach(this::createTransaction);
        document.getPlace().forEach(this::createPlace);
        document.getTransition().forEach(this::createTransition);
        document.getArc().forEach(this::createArc);
        document.getMapping().forEach(this::applyMapping);
        document.getTransition().forEach(this::resolveTransitionActions);
        document.getData().forEach(this::resolveDataActions);

        return Optional.of(repository.save(net));
    }

    @Transactional
    protected void addI18N(I18N importI18N) {
        String locale = importI18N.getLocale();
        importI18N.getI18NString().forEach(translation -> addTranslation(translation, locale));
    }

    @Transactional
    protected void addTranslation(I18NStringType i18NStringType, String locale) {
        String name = i18NStringType.getName();
        I18nString translation = i18n.get(name);
        if (translation == null) {
            translation = new I18nString();
            i18n.put(name, translation);
        }
        translation.addTranslation(locale, i18NStringType.getValue());
    }

    @Transactional
    protected void applyMapping(Mapping mapping) {
        Transition transition = transitions.get(mapping.getTransitionRef());
        mapping.getRoleRef().forEach(roleRef -> addRoleLogic(transition, roleRef));
        mapping.getDataRef().forEach(dataRef -> addDataLogic(transition, dataRef));
        mapping.getDataGroup().forEach(dataGroup -> addDataGroup(transition, dataGroup));
        mapping.getTrigger().forEach(trigger -> addTrigger(transition, trigger));
    }

    @Transactional
    protected void resolveDataActions(Data data) {
        if (data.getAction() != null) {
            fields.get(data.getId()).setActions(buildActions(data.getAction(), fields.get(data.getId()).getStringId(), null));
        }
    }

    @Transactional
    protected void resolveTransitionActions(com.netgrif.workflow.importer.model.Transition trans) {
        if (trans.getDataRef() != null) {
            trans.getDataRef().forEach(ref -> {
                if (ref.getLogic().getAction() != null) {
                    String fieldId = fields.get(ref.getId()).getStringId();
                    transitions.get(trans.getId()).addActions(fieldId, buildActions(ref.getLogic().getAction(),
                            fieldId,
                            transitions.get(trans.getId()).getStringId()));
                }
            });
        }
    }

    @Transactional
    protected void createArc(com.netgrif.workflow.importer.model.Arc importArc) {
        Arc arc = arcFactory.getArc(importArc);
        arc.setImportId(importArc.getId());
        arc.setMultiplicity(importArc.getMultiplicity());
        arc.setSource(getNode(importArc.getSourceId()));
        arc.setDestination(getNode(importArc.getDestinationId()));

        net.addArc(arc);
    }

    @Transactional
    protected void createDataSet(Data importData) {
        Field field = fieldFactory.getField(importData, this);

        net.addDataSetField(field);
        fields.put(importData.getId(), field);
    }

    @Transactional
    protected void createTransition(com.netgrif.workflow.importer.model.Transition importTransition) {
        Transition transition = new Transition();
        transition.setImportId(importTransition.getId());
        transition.setTitle(toI18NString(importTransition.getLabel()));
        transition.setPosition(importTransition.getX(), importTransition.getY());
        transition.setPriority(importTransition.getPriority());
        transition.setIcon(importTransition.getIcon());

        if (importTransition.getRoleRef() != null) {
            importTransition.getRoleRef().forEach(roleRef ->
                    addRoleLogic(transition, roleRef)
            );
        }
        if (importTransition.getDataRef() != null) {
            importTransition.getDataRef().forEach(dataRef ->
                    addDataLogic(transition, dataRef)
            );
        }
        if (importTransition.getTrigger() != null) {
            importTransition.getTrigger().forEach(trigger ->
                    addTrigger(transition, trigger)
            );
        }
        if (importTransition.getTransactionRef() != null) {
            addToTransaction(transition, importTransition.getTransactionRef());
        }
        if (importTransition.getDataGroup() != null) {
            importTransition.getDataGroup().forEach(dataGroup ->
                    addDataGroup(transition, dataGroup)
            );
        }
        if (isDefaultRoleAllowedFor(importTransition, document)) {
            addDefaultRole(transition);
        }

        net.addTransition(transition);
        transitions.put(importTransition.getId(), transition);
    }

    @Transactional
    protected void addDefaultRole(Transition transition) {
        Logic logic = new Logic();
        logic.setDelegate(true);
        logic.setPerform(true);
        transition.addRole(defaultRole.getStringId(), roleFactory.getPermissions(logic));
    }

    @Transactional
    protected void addDataGroup(Transition transition, com.netgrif.workflow.importer.model.DataGroup importDataGroup) {
        String alignment = importDataGroup.getAlignment() != null ? importDataGroup.getAlignment().value() : "";
        DataGroup dataGroup = new DataGroup();
        dataGroup.setTitle(toI18NString(importDataGroup.getTitle()));
        dataGroup.setAlignment(alignment);
        dataGroup.setStretch(importDataGroup.isStretch());
        importDataGroup.getDataRef().forEach(dataRef -> dataGroup.addData(fields.get(dataRef.getId()).getStringId()));
        transition.addDataGroup(dataGroup);
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
        Logic logic = roleRef.getLogic();
        String roleId = roles.get(roleRef.getId()).getStringId();

        if (logic == null || roleId == null)
            return;

        transition.addRole(roleId, roleFactory.getPermissions(logic));
    }

    @Transactional
    protected void addDataLogic(Transition transition, DataRef dataRef) {
        Logic logic = dataRef.getLogic();
        try {
            String fieldId = fields.get(dataRef.getId()).getStringId();
            if (logic == null || fieldId == null)
                return;

            Set<FieldBehavior> behavior = new HashSet<>();
            if (logic.getBehavior() != null)
                logic.getBehavior().forEach(b -> behavior.add(FieldBehavior.fromString(b)));

            transition.addDataSet(fieldId, behavior, null);
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Wrong dataRef id [" + dataRef.getId() + "] on transition [" + transition.getTitle() + "]", e);
        }
    }

    @Transactional
    protected LinkedHashSet<Action> buildActions(List<ActionType> imported, String fieldId, String transitionId) {
        final LinkedHashSet<Action> actions = new LinkedHashSet<>();
        imported.forEach(action -> {
            if (action.getTrigger() == null)
                throw new IllegalArgumentException("Action [" + action.getValue() + "] doesn't have trigger");

            String definition = action.getValue();
            try {
                definition = parseObjectIds(definition, fieldId, FIELD_KEYWORD);
                definition = parseObjectIds(definition, transitionId, TRANSITION_KEYWORD);
            } catch (NumberFormatException e) {
//                todo: message
                throw new IllegalArgumentException("Error parsing ids of action [" + action.getValue() + "]", e);
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
            if (processedObject.equalsIgnoreCase(FIELD_KEYWORD)) return fields.get(xmlId).getStringId();
            if (processedObject.equalsIgnoreCase(TRANSITION_KEYWORD)) return transitions.get(xmlId).getStringId();
        } catch (Exception e) {
            throw new IllegalArgumentException("Object " + processedObject + "." + xmlId + " does not exists");
        }
        return "";
    }

    @Transactional
    protected void addTrigger(Transition transition, com.netgrif.workflow.importer.model.Trigger importTrigger) {
        Trigger trigger = triggerFactory.buildTrigger(importTrigger);

        transition.addTrigger(trigger);
    }

    @Transactional
    protected void createPlace(com.netgrif.workflow.importer.model.Place importPlace) {
        Place place = new Place();
        place.setImportId(importPlace.getId());
        if (importPlace.isStatic() == null)
            place.setIsStatic(importPlace.isIsStatic());
        else
            place.setIsStatic(importPlace.isStatic());
        place.setTokens(importPlace.getTokens());
        place.setPosition(importPlace.getX(), importPlace.getY());
        place.setTitle(toI18NString(importPlace.getLabel()));

        net.addPlace(place);
        places.put(importPlace.getId(), place);
    }

    @Transactional
    protected void createRole(Role importRole) {
        ProcessRole role = new ProcessRole();
        if (importRole.getName() == null)
            role.setName(toI18NString(importRole.getTitle()));
        else
            role.setName(toI18NString(importRole.getName()));
        role = roleRepository.save(role);

        net.addRole(role);
        roles.put(importRole.getId(), role);
    }

    @Transactional
    protected void createTransaction(com.netgrif.workflow.importer.model.Transaction importTransaction) {
        Transaction transaction = new Transaction();
        transaction.setTitle(toI18NString(importTransaction.getTitle()));

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

    I18nString toI18NString(I18NStringType imported) {
        if (imported == null)
            return null;
        I18nString string = i18n.getOrDefault(imported.getName(), new I18nString(imported.getValue()));
        if (string.getDefaultValue() == null)
            string.setDefaultValue(imported.getValue());
        return string;
    }

    private boolean isDefaultRoleAllowedFor(com.netgrif.workflow.importer.model.Transition transition, Document document) {
        // FALSE if defaultRole not allowed in net
        if (document.isDefaultRole() == null || !document.isDefaultRole())
            return false;
        // FALSE if role or trigger mapping
        for (Mapping mapping : document.getMapping()) {
            if (mapping.getTransitionRef() == transition.getId() && (mapping.getRoleRef() == null || mapping.getRoleRef().isEmpty()) && (mapping.getTrigger() == null || mapping.getTrigger().isEmpty()))
                return false;
        }
        // TRUE if no roles and no triggers
        return (transition.getRoleRef() == null || transition.getRoleRef().isEmpty()) && (transition.getTrigger() == null || transition.getTrigger().isEmpty());
    }

    PetriNet getNetByImportId(Long id) {
        return repository.findByImportId(id);
    }

    public Map<Long, ProcessRole> getRoles() {
        return roles;
    }
}