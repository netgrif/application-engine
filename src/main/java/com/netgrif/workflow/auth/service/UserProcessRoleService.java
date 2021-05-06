package com.netgrif.workflow.auth.service;

import com.netgrif.workflow.auth.domain.UserProcessRole;
import com.netgrif.workflow.auth.domain.repositories.UserProcessRoleRepository;
import com.netgrif.workflow.auth.service.interfaces.IUserProcessRoleService;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRole;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@Service
public class UserProcessRoleService implements IUserProcessRoleService {

    private static final Logger log = LoggerFactory.getLogger(UserProcessRoleService.class);

    @Autowired
    private UserProcessRoleRepository repository;

    @Autowired
    private ProcessRoleRepository processRoleRepository;

    private String DEFAULT_ROLE_ID;

    @Override
    public List<UserProcessRole> findAll() {
        return repository.findAll();
    }

    @Override
    public UserProcessRole findDefault() {
        UserProcessRole defaultRole = repository.findByRoleId(getDefaultRoleId());
        if (defaultRole == null) {
            defaultRole = repository.save(new UserProcessRole(getDefaultRoleId()));
        }
        return defaultRole;
    }

    @Override
    public List<UserProcessRole> saveRoles(Collection<ProcessRole> values, String netId) {
        List<UserProcessRole> userProcessRoles = new LinkedList<>();
        for (ProcessRole value : values) {
            UserProcessRole userRole = new UserProcessRole();
            userRole.setRoleId(value.getStringId());
            userRole.setNetId(netId);
            userProcessRoles.add(userRole);
        }
        return repository.saveAll(userProcessRoles);
    }

    @Override
    public UserProcessRole findByRoleId(String roleId) {
        return repository.findByRoleId(roleId);
    }

    private String getDefaultRoleId() {
        if (DEFAULT_ROLE_ID == null) {
            ProcessRole role = processRoleRepository.findByName_DefaultValue(ProcessRole.DEFAULT_ROLE);
            if (role == null)
                throw new NullPointerException("Default process role not found");
            DEFAULT_ROLE_ID = role.getStringId();
        }
        return DEFAULT_ROLE_ID;
    }

    @Override
    public void deleteRolesOfNet(PetriNet net) {
        log.info("[" + net.getStringId() + "]: Deleting all user process roles of Petri net " + net.getIdentifier() + " version " + net.getVersion().toString());
        this.repository.deleteAllByNetId(net.getStringId());
    }
}