package com.fmworkflow.importer;

import com.fmworkflow.importer.model.*;
import com.fmworkflow.importer.model.datalogic.ImportPlusYears;
import com.fmworkflow.petrinet.domain.*;
import com.fmworkflow.petrinet.domain.dataset.Field;
import com.fmworkflow.petrinet.domain.dataset.logic.Editable;
import com.fmworkflow.petrinet.domain.dataset.logic.Required;
import com.fmworkflow.petrinet.domain.dataset.logic.PlusYears;
import com.fmworkflow.petrinet.domain.dataset.logic.Visible;
import com.fmworkflow.petrinet.domain.roles.AssignFunction;
import com.fmworkflow.petrinet.domain.roles.DelegateFunction;
import com.fmworkflow.petrinet.domain.roles.ProcessRole;
import com.fmworkflow.petrinet.domain.roles.ProcessRoleRepository;
import com.fmworkflow.petrinet.service.ArcFactory;
import com.fmworkflow.petrinet.service.FieldFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component
public class Importer {
    private Document document;
    private PetriNet net;
    private Map<Long, ProcessRole> roles;
    private Map<Long, Field> fields;
    private Map<Long, Transition> transitions;
    private Map<Long, Place> places;

    @Autowired
    private PetriNetRepository repository;

    @Autowired
    private ProcessRoleRepository roleRepository;

    public Importer() {
        this.roles = new HashMap<>();
        this.transitions = new HashMap<>();
        this.places = new HashMap<>();
        this.fields = new HashMap<>();
    }

    @Transactional
    public void importPetriNet(File xml, String title, String initials) {
        try {
            unmarshallXml(xml);
            createPetriNet(title, initials);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    @Transactional
    private void unmarshallXml(File xml) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(Document.class);

        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        document = (Document) jaxbUnmarshaller.unmarshal(xml);
    }

    @Transactional
    private void createPetriNet(String title, String initials) {
        net = new PetriNet();
        net.setTitle(title);
        net.setInitials(initials);

        Arrays.stream(document.getImportData()).forEach(this::createDataSet);
        Arrays.stream(document.getImportRoles()).forEach(this::createRole);
        Arrays.stream(document.getImportPlaces()).forEach(this::createPlace);
        Arrays.stream(document.getImportTransitions()).forEach(this::createTransition);
        Arrays.stream(document.getImportArc()).forEach(this::createArc);

        repository.save(net);
    }

    @Transactional
    private void createArc(ImportArc importArc) {
        Arc arc = ArcFactory.getArc(importArc.getType());
        arc.setMultiplicity(importArc.getMultiplicity());
        arc.setSource(getNode(importArc.getSourceId()));
        arc.setDestination(getNode(importArc.getDestinationId()));

        net.addArc(arc);
    }

    @Transactional
    private void createDataSet(ImportData importData) {
        Field field = FieldFactory.getField(importData.getType(), importData.getValues());
        field.setName(importData.getTitle());

        net.addDataSetField(field);
        fields.put(importData.getId(), field);
    }

    @Transactional
    private void createTransition(ImportTransition importTransition) {
        Transition transition = new Transition();
        transition.setTitle(importTransition.getLabel());
        transition.setPosition(importTransition.getX(), importTransition.getY());

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

        net.addTransition(transition);
        transitions.put(importTransition.getId(), transition);
    }

    @Transactional
    private void addRoleLogic(Transition transition, RoleRef roleRef) {
        RoleLogic logic = roleRef.getLogic();
        String roleId = roles.get(roleRef.getId()).getObjectId();

        if (logic == null || roleId == null)
            return;

        if (logic.getAssign())
            transition.addRole(roleId, new AssignFunction(roleId));
        if (logic.getDelegate())
            transition.addRole(roleId, new DelegateFunction(roleId));
    }

    @Transactional
    private void addDataLogic(Transition transition, DataRef dataRef) {
        DataLogic logic = dataRef.getLogic();
        String fieldId = fields.get(dataRef.getId()).getObjectId();

        if (logic == null || fieldId == null)
            return;

        if (logic.getRequired() != null) {
            transition.addDataSet(fieldId, new Required());
        }
        if (logic.getEditable())
            transition.addDataSet(fieldId, new Editable());
        if (logic.getVisible())
            transition.addDataSet(fieldId, new Visible());
        if (logic.getPlusYears() != null) {
            ImportPlusYears plusYears = logic.getPlusYears();
            transition.addDataSet(fieldId, new PlusYears(String.valueOf(plusYears.getRef()), plusYears.getContent()));
        }

    }

    @Transactional
    private void createPlace(ImportPlace importPlace) {
        Place place = new Place();
        place.setStatic(importPlace.getIsStatic());
        place.setTokens(importPlace.getTokens());
        place.setPosition(importPlace.getX(), importPlace.getY());
        place.setTitle(importPlace.getLabel());

        net.addPlace(place);
        places.put(importPlace.getId(), place);
    }

    @Transactional
    private void createRole(ImportRole importRole) {
        ProcessRole role = new ProcessRole();
        role.setName(importRole.getName());
        role = roleRepository.save(role);

        net.addRole(role);
        roles.put(importRole.getId(), role);
    }

    @Transactional
    private Node getNode(Long id) {
        // TODO: 18/02/2017 maybe throw exception if transitions doesn't contain id
        if (places.containsKey(id))
            return places.get(id);
        else
            return transitions.get(id);
    }
}