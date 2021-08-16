package com.netgrif.workflow.workflow.service;

import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.dataset.EnumerationMapField;
import com.netgrif.workflow.petrinet.domain.dataset.MultichoiceMapField;
import com.netgrif.workflow.petrinet.domain.repositories.PetriNetRepository;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRole;
import com.netgrif.workflow.petrinet.domain.version.StringToVersionConverter;
import com.netgrif.workflow.petrinet.domain.version.Version;
import com.netgrif.workflow.petrinet.service.PetriNetService;
import com.netgrif.workflow.workflow.service.interfaces.IConfigurableMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ConfigurableMenuService implements IConfigurableMenuService {

    @Autowired
    private PetriNetRepository petriNetRepository;
    @Autowired
    private PetriNetService petriNetService;



    @Override
    public Map<String, I18nString> getNetsByAuthor(Long authorId){

        List<PetriNet> nets = petriNetRepository.findAll()
                .stream()
                .filter(n -> n.getAuthor().getId().equals(authorId) && !n.getRoles().isEmpty())
                .collect(Collectors.toList());

        Map<String, I18nString> options = new HashMap<>();

        for(PetriNet net : nets) {
            I18nString titleAndVersion = new I18nString(net.getTitle().toString() + " :" + net.getVersion().toString());
            options.put(net.getIdentifier(), titleAndVersion);
        }

        return options;
    }

    @Override
    public Map<String, I18nString> getAvailableRolesFromNet (EnumerationMapField processField, MultichoiceMapField permittedRoles, MultichoiceMapField bannedRoles) {

        String netId = processField.getValue();
        String versionString = processField.getOptions().get(netId).toString().split(":")[1];
        StringToVersionConverter converter = new StringToVersionConverter();
        Version version = converter.convert(versionString);
        PetriNet net = petriNetService.getPetriNet(netId, version);

        Map<String, I18nString> roles = new HashMap<>();

        for (ProcessRole role : net.getRoles().values()) {
            if (!permittedRoles.getOptions().containsKey(role.getStringId())
                && !bannedRoles.getOptions().containsKey(role.getStringId()))

                roles.put(role.getStringId(), role.getName());
        }
        return roles;
    }


    @Override
    public Map<String, I18nString> removeSelectedRoles(MultichoiceMapField addedRoles) {

        Map<String, I18nString> updatedRoles = new LinkedHashMap<>(addedRoles.getOptions());

        for(String roleId : addedRoles.getValue()) {
            updatedRoles.remove(roleId);
        }

        return updatedRoles;
    }

    @Override
    public Map<String, I18nString> addSelectedRoles(MultichoiceMapField addedRoles, EnumerationMapField netField, MultichoiceMapField rolesAvailable) {

        String netAndVersion = " (" + netField.getOptions().get(netField.getValue()) + ")";
        Map<String, I18nString> updatedRoles = new LinkedHashMap<>(addedRoles.getOptions());

        for(String roleId : rolesAvailable.getValue()) {
            String roleNetVersion = rolesAvailable.getOptions().get(roleId).toString() + netAndVersion;
            updatedRoles.put(roleId, new I18nString(roleNetVersion));
        }

        return updatedRoles;
    }
}
