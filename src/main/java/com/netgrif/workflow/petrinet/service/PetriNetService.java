package com.netgrif.workflow.petrinet.service;

import com.netgrif.workflow.auth.domain.Authority;
import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.importer.Importer;
import com.netgrif.workflow.petrinet.domain.repositories.PetriNetRepository;
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.workflow.petrinet.web.responsebodies.DataFieldReference;
import com.netgrif.workflow.petrinet.web.responsebodies.PetriNetReference;
import com.netgrif.workflow.petrinet.web.responsebodies.TransitionReference;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.Transition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PetriNetService implements IPetriNetService {

    @Autowired
    private Importer importer;

    @Autowired
    private PetriNetRepository repository;

    @Autowired
    private MongoTemplate mongoTemplate;

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
    public List<PetriNetReference> getAllReferences(LoggedUser user) {
        List<PetriNet> nets = loadAll();
        if(user.getAuthorities().contains(new Authority(Authority.admin)))
            return nets.stream().map(net -> new PetriNetReference(net.getObjectId().toString(),net.getTitle())).collect(Collectors.toList());
        return nets.stream().filter(net -> net.getRoles().keySet().stream().anyMatch(user.getProcessRoles()::contains))
                .map(net -> new PetriNetReference(net.getObjectId().toString(), net.getTitle())).collect(Collectors.toList());
    }

    @Override
    public PetriNetReference getReferenceByTitle(LoggedUser user, String title){
        List<PetriNet> nets = repository.findByTitle(title);
        return nets.stream().filter(net -> net.getRoles().keySet().stream().anyMatch(user.getProcessRoles()::contains))
                .map(net -> new PetriNetReference(net.getObjectId().toString(), net.getTitle())).findFirst().orElse(new PetriNetReference("",""));

    }

    @Override
    public List<TransitionReference> getTransitionReferences(List<String> netsIds, LoggedUser user) {
        Iterable<PetriNet> nets = repository.findAll(netsIds);
        List<TransitionReference> transRefs = new ArrayList<>();
        nets.forEach(net -> transRefs.addAll(net.getTransitions().entrySet().stream()
                .filter(entry -> entry.getValue().getRoles().keySet().stream().anyMatch(user.getProcessRoles()::contains))
                .map(entry -> new TransitionReference(entry.getKey(),entry.getValue().getTitle(),net.getStringId()))
                .collect(Collectors.toList())));
        return transRefs;
    }

    @Override
    public List<DataFieldReference> getDataFieldReferences(List<String> petriNetIds, List<String> transitionIds) {
        Iterable<PetriNet> nets = repository.findAll(petriNetIds);
        List<DataFieldReference> dataRefs = new ArrayList<>();

        transitionIds.forEach(transId -> nets.forEach(net -> {
            Transition trans;
            if ((trans = net.getTransition(transId)) != null) {
                trans.getDataSet().forEach((key, value) ->
                        dataRefs.add(new DataFieldReference(key, net.getDataSet().get(key).getName(), net.getStringId(), transId))
                );
            }
        }));

        return dataRefs;
    }

    public List<PetriNetReference> getAllAccessibleReferences(LoggedUser user){
        StringBuilder builder = new StringBuilder(8+(user.getProcessRoles().size()*50));
        builder.append("{$or:[");
        user.getProcessRoles().forEach(role -> {
            builder.append("{\"roles.");
            builder.append(role);
            builder.append("\":{$exists:true}},");
        });
        builder.deleteCharAt(builder.length() - 1);
        builder.append("]}");
        BasicQuery query = new BasicQuery(builder.toString(),"{_id:1,title:1}");
        List<PetriNet> nets = mongoTemplate.find(query, PetriNet.class);
        return nets.stream().map(PetriNetReference::new).collect(Collectors.toList());
    }
}