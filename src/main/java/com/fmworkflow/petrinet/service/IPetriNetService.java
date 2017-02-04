package com.fmworkflow.petrinet.service;

import com.fmworkflow.petrinet.domain.PetriNet;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.List;

public interface IPetriNetService {

    void importPetriNet(File xmlFile, String name) throws IOException, SAXException, ParserConfigurationException;

    void savePetriNet(PetriNet petriNet);

    PetriNet loadPetriNet(String id);

    List<PetriNet> loadAll();
}
