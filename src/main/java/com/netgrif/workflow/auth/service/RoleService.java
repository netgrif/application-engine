package com.netgrif.workflow.auth.service;

import com.netgrif.workflow.auth.domain.Role;
import com.netgrif.workflow.auth.domain.repositories.RoleRepository;
import com.netgrif.workflow.auth.service.interfaces.IRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService implements IRoleService {

    @Autowired
    private RoleRepository repository;

    @Override
    public List<Role> findAll() {
        return repository.findAll();
    }
}