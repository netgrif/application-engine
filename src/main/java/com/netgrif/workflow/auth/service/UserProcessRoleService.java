package com.netgrif.workflow.auth.service;

import com.netgrif.workflow.auth.domain.UserProcessRole;
import com.netgrif.workflow.auth.domain.repositories.UserProcessRoleRepository;
import com.netgrif.workflow.auth.service.interfaces.IUserProcessRoleService;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRole;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserProcessRoleService implements IUserProcessRoleService {

    @Autowired
    private UserProcessRoleRepository repository;

    @Autowired
    private ProcessRoleRepository processRoleRepository;

    private String DEFAULT_ROLE_ID;

    @Override
    public List<UserProcessRole> findAllMinusDefault() {
        List<UserProcessRole> roles = repository.findAll();
        roles.removeIf(role -> role.getRoleId().equals(getDefaultRoleId()));
        return roles;
    }

    @Override
    public UserProcessRole findDefault() {
        UserProcessRole defaultRole = repository.findByRoleId(getDefaultRoleId());
        if (defaultRole == null) {
            defaultRole = repository.save(new UserProcessRole(getDefaultRoleId()));
        }
        return defaultRole;
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
}