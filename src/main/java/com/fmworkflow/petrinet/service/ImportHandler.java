package com.fmworkflow.petrinet.service;

import com.fmworkflow.petrinet.domain.*;
import com.fmworkflow.petrinet.domain.dataset.Field;
import com.fmworkflow.petrinet.domain.dataset.FieldType;
import com.fmworkflow.petrinet.domain.dataset.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ImportHandler extends DefaultHandler {
    private final Logger log = LoggerFactory.getLogger(ImportHandler.class);

    private Map<Integer, Node> nodes;
    private Map<Integer, Field> fields;
    private Map<Transition, Set<Integer>> dataset;
    private PetriNet net;
    private Element element;
    private PetriNetObject object;
    private Field field;
    private int fieldId;

    public ImportHandler(PetriNet net) {
        this.net = net;
        this.nodes = new HashMap<>();
        this.fields = new HashMap<>();
        this.dataset = new HashMap<>();
    }

    @Override
    public void startDocument() throws SAXException {
        log.debug("Parsing started");
    }

    @Override
    public void endDocument() throws SAXException {
        net.initializeArcsFromSkeleton();
//        net.getTransitions().entrySet().stream()
//                .filter(entry -> dataset.containsKey(entry.getValue()))
//                .forEach(entry -> {
//                    Map<String, ILogicFunction> logic = new HashMap<>();
//                    dataset.get(entry.getValue()).forEach(id -> {
//                        // TODO: 16/02/2017
////                        logic.put(, new Editable());
//                    });
//                    entry.getValue().setDataSet(logic);
//                });
        log.debug("Parsing ended");
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        element = Element.fromString(qName);
        switch (element) {
            case PLACE:
                object = new Place();
                break;
            case TRANSITION:
                object = new Transition();
                break;
            case ARC:
                object = new Arc();
                break;
            case DATA:
                FieldType type = FieldType.valueOf(attributes.getValue("type").toUpperCase());
                switch (type) {
                    case TEXT:
                        field = new TextField();
                }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        Element endElement = Element.fromString(qName);
        switch (endElement) {
            case TRANSITION:
                net.addTransition((Transition) object);
                break;
            case PLACE:
                net.addPlace((Place) object);
                break;
            case ARC:
                net.addArcSkelet((Arc) object);
                break;
            case DATA:
                fields.put(fieldId, field);
                net.addDataSetField(field);
                field = null;
                break;
        }
        element = Element.DOCUMENT;
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String characters = new String(ch, start, length);
        switch (element) {
            case SOURCEID:
                Node source = getNodeWithId(characters);
                ((Arc) object).setSource(source);
                break;
            case DESTINATIONID:
                Node destination = getNodeWithId(characters);
                ((Arc) object).setDestination(destination);
                break;
            case ID:
                if (field != null)
                    fieldId = Integer.valueOf(characters);
                if (object.getClass() != Arc.class)
                    setNodeWithId(characters);
                break;
            case MULTIPLICITY:
                Integer multiplicity = Integer.parseInt(characters);
                ((Arc) object).setMultiplicity(multiplicity);
                break;
            case TOKENS:
                Integer tokens = Integer.parseInt(characters);
                ((Place) object).setTokens(tokens);
                break;
            case X:
                Integer x = Integer.parseInt(characters);
                ((Node) object).setPositionX(x);
                break;
            case Y:
                Integer y = Integer.parseInt(characters);
                ((Node) object).setPositionY(y);
                break;
            case LABEL:
                ((Node) object).setTitle(characters);
                break;
            case STATIC:
                Boolean isStatic = Boolean.parseBoolean(characters);
                ((Place) object).setStatic(isStatic);
                break;
            case TYPE:
                if (Arc.Type.valueOf(characters) == Arc.Type.reset)
                    object = new ResetArc((Arc) object);
                else if (Arc.Type.valueOf(characters) == Arc.Type.inhibitor)
                    object = new InhibitorArc((Arc) object);
                break;
            case FIELD:
//                if (dataset.containsKey((Transition) object)) {
//                    dataset.get((Transition) object).add(Integer.parseInt(characters));
//                } else {
//                    Set<Integer> fieldIds = new HashSet<>();
//                    fieldIds.add(Integer.parseInt(characters));
//                    dataset.put((Transition) object, fieldIds);
//                }
//                break;
            default:
        }
    }

    private void setNodeWithId(String idString) {
        Integer id = Integer.parseInt(idString);
        nodes.put(id, (Node) object);
    }

    private Node getNodeWithId(String idString) {
        Integer id = Integer.parseInt(idString);
        return nodes.get(id);
    }

    enum Element {
        DOCUMENT ("document"),
        PLACE ("place"),
        TRANSITION ("transition"),
        ARC ("arc"),
        ID ("id"),
        X ("x"),
        Y ("y"),
        LABEL ("label"),
        SOURCEID ("sourceId"),
        DESTINATIONID ("destinationId"),
        MULTIPLICITY ("multiplicity"),
        TOKENS ("tokens"),
        STATIC ("static"),
        TYPE ("type"),
        DATA ("data"),
        TITLE ("title"),
        DESC ("desc"),
        LOGIC ("logic"),
        EDITABLE ("editable"),
        FIELD ("field");

        String name;

        Element(String name) {
            this.name = name;
        }

        public static Element fromString(String name) {
            return Element.valueOf(name.toUpperCase());
        }
    }
}