package com.netgrif.workflow.petrinet.service;

import com.netgrif.workflow.auth.domain.Authority;
import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.service.UserProcessRoleService;
import com.netgrif.workflow.event.events.model.UserImportModelEvent;
import com.netgrif.workflow.importer.service.Importer;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.Transition;
import com.netgrif.workflow.petrinet.domain.repositories.PetriNetRepository;
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.workflow.petrinet.web.responsebodies.DataFieldReference;
import com.netgrif.workflow.petrinet.web.responsebodies.PetriNetReference;
import com.netgrif.workflow.petrinet.web.responsebodies.PetriNetSmall;
import com.netgrif.workflow.petrinet.web.responsebodies.TransitionReference;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public abstract class PetriNetService implements IPetriNetService {

    private static final Logger log = Logger.getLogger(PetriNetService.class);

    @Lookup("importer")
    abstract Importer getImporter();

    @Autowired
    private UserProcessRoleService userProcessRoleService;

    @Autowired
    private PetriNetRepository repository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Override
    public Optional<PetriNet> importPetriNetAndDeleteFile(File xmlFile, String name, String initials, LoggedUser user) throws IOException {
        Optional<PetriNet> imported = importPetriNet(xmlFile, name, initials, user);
        if (!xmlFile.delete())
            throw new IOException("File of process was not deleted");
        return imported;
    }

    @Override
    public Optional<PetriNet> importPetriNet(File xmlFile, String name, String initials, LoggedUser user) throws IOException {
        Optional<PetriNet> imported = getImporter().importPetriNet(xmlFile, name, initials);
        if (imported.isPresent()) {
            PetriNet net = imported.get();
            net.setAuthor(user.transformToAuthor());
            getImporter().saveNetFile(net, xmlFile);
            repository.save(imported.get());
            userProcessRoleService.saveRoles(net.getRoles().values(), net.getStringId());

            log.info("Petri net " + name + " (" + initials + ") imported successfully");
            publisher.publishEvent(new UserImportModelEvent(user, xmlFile, name, initials));
        }
        return imported;
    }

    @Override
    public void savePetriNet(PetriNet petriNet) {
        repository.save(petriNet);
    }

    @Override
    public PetriNet loadPetriNet(String id) {
        PetriNet net = repository.findOne(id);
        if (net == null)
            throw new IllegalArgumentException("No model with id: " + id + " was found.");

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
    public FileSystemResource getNetFile(String netId, StringBuilder title) {
        if (title.length() == 0) {
            List<PetriNet> nets = mongoTemplate.find(new BasicQuery("{_id:{$oid:\"" + netId + "\"}}", "{_id:1,title:1}"), PetriNet.class);
            if (nets.isEmpty())
                return null;
            title.append(nets.get(0).getTitle().getDefaultValue());
        }
        return new FileSystemResource(Importer.ARCHIVED_FILES_PATH + netId + "-" + title.toString() + Importer.FILE_EXTENSION);
    }

    @Override
    public List<PetriNetReference> getAllReferences(LoggedUser user, Locale locale) {
        List<PetriNet> nets = loadAll();
        return nets.stream()
                .map(net -> new PetriNetReference(net.getObjectId().toString(), net.getTitle().getTranslation(locale)))
                .collect(Collectors.toList());
    }

    @Override
    public PetriNetReference getReferenceByTitle(LoggedUser user, String title, Locale locale) {
        List<PetriNet> nets = repository.findByTitle_DefaultValue(title);
        return nets.stream().filter(net -> net.getRoles().keySet().stream().anyMatch(user.getProcessRoles()::contains))
                .map(net -> new PetriNetReference(net.getObjectId().toString(), net.getTitle().getTranslation(locale))).findFirst().orElse(new PetriNetReference("", ""));
    }

    @Override
    public List<TransitionReference> getTransitionReferences(List<String> netsIds, LoggedUser user, Locale locale) {
        Iterable<PetriNet> nets = repository.findAll(netsIds);
        List<TransitionReference> transRefs = new ArrayList<>();

        nets.forEach(net -> transRefs.addAll(net.getTransitions().entrySet().stream()
                .map(entry -> new TransitionReference(entry.getKey(), entry.getValue().getTitle().getTranslation(locale), net.getStringId()))
                .collect(Collectors.toList())));

        return transRefs;
    }

    @Override
    public List<DataFieldReference> getDataFieldReferences(List<String> petriNetIds, List<String> transitionIds, Locale locale) {
        Iterable<PetriNet> nets = repository.findAll(petriNetIds);
        List<DataFieldReference> dataRefs = new ArrayList<>();

        transitionIds.forEach(transId -> nets.forEach(net -> {
            Transition trans;
            if ((trans = net.getTransition(transId)) != null) {
                trans.getDataSet().forEach((key, value) ->
                        dataRefs.add(new DataFieldReference(key, net.getDataSet().get(key).getName().getTranslation(locale), net.getStringId(), transId))
                );
            }
        }));

        return dataRefs;
    }

    @Override
    public List<PetriNetReference> getAllAccessibleReferences(LoggedUser user, Locale locale) {
        StringBuilder builder = new StringBuilder(8 + (user.getProcessRoles().size() * 50));
        builder.append("{$or:[");
        user.getProcessRoles().forEach(role -> {
            builder.append("{\"roles.");
            builder.append(role);
            builder.append("\":{$exists:true}},");
        });
        builder.deleteCharAt(builder.length() - 1);
        builder.append("]}");
        BasicQuery query = new BasicQuery(builder.toString(), "{_id:1,title:1}");
        List<PetriNet> nets = mongoTemplate.find(query, PetriNet.class);
        return nets.stream().map(pn -> new PetriNetReference(pn.getStringId(), pn.getTitle().getTranslation(locale))).collect(Collectors.toList());
    }

    @Override
    public Page<PetriNetSmall> searchPetriNet(Map<String, Object> criteria, LoggedUser user, Pageable pageable, Locale locale) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("{");

        if (!user.isAdmin())
            queryBuilder.append(getQueryByRoles(user));

        if (criteria != null && !criteria.isEmpty()) {
            if (!user.isAdmin())
                queryBuilder.append(",");

//            if(criteria.containsKey("author")){
//                queryBuilder.append(getQueryByTextValue("author",criteria.get("author")));
//                queryBuilder.append(",");
//            }
            if (criteria.containsKey("title")) {
                queryBuilder.append(getQueryByTextValue("title", criteria.get("title")));
                queryBuilder.append(",");
            }
            if (criteria.containsKey("initials")) {
                queryBuilder.append(getQueryByTextValue("initials", criteria.get("initials")));
                queryBuilder.append(",");
            }

            queryBuilder.deleteCharAt(queryBuilder.length() - 1);
        }
        queryBuilder.append("}");

        BasicQuery query = new BasicQuery(queryBuilder.toString());
        query = (BasicQuery) query.with(pageable);
        List<PetriNet> nets = mongoTemplate.find(query, PetriNet.class);
        return new PageImpl<>(nets.stream().map(net -> PetriNetSmall.fromPetriNet(net, locale)).collect(Collectors.toList()),
                pageable, mongoTemplate.count(new BasicQuery(queryBuilder.toString(), "{_id:1}"), PetriNet.class));
    }

    private String getQueryByRoles(LoggedUser user) {
        final StringBuilder builder = new StringBuilder();
        builder.append("$or:[");
        user.getProcessRoles().forEach(role -> {
            builder.append("{\"roles.");
            builder.append(role);
            builder.append("\":{$exists:true}},");
        });
        if (!user.getProcessRoles().isEmpty())
            builder.deleteCharAt(builder.length() - 1);
        builder.append("]");
        return builder.toString();
    }

    private String getQueryByTextValue(String attributeName, Object object) {
        StringBuilder builder = new StringBuilder();
        builder.append("\"" + attributeName + "\":");
        if (object instanceof String) {
            builder.append("\"" + object + "\"");
        } else if (object instanceof List) {
            builder.append(getMongoInQuery((List<Object>) object));
        }
        return builder.toString();
    }

    private static String getMongoInQuery(List<Object> objs) {
        StringBuilder builder = new StringBuilder();
        builder.append("{$in:[");
        objs.forEach(o -> {
            builder.append("\"" + o.toString() + "\"");
            builder.append(",");
        });
        if (!objs.isEmpty())
            builder.deleteCharAt(builder.length() - 1);
        builder.append("]}");
        return builder.toString();
    }
}