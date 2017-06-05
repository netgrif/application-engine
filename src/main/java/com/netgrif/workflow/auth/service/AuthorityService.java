package com.netgrif.workflow.auth.service;

import com.netgrif.workflow.auth.domain.Authority;
import com.netgrif.workflow.auth.domain.repositories.AuthorityRepository;
import com.netgrif.workflow.auth.service.interfaces.IAuthorityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthorityService implements IAuthorityService {

    @Autowired
    private AuthorityRepository repository;

    @Override
    public List<Authority> findAll() {
        return repository.findAll();
    }
}