package com.fmworkflow.petrinet.service;

import com.fmworkflow.petrinet.domain.PetriNet;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;

public class PetriNetImporter {

    @Autowired
    IPetriNetService petriNetService;

    private File xmlFile;
    private PetriNet petriNet;

    public PetriNetImporter() {
        this.petriNet = new PetriNet();
    }

    public PetriNetImporter(File xmlFile) {
        this();
        this.xmlFile = xmlFile;
    }

    public void importPetriNet() {
        parseXml();
        persistNet();
    }

    private void parseXml() {
    }

    private void persistNet() {
        petriNetService.savePetriNet(petriNet);
    }
}