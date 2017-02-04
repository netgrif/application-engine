package com.fmworkflow.petrinet.service;

import com.fmworkflow.petrinet.domain.PetriNet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.*;
import java.io.File;
import java.io.IOException;

@Component
public class Importer {

    @Autowired
    private IPetriNetService service;

    private File xmlFile;
    private PetriNet petriNet;
    private SAXParserFactory factory;
    private SAXParser parser;
    private DefaultHandler handler;

    public Importer() {
        this.petriNet = new PetriNet();
    }

    public void importPetriNet(File xml, String title) throws ParserConfigurationException, SAXException, IOException {
        initializeParser();
        parseXml(xml);
        persistNet(title);
    }

    private void parseXml(File xmlFile) throws ParserConfigurationException, SAXException, IOException {
        try {
            parser.parse(xmlFile, handler);
        } catch (IllegalArgumentException e) {
            // TODO: 26. 1. 2017 illegal xml tag
            e.printStackTrace();
        }
    }

    private void initializeParser() throws ParserConfigurationException, SAXException {
        factory = SAXParserFactory.newInstance();
        parser = factory.newSAXParser();
        handler = new ImportHandler(petriNet);
    }

    private void persistNet(String title) {
        petriNet.setTitle(title);
        service.savePetriNet(petriNet);
    }
}