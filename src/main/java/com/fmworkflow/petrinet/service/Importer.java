package com.fmworkflow.petrinet.service;

import com.fmworkflow.petrinet.domain.PetriNet;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.*;
import java.io.File;
import java.io.IOException;

public class Importer {

    @Autowired
    IPetriNetService petriNetService;

    private File xmlFile;
    private PetriNet petriNet;

    public Importer() {
        this.petriNet = new PetriNet();
    }

    public Importer(File xmlFile) {
        this();
        this.xmlFile = xmlFile;
    }

    public void importPetriNet() throws ParserConfigurationException, SAXException, IOException {
        parseXml();
        persistNet();
    }

    private void parseXml() throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        DefaultHandler handler = new ImportHandler();

        try {
            parser.parse(xmlFile, handler);
        } catch (IllegalArgumentException e) {
            // TODO: 26. 1. 2017 illegal xml tag
            e.printStackTrace();
        }
    }

    private void persistNet() {
//        petriNetService.savePetriNet(petriNet);
    }
}