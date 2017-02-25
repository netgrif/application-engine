package com.fmworkflow.petrinet.service;

import com.fmworkflow.importer.Importer;
import com.fmworkflow.petrinet.domain.*;
import com.fmworkflow.petrinet.web.responsebodies.PetriNetReference;
import com.fmworkflow.petrinet.web.responsebodies.TransitionReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PetriNetService implements IPetriNetService {

    @Autowired
    private Importer importer;

    @Autowired
    private PetriNetRepository repository;

    @Override
    public void importPetriNet(File xmlFile, String name, String initials) throws IOException, SAXException, ParserConfigurationException {
        importer.importPetriNet(xmlFile, name, initials);
        xmlFile.delete();
    }

    @Override
    public void savePetriNet(PetriNet petriNet) {
        repository.save(petriNet);
    }

    @Override
    public PetriNet loadPetriNet(String id) {
        PetriNet net = repository.findOne(id);
        net.initializeArcs();
        return net;
    }

    @Override
    public List<PetriNet> loadAll() {
        List<PetriNet> nets = repository.findAll();
        nets.forEach(PetriNet::initializeArcs);
        return nets;
    }

    @Override
    public List<PetriNetReference> getAllReferences(){
        List<PetriNet> nets = loadAll();
        List<PetriNetReference> refs = new ArrayList<>();
        for(PetriNet net: nets){
            refs.add(new PetriNetReference(net.get_id().toString(), net.getTitle()));
        }
        return refs;
    }

    public List<TransitionReference> getTransitionReferences(List<String> netsIds){
        Iterable<PetriNet> nets = repository.findAll(netsIds);
        List<TransitionReference>  transRefs = new ArrayList<>();

        nets.forEach(net -> net.getTransitions().forEach((s, transition) ->
                transRefs.add(new TransitionReference(transition.getStringId(),
                        net.get_id().toString(),
                        transition.getTitle()))));

        return transRefs;
    }
}