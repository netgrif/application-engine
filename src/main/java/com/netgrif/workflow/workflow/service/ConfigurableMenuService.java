package com.netgrif.workflow.workflow.service;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.dataset.EnumerationMapField;
import com.netgrif.workflow.petrinet.domain.dataset.MultichoiceMapField;
import com.netgrif.workflow.petrinet.domain.repositories.PetriNetRepository;
import com.netgrif.workflow.petrinet.domain.version.StringToVersionConverter;
import com.netgrif.workflow.petrinet.domain.version.Version;
import com.netgrif.workflow.petrinet.service.PetriNetService;
import com.netgrif.workflow.petrinet.web.responsebodies.PetriNetReference;
import com.netgrif.workflow.workflow.service.interfaces.IConfigurableMenuService;
import groovy.lang.Closure;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ConfigurableMenuService implements IConfigurableMenuService {

    @Autowired
    private PetriNetRepository petriNetRepository;
    @Autowired
    private PetriNetService petriNetService;
    @Autowired
    private StringToVersionConverter converter;
    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * Format of returned users netsMap field keys: NET_IMPORT_ID:VERSION
     * Mongo doesn't allow dots inside map keys that's why they are replaced with dashes in version string.
     */
    @Override
    public Map<String, I18nString> getNetsByAuthor(User user, Locale locale){
        LoggedUser author = user.transformToLoggedUser();
        List<PetriNetReference> nets = petriNetService.getReferencesByUsersProcessRoles(author, locale);
//
//        Query query = Query.query(Criteria.where("author").is(author));
//        query.fields().include("author");
//        List<PetriNet> nets = mongoTemplate.find(query, PetriNet.class);

        Map<String, I18nString> options = new HashMap<>();

        for(PetriNetReference net : nets){
            String[] versionSplit = net.getVersion().split("\\.");
            I18nString titleAndVersion = new I18nString(net.getTitle() + " :" + net.getVersion());
            options.put(net.getIdentifier() + ":" + versionSplit[0] + "-" + versionSplit[1] + "-" + versionSplit[2], titleAndVersion);
        }

//        List<PetriNet> nets = petriNetRepository.findAll()
//                .stream()
//                .filter(n -> n.getAuthor().getId().equals(authorId) && !n.getRoles().isEmpty())
//                .collect(Collectors.toList());
//
//
//        for(PetriNet net : nets) {
//            String[] versionSplit = net.getVersion().toString().split("\\.");
//            I18nString titleAndVersion = new I18nString(net.getTitle().toString() + " :" + net.getVersion().toString());
//            options.put(net.getIdentifier() + ":" + versionSplit[0] + "-" + versionSplit[1] + "-" + versionSplit[2], titleAndVersion);
//        }

        return options;
    }

    @Override
    public Map<String, I18nString> getAvailableRolesFromNet (EnumerationMapField processField, MultichoiceMapField permittedRoles, MultichoiceMapField bannedRoles) {

        String netImportId = processField.getValue().split(":")[0];
        String versionString = processField.getOptions().get(processField.getValue()).toString().split(":")[1].replace("-", ".");
        Version version = converter.convert(versionString);
        PetriNet net = petriNetService.getPetriNet(netImportId, version);

        return net.getRoles().values().stream()
                .filter(role -> (!permittedRoles.getOptions().containsKey(role.getImportId() + ":" + netImportId)
                && !bannedRoles.getOptions().containsKey(role.getImportId() + ":" + netImportId)))
                .map(o -> o.getImportId() + ":" + netImportId + "," + o.getName())
                .collect(Collectors.toMap(o -> o.split(",")[0], v -> new I18nString(v.split(",")[1])));
    }

    @Override
    public Map<String, I18nString> removeSelectedRoles(MultichoiceMapField addedRoles) {

        Map<String, I18nString> updatedRoles = new LinkedHashMap<>(addedRoles.getOptions());
        updatedRoles.keySet().removeAll(addedRoles.getValue());
        return updatedRoles;
    }

    @Override
    public Map<String, I18nString> addSelectedRoles(MultichoiceMapField addedRoles, EnumerationMapField processField, MultichoiceMapField rolesAvailable) {

        String netName = " (" + processField.getOptions().get(processField.getValue()).toString().split(":")[0] + ")";
        Map<String, I18nString> updatedRoles = new LinkedHashMap<>(addedRoles.getOptions());

        updatedRoles.putAll(rolesAvailable.getValue().stream()
                .collect(Collectors.toMap(x -> x, v -> new I18nString(rolesAvailable.getOptions().get(v).toString() + netName))));

        return updatedRoles;
    }
}
