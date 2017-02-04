package com.fmworkflow.petrinet.service;

import com.fmworkflow.petrinet.domain.Arc;
import com.fmworkflow.petrinet.domain.PetriNet;
import com.fmworkflow.petrinet.domain.PetriNetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

@Service
public class PetriNetService implements IPetriNetService {

    @Autowired
    private Importer importer;

    @Autowired
    private PetriNetRepository repository;

    @Override
    public void importPetriNet(File xmlFile, String name) throws IOException, SAXException, ParserConfigurationException {
        importer.importPetriNet(xmlFile, name);
    }

    @Override
    public void savePetriNet(PetriNet petriNet) {
        repository.save(petriNet);
    }

    @Override
    public PetriNet loadPetriNet(String id) {
        PetriNet net = repository.findOne(id);
        for (Arc arc : net.getArcs()) {
            arc.setSource(net.getNode(arc.getSourceId()));
            arc.setDestination(net.getNode(arc.getDestinationId()));
        }
        return net;
    }
}